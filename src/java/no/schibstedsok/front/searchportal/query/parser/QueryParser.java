/* Copyright (2005-2006) Schibsted Søk AS
 * QueryParser.java
 *
 * Created on 12 January 2006, 12:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.parser;

import no.schibstedsok.front.searchportal.query.Query;


/** Parser responsible for building the Query and its clause heirarchy.
 * This interface does not define the behaviour as to how the QueryParser will get the query string.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface QueryParser {
    /** Get the Query.
     *
     *@return the Query object.
     *@throws ParseException when parsing the inputted query string.
     **/
    Query getQuery() throws ParseException;
}
