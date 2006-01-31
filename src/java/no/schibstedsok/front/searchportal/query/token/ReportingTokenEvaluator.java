/*
 * Copyright (2005) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.token;

import java.util.List;

public interface ReportingTokenEvaluator {
    /**
     * Returns a list of all matches of type token. If the query for example is
     * "brødrene dahl oslo" and the token is "geo" reportToken could return
     * "dahl" and "oslo". If there are no matches, the empty list is returned.
     * 
     * @param token
     *            the token to look for.
     * @param query
     *            the query to look in.
     * @return the matches.
     */
    List reportToken(String token, String query);

}
