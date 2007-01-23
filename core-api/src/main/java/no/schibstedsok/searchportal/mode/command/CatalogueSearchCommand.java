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

    /** Creates a new catalogue search command.
     * TODO. Rewrite from scratch. This is insane.
     **/
    public CatalogueSearchCommand(final Context cxt, final Map parameters) {
    	super(cxt, parameters);
    	LOG.info("CatalogueSearchCommand constructor.");
    	LOG.info("Where:"+getSingleParameter("where"));

    	// hvis "where" parametern er sendt inn, så tar vi og leser inn query fra
    	// den.
    	if(getSingleParameter("where") != null){
	        final ReconstructedQuery rq = createQuery(getSingleParameter("where"));
	        final Query query = rq.getQuery();
	
	    	queryTwo = query.getQueryString();

    	}
   
    }

    /** TODO comment me. **/
    public SearchResult execute() {
        LOG.info("execute()");
    	

    	
        // kør vanligt søk, keywords.
        searchForName=false;
    	LOG.info("Søk med keyword query is :" + getTransformedQuery());     
    	SearchResult result = super.execute();
        
    	
    	searchForName=true;    	
    	super.performQueryTransformation();
    	
        // søk etter firmanavn
    	LOG.info("Søk med firmanavn query is :" + getTransformedQuery());     
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
    	
    	// to ulike søk utføres av denne komponenten, søk med keywords og søk med firmanavn.
    	if(!searchForName){	    	
	    	appendToQueryRepresentation("(lemiypcfkeywords:"+transformed+" ANY lemiypcfkeywordslow:"+transformed+")");
    	}else{
    		appendToQueryRepresentation("(iypcfnavn:"+transformed+" ANDNOT (lemiypcfkeywords:"+transformed+" OR lemiypcfkeywordslow:"+transformed+"))");
    		
    	}	    	
    }
    
    
    protected void visitImpl(final AndClause clause) {
    	super.visitImpl(clause);
    }    
    
    @Override
    protected void visitImpl(DefaultOperatorClause clause) {
    	// TODO Auto-generated method stub
    	super.visitImpl(clause);
    }
    
    @Override
    protected void visitImpl(OrClause clause) {
    	// TODO Auto-generated method stub
    	super.visitImpl(clause);
    }
    
    @Override
    protected void visitImpl(AndNotClause clause) {
    	// TODO Auto-generated method stub
    	super.visitImpl(clause);
    }
    
    @Override
    protected void visitImpl(NotClause clause) {
    	// TODO Auto-generated method stub
    	super.visitImpl(clause);
    }
    
    
}
