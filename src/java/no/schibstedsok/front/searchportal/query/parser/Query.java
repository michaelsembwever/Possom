/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface Query {

    /**
     *
     * @return
     */
    Clause getRootClause();
    
    /**
     *
     * @return
     */
    String getQueryString();
    
    /**
     *
     * @return
     */
    Clause getFirstLeafClause();

}
