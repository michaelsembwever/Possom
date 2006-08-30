/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchType;
import no.schibstedsok.searchportal.query.IntegerClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.PhoneNumberClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenMatch;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.YellowSearchResult;
import org.apache.log4j.Logger;

public class YellowSearchCommand extends CorrectingFastSearchCommand {

    private static final Logger LOG = Logger.getLogger(YellowSearchCommand.class);

    private boolean ignoreGeoNav = false;

    private boolean isLocal;

    private boolean isTop3 = false;

    private boolean ypkeywordsgeo = false;

    private StringBuilder filterBuilder = null;

    /** Creates a new yellow search command.
     *
     **/
    public YellowSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    /** TODO comment me. **/
    protected Map getNavigators() {

        if (ignoreGeoNav && super.getNavigators() != null) {
            final Map m = new HashMap();
            m.putAll(super.getNavigators());
            m.remove("geographic");
            return m;
        }

        return super.getNavigators();
    }

    /** TODO comment me. **/
    public SearchResult execute() {

        boolean viewAll = false;

        if (getParameters().containsKey("ypviewall")) {
            viewAll = true;
        }

        if (isLocalSearch() && !viewAll) {
            LOG.debug("Search is local");

            // The search containing all hits. Including non-local.
            ignoreGeoNav = true;
            isLocal = false;

            ypkeywordsgeo = true;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            ignoreGeoNav = false;
            isTop3 = true;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;

            // Perform local search.
            ignoreGeoNav = false;
            isLocal = true;
            final FastSearchResult localResult = (FastSearchResult) super.execute();

            final YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, isLocalSearch() && !viewAll);
            return result;
        } else if (!viewAll) {
            isLocal = false;
            isTop3 = true;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;
            ypkeywordsgeo = true;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            final YellowSearchResult result = new YellowSearchResult(this, null, nationalHits, top3, false);
            return result;
        } else {

            ypkeywordsgeo = false;

            isLocal = true;
            ignoreGeoNav = true;
            final FastSearchResult localResult = (FastSearchResult) super.execute();
            ignoreGeoNav = false;
            isLocal = false;

            isTop3 = true;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;

            isLocal = false;
            ypkeywordsgeo = true;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            final YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, false);
            return result;
        }
    }

    private boolean isLocalSearch() {
        return getRunningQuery().getGeographicMatches().size() > 0;
    }


    private TokenMatch getLastGeoMatch() {
        final List<TokenMatch> matches = getRunningQuery().getGeographicMatches();

        if (matches.size() > 0) {
            return matches.get(matches.size() - 1);
        } else {
            return null;
        }
    }


    /** TODO comment me. **/
    public String getTransformedQuery() {

        String t = super.getTransformedQuery();
        
        final TokenEvaluationEngine engine 
                = getTokenEvaluationEngine() != null ? 
            getTokenEvaluationEngine() :
            context.getRunningQuery().getTokenEvaluationEngine();

        final boolean exactCompany = TokenPredicate.EXACTCOMPANYRANK.evaluate(engine);

        if (exactCompany && !isTop3) {
            return t.replaceAll("yellowphon", "yellownamephon");
        }

        if (isTop3) {
            return t.replaceAll("yellowphon:", "").replaceAll("-", " ");
        }

        if (isLocal) {
            return t.replaceAll("-", " ");
        } else {
            return t.replaceAll("yellowphon", "yellowgeophon").replaceAll("-", " ");
        }
    }

    /** TODO comment me. **/
    protected int getResultsToReturn() {
        if (isTop3) {
            return 3;
        } else {
            return super.getResultsToReturn();
        }
    }

    /** TODO comment me. **/
    protected String getAdditionalFilter() {

        synchronized (this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuilder(super.getAdditionalFilter());
            }

            return ypkeywordsgeo && getLastGeoMatch() != null
                    ? filterBuilder.toString() + " +ypkeywordsgeo:" + getLastGeoMatch().getMatch()
                    : filterBuilder.toString();
        }
    }

    /** TODO comment me. **/
    protected String getSortBy() {
        final TokenEvaluationEngine engine = getRunningQuery().getTokenEvaluationEngine();
        final boolean exactCompany = TokenPredicate.EXACTCOMPANYRANK.evaluate(engine);

        if (exactCompany) {
            return "yellowname";
        }

        return (isLocal ? "yellowpages2new +ypnavn" : "yellowpages2geo +ypnavn");
    }

    /** TODO comment me. **/
    public String getQueryInfo() {
        return getTransformedQuery() + " " + getSortBy() + " " + getAdditionalFilter();
    }


    // Query Builder

    private static final String PREFIX_INTEGER="yellowpages:";
    private static final String PREFIX_PHONETIC="yellowphon:";

    /**
     * Adds non phonetic prefix to integer terms.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final IntegerClause clause) {
        if (!getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER);
        }

        super.visitImpl(clause);
    }

    /**
     * Adds non phonetic prefix to phone number terms.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final PhoneNumberClause clause) {
        if (!getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER);
        }
        super.visitImpl(clause);
    }

    /**
     * Adds phonetic prefix to a leaf clause.
     * Remove dots from words. (people, street, suburb, or city names do not have dots.)
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
            if (!getTransformedTerm(clause).equals("")) {
                appendToQueryRepresentation(PREFIX_PHONETIC);
            }

            appendToQueryRepresentation(getTransformedTerm(clause).replaceAll("\\.", ""));
        }
    }
    protected void setAdditionalParameters(final ISearchParameters params) {
        super.setAdditionalParameters(params);
        params.setParameter(new SearchParameter(BaseParameter.TYPE, SearchType.SEARCH_ADVANCED.getValueString()));
    }
    protected void visitImpl(final XorClause clause) {
        
        if( XorClause.PHRASE_ON_LEFT == clause.getHint()){
            clause.getSecondClause().accept(this);
        }else{
            super.visitImpl(clause);
        }
    }
}
