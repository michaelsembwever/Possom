package no.schibstedsok.front.searchportal.executor;

import edu.emory.mathcs.backport.java.util.concurrent.ExecutionException;
import edu.emory.mathcs.backport.java.util.concurrent.FutureTask;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.TimeoutException;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SearchTask extends FutureTask {

    private static Log log = LogFactory.getLog(SearchTaskExecutorService.class);

    private SearchCommand command;

    public SearchTask(SearchCommand command) {
        super(command);

        this.command = command;
    }

    public SearchCommand getCommand() {
        return command;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        log.debug("Cancel called " + command);

        return super.cancel(mayInterruptIfRunning);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public synchronized Object get() {
        try {
            log.debug("Calling get on " + command);
            return super.get();
        } catch (InterruptedException e) {
            log.error("Search was interrupted " + command);
            return null;
        } catch (ExecutionException e) {
            log.error("Search exited with error " + command, e);
            return null;
        }
    }

    public synchronized Object get(long timeout, TimeUnit unit) {
        try {
            return super.get(timeout, unit);
        } catch (InterruptedException e) {
            log.error("Search was interrupted " + command);
            return null;
        } catch (ExecutionException e) {
            log.error("Search exited with error ", e);
            return null;
        } catch (TimeoutException e) {
            log.error("Search timed out " + command);
            return null;
        }
    }
}
