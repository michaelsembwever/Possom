/*
 * AbstractSearchCommandExecutor.java
 *
 * Created on 29 April 2006, 22:26
 *
 */

package no.sesat.search.mode.executor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
abstract class AbstractSearchCommandExecutor implements SearchCommandExecutor {


    protected static final Logger LOG = Logger.getLogger(AbstractSearchCommandExecutor.class);
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


    public Map<Future<ResultList<? extends ResultItem>>,Callable<ResultList<? extends ResultItem>>> invokeAll(
            Collection<Callable<ResultList<? extends ResultItem>>> callables, 
            int timeoutInMillis) throws InterruptedException  {

        LOG.debug(DEBUG_INVOKEALL + getClass().getSimpleName());
        
        final Map<Future<ResultList<? extends ResultItem>>,Callable<ResultList<? extends ResultItem>>> results 
                = new HashMap<Future<ResultList<?>>,Callable<ResultList<?>>>(); 
        
        final ExecutorService es = getExecutorService();
                
        for (Callable<ResultList<?>> c : callables) {
            results.put(es.submit(c), c);
        }
        
        return results;
    }

    public void stop() {
        
        LOG.warn("Shutting down thread pool");
        getExecutorService().shutdownNow();
    }

    protected abstract ExecutorService getExecutorService();

}
