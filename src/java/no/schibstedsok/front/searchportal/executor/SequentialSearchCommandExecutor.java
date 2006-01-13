/*
 * Copyright (2005) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.executor;

import edu.emory.mathcs.backport.java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A simple SearchCommandExecutor that executes the tasks sequentially
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SequentialSearchCommandExecutor implements SearchCommandExecutor {

    private static Log log = LogFactory.getLog(SequentialSearchCommandExecutor.class);

    public List invokeAll(Collection callables, int timeoutInMillis) {

        List results = new ArrayList();

        for (Iterator iterator = callables.iterator(); iterator.hasNext();) {

            Callable callable = (Callable) iterator.next();

            try {
                results.add(callable.call());
            } catch (Exception e) {
                log.error("Execution of callable failed", e);
            }
        }

        return results;
    }

    public void start() {
    }

    public void stop() {
    }
}
