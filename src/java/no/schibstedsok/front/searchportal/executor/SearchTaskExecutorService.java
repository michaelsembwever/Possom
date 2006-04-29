// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.executor;


import java.util.concurrent.Callable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
final class SearchTaskExecutorService extends ThreadPoolExecutor {

    private static final Logger LOG = Logger.getLogger(SearchTaskExecutorService.class);

    public SearchTaskExecutorService() {
        super(20, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue());
    }

    protected SearchTask newTaskFor(final Callable<SearchResult> callable) {

        final SearchCommand command = (SearchCommand) callable;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new search task " + command.getSearchConfiguration().getName());
        }

        return new SearchTask(command);
    }

}
