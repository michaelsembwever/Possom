 /*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package no.sesat.search.mode.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

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
final class ThrottledSearchCommandExecutor extends ParallelSearchCommandExecutor {
   
    private static final Map<SearchConfiguration,Throttle> THROTTLES 
            = new HashMap<SearchConfiguration,Throttle>();
    
    private static final ReadWriteLock THROTTLES_LOCK = new ReentrantReadWriteLock();
    
    private static volatile long lastThrottleLog = System.currentTimeMillis() / 60000;
    
    public ThrottledSearchCommandExecutor(){}
    

    @Override
    public Map<Future<ResultList<? extends ResultItem>>,SearchCommand> invokeAll(
            Collection<SearchCommand> callables) throws InterruptedException  {
        
        final Collection<SearchCommand> allowedCallables = new ArrayList<SearchCommand>(callables);
        
        for(SearchCommand command : callables){
            
            final Throttle throttle = getThrottle(command.getSearchConfiguration());
            
            if(throttle.isThrottled()){
                
                LOG.error(command.getSearchConfiguration() + " is throttled and will not be executed");
                allowedCallables.remove(command);
                command.handleCancellation();
                
            }else{
                
                throttle.incrementPedal();
            }
        }

        if (LOG.isDebugEnabled() && System.currentTimeMillis() / 60000 != lastThrottleLog) {

            logThrottles();
            lastThrottleLog = System.currentTimeMillis() / 60000;
        }        
        
        return super.invokeAll(allowedCallables);
    }

    @Override
    public Map<Future<ResultList<? extends ResultItem>>, SearchCommand> waitForAll(
            final Map<Future<ResultList<? extends ResultItem>>,SearchCommand> results, 
            final int timeoutInMillis) throws InterruptedException, TimeoutException, ExecutionException {
        
        try{
            return super.waitForAll(results, timeoutInMillis);
            
        }finally{
            
            for(SearchCommand command : results.values()){

                final Throttle throttle = getThrottle(command.getSearchConfiguration());
                                
                if(command.isCancelled()){
                    
                    LOG.warn("FREEZING (at " + Math.max(1, throttle.getPedal()) 
                            + ") THREAD POOL EXECUTOR " + command.getSearchConfiguration() + '\n');

                    // we freeze thread pool at current size (excluding the just failed callable)
                    throttle.throttle();

                }else if(throttle.isThrottled()){
                    
                    LOG.warn("Restoring ThreadPoolExecutor " + command.getSearchConfiguration() + '\n');

                    // command was successful unfreeze thread pool
                    throttle.unthrottle();
                }

                throttle.decrementPedal();
            }
        }
    }
    
    protected Throttle getThrottle(final SearchConfiguration config) {
        
        Throttle throttle;
        try{
            
            THROTTLES_LOCK.readLock().lock();
            throttle = THROTTLES.get(config);
        
        }finally{
            THROTTLES_LOCK.readLock().unlock();
        }
        
        if(null == throttle){
            try{
            
                THROTTLES_LOCK.writeLock().lock();
                
                throttle = new Throttle();
                
                THROTTLES.put(config, throttle);
                
            }finally{
                THROTTLES_LOCK.writeLock().unlock();
            }
        }
        
        return throttle;
    }
    
    private static void logThrottles(){
        
        final StringBuilder sb = new StringBuilder();
        THROTTLES_LOCK.readLock().lock();
        for(Map.Entry<SearchConfiguration,Throttle> entry : THROTTLES.entrySet()){
            sb.append('\n' + entry.getKey().toString() + " executing " + entry.getValue().getPedal());
        }
        THROTTLES_LOCK.readLock().unlock();
        
        LOG.debug(sb.toString());
    }
            
        
    private static final class Throttle{
    
        private int limit = Integer.MAX_VALUE;

        private volatile int pedal = 0;

        boolean isThrottled(){
            return pedal >= limit;
        }

        int getLimit(){
            return limit;
        }

        void throttle(){
            this.limit = Math.max(1, pedal);
        }

        void unthrottle(){
            limit = Integer.MAX_VALUE;
        }

        void incrementPedal(){
            ++pedal;
        }

        void decrementPedal(){
            --pedal;
        }

        int getPedal(){
            return pedal;
        }
    }
}
