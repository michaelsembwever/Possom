// Copyright (2006-2007) Schibsted Søk AS
/*
 * WebSearchCommand.java
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 *
 * A search command for the web search.
 * @author magnuse
 */
public class CatalogueAdsSearchCommand extends AdvancedFastSearchCommand {

    private static final Logger LOG = Logger.getLogger(CatalogueAdsSearchCommand.class);


    private String queryTwo=null;

    /** Creates a new instance of WebSearchCommand.
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public CatalogueAdsSearchCommand(
            final Context cxt,
            final DataModel datamodel) {

        super(cxt, datamodel);

    	// hvis "where" parametern er sendt inn, så tar vi og leser inn query fra
    	// den.
    	if(getSingleParameter("where") != null && getSingleParameter("where").length()>0){
	        final ReconstructedQuery rq = createQuery(getSingleParameter("where"));

	        final Query query = rq.getQuery();

	    	queryTwo = query.getQueryString();
    	}else{
    		queryTwo = "ingensteds";
    	}

    	LOG.info("Search configuration name "+getSearchConfiguration().getName());
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
    	appendToQueryRepresentation("(");
    	appendToQueryRepresentation("( (iypcfspkeywords5:"+term+queryTwo+") OR ((iypcfspkeywords5:"+term+"ingensteds) ANDNOT (iypcfspkeywords5:"+term+queryTwo+"))) OR");
    	appendToQueryRepresentation("( (iypcfspkeywords4:"+term+queryTwo+") OR ((iypcfspkeywords4:"+term+"ingensteds) ANDNOT (iypcfspkeywords4:"+term+queryTwo+"))) OR");
		appendToQueryRepresentation("( (iypcfspkeywords3:"+term+queryTwo+") OR ((iypcfspkeywords3:"+term+"ingensteds) ANDNOT (iypcfspkeywords3:"+term+queryTwo+"))) OR");
		appendToQueryRepresentation("( (iypcfspkeywords2:"+term+queryTwo+") OR ((iypcfspkeywords2:"+term+"ingensteds) ANDNOT (iypcfspkeywords2:"+term+queryTwo+"))) OR");
		appendToQueryRepresentation("( (iypcfspkeywords1:"+term+queryTwo+") OR ((iypcfspkeywords1:"+term+"ingensteds) ANDNOT (iypcfspkeywords1:"+term+queryTwo+")))");
    	appendToQueryRepresentation(")");
    }
}
