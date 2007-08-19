/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * 
 */
package no.sesat.searchportal.mode.command;

/**
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public class SearchCommandException extends RuntimeException {

    /**
     * Create a new InfrastructureException.
     * 
     * @param e
     */
    public SearchCommandException(Exception e) {
        super(e);
    }

    /**
     * Create a new InfrastructureException.
     * 
     * @param s
     * @param e
     */
    public SearchCommandException(String s, Exception e) {
        super(s, e);
    }
}
