// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.executor;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A {@link no.schibstedsok.searchportal.executor.SearchCommandExecutor} executing a list of callables in parallel.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class ParallelSearchCommandExecutor extends AbstractSearchCommandExecutor {

    private static final ExecutorService EXECUTOR = //Executors.newCachedThreadPool();
            // Alternative to find memory leakages
            new DebugThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());


    /**
     * Creates a new parallel EXECUTOR.
     */
    public ParallelSearchCommandExecutor() {
    }

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
