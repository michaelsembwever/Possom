// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.executor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SearchTask extends FutureTask<SearchResult> {

    private static final Logger LOG = Logger.getLogger(SearchTask.class);

    private SearchCommand command;

    public SearchTask(final SearchCommand command) {
        super(command);

        this.command = command;
    }

    public SearchCommand getCommand() {
        return command;
    }

    public boolean cancel(final boolean mayInterruptIfRunning) {
        LOG.debug("Cancel called " + command);

        return super.cancel(mayInterruptIfRunning);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public synchronized SearchResult get() {

        try {
            LOG.debug("Calling get on " + command);
            return super.get();

        } catch (InterruptedException e) {
            LOG.error("Search was interrupted " + command);
            return null;
        } catch (ExecutionException e) {
            LOG.error("Search exited with error " + command, e);
            return null;
        }
    }

    public synchronized SearchResult get(final long timeout, final TimeUnit unit) {

        try {
            return super.get(timeout, unit);

        } catch (InterruptedException e) {
            LOG.error("Search was interrupted " + command);
            return null;
        } catch (ExecutionException e) {
            LOG.error("Search exited with error ", e);
            return null;
        } catch (TimeoutException e) {
            LOG.error("Search timed out " + command);
            return null;
        }
    }
}
