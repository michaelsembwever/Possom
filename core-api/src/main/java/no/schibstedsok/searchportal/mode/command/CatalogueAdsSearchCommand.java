// Copyright (2006-2007) Schibsted Søk AS
/*
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.mode.command.AbstractSearchCommand.ReconstructedQuery;
import no.schibstedsok.searchportal.mode.config.CatalogueAdsCommandConfig;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.log4j.Logger;

/**
 * Search command builds, executes and filters search result for
 * sponsed links in catalogue website.
 *
 * Executes two different queries, and compares them to summarize one
 * search result that is presented in the frontend.
 *
 * @version $Revision:$
 * @author daniele@conduct.no
 */
public class CatalogueAdsSearchCommand extends AdvancedFastSearchCommand {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(CatalogueAdsSearchCommand.class);

    /** String that hold the original untransformed query supplied by the user. */
    private String originalQuery;

    /**
     * String that hold the geographic part of the query, supplied in the
     *  where field in the frontend.
     */
    private String queryGeoString = null;

    /**
     * Two different queries are executed by this command each time the command is
     * executed.
     */
    private enum QueryType {
        GEO,            // include user supplied geographic in query
        INGENSTEDS      // use "ingensteds" as geographic in query.
    }

    /** The query type to run. */
    private QueryType whichQueryToRun;

    /** Constant for no defined geographic location. */
    private final static String DOMESTIC_SEARCH = "ingensteds";

    /**
     * Creates a new instance of CatalogueAdsSearchCommand.
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public CatalogueAdsSearchCommand(final Context cxt) {

        super(cxt);

        final CatalogueAdsCommandConfig conf = (CatalogueAdsCommandConfig) cxt
                .getSearchConfiguration();

        final String whereParameter = conf.getQueryParameterWhere();

        /**
         *  Use the geographic parameters in "where" if supplied by user.
         *  If user hasent supplied any geographic, use "ingensteds" instead.
         */
        if (getSingleParameter(whereParameter) != null
                && getSingleParameter(whereParameter).length() > 0) {

            final ReconstructedQuery queryGeo
                    = createQuery(getSingleParameter(whereParameter));

            queryGeoString = queryGeo.getQuery().getQueryString().replaceAll("\\W", "").toLowerCase();
        } else {
            queryGeoString = DOMESTIC_SEARCH;
        }        
        
        
        
        if(getSingleParameter("who")!=null) {
            originalQuery = getSingleParameter("who").replaceAll("\\W", "").toLowerCase();            
        }else{
            originalQuery = super.getTransformedQuery().replaceAll("\\W", "").toLowerCase();
        }
        
