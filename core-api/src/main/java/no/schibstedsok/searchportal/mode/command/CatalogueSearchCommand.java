/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.command.AbstractSearchCommand.ReconstructedQuery;
import no.schibstedsok.searchportal.mode.config.CatalogueSearchConfiguration;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter;
import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter.Application;
import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter.WhoWhereSplit;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;

import org.apache.log4j.Logger;

/**
 * The CatalogueSearchCommand is responsible for the query to search for
 * companies in the Katalog-website. It is executed for enrichment on sesam.no
 * and to retrieve the company info page.
 *
 * The search command uses a second query parameter to specify geographic
 * locations by the user. This geographic locations are parsed and an extra
 * query is created and appended to the default query. The parsing of
 * the geographic query is done by the inner class GeoVisitor in this file.
 *
 * By setting parameters in the modes.xml file, it is possible to specify
 * that the search command should analyze the q-parameter query content and
 * move recognized geographic locations to the geographic query part.
 *
 * The following attributes is configurable for this command in the modes.xml:
 *
 * split="true/false"
 * query-parameter-where="where"
 *
 *
 *
 * The functionality in this class is enhanced by several QueryTransformers;
 *
 * @see no.schibstedsok.searchportal.query.transform.CatalogueExactTitleMatchQueryTransformer
 * @see no.schibstedsok.searchportal.query.transform.CatalogueEmptyQueryTransformer
 * @see no.schibstedsok.searchportal.query.transform.CatalogueInfopageQueryTransformer
 *
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version $Revision:$
 */
public class CatalogueSearchCommand extends AdvancedFastSearchCommand {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(CatalogueSearchCommand.class);

    /** User supplied query for geographic locations. */
    private String queryGeoString = null;

    /** User supplied value for sorting type of search result. */
    private String userSortBy = "kw"; // default er sorting på keywords

    /**
     * Indicate if q-parameter should be split on recogniced geographic
     * location, set in modes.xml.
     */
    private boolean split = false;    // default value is false.

    /**
     *  If split on known geo, put resulting found geographic locations in
     *  knownGeo to be used while constructing query for catalogue search.
     */
    private List<String> knownGeo;

    /**
     *  Log strings for this class.
     */
    private static final String DEBUG_CONF_NFO    = "CatalogueSearchCommand Conf details->";
    private static final String DEBUG_SEARCHING_1 = "Catalogue Searching for who->";
    private static final String DEBUG_SEARCHING_2 = "Catalogue Searching for where->";
    private static final String DEBUG_SEARCHING_3 = "Catalogue Searching for geo->";

    /**
     *  Possible values to use as sort by in this search command.
     */
    private static final String SORTBY_COMPANYNAME = "iyprpnavn";
    private static final String SORTBY_KEYWORD = "iyprpkw";

    /**
     *  The names of the parameters where the result of the geographic split
     *  procedure is stored.
     */
    private static final String PARAMETER_NAME_WHAT = "catalogueWhat";
    private static final String PARAMETER_NAME_WHERE = "catalogueWhere";

    /**
     * Creates a new catalogue search command.
     * @param cxt current context for this search command.
     */
    public CatalogueSearchCommand(final Context cxt) {

        super(cxt);

        final CatalogueSearchConfiguration conf = (CatalogueSearchConfiguration) cxt
                .getSearchConfiguration();

        LOG.debug(DEBUG_CONF_NFO + conf.getSearchBy() + ' '
                + conf.getQueryParameterWhere() + ' '
                + conf.getSplit());

        split = conf.getSplit();
        splitGeographicFromQuery();
        createGeoQueryString();


        // user may specify sorting in two different ways from the GUI,
        // by company name or by keyword. Default is by keyword.
        if (getSingleParameter("userSortBy") != null
                && getSingleParameter("userSortBy").length() > 0
                && getSingleParameter("userSortBy").equals("name")) {
            userSortBy = "name";
        } else {
            userSortBy = "kw";
        }
    }


