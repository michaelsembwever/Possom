/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

import java.util.Set;

/** A Clause in this project represents a single term or operation on or between terms in a Query string.
 * A heirarchy of Clause objects will therefore represent a Query and avoid unneccessary string manipulations.
 * <b>All Clause subclasses MUST be immutable.</b>
 * State describing information will be stored in the wrapping Query class.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface Clause extends Visitable {
    /**
     * get the term.
     * @return the term.
     */
    String getTerm();

    /**
     * get the set of known predicates.
     * @return the set of known predicates.
     */
    Set/*<Predicate>*/ getKnownPredicates();

    /**
     * the set of possible predicates.
     * @return the set of possible predicates.
     */
    Set/*<Predicate>*/ getPossiblePredicates();

}
