/*
 * Copyright (2005) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.result.SearchResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A simple SearchCommandExecutor that executes the tasks sequentially
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class SequentialSearchCommandExecutor implements SearchCommandExecutor {

    private static Log log = LogFactory.getLog(SequentialSearchCommandExecutor.class);

    public List<Future<SearchResult>> invokeAll(Collection<Callable<SearchResult>> callables, int timeoutInMillis) {

        final List<Future<SearchResult>> results = new ArrayList<Future<SearchResult>>();

        for (Callable<SearchResult> callable : callables) {

            final SearchCommand command = (SearchCommand) callable;
            try {
                final SearchResult result = command.call();

                final SearchTask task = new SearchTask(command){
                    public SearchResult get(final long timeout, final TimeUnit unit) {
                        return get();
                    }
                    public boolean isDone() {
                        return true;
                    }
                    public SearchResult get() {
                        return result;
                    }
                };
                results.add(task);
            
            } catch (Exception e) {
                log.error("Execution of search command failed", e);
            }
        }

        return results;
    }

    public void stop() {
    }
}
