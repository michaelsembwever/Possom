/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
package no.schibstedsok.searchportal.mode.executor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * An object that executes a list of {@link java.util.concurrent.Callable} tasks.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchCommandExecutor {

    /**
     * Stops this executor, cancelling all pending tasks.
     */
    void stop();

    /**
     * Invoke all commands returning a list of {@link java.util.concurrent.Future}
     * holding their results.
     *
     * @param callables       The list of {@link java.util.concurrent.Callable} to execute.
     * @param timeoutInMillis The timeout in milliseconds
     * @return the list of Futures holding the results.
     * @throws InterruptedException
     */
    Map<Future<ResultList<? extends ResultItem>>,Callable<ResultList<? extends ResultItem>>> invokeAll(
            Collection<Callable<ResultList<? extends ResultItem>>> callables, 
            int timeoutInMillis) throws InterruptedException;

}
