/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 */
package no.schibstedsok.searchportal.mode.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple SearchCommandExecutor that executes the tasks sequentially
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class SequentialSearchCommandExecutor extends AbstractSearchCommandExecutor {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    protected ExecutorService getExecutorService() {
        return EXECUTOR;
    }
}
