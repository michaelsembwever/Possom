package no.schibstedsok.front.searchportal.executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.schibstedsok.front.searchportal.result.SearchResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.List;

/**
 * A {@link no.schibstedsok.front.searchportal.executor.SearchCommandExecutor} executing a list of callables in parallel.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class ParallelSearchCommandExecutor implements SearchCommandExecutor {

    private static final int INSPECTOR_PERIOD = 300000;

    private transient static ExecutorService executor = new SearchTaskExecutorService();
    private transient static Log LOG = LogFactory.getLog(ParallelSearchCommandExecutor.class);
    private transient static ThreadPoolInspector inspector = new ThreadPoolInspector((ThreadPoolExecutor) executor, INSPECTOR_PERIOD);

    /**
     * Creates a new parallel executor.
     */
    public ParallelSearchCommandExecutor() {
    }

    public List<Future<SearchResult>> invokeAll(Collection<Callable<SearchResult>> callables, int timeoutInMillis)  {

        final List<Future<SearchResult>> results = new ArrayList<Future<SearchResult>>();
        try {
            results.addAll( executor.invokeAll(callables, timeoutInMillis, TimeUnit.MILLISECONDS) );

//            for( Callable<SearchResult> c : callables ){
//                results.add( executor.submit(c) );
//            }
        } catch (InterruptedException e) {
            LOG.error(e);  //To change body of catch statement use File | Settings | File Templates.
        }
        return results;
    }

    public void stop() {
        LOG.info("Shutting down thread pool inspector");
        inspector.cancel();
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) executor;
        LOG.info("Shutting down thread pool");
        LOG.info(threadPool.getTaskCount() + " processed");
        threadPool.shutdownNow();
    }
}
