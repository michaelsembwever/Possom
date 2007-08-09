/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 * QueryStringContext.java
 *
 */

package no.schibstedsok.searchportal.query;

import no.schibstedsok.commons.ioc.BaseContext;

/** Used when the Query object is part of the Context and access to the datamodel is not available.
 * 
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface QueryContext extends BaseContext {
    /** Get the query object heirarchy.
     *
     * @return the query object heirarchy.
     */
    Query getQuery();
}
