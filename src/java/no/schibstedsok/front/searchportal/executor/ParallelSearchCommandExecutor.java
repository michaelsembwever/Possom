// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.executor;

import java.util.concurrent.ExecutorService;

/**
 * A {@link no.schibstedsok.front.searchportal.executor.SearchCommandExecutor} executing a list of callables in parallel.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class ParallelSearchCommandExecutor extends AbstractSearchCommandExecutor {

    private static final int INSPECTOR_PERIOD = 300000;

    private transient static final SearchTaskExecutorService EXECUTOR = new SearchTaskExecutorService();
    private transient static final ThreadPoolInspector INSPECTOR = new ThreadPoolInspector(EXECUTOR, INSPECTOR_PERIOD);

    /**
     * Creates a new parallel EXECUTOR.
     */
    public ParallelSearchCommandExecutor() {
    }

    public void stop() {
        LOG.info("Shutting down thread pool inspector");
        INSPECTOR.cancel();
        LOG.warn(EXECUTOR.getTaskCount() + " processed");
        super.stop();
    }

    protected ExecutorService getExecutorService(){
        return EXECUTOR;
    }
}
