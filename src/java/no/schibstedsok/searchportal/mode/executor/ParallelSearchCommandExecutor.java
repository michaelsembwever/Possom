// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A {@link no.schibstedsok.searchportal.executor.SearchCommandExecutor} executing a list of callables in parallel.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class ParallelSearchCommandExecutor extends AbstractSearchCommandExecutor {

//    private static final int INSPECTOR_PERIOD = 300000;

    //private transient static final SearchTaskExecutorService EXECUTOR = new SearchTaskExecutorService();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    //private static final ThreadPoolInspector INSPECTOR = new ThreadPoolInspector(EXECUTOR, INSPECTOR_PERIOD);

    /**
     * Creates a new parallel EXECUTOR.
     */
    public ParallelSearchCommandExecutor() {
    }

    protected ExecutorService getExecutorService(){
        return EXECUTOR;
    }
}
