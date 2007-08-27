/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 */
package no.sesat.search.mode.executor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

/**
 * An extension to the ParallelSearchCommandExecutor that supports individual thread pools for each skin's different
 *  commands.
 * Since each command's SearchConfiguration is a singleton (against the given the skin, mode, and id).
 * 
 * Any SearchCommand that misbehaves has it's corresponding pool frozen to the active number of threads.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
final class ThrottledSearchCommandExecutor extends AbstractSearchCommandExecutor {
    
    private static final Map<SearchConfiguration,ExecutorService> EXECUTORS 
            = new HashMap<SearchConfiguration,ExecutorService>();
    
    private static final ReadWriteLock EXECUTORS_LOCK = new ReentrantReadWriteLock();
    
    public ThrottledSearchCommandExecutor(){}

    @Override
    public Map<Future<ResultList<? extends ResultItem>>, SearchCommand> waitForAll(
            final Map<Future<ResultList<? extends ResultItem>>,SearchCommand> results, 
            final int timeoutInMillis) throws InterruptedException, TimeoutException, ExecutionException {
        
        try{
            return super.waitForAll(results, timeoutInMillis);
            
        }finally{
            
            for(SearchCommand command : results.values()){
                
                final ThreadPoolExecutor executor = getExecutorService(command);
                
                if(command.isCancelled()){
                    
                    LOG.warn("");
                    LOG.warn("FREEZING THREAD POOL EXECUTOR " + command.getSearchConfiguration());
                    LOG.warn(" at " + Math.max(1, executor.getActiveCount()));
                    LOG.warn("");

                    // we freeze thread pool at current size (excluding the just failed callable)
                    executor.setMaximumPoolSize(Math.max(1, executor.getActiveCount()));

                }else if(Integer.MAX_VALUE > executor.getMaximumPoolSize()){
                    
                    LOG.warn("");
                    LOG.warn("Restoring ThreadPoolExecutor " + command.getSearchConfiguration());
                    LOG.warn("");

                    // command was successful unfreeze thread pool
                    executor.setMaximumPoolSize(Integer.MAX_VALUE);
                }
            }
        }
    }

    protected ThreadPoolExecutor getExecutorService(final SearchCommand command) {
        
        ThreadPoolExecutor service;
        try{
            
            EXECUTORS_LOCK.readLock().lock();
            service = (ThreadPoolExecutor)EXECUTORS.get(command.getSearchConfiguration());
        
        }finally{
            EXECUTORS_LOCK.readLock().unlock();
        }
        
        if(null == service){
            try{
            
                EXECUTORS_LOCK.writeLock().lock();
                
                service = new ThreadPoolExecutor(
                        1, 
                        Integer.MAX_VALUE, 
                        60L, 
                        TimeUnit.SECONDS, 
                        new SynchronousQueue<Runnable>());
                
                EXECUTORS.put(command.getSearchConfiguration(), service);
                
            }finally{
                EXECUTORS_LOCK.writeLock().unlock();
            }
        }
        
        return service;
    }
    
    protected Collection<ExecutorService> getExecutorServices() {
        return EXECUTORS.values();
    }
        
    
}