        LOG.info("CatalogueAdsSearch Debug output");
        LOG.info("Who: "+originalQuery);
        LOG.info("Where: "+queryGeoString);
        LOG.info("Q: "+getQuery().getQueryString());
        
    }

    /**
     * Execute search command.
     *
     * Run first query, which fetch all sponsorlinks
     * for a given geographic place.
     *
     * If there was 5 sponsor links found in first
     * search result, exit and return them from method.
     *
     * If there was less than 5 sponsor links from a
     * specific geographic place, we could add sponsor
     * links that does not have any specific geographic.
     *
     * Check where the search results from the first query
     * were located in the list, index 1 to 5 and add them to
     * the right spot.
     *
     * Run second query, which fetch all sponsod links
     * for a given set of keywords, without geographic.
     *
     * Check where the search results from the second query
     * were located in the list, index 1 to 5 and add them to
     * the right spot if still available (the spot is still NULL)
     *
     * Reverse result list,
     * index 5, is the most valued spot, and should be presented first in list.
     * index 1, is the least valued spot, and is presented last in list.
     *
     * @see no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand#execute
     */
    @Override
    public ResultList<? extends ResultItem> execute() {

        /**
         * search result from first query, this will be reused and returned
         * out from this method, with processed search result if needed.
         */
        ResultList<ResultItem> firstQueryResult = null;

        ResultList<? extends ResultItem> secondQueryResult = null;

        // if the query type is domestic, then we dont need to run more than 
        // the domestic search and return the results the way they were returned
        // by Fast.
        if(queryGeoString.equals(DOMESTIC_SEARCH)){
            whichQueryToRun = QueryType.INGENSTEDS;
            firstQueryResult = (ResultList<ResultItem>) super.execute();   
            return firstQueryResult;
        }
        
        whichQueryToRun = QueryType.GEO;
        firstQueryResult = (ResultList<ResultItem>) super.execute();

        if (firstQueryResult.getHitCount() < 5) {

            // Build the search result to return from this method
            // during processing in this array.
            final ResultItem[] searchResults = new ResultItem[5];

            for (ResultItem item : firstQueryResult.getResults()) {
            
                Pattern p = Pattern.compile("(^|;)"+originalQuery+queryGeoString+"(;|$)");
                
                int i = 0;
                boolean found = false;
                for(; i < 5 ; i++){
                    if(item.getField("iypspkeywords"+(i+1))!=null){
                        Matcher m = p.matcher(item.getField("iypspkeywords"+(i+1)).trim().toLowerCase());
                        found = m.find();

                        if(found){
                            break;
                        }
                    }
                }

                if(found){
                    searchResults[i] = item;
                    LOG.info("Fant sponsortreff for plass " + (i+1) + ", " + item.getField("iypspkeywords"+(i+1)));
                }else{
                    LOG.error("Fant IKKE sponsortreff, det er ikke mulig.");
                    LOG.error("iypspkeywords5:"+item.getField("iypspkeywords5").trim().toLowerCase()+"\n"
                            + "iypspkeywords4:"+item.getField("iypspkeywords4").trim().toLowerCase()+"\n"
                            + "iypspkeywords3:"+item.getField("iypspkeywords3").trim().toLowerCase()+"\n"
                            + "iypspkeywords2:"+item.getField("iypspkeywords2").trim().toLowerCase()+"\n"
                            + "iypspkeywords1:"+item.getField("iypspkeywords1").trim().toLowerCase());
                    LOG.error("Pattern was: "+ p.pattern());
                    
                    throw new IllegalStateException("Missing sponsor link in resultset, " +
                            "it was returned from qserver but not found by code.");
                }
            }



            // run second query, which fetch all sponsorlinks
            // for a given set of keywords, without geographic.
            whichQueryToRun = QueryType.INGENSTEDS;
            performQueryTransformation();
            secondQueryResult = super.execute();
            for (ResultItem item : secondQueryResult.getResults()) {

                Pattern p = Pattern.compile("(^|;)"+originalQuery+DOMESTIC_SEARCH+"(;|$)");

                int i = 0;
                boolean found = false;
                for(; i < 5 ; i++){
                    if(item.getField("iypspkeywords"+(i+1))!=null){
                        Matcher m = p.matcher(item.getField("iypspkeywords"+(i+1)).trim().toLowerCase());
                        found = m.find();

                        if(found) break;
                    }
                }

                if(found && searchResults[i]==null){
                    searchResults[i]=item;
                    LOG.info("Fant sponsortreff for plass " + (i+1) + ", " + item.getField("iypspkeywords"+(i+1)));
                }
            } // end for

            firstQueryResult.removeResults();

            for(int i= searchResults.length-1; 0 <= i;  --i){
                final ResultItem item = searchResults[i];
                if(item!=null){
                    firstQueryResult.addResult(item);
                    LOG.info("Added item "+item.getField("iypcompanyid"));
                }
            }

            firstQueryResult.setHitCount(firstQueryResult.getResults().size());
            LOG.info("Resulting in "+firstQueryResult.getHitCount()+" sponsor links");
        }

        return firstQueryResult;
    }

    /**
     *  Create query for search command.
     *  Based on whichQueryToRun, creates query with user supplied geographic
     *  or query with geograhic set to "ingensteds".
     *
     *  @see no.schibstedsok.searchportal.mode.command.AbstractSearchCommand#getTransformedQuery
     */
    @Override
    public String getTransformedQuery() {

        String query = null;
        String completeQuery = null;

        if (whichQueryToRun == QueryType.GEO) {
            query = originalQuery
            + queryGeoString.replaceAll("\\W", "");
            
            
        } else {
            query = originalQuery
            + DOMESTIC_SEARCH;
            
        }
        
        completeQuery = "iypcfspkeywords5:" + query + " OR iypcfspkeywords4:" + query + " OR iypcfspkeywords3:" + query
            + " OR iypcfspkeywords2:" + query + " OR iypcfspkeywords1:" + query;            
        
        return completeQuery;
    }

    /**
     *  Overriden methods below, because we dont want to output any FQL
     *  syntax in our query, we just want them to return the terms
     *  in one line with spaces between.
     */

    /**
     *  @see no.schibstedsok.searchportal.mode.command.AbstractAdvancedFastSearchCommand
     */
    @Override
    protected void visitImpl(AndClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     *  @see no.schibstedsok.searchportal.mode.command.AbstractAdvancedFastSearchCommand
     */
    @Override
    protected void visitImpl(AndNotClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     *  @see no.schibstedsok.searchportal.mode.command.AbstractAdvancedFastSearchCommand
     */
    @Override
    protected void visitImpl(DefaultOperatorClause clause) {

        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     *  @see no.schibstedsok.searchportal.mode.command.AbstractAdvancedFastSearchCommand
     */
    @Override
    protected void visitImpl(NotClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     *  @see no.schibstedsok.searchportal.mode.command.AbstractAdvancedFastSearchCommand
     */
    @Override
    protected void visitImpl(OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     *  @see no.schibstedsok.searchportal.mode.command.AbstractAdvancedFastSearchCommand
     */
    @Override
    protected void visitImpl(OrClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }
    
    protected void visitXorClause(Visitor visitor, XorClause clause){
        clause.getSecondClause().accept(this);
    }
    
}
