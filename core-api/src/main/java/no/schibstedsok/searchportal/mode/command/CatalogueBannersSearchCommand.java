// Copyright (2006-2007) Schibsted Søk AS
/*
 * WebSearchCommand.java
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
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
 *
 * A search command for the web search.
 * @author Stian Hegglund
 * @version $Revision:$
 */
public class CatalogueBannersSearchCommand extends AdvancedFastSearchCommand {

	@Override
	protected void visitImpl(AndClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

	@Override
	protected void visitImpl(AndNotClause clause) {
		clause.getFirstClause().accept(this);
	}

	@Override
	protected void visitImpl(DefaultOperatorClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

	@Override
	protected void visitImpl(NotClause clause) {
		clause.getFirstClause().accept(this);
	}

	@Override
	protected void visitImpl(OperationClause clause) {
		clause.getFirstClause().accept(this);
	}

	@Override
	protected void visitImpl(OrClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

	/** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(CatalogueBannersSearchCommand.class);


    private String queryTwo=null;

    /** Creates a new instance of WebSearchCommand.
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public CatalogueBannersSearchCommand(
            final Context cxt,
            final DataModel datamodel) {

        super(cxt, datamodel);

    	// hvis "where" parametern er sendt inn, så tar vi og leser inn query fra
    	// den.
    	if(getSingleParameter("where") != null && getSingleParameter("where").length()>0){
	        final ReconstructedQuery rq = createQuery(getSingleParameter("where"));

	        final Query query = rq.getQuery();

	    	queryTwo = query.getQueryString();
	    	queryTwo = queryTwo.replaceAll(" ", "");
    	}else{
    		queryTwo = "ingensteds";
    	}

    	LOG.info("Search configuration name "+getSearchConfiguration().getName());
    }

    @Override
    public String getTransformedQuery() {
    	// TODO Auto-generated method stub
    	return "iypcfbannerkw:"+super.getTransformedQuery()+queryTwo;
    }
    
    @Override
    public SearchResult execute() {
    	SearchResult r = null;

    	// TODO Auto-generated method stub
    	r = super.execute();
    	return r;
    }

    /**
     * Legg til  iypcfspkeywords forran alle ord.
     *
     */
    protected void visitImpl(final LeafClause clause) {

    	String term = getTransformedTerm(clause);
    	appendToQueryRepresentation(term);
    }
}
