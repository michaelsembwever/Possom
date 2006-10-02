/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.searchportal.query.token;

/** Something went wrong querying the fast list.
 * The VeryFastTokenEvaluator wont work because of this failure.
 * TokenPredicates (because of their associated known and possible predicates) cannot be wealy cached.
 *
 * @author <a href="mailto:mick@sesam.no">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class VeryFastListQueryException extends Exception {

    /**
     * Create a new VeryFastListQueryException.
     *
     * @param s detailed message
     * @param e underlying exception
     */
    public VeryFastListQueryException(final String s, final Exception e) {
        super(s, e);
    }
}
