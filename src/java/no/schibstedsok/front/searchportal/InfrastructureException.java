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
    public InfrastructureException(Exception e) {
        super(e);
    }

    public InfrastructureException(String s, Exception e) {
        super(s, e);
    }
}
