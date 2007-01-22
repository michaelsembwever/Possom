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

import no.schibstedsok.searchportal.query.LeafClause;
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

    
    String subQuery1 = "";
    String subQuery2 = "";
    String subQuery3 = "";
    String subQuery4 = "";
    String subQuery5 = "";
    
    /**
     * Legg til  iypcfspkeywords forran alle ord.
     *
     */
    protected void visitImpl(final LeafClause clause) {
    	
    	
    	subQuery1 += (subQuery1.length()>0?QL_AND:" ") + " iypcfspkeywords1:"+clause.getTerm()+" ";
    	subQuery2 += (subQuery2.length()>0?QL_AND:" ") + " iypcfspkeywords2:"+clause.getTerm()+" ";
    	subQuery3 += (subQuery3.length()>0?QL_AND:" ") + " iypcfspkeywords3:"+clause.getTerm()+" ";
    	subQuery4 += (subQuery4.length()>0?QL_AND:" ") + " iypcfspkeywords4:"+clause.getTerm()+" ";
    	subQuery5 += (subQuery5.length()>0?QL_AND:" ") + " iypcfspkeywords5:"+clause.getTerm()+" ";    	
    }
    
    
    @Override
    public String getTransformedQuery() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("( ");
    	sb.append(" (%5$s AND iypspgeo5:%6$s) OR ");
    	sb.append(" ( ");
    	sb.append("  (%5$s AND iypspgeo5:ingensteds) ANDNOT ");
    	sb.append("  (%5$s AND iypspgeo5:\"%6$s\") ");
    	sb.append(" ) ");
    	sb.append(") ");
    	sb.append("OR ");
    	sb.append("( ");
    	sb.append(" (%4$s AND iypspgeo4:%6$s) OR ");
    	sb.append(" ( ");
    	sb.append("  (%4$s AND iypspgeo4:ingensteds) ANDNOT ");
    	sb.append("  (%4$s AND iypspgeo4:\"%6$s\") ");
    	sb.append(" ) ");
    	sb.append(") ");
    	sb.append("OR ");
    	sb.append("( ");
    	sb.append(" (%3$s AND iypspgeo3:%6$s) OR ");
    	sb.append(" ( ");
    	sb.append("  (%3$s AND iypspgeo3:ingensteds) ANDNOT ");
    	sb.append("  (%3$s AND iypspgeo3:\"%6$s\") ");
    	sb.append(" ) ");
    	sb.append(") ");
    	sb.append("OR ");
    	sb.append("( ");
    	sb.append(" (%2$s AND iypspgeo2:\"%6$s\") OR ");
    	sb.append(" ( ");
    	sb.append("  (%2$s AND iypspgeo2:ingensteds) ANDNOT ");
    	sb.append("  (%2$s AND iypspgeo2:\"%6$s\") ");
    	sb.append(" ) ");
    	sb.append(") ");
    	sb.append("OR ");
    	sb.append("( ");
    	sb.append(" (%1$s AND iypspgeo1:\"%6$s\") OR ");
    	sb.append(" ( ");
    	sb.append("  (%1$s AND iypspgeo1:ingensteds) ANDNOT ");
    	sb.append("  (%1$s AND iypspgeo1:\"%6$s\") ");
    	sb.append(" ) ");
    	sb.append(")");    	
    	
    	StringBuilder result = new StringBuilder();
    	Formatter formatter = new Formatter(result);

    	formatter.format(sb.toString(), subQuery1, subQuery2, subQuery3,subQuery4, subQuery5,queryTwo);
    	
    	LOG.info("Sponsorquery: "+result);
    	return result.toString();
    }
}
