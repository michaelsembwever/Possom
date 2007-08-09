/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
package no.schibstedsok.searchportal.mode.executor;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * A {@link no.schibstedsok.searchportal.executor.SearchCommandExecutor} executing a list of callables in parallel.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class ParallelSearchCommandExecutor extends AbstractSearchCommandExecutor {

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
    public Map<Future<ResultList<? extends ResultItem>>,Callable<ResultList<? extends ResultItem>>> invokeAll(
            Collection<Callable<ResultList<? extends ResultItem>>> callables, 
            int timeoutInMillis) throws InterruptedException  {

        
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
        
        return super.invokeAll(callables, timeoutInMillis);
    }    

    /**
     * 
     * @return 
     */
    @Override
    protected ExecutorService getExecutorService(){
        return EXECUTOR;
    }
    
    static class DebugThreadPoolExecutor extends ThreadPoolExecutor{
        
        final Vector<Runnable> EXECUTING = new Vector<Runnable>();
        
        DebugThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
            
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }
        
        protected void beforeExecute(final Thread t, final Runnable r) {
            super.beforeExecute(t, r);
            
            EXECUTING.add(r);
        }

        protected void afterExecute(final Runnable r, final Throwable t) {
            super.afterExecute(r, t);
            
            EXECUTING.remove(r);
        }        
    }
}
