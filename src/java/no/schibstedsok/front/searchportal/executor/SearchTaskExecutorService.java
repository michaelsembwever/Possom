package no.schibstedsok.front.searchportal.executor;

import edu.emory.mathcs.backport.java.util.concurrent.*;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SearchTaskExecutorService extends ThreadPoolExecutor {

    private static Log log = LogFactory.getLog(SearchTaskExecutorService.class);

    public SearchTaskExecutorService() {
        super(20, 100, 5L, TimeUnit.SECONDS, new SynchronousQueue());
    }

    protected RunnableFuture newTaskFor(Callable callable) {
        if (log.isDebugEnabled()) {
            SearchCommand command = (SearchCommand) callable;
            log.debug("Creating new search task " + command.getSearchConfiguration().getName());
        }
        return new SearchTask((SearchCommand)callable);
    }
}
