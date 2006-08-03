/*
 * AbstractSearchCommandExecutor.java
 *
 * Created on 29 April 2006, 22:26
 *
 */

package no.schibstedsok.searchportal.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
abstract class AbstractSearchCommandExecutor implements SearchCommandExecutor {


    protected transient static final Logger LOG = Logger.getLogger(AbstractSearchCommandExecutor.class);
    private static final String DEBUG_INVOKEALL = "invokeAll using ";


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of AbstractSearchCommandExecutor
     */
    public AbstractSearchCommandExecutor() {
    }

    // Public --------------------------------------------------------


    public List<Future<SearchResult>> invokeAll(final Collection<Callable<SearchResult>> callables, final int timeoutInMillis)  {

        LOG.debug(DEBUG_INVOKEALL + getClass().getSimpleName());
        final List<Future<SearchResult>> results = new ArrayList<Future<SearchResult>>();
        try {
            results.addAll(getExecutorService().invokeAll(callables, timeoutInMillis, TimeUnit.MILLISECONDS));

//            for( Callable<SearchResult> c : callables ){
//                results.add( EXECUTOR.submit(c) );
//            }
        }  catch (InterruptedException e) {
            LOG.error(e);
        }
        return results;
    }

    public void stop() {
        LOG.warn("Shutting down thread pool");
        getExecutorService().shutdownNow();
    }

    protected abstract ExecutorService getExecutorService();



    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