    /**
     *  Create geographic query.
     *  If present, add the recogniced splitted geographic locations.
     */
    private void createGeoQueryString() {
        final CatalogueSearchConfiguration conf = (CatalogueSearchConfiguration) context
                .getSearchConfiguration();

        ReconstructedQuery queryGeo = null;
        if (getSingleParameter(conf.getQueryParameterWhere()) != null) {
            String tmp = getSingleParameter(conf.getQueryParameterWhere());
            if(getKnownGeoString()!=null) tmp = tmp + " " + getKnownGeoString();

            queryGeo = createQuery(tmp);

            GeoVisitor geoVisitor = new GeoVisitor();
            geoVisitor.visit(queryGeo.getQuery().getRootClause());

            queryGeoString = geoVisitor.getQueryRepresentation();
            LOG.info(DEBUG_SEARCHING_3 + queryGeoString);
        }
    }


    /**
     *  Do the split on recogniced known geographic locations, if specified
     *  in modes.xml.
     *
     *  Put the result into two different attributes, catalogueWhat and
     *  catalogueWhere to be used in the frontend.
     *
     *  If the split results in empty Who and Where from split, use the 
     *  original untransformed query.
     *
     *  Populate the knownGeo and knownGeoString which is used by the visitXxx
     *  methods to known which terms to ignore when constructing the
     *  query for this searchcommand.
     */
    private void splitGeographicFromQuery() {

        if(split){
            final WhoWhereSplitter splitter = new WhoWhereSplitter(new WhoWhereSplitter.Context(){
                private final List<Application> applications = Arrays.asList(Application.YELLOW);
                public Map<Clause,String> getTransformedTerms(){
                    return CatalogueSearchCommand.this.getTransformedTerms();
                }
                public Query getQuery() {
                    return datamodel.getQuery().getQuery();
                }
                public List<Application> getApplications() {
                    return applications;
                }
            });

            final WhoWhereSplit splitQuery = splitter.getWhoWhereSplit();

            if((splitQuery.getWho()==null || splitQuery.getWho().length()==0)
               && (splitQuery.getWhere()==null || splitQuery.getWhere().length()==0)){
                getParameters().put(PARAMETER_NAME_WHAT, getParameters().get("q"));
                getParameters().put(PARAMETER_NAME_WHERE, "");
               
            
            }else{
                getParameters().put(PARAMETER_NAME_WHAT, splitQuery.getWho());
                getParameters().put(PARAMETER_NAME_WHERE, splitQuery.getWhere());
                
            }


            String[] where = splitQuery.getWhere().split(" ");
            List w = Arrays.asList(where);

            LOG.debug(DEBUG_SEARCHING_1 + splitQuery.getWho());
            LOG.debug(DEBUG_SEARCHING_2 + splitQuery.getWhere());

            knownGeo=w;
        }
    }


    /**
     *  Execute the search command query.
     *  Collect the result and copy the values over to our type of result
     *  objects. The search result, may be enriched by our resulthandler
     *  if specified in the modes.xml, this is only done in the Info Page
     *  search result.
     * @return the search result found by the executed query.
     */
    public SearchResult execute() {

        SearchResult result = super.execute();

        List<CatalogueSearchResultItem> nyResultListe = new ArrayList<CatalogueSearchResultItem>();
        
        Iterator iter = result.getResults().listIterator();
        while (iter.hasNext()) {
            BasicSearchResultItem basicResultItem = (BasicSearchResultItem) iter
                    .next();

            CatalogueSearchResultItem resultItem = new CatalogueSearchResultItem();
            for (Object o : basicResultItem.getFieldNames()) {
                String s = (String) o;
                String v = basicResultItem.getField(s);
                resultItem.addField(s, v);
            }

            nyResultListe.add(resultItem);
        }

        // clear the old BasicSearchResult, and add our new CatalogueSearchResult
        // to be used instead.
        result.getResults().clear();
        result.getResults().addAll(nyResultListe);

        return result;
    }

