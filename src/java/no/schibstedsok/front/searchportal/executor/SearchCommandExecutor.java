// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import no.schibstedsok.front.searchportal.result.SearchResult;

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
    List<Future<SearchResult>> invokeAll(Collection<Callable<SearchResult>> callables, int timeoutInMillis)
            throws InterruptedException;

}
