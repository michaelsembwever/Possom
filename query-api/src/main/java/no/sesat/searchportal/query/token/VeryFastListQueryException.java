/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 */
package no.sesat.searchportal.query.token;

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
