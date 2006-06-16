/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.schibstedsok.front.searchportal.query.IntegerClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.OrganisationNumberClause;
import no.schibstedsok.front.searchportal.query.PhoneNumberClause;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import no.schibstedsok.front.searchportal.query.token.TokenMatch;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.YellowSearchResult;

public class YellowSearchCommand extends FastSearchCommand {
    private static Log log = LogFactory.getLog(YellowSearchCommand.class);

    private boolean ignoreGeoNav = false;

    private boolean isLocal;

    private boolean isTop3 = false;

    private boolean ypkeywordsgeo = false;

    private StringBuilder filterBuilder = null;

    public YellowSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    protected Map getNavigators() {

        if (ignoreGeoNav && super.getNavigators() != null) {
            final Map m = new HashMap();
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


    public String getTransformedQuery() {
        final TokenEvaluatorFactory factory = getRunningQuery().getTokenEvaluatorFactory();
        final boolean exactCompany = TokenPredicate.EXACTCOMPANYRANK.evaluate(factory);

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
            }

            return ypkeywordsgeo && getLastGeoMatch() != null
                    ? filterBuilder.toString() + " +ypkeywordsgeo:" + getLastGeoMatch().getMatch()
                    : filterBuilder.toString();
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

    /**
     * Add integer prefix to the organisation number.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final OrganisationNumberClause clause) {
        if (!getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER + getTransformedTerm(clause));
        }
    }

}