    /**
     * Get the query string after all transformations has been executed on it.
     *
     * If user has supplied the geographic where parameter or the split query
     * procedure resulted in geographic query to be generated for the search,
     * add it to the query.
     * @return the query that is going to be executed.
     */
    @Override
    public String getTransformedQuery() {
        String query = super.getTransformedQuery();

        boolean hasQueryString= (query!=null && query.length()>0);
        boolean hasGeoQueryString = (queryGeoString != null && queryGeoString.length() > 0);
        
        // two possible paths, with both what and where in query,
        // or just where. If nothing in either, something is wrong.
        if (hasQueryString && hasGeoQueryString){

            // both
            query += ") " + QL_AND + " (" + queryGeoString + ")";
            query = "(" + query;

        } else if (!hasQueryString && hasGeoQueryString) {

            // just where
            query = queryGeoString;

        } else if (hasQueryString && !hasGeoQueryString) {
            
            // just what,
            // dosent need to do anything with the query, should just leave
            // the query as it is after transformation.

        } else{

            // none of what and where, this should not be possible.
            throw new IllegalStateException("Emty query strings, should not be possible. [Primary="+query+", Geo="+queryGeoString+"]");
        }

        return query;
    }



    /**
     * Set what to sort the resultset by.
     * The possible value is sort by company name, or by keywords.
     *
     * This value is used by the call to Fast-servers, to specify
     * which rank-profile to sort by.
     *
     * The sorting may be altered if user has supplied the userSortBy
     * parameter.
     * @return the sorting to be used when executing the query.
     */
    @Override
    protected String getSortBy() {
        String sortBy = SORTBY_KEYWORD;
        if ("name".equalsIgnoreCase(userSortBy)) {
            sortBy = SORTBY_COMPANYNAME;
        }
        return sortBy;
    }

    
    /**
     * Create query syntax for a phrase term.
     * @param term the term to make query syntax for.
     * @return created phrase query fragment for one term.
     */
    private String createPhraseQuerySyntax(final String term) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("iypcfnavn:" + term + " ANY ");
        sb.append("lemiypcfkeywords:" + term + " ANY ");
        sb.append("lemiypcfkeywordslow:" + term);
        sb.append(")");
        return sb.toString();
    }    
    
    /**
     * Create the query syntax for a search term.
     *
     * If the query is defined to split known geographic locations from
     * the keywords, ignore the term.
     *
     * Check if there is any special characters in the query, if there
     * is, wrap term in " " characters and use none-phonetic composite field
     * in index for part of query.
     *
     * If the term is '*', also ignore it.
     * @param clause the clause to process.
     */
    @Override
    protected void visitImpl(final LeafClause clause) {
        boolean useTerm = true;
        boolean hasNotWordCharacters = false;
        
        if(split && knownGeo.contains(clause.getTerm())){
            useTerm=false;
        }

        Pattern p = Pattern.compile("\\.|\\-");        
        Matcher m = p.matcher(getTransformedTerms().get(clause));

        hasNotWordCharacters = m.find();

        if(useTerm){
            
            if(hasNotWordCharacters){
            
                appendToQueryRepresentation(
                        createPhraseQuerySyntax("\""+getTransformedTerms().get(clause) + "\""));
            
            }else if(!getTransformedTerms().get(clause).equals("*")) {
                StringBuilder sb = new StringBuilder();
                sb.append("(");
                
                sb.append("iypcfphnavn:"+getTransformedTerms().get(clause) + " ANY ");
                
                sb.append("lemiypcfkeywords:" + getTransformedTerms().get(clause)
                        + " ANY ");
                sb.append("lemiypcfkeywordslow:"
                        + getTransformedTerms().get(clause));
                sb.append(")");
                appendToQueryRepresentation(sb.toString());
            }
        }
    }

    /**
     * If the user has searched for a phrase, create the query syntax for
     * phrases in the query.
     *
     * If the query is defined to split known geographic locations from
     * the keywords, ignore the term.
     *
     * If the term is '*', also ignore it.
     * @param clause the clause to process.
     */
    protected void visitImpl(final PhraseClause clause) {
        boolean useTerm = true;

        if(split && knownGeo.contains(clause.getTerm())){
            useTerm=false;
        }

        if(useTerm){
            if (!getTransformedTerms().get(clause).equals("*")) {
                appendToQueryRepresentation(
                        createPhraseQuerySyntax(getTransformedTerms().get(clause)));
            }
        }
    }


    /**
     * {@inheritDoc}
     * @param clause the clause to process.
     */
    @Override
    protected void visitImpl(final DefaultOperatorClause clause) {

        clause.getFirstClause().accept(this);

        final boolean hasKnownGeo = isKnownGeo(clause.getFirstClause()) || isKnownGeo(clause.getSecondClause());

        if (!(hasKnownGeo || clause.getSecondClause() instanceof NotClause)) {
            appendToQueryRepresentation(QL_AND);
        }

        clause.getSecondClause().accept(this);
    }


    /**
     * Returns true if the clause is a leaf clause and if it will not produce
     * any output in the query representation.
     *
     * If split is not done for the query, this method returns false for all
     * clauses.
     *
     * @param clause The clause to examine.
     * @return true if leaf is known geographic location and should be filtered.
     */
    private boolean isKnownGeo(final Clause clause) {
        if(knownGeo==null) return false;

        if (clause instanceof LeafClause) {
            final LeafClause leafClause = (LeafClause) clause;
            return knownGeo.contains(getTransformedTerm(clause));
        } else {
            return false;
        }
    }

    /**
     * Get the String representation of knownGeo-list.
     * @return the known geo list represented as a string with whitespace separating
     * the geographic locations.
     */
    public String getKnownGeoString() {
        if(knownGeo==null) return null;

        StringBuilder sb=new StringBuilder();

        for(String s : knownGeo) {
            sb.append(s).append(" ");
        }
        return sb.toString();
    }


    /**
     * Query builder for creating the geographic query.
     *
     */
    private final class GeoVisitor extends AbstractReflectionVisitor {
        
        /** the composite field in the index to search in. */
        private static final String GEO_COMPOSITE_FIELD_NAME = "iypcfgeo:";

        /** used while building the query. */
        private final StringBuilder sb = new StringBuilder();

        /**
         * Returns the generated query.
         *
         * @return The query.
         */
        String getQueryRepresentation() {
            return sb.toString();
        }

        protected void visitImpl(final LeafClause clause) {
            if (clause.getTerm() != null && clause.getTerm().length() > 0) {
                sb.append(GEO_COMPOSITE_FIELD_NAME + clause.getTerm());
            }
        }

        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            if (!(clause.getSecondClause() instanceof NotClause)) {
                sb.append(QL_AND);
            }
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final OrClause clause) {
            sb.append("(");
            clause.getFirstClause().accept(this);

            sb.append(QL_OR);

            clause.getSecondClause().accept(this);
            sb.append(")");
        }

        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            if (!(clause.getSecondClause() instanceof NotClause)) {
                sb.append(QL_AND);
            }
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final NotClause clause) {

            final String childsTerm = clause.getFirstClause().getTerm();
            if (childsTerm != null && childsTerm.length() > 0) {
                sb.append(QL_ANDNOT);
                clause.getFirstClause().accept(this);
            }
        }

        protected void visitImpl(final AndNotClause clause) {
            // the first term can not be ANDNOT term.
            if(sb.toString().trim().length()>0){
                sb.append(QL_ANDNOT);
                clause.getFirstClause().accept(this);
            }
        }

        protected void visitImpl(final XorClause clause){
            clause.getFirstClause().accept(this);
        }
    }
}
