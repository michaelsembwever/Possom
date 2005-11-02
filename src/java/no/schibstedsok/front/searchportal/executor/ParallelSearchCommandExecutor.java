package no.schibstedsok.front.searchportal.executor;

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
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
public class ParallelSearchCommandExecutor implements SearchCommandExecutor {

    private static final int INSPECTOR_PERIOD = 300000;

    private transient static ExecutorService executor = new SearchTaskExecutorService();
    private transient static Log log = LogFactory.getLog(ParallelSearchCommandExecutor.class);
    private transient static ThreadPoolInspector inspector = new ThreadPoolInspector((ThreadPoolExecutor) executor, INSPECTOR_PERIOD);

    /**
     * Creates a new parallel executor.
     */
    public ParallelSearchCommandExecutor() {
    }

    public List invokeAll(Collection callables, int timeoutInMillis)  {
        try {
            return executor.invokeAll(callables, timeoutInMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    public void stop() {
        log.info("Shutting down thread pool inspector");
        inspector.cancel();
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) executor;
        log.info("Shutting down thread pool");
        log.info(threadPool.getTaskCount() + " processed");
        threadPool.shutdownNow();
    }
}
