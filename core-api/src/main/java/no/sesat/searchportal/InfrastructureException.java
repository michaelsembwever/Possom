/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.searchportal;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class InfrastructureException extends RuntimeException {
    /** The serialVersionUID */
    private static final long serialVersionUID = -4397027929558851526L;

    /**
     * Create a new InfrastructureException.
     * 
     * @param e
     */
    public InfrastructureException(Exception e) {
        super(e);
    }

    /**
     * Create a new InfrastructureException.
     * 
     * @param s
     * @param e
     */
    public InfrastructureException(String s, Exception e) {
        super(s, e);
    }
}
