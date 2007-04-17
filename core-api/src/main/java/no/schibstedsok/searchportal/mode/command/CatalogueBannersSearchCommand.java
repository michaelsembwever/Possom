// Copyright (2006-2007) Schibsted Søk AS
/*
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.mode.command.AbstractSearchCommand.ReconstructedQuery;
import no.schibstedsok.searchportal.mode.config.CatalogueBannersSearchConfiguration;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 * Responsible for executing a search against catalogue index to
 * retreive banners for the catalogue search result page.
 *
 * The banners are links to images.
 *
 * @author Stian Hegglund
 * @version $Revision:$
 */
public class CatalogueBannersSearchCommand extends AdvancedFastSearchCommand {
    
    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(CatalogueBannersSearchCommand.class);
    
    /** The user supplied geographic location. */
    private String queryGeoString=null;
    
    /** Constant for no defined geographic location. */
    private static final String DOMESTIC_SEARCH = "ingensteds";
    
    /** Creates a new instance of CatalogueBannersSearchCommand.
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public CatalogueBannersSearchCommand(final Context cxt) {
        
        super(cxt);
                
        final CatalogueBannersSearchConfiguration conf = (CatalogueBannersSearchConfiguration) cxt
                .getSearchConfiguration();
        
        final String whereParameter = conf.getQueryParameterWhere();
        
        /**
         *  Use the geographic parameters in "where" if supplied by user.
         *  If user hasent supplied any geographic, use "ingensteds" instead.
         */
        if(getSingleParameter(whereParameter) != null && getSingleParameter(whereParameter).length()>0){
            final ReconstructedQuery rq = createQuery(getSingleParameter(whereParameter));
            
            final Query query = rq.getQuery();
            
            queryGeoString = query.getQueryString();
            queryGeoString = queryGeoString.replaceAll(" ", "").replace("\"", "");
        }else{
            queryGeoString = "";
        }
    }
    
    /**
     * Creates the query to execute.
     * @see no.schibstedsok.searchportal.mode.command.AbstractSearchCommand#getTransformedQuery
     */
    @Override
    public String getTransformedQuery() {
        return "iypcfbannerkw:"+super.getTransformedQuery()+queryGeoString;
    }

    /**
     * Executes the query and returns the results based on the parameters in modes.xml.
     *
     * @see no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand#execute
     */
    @Override
    public SearchResult execute() {
        return super.execute();
    }
    
    /**
     * Add term to the query.
     */
    protected void visitImpl(final LeafClause clause) {
        String term = getTransformedTerm(clause);
        appendToQueryRepresentation(term);
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
}
