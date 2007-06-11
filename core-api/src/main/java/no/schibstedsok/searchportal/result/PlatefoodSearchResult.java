// Copyright (2007) Schibsted Søk AS
/*
 * PlatefoodSearchResult.java
 *
 * Created on 24. august 2006, 15:23
 *
 */

package no.schibstedsok.searchportal.result;

import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.mode.command.SearchCommand;

/**
 *
 * @param T 
 * @author SSTHKJER
 * @version $Id$
 */
public final class PlatefoodSearchResult<T extends ResultItem> extends BasicResultList<T> {
    
    private boolean ppcTopListQuery = false;

    /**
     * 
     * @param cmd 
     * @param top 
     */
    public PlatefoodSearchResult(final SearchCommand cmd, final boolean top) {
        super();
        ppcTopListQuery = top;
    }

    /**
     * 
     * @return 
     */
    public boolean isTopListQuery() {
        return ppcTopListQuery;
    }
    
}
