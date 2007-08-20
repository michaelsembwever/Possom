/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.mode.executor;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

/**
 * A {@link no.sesat.search.executor.SearchCommandExecutor} executing a list of callables in parallel.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
final class ParallelSearchCommandExecutor extends AbstractSearchCommandExecutor {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
            // Alternative to find memory leakages
            //new DebugThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    
    private static final String DEBUG_POOL_COUNT = "Pool size: ";
    

    /**
     * Creates a new parallel EXECUTOR.
     */
    public ParallelSearchCommandExecutor() {
    }
    
    @Override
    public Map<Future<ResultList<? extends ResultItem>>,SearchCommand> invokeAll(
            Collection<SearchCommand> callables) throws InterruptedException  {

        
        if(LOG.isDebugEnabled()){
            for(SearchCommand command : callables){
                if( getExecutorService(command) instanceof ThreadPoolExecutor){

                    final ThreadPoolExecutor tpe = (ThreadPoolExecutor)getExecutorService(command);
                    LOG.debug(DEBUG_POOL_COUNT + tpe.getActiveCount() + '/' + tpe.getPoolSize());

                    if(tpe instanceof ParallelSearchCommandExecutor.DebugThreadPoolExecutor){

                        final ParallelSearchCommandExecutor.DebugThreadPoolExecutor dtpe 
                                = (ParallelSearchCommandExecutor.DebugThreadPoolExecutor)tpe;

                        LOG.debug("Still executing...");

                        synchronized( dtpe.EXECUTING ){
                            for(Runnable r : dtpe.EXECUTING){
                                try {
                                    LOG.debug(" " + ((FutureTask)r).get());

                                } catch (InterruptedException ex) {
                                    LOG.debug(ex);
                                } catch (ExecutionException ex) {
                                    LOG.debug(ex);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return super.invokeAll(callables);
    }    

    /**
     * 
     * @return 
     */
    @Override
    protected ExecutorService getExecutorService(final SearchCommand command){
        return EXECUTOR;
    }
    
    @Override
    protected Collection<ExecutorService> getExecutorServices(){
        
        return null;
    }
            
    static class DebugThreadPoolExecutor extends ThreadPoolExecutor{
        
        //final Collection<Runnable> EXECUTING = new ConcurrentSkipListSet<Runnable>(); // jdk1.6
        final Collection<Runnable> EXECUTING = new Vector<Runnable>();
        
        DebugThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
            
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }
        
        @Override
        protected void beforeExecute(final Thread t, final Runnable r) {
            super.beforeExecute(t, r);
            
            EXECUTING.add(r);
        }

        @Override
        protected void afterExecute(final Runnable r, final Throwable t) {
            super.afterExecute(r, t);
            
            EXECUTING.remove(r);
        }        
    }
}
