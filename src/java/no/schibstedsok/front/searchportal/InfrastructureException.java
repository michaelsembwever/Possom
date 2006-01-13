/*
 * Copyright (2005) Schibsted Søk AS
 * 
 */
package no.schibstedsok.front.searchportal;

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
