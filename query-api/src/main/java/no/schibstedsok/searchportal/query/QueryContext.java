/* Copyright (2005-2007) Schibsted Søk AS
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
