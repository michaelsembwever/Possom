// Copyright (2006) Schibsted Søk AS
/*
 * WebSearchCommand.java
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.Formatter;
import java.util.Map;

import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.Query;

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
    public CatalogueAdsSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);

    	// hvis "where" parametern er sendt inn, så tar vi og leser inn query fra
    	// den.
    	if(getSingleParameter("where") != null && getSingleParameter("where").length()>0){
	        final ReconstructedQuery rq = createQuery(getSingleParameter("where"));
	        
	        final Query query = rq.getQuery();
	
	    	queryTwo = query.getQueryString();
    	}else{
    		queryTwo = "ingensteds";
    	}
	    	
    }

    
    /**
     * Legg til  iypcfspkeywords forran alle ord.
     *
     */
    protected void visitImpl(final LeafClause clause) {
    	
    	
//    	subQuery1 += (subQuery1.length()>0?QL_AND:" ") + " iypcfspkeywords1:"+clause.getTerm()+queryTwo;
//    	subQuery2 += (subQuery2.length()>0?QL_AND:" ") + " iypcfspkeywords2:"+clause.getTerm()+queryTwo;
//    	subQuery3 += (subQuery3.length()>0?QL_AND:" ") + " iypcfspkeywords3:"+clause.getTerm()+queryTwo;
//    	subQuery4 += (subQuery4.length()>0?QL_AND:" ") + " iypcfspkeywords4:"+clause.getTerm()+queryTwo;
//    	subQuery5 += (subQuery5.length()>0?QL_AND:" ") + " iypcfspkeywords5:"+clause.getTerm()+queryTwo;    	
    	
    	String term = clause.getTerm();
    	appendToQueryRepresentation("(");
    	appendToQueryRepresentation("( (iypcfspkeywords5:"+term+queryTwo+") OR ((iypcfspkeywords5:"+term+"ingensteds AND iypspgep5:ingensteds) ANDNOT (iypcfspkeywords5:"+term+queryTwo+"))) OR"); 
    	appendToQueryRepresentation("( (iypcfspkeywords4:"+term+queryTwo+") OR ((iypcfspkeywords4:"+term+"ingensteds AND iypspgeo4:ingensteds) ANDNOT (iypcfspkeywords4:"+term+queryTwo+"))) OR"); 
		appendToQueryRepresentation("( (iypcfspkeywords3:"+term+queryTwo+") OR ((iypcfspkeywords3:"+term+"ingensteds AND iypspgeo3:ingensteds) ANDNOT (iypcfspkeywords3:"+term+queryTwo+"))) OR"); 
		appendToQueryRepresentation("( (iypcfspkeywords2:"+term+queryTwo+") OR ((iypcfspkeywords2:"+term+"ingensteds AND iypspgeo2:ingensteds) ANDNOT (iypcfspkeywords2:"+term+queryTwo+"))) OR");
		appendToQueryRepresentation("( (iypcfspkeywords1:"+term+queryTwo+") OR ((iypcfspkeywords1:"+term+"ingensteds AND iypspgeo1:ingensteds) ANDNOT (iypcfspkeywords1:"+term+queryTwo+")))");
    	appendToQueryRepresentation(")");
    }
    
    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final DefaultOperatorClause clause) {
    	LOG.info("visitImpl DefaultOperatorClause");
        clause.getFirstClause().accept(this);

        final boolean hasEmptyLeaf = isEmptyLeaf(clause.getFirstClause()) || isEmptyLeaf(clause.getSecondClause());

        if (!(hasEmptyLeaf || clause.getSecondClause() instanceof NotClause)) {
            appendToQueryRepresentation("OR");
        }

        clause.getSecondClause().accept(this);    	
    }
    
    /**
     * Returns true iff the clause is a leaf clause and if it will not produce any output in the query representation.
     *
     * @param clause The clause to examine.
     *
     * @return true iff leaf is empty.
     */
    private boolean isEmptyLeaf(final Clause clause) {
        if (clause instanceof LeafClause) {
            final LeafClause leafClause = (LeafClause) clause;
            return getFieldFilter(leafClause) != null || getTransformedTerm(clause).equals("");
        } else {
            return false;
        }
    }    
}
