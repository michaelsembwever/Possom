package no.schibstedsok.front.searchportal.executor;

import java.util.Collection;
import java.util.List;

/**
 * An object that executes a list of {@link edu.emory.mathcs.backport.java.util.concurrent.Callable} tasks.
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
     * Invoke all commands returning a list of {@link edu.emory.mathcs.backport.java.util.concurrent.Future}
     * holding their results.
     *
     * @param callables       The list of {@link edu.emory.mathcs.backport.java.util.concurrent.Callable} to execute.
     * @param timeoutInMillis The timeout in milliseconds
     * @return the list of Futures holding the results.
     * @throws InterruptedException
     */
    List invokeAll(Collection callables, int timeoutInMillis) throws InterruptedException;

}
