package no.schibstedsok.front.searchportal.executor;

import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class ThreadPoolInspector extends TimerTask {

    private ThreadPoolExecutor threadPool;

    public ThreadPoolInspector(ThreadPoolExecutor threadPool, int msPeriod) {
        this.threadPool = threadPool;
        Timer t = new Timer();
        log.info("Scheduling to run every " + msPeriod + "ms");
        t.schedule(this, 0, msPeriod);
    }

    private Log log = LogFactory.getLog(ThreadPoolInspector.class);

    public void run() {
        log.info("Thread pool size: " + threadPool.getPoolSize());
        log.info("Largest size: " + threadPool.getLargestPoolSize());
        log.info("Active threads: " + threadPool.getActiveCount());
        log.info("Approx. task count: " + threadPool.getTaskCount());
        log.info("Completed count: " + threadPool.getCompletedTaskCount());
    }
}
