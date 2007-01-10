/* Copyright (2005-2006) Schibsted Søk AS
 * QueryStringContext.java
 *
 */

package no.schibstedsok.searchportal.query;

import no.schibstedsok.commons.ioc.BaseContext;

/**
 * @version $Id: QueryStringContext.java 2153 2006-02-02 13:04:13Z mickw $
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface QueryContext extends BaseContext {
    /** Get the query object heirarchy.
     *
     * @return the query object heirarchy.
     */
    Query getQuery();
}
