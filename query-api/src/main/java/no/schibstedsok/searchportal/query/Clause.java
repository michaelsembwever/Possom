/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.query;

import java.util.Set;
import no.schibstedsok.searchportal.query.token.TokenPredicate;

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
    Set<TokenPredicate> getKnownPredicates();

    /**
     * the set of possible predicates.
     * @return the set of possible predicates.
     */
    Set<TokenPredicate> getPossiblePredicates();

}
