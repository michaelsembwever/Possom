/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
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
    private boolean searchForInfoPage = false;
    private String sortBy="kw"; // defualtsøket er på keyword

    
    /** Creates a new catalogue search command.
     * TODO. Rewrite from scratch. This is insane.
     **/
    public CatalogueSearchCommand(final Context cxt, final Map parameters) {
    	super(cxt, parameters);

    
    	// hvis "where" parametern er sendt inn, så tar vi og leser inn query fra
    	// den.
    	if(getSingleParameter("where") != null){
	        final ReconstructedQuery rq = createQuery(getSingleParameter("where"));

	    	GeoVisitor geo = new GeoVisitor(); 
	    	geo.visit(rq.getQuery().getRootClause());
	        
	    	queryTwo = geo.getQueryRepresentation();
	    	LOG.info("Dette ble det: "+queryTwo);
    	}
    	
    	if(getSingleParameter("userSortBy")!=null
    			&& getSingleParameter("userSortBy").length()>0){
    		sortBy=getSingleParameter("userSortBy");
    	}else{
    		sortBy="kw";
    	}
    
    
    }

    /** TODO comment me. **/
    public SearchResult execute() {
    	// kjør søk for keyword.
    	searchForName=false;
    	super.performQueryTransformation();
    	SearchResult result = super.execute();
        
    	searchForName=true;
        super.performQueryTransformation();        
        // søk etter firmanavn
        SearchResult nameQueryResult = super.execute();
        
    	// hvis det er angitt at det er sortert på navn, 
        // viser vi treff på navn først. Hvis det er angitt att
        // det skal sorteres på keywords, viser vi keywords først.
        if(sortBy.equals("kw")){
        	
        	
            result.getResults().addAll(nameQueryResult.getResults());
            result.setHitCount(result.getHitCount()+nameQueryResult.getHitCount());
        }else{
        	nameQueryResult.getResults().addAll(result.getResults());
        	nameQueryResult.setHitCount(result.getHitCount()+nameQueryResult.getHitCount());  
        }       
         
        
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
    public String getTransformedQuery() {
    	String query = super.getTransformedQuery();
    	
    	if(queryTwo!=null&&queryTwo.length()>0){
    		query += ") " + QL_AND +" (" + queryTwo+")";
    		query= "("+query;
    	}
    		
    	return query;
    }
    
    /**
     * Legg til  iypcfspkeywords forran alle ord.
     *
     */
    protected void visitImpl(final LeafClause clause) {

    	
    	String transformed = getTransformedTerm(clause);
    	
    	if(searchForName){
    		LOG.info("Add transformed to name query \""+transformed+"\"");
    		appendToQueryRepresentation("(");
    		appendToQueryRepresentation("(iypcfphnavn:"+transformed+")"); 
    		appendToQueryRepresentation(" ANDNOT (");
    		appendToQueryRepresentation("(lemiypcfkeywords:"+transformed+") OR ");
    		appendToQueryRepresentation("(lemiypcfkeywordslow:"+transformed+")");
    		appendToQueryRepresentation(")");
    		appendToQueryRepresentation(")");
    	}else{
    		LOG.info("Add transformed to keyword query \""+transformed+"\"");
    		appendToQueryRepresentation("(lemiypcfkeywords:"+transformed+" ANY lemiypcfkeywordslow:"+transformed+")");
    	}    	
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


    
    /**
     * Query builder for creating a query syntax similar to sesam's own.
     */
    private final class GeoVisitor extends AbstractReflectionVisitor{

        // AbstractReflectionVisitor overrides ----------------------------------------------
        private final StringBuilder sb = new StringBuilder();
        
        /**
         * Returns the generated query.
         *
         * @return The query.
         */
        String getQueryRepresentation() {
            return sb.toString();
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final LeafClause clause) {
        	if(clause.getTerm()!=null && clause.getTerm().length()>0){
        		sb.append("iypcfgeo:"+clause.getTerm());
        	}
        }

        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            if(!(clause.getSecondClause() instanceof NotClause)){
                sb.append(QL_AND);
            }
            clause.getSecondClause().accept(this);
        }
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final OrClause clause) {
        	sb.append("(");
            clause.getFirstClause().accept(this);

            sb.append(QL_OR);

            clause.getSecondClause().accept(this);
            sb.append(")");
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            if(!(clause.getSecondClause() instanceof NotClause)){
                sb.append(QL_AND);
            }
            clause.getSecondClause().accept(this);
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final NotClause clause) {

            final String childsTerm = clause.getFirstClause().getTerm();
            if (childsTerm != null && childsTerm.length() > 0) {
                sb.append(QL_ANDNOT);
                clause.getFirstClause().accept(this);
            }
        }

        
        
        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final AndNotClause clause) {
        	sb.append(QL_ANDNOT);
            clause.getFirstClause().accept(this);
        }
    }
}
