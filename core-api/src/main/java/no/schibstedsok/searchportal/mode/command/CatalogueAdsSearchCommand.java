// Copyright (2006) Schibsted Søk AS
/*
 * WebSearchCommand.java
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.Map;

import org.apache.log4j.Logger;

import no.fast.ds.search.SearchParameter;
import no.schibstedsok.searchportal.mode.command.AbstractSearchCommand.ReconstructedQuery;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.result.SearchResult;

/**
 *
 * A search command for the web search.
 * @author magnuse
 */
public class CatalogueAdsSearchCommand extends FastSearchCommand {
	
    private static final Logger LOG = Logger.getLogger(CatalogueAdsSearchCommand.class);
    
    
    private String queryTwo=null;
    
    /** Creates a new instance of WebSearchCommand.
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public CatalogueAdsSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);

    	// hvis "where" parametern er sendt inn, så tar vi og leser inn query fra
    	// den.
    	if(getSingleParameter("where") != null && getSingleParameter("where").length()>0){
	        final ReconstructedQuery rq = createQuery(getSingleParameter("where"));
	        
	        final Query query = rq.getQuery();
	
	    	queryTwo = query.getQueryString();
    	}else{
    		queryTwo = " ingen";
    	}
	    	
    }

    
    /**
     * Legg til  iypcfspkeywords forran alle ord.
     *
     */
    protected void visitImpl(final LeafClause clause) {
    	appendToQueryRepresentation("iypcfspkeywords:");
        super.visitImpl(clause);
                
    }
    
    
    /**
     * 	hent hele den transformerte querien.
     * 	Den innholder query to også for qeo.
     */
    public String getTransformedQuery() {
    	// hvis det finnes en ekstra query, legg til denne i søket.    	
        return queryTwo!=null ? super.getTransformedQuery()+" iypcfspgeo:\""+queryTwo+"\"" : super.getTransformedQuery();
    }
}
