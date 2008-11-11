/* Copyright (2006-2008) Schibsted Søk AS
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

/**
 *
 * @version <tt>$Id$</tt>
 */
public final class SearchTask extends FutureTask<ResultList<ResultItem>> {

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

    public synchronized ResultList<ResultItem> get() {

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

    public synchronized ResultList<ResultItem> get(final long timeout, final TimeUnit unit) {

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
