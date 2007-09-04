/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import no.sesat.search.mode.command.SearchCommand;
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


    public Map<Future<ResultList<? extends ResultItem>>,SearchCommand> invokeAll(
            final Collection<SearchCommand> callables) throws InterruptedException  {

        LOG.debug(DEBUG_INVOKEALL + getClass().getSimpleName());
        
        final Map<Future<ResultList<? extends ResultItem>>,SearchCommand> results 
                = new HashMap<Future<ResultList<?>>,SearchCommand>(); 
        
        for (SearchCommand c : callables) {
            
            final ExecutorService es = getExecutorService(c);
            results.put(es.submit(c), c);
        }

        return results;
    }
    
    public Map<Future<ResultList<? extends ResultItem>>,SearchCommand> waitForAll(
            final Map<Future<ResultList<? extends ResultItem>>,SearchCommand> results,
            final int timeoutInMillis) throws InterruptedException, TimeoutException, ExecutionException{
        
        // Give the commands a chance to finish its work
        //  Note the current time and subtract any elapsed time from the timeout value
        //   (as the timeout value is intended overall and not for each).
        final long invokedAt = System.currentTimeMillis();
        for (Future<ResultList<? extends ResultItem>> task : results.keySet()) {

            task.get(timeoutInMillis - (System.currentTimeMillis() - invokedAt), TimeUnit.MILLISECONDS);
        }
        
        // Ensure any cancellations are properly handled
        for(SearchCommand command : results.values()){
            command.handleCancellation();
        }
        
        return results;
    }

    public void stop() {
        
        LOG.warn("Shutting down thread pool");
        for(ExecutorService service : getExecutorServices()){
            service.shutdownNow();
        }
    }

    protected abstract ExecutorService getExecutorService(SearchCommand searchCommand);
    
    protected abstract Collection<ExecutorService> getExecutorServices();

}
