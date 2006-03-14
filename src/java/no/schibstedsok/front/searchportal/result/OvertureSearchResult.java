/*
 * OvertureSearchResult.java
 *
 * Created on February 28, 2006, 2:00 PM
 *
 */
package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.command.SearchCommand;

/**
 *
 * @author magnuse
 */
public class OvertureSearchResult extends BasicSearchResult {

    private boolean ppcTopListQuery = false;

    public OvertureSearchResult(final SearchCommand cmd, final boolean top) {
        super(cmd);
        ppcTopListQuery = top;
    }

    public boolean isTopListQuery() {
        return ppcTopListQuery;
    }
}
