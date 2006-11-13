/*
 * AbstractSearchCommandExecutor.java
 *
 * Created on 29 April 2006, 22:26
 *
 */

package no.schibstedsok.searchportal.mode.executor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
abstract class AbstractSearchCommandExecutor implements SearchCommandExecutor {


    protected static final Logger LOG = Logger.getLogger(AbstractSearchCommandExecutor.class);
    private static final String DEBUG_INVOKEALL = "invokeAll using ";
    private static final String DEBUG_POOL_COUNT = "Pool size: ";


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


    public Map<String,Future<SearchResult>> invokeAll(final Collection<Callable<SearchResult>> callables, final Map<String, Future<SearchResult>> results, final int timeoutInMillis)  {

        LOG.debug(DEBUG_INVOKEALL + getClass().getSimpleName());
        
        if(LOG.isDebugEnabled() && getExecutorService() instanceof ThreadPoolExecutor){
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)getExecutorService();
            LOG.debug(DEBUG_POOL_COUNT + tpe.getActiveCount() + '/' + tpe.getPoolSize());
            if(tpe instanceof ParallelSearchCommandExecutor.DebugThreadPoolExecutor){
                final ParallelSearchCommandExecutor.DebugThreadPoolExecutor dtpe 
                        = (ParallelSearchCommandExecutor.DebugThreadPoolExecutor)tpe;
                LOG.debug("Still executing...");
                synchronized( dtpe.EXECUTING ){
                    for(Runnable r : dtpe.EXECUTING){
                        try {
                            LOG.debug(" " + ((SearchResult)(((FutureTask)r).get())).getSearchCommand());
                        } catch (InterruptedException ex) {
                            LOG.debug(ex);
                        } catch (ExecutionException ex) {
                            LOG.debug(ex);
                        }
                    }
                }
            }
        }
        
        ExecutorService es = getExecutorService();
        for (Callable<SearchResult> c : callables) {
            results.put(((SearchCommand) c).getSearchConfiguration().getName(), es.submit(c));
        }
        
        return results;
    }

    public void stop() {
        LOG.warn("Shutting down thread pool");
        getExecutorService().shutdownNow();
    }

    protected abstract ExecutorService getExecutorService();

}
