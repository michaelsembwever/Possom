/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * QueryStringContext.java
 *
 * Created on 23 January 2006, 14:02
 *
 */

package no.sesat.search.query;

import no.schibstedsok.commons.ioc.BaseContext;

/** Used when the query string is a requirement of the Context and neither the Query object or datamodel are available.
 * 
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface QueryStringContext extends BaseContext{
    /** Get the original query string.
     *
     * @return the original query string.
     */
    String getQueryString();
}
