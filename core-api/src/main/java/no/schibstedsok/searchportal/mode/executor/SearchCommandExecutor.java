// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.executor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import no.schibstedsok.searchportal.result.SearchResult;

/**
 * An object that executes a list of {@link java.util.concurrent.Callable} tasks.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchCommandExecutor {

    /**
     * Stops this executor, cancelling all pending tasks.
     */
    void stop();

    /**
     * Invoke all commands returning a list of {@link java.util.concurrent.Future}
     * holding their results.
     *
     * @param callables       The list of {@link java.util.concurrent.Callable} to execute.
     * @param timeoutInMillis The timeout in milliseconds
     * @return the list of Futures holding the results.
     * @throws InterruptedException
     */
    Map<String,Future<SearchResult>> invokeAll(Collection<Callable<SearchResult>> callables, Map<String, Future<SearchResult>> results, int timeoutInMillis)
            throws InterruptedException;

}
