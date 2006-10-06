/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * TODO. Rewrite from scratch. This class is insane.
 */
public class YellowSearchCommand extends CorrectingFastSearchCommand {

    private static final Logger LOG = Logger.getLogger(YellowSearchCommand.class);

    private boolean ignoreGeoNav = false;

    private boolean isLocal;

    private boolean isTop3 = false;

    private boolean ypkeywordsgeo = false;

    private StringBuilder filterBuilder = null;

    boolean exactCompany;
    boolean companyRank = false;

    private boolean correct = false;

    /** Creates a new yellow search command.
     * TODO. Rewrite from scratch. This is insane.
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
            correct = false;
            ignoreGeoNav = true;
            isLocal = false;
            ypkeywordsgeo = true;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            correct = false;
            ignoreGeoNav = false;
            isTop3 = true;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;

            // Perform local search.
            correct = true;
            ignoreGeoNav = false;
            isLocal = true;
            final FastSearchResult localResult = (FastSearchResult) super.execute();

            final YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, isLocalSearch() && !viewAll);

            final String yprank = companyRank ? "company" : "default";
            result.addField("yprank", yprank);

            return result;
        } else if (!viewAll) {
            isLocal = false;
            isTop3 = true;
            correct = false;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;
            ypkeywordsgeo = true;
            correct = true;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            final YellowSearchResult result = new YellowSearchResult(this, null, nationalHits, top3, false);

            final String yprank = companyRank ? "company" : "default";
            result.addField("yprank", yprank);

            return result;
        } else {
            correct = true;
            ypkeywordsgeo = false;
            isLocal = true;
            ignoreGeoNav = true;
            final FastSearchResult localResult = (FastSearchResult) super.execute();
            ignoreGeoNav = false;
            isLocal = false;

            isTop3 = true;
            correct = false;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;

            isLocal = false;
            ypkeywordsgeo = true;
            correct = false;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            final String yprank = companyRank ? "company" : "default";

            final YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, false);
            result.addField("yprank", yprank);
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

        final String t = super.getTransformedQuery();

        final TokenEvaluationEngine engine = context.getTokenEvaluationEngine();

        exactCompany = engine.evaluateQuery(TokenPredicate.EXACTCOMPANYRANK, context.getQuery());

        companyRank = exactCompany && !isTop3 && !getParameter("yprank").equals("standard") || getParameter("yprank").equals("company");

        if (companyRank) {
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
    protected boolean isCorrectionEnabled() {
        return correct;
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

        final TokenEvaluationEngine engine = context.getTokenEvaluationEngine();

        if (engine.evaluateQuery(TokenPredicate.EXACTCOMPANYRANK, context.getQuery())) {
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

            appendToQueryRepresentation(getTransformedTerm(clause).replaceAll("\\.", " "));
        }
    }

    /**
     * An implementation that ignores phrase searches.
     *
     * Visits only the left clause, unless that clause is a clause, in which
     * case only the right clause is visited. Phrase searches are not possible
     * against the yellow index.
     */
    protected void visitImpl(final XorClause clause) {
        // If we have a match on an international phone number, but it is not recognized as
        // a local phone number, force it to use the original number string.
        if (clause.getHint() == XorClause.PHONE_NUMBER_ON_LEFT
                && !clause.getFirstClause().getKnownPredicates().contains(TokenPredicate.PHONENUMBER)) {
            clause.getSecondClause().accept(this);
        } else if(XorClause.PHRASE_ON_LEFT == clause.getHint()){
            clause.getSecondClause().accept(this);
        } else {
            super.visitImpl(clause);
        }
    }
}
