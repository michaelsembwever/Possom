/*
 * Copyright (2005) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import no.schibstedsok.front.searchportal.query.token.TokenMatch;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.YellowSearchResult;

public class YellowGeoSearch extends FastSearchCommand {
    private static Log log = LogFactory.getLog(YellowGeoSearch.class);
    
    private boolean ignoreGeoNav = false;
    
    private boolean isLocal;
    
    private boolean isTop3 = false;
    
    private boolean ypkeywordsgeo = false;
    
    private StringBuilder filterBuilder = null;
    
    public YellowGeoSearch(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }
    
    protected Map getNavigators() {
        
        if (ignoreGeoNav && super.getNavigators() != null) {
            Map m = new HashMap();
            m.putAll(super.getNavigators());
            m.remove("geographic");
            return m;
        }
        
        return super.getNavigators();
    }
    
    public SearchResult execute() {
        
        boolean viewAll = false;
        
        if (getParameters().containsKey("ypviewall")) {
            viewAll = true;
        }
        
        if (isLocalSearch() && !viewAll) {
            log.debug("Search is local");
            
            // The search containing all hits. Including non-local.
            ignoreGeoNav = true;
            isLocal = false;
            
            ypkeywordsgeo = true;
            FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;
            
            ignoreGeoNav = false;
            isTop3 = true;
            FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;
            
            // Perform local search.
            ignoreGeoNav = false;
            isLocal = true;
            FastSearchResult localResult = (FastSearchResult) super.execute();
            
            YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, isLocalSearch() && !viewAll);
            return result;
        } else if (!viewAll) {
            isLocal = false;
            isTop3 = true;
            FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;
            ypkeywordsgeo = true;
            FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;
            
            YellowSearchResult result = new YellowSearchResult(this, null, nationalHits, top3, false);
            return result;
        } else {
            
            ypkeywordsgeo = false;
            
            isLocal = true;
            ignoreGeoNav = true;
            FastSearchResult localResult = (FastSearchResult) super.execute();
            ignoreGeoNav = false;
            isLocal = false;
            
            isTop3 = true;
            FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;
            
            isLocal = false;
            ypkeywordsgeo = true;
            FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;
            
            YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, false);
            return result;
        }
    }
    
    private boolean isLocalSearch() {
        return getRunningQuery().getGeographicMatches().size() > 0;
    }
    
    
    private TokenMatch getLastGeoMatch() {
        List<TokenMatch> matches = getRunningQuery().getGeographicMatches();
        
        if (matches.size() > 0) {
            return matches.get(matches.size() - 1);
        } else {
            return null;
        }
    }
    
    
    public String getTransformedQuery() {
        TokenEvaluatorFactory factory = getRunningQuery().getTokenEvaluatorFactory();
        boolean exactCompany = TokenPredicate.EXACTCOMPANYRANK.evaluate(factory);
    
        if (exactCompany && !isTop3) {
            return super.getTransformedQuery().replaceAll("yellowphon", "yellownamephon");
        }
        
        if (isTop3) {
            return super.getTransformedQuery().replaceAll("yellowphon:", "").replaceAll("-", " ");
        }
        
        if (isLocal) {
            return super.getTransformedQuery().replaceAll("-", " ");
        } else {
            return super.getTransformedQuery().replaceAll("yellowphon", "yellowgeophon").replaceAll("-", " ");
        }
    }
    
    protected int getResultsToReturn() {
        if (isTop3) {
            return 3;
        } else {
            return super.getResultsToReturn();
        }
    }
    
    protected String getAdditionalFilter() {

        synchronized (this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuilder(super.getAdditionalFilter());

                if (ypkeywordsgeo && getLastGeoMatch() != null) {
                    filterBuilder.append("+ypkeywordsgeo:" + getLastGeoMatch().getMatch());
                }
            }

            return filterBuilder.toString();
        }
    }

    protected String getSortBy() {
        final TokenEvaluatorFactory factory = getRunningQuery().getTokenEvaluatorFactory();
        final boolean exactCompany = TokenPredicate.EXACTCOMPANYRANK.evaluate(factory);
        
        if (exactCompany) {
            return "yellowname";
        }

        return (isLocal ? "yellowpages2new +ypnavn" : "yellowpages2geo +ypnavn");
    }

    public String getQueryInfo() {
        return getTransformedQuery() + " " + getSortBy() + " " + getAdditionalFilter();
    }
    
}
