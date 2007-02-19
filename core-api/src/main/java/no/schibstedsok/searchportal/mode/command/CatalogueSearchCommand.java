/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;

import org.apache.log4j.Logger;


/**
 * 
 */
public class CatalogueSearchCommand extends AdvancedFastSearchCommand {

    private static final Logger LOG = Logger.getLogger(CatalogueSearchCommand.class);
    private String queryTwo = null;
    private String queryName = "";
    private boolean searchForName = false;
    private List<String> terms = new ArrayList<String>();
    private boolean searchForInfoPage = false;
    
    
    /** Creates a new catalogue search command.
     * TODO. Rewrite from scratch. This is insane.
     **/
    public CatalogueSearchCommand(final Context cxt, final Map parameters) {
    	super(cxt, parameters);

    	// hvis "where" parametern er sendt inn, så tar vi og leser inn query fra
    	// den.
    	if(getSingleParameter("where") != null){
	        final ReconstructedQuery rq = createQuery(getSingleParameter("where"));
	        final Query query = rq.getQuery();
	
	    	queryTwo = query.getQueryString();
    	}
    	
    	if(getSingleParameter("companyid")!=null){
    		searchForInfoPage=true;
    	}
    }

    /** TODO comment me. **/
    public SearchResult execute() {
        LOG.debug("execute()");
    	
        // kør vanligt søk, keywords.
        searchForName=false;
    	SearchResult result = super.execute();
        
    	searchForName=true;
    	
        // søk etter firmanavn
        SearchResult nameQueryResult = super.execute();
        
        // legg til navnsøk.
        result.getResults().addAll(nameQueryResult.getResults());
        result.setHitCount(result.getHitCount()+nameQueryResult.getHitCount());
        
        // konverter til denne.
        List<CatalogueSearchResultItem> nyResultListe = new ArrayList<CatalogueSearchResultItem>();
		
    	//TODO: get all keys to lookup and execute one call instead of iterating like this...
    	Iterator iter = result.getResults().listIterator();
    	
    	
    	while (iter.hasNext()) {
    		BasicSearchResultItem basicResultItem = (BasicSearchResultItem) iter.next();

    		CatalogueSearchResultItem resultItem = new CatalogueSearchResultItem();
    		for(Object o : basicResultItem.getFieldNames()){
    			String s = (String) o;
    			String v = basicResultItem.getField(s);
    			resultItem.addField(s,v);
    		}
    		
    		nyResultListe.add(resultItem);
    	}
    	
    	// fjern de gamle BasicResultItems, og erstatt dem med nye CatalogueResultItems. 
    	result.getResults().clear();
    	result.getResults().addAll(nyResultListe);
    	
    	return result;
    }
    
    @Override
    protected String getAdditionalFilter() {
    	String query=null;
    	
		// hvis det finnes en ekstra query, legg til denne i søket som et filter.	
		query = queryTwo!=null&&queryTwo.length()>0 ? " +iypcfgeo:\""+queryTwo.trim()+"\"" : "";
    	
		return query;
    }
    
    /**
     * Legg til  iypcfspkeywords forran alle ord.
     *
     */
    protected void visitImpl(final LeafClause clause) {
    	String transformed = getTransformedTerm(clause);
    	terms.add(transformed);

    	appendToQueryRepresentation(transformed);
    }
    
    
    @Override
    public String getTransformedQuery() {    	
    	String query="";
    	
    	if(searchForInfoPage){ 
    		return super.getTransformedQuery();
    	}
    	
    	if(!searchForName){
    		String query1="(";
    		String query2="(";
    		// (lemiypcfkeywords:ord1 AND lemiypcfkeywords:ord2 AND lemiypcfkeywords:ord3) ANY
    		// (lemiypcfkeywordslow:ord1 AND lemiypcfkeywordslow:ord2 AND lemiypcfkeywordslow:ord3) 
    		
    		Iterator<String> it = terms.iterator();
    		for(;it.hasNext();){
    			String t = it.next();
    			query1 += "lemiypcfkeywords:"+t;
    			query2 += "lemiypcfkeywordslow:"+t;    			
    			
    			// hvis det finnes flere
    			if(it.hasNext()){
    				query1 += " "+QL_AND+" ";
    				query2 += " "+QL_AND+" ";
    			}
    		}

    		// close queries.
			query1 += ") ANY";
			query2 += ")";
			query = query1 + query2;    		
    		
    	}else{
        	String query1 = " (";
        	String query2 = " (";
        	String query3 = " (";
        	
        	// eksempel på query.
    		// query1 = (iypcfphnavn:ord1 AND iypcfphnavn:ord2 AND iypcfphnavn:ord3) ANDNOT (
    		// query2 = (lemiypcfkeywords:ord1 AND lemiypcfkeywords:ord2 AND lemiypcfkeywords:ord3) OR
    		// query3 = (lemiypcfkeywordslow:ord1 AND lemiypcfkeywordslow:ord2 AND lemiypcfkeywordslow:ord3))    	

    		Iterator<String> it = terms.iterator();
    		for(;it.hasNext();){
    			String t = it.next();
    			query1 += "iypcfphnavn:"+t;
    			query2 += "lemiypcfkeywords:"+t;    			
    			query3 += "lemiypcfkeywordslow:"+t;
    			
    			// hvis det finnes flere
    			if(it.hasNext()){
    				query1 += " "+QL_AND+" ";
    				query2 += " "+QL_AND+" ";
    				query3 += " "+QL_AND+" ";
    			}
    		}

    		// close queries.
			query1 += ") ANDNOT (";
			query2 += ") OR";
			query3 += "))";
			query=query1+query2+query3;
    	}

    	return query;
    }
    
    
    @Override
    protected String getSortBy() {
    	// hvis man søker etter firmanavn, sorterer vi etter "iyprpnavn"
    	// ellers søker vi etter keywords, og da sorterer vi etter "iyprpkw" istedet.
    	String sortBy="iyprpkw";
    	if(searchForName){
    		sortBy="iyprpnavn";
    	}
    	return sortBy;
    }    

}
