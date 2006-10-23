/*
 * PlatefoodSearchResult.java
 *
 * Created on 24. august 2006, 15:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.result;

import no.schibstedsok.searchportal.mode.command.SearchCommand;

/**
 *
 * @author SSTHKJER
 */
public class PlatefoodSearchResult extends BasicSearchResult {
    
    private boolean ppcTopListQuery = false;

    public PlatefoodSearchResult(final SearchCommand cmd, final boolean top) {
        super(cmd);
        ppcTopListQuery = top;
    }

    public boolean isTopListQuery() {
        return ppcTopListQuery;
    }
    
}
