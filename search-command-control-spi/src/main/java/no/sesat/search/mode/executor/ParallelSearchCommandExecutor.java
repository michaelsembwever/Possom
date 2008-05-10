/* Copyright (2006-2007) Schibsted Søk AS
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

 */
package no.sesat.search.mode.executor;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

/**
 * A {@link no.sesat.search.executor.SearchCommandExecutor} executing a list of callables in parallel.
 *
 *
 * @version <tt>$Id$</tt>
 */
class ParallelSearchCommandExecutor extends AbstractSearchCommandExecutor {

    private final ExecutorService EXECUTOR =
                new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
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
            if( getExecutorService() instanceof ThreadPoolExecutor){

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
        }

        return super.invokeAll(callables);
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
