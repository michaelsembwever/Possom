/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import no.schibstedsok.searchportal.query.IntegerClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.PhoneNumberClause;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenMatch;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.ProductResult;
import no.schibstedsok.searchportal.result.ProductResultItem;
import no.schibstedsok.searchportal.result.ProductSearchResult;
import no.schibstedsok.searchportal.result.ProductSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.YellowSearchResult;

import org.apache.log4j.Logger;

/**
 * TODO. Rewrite from scratch. This class is insane.
 * 
 * COMMENT: Jag har tagit utgångspunkt i YellowCommand. Det är massa i denna klass
 * som jag inte har koll på i detalj!
 * 
 * top3 är gamla (dagens) import av produkter från IK som ska bytas ut. 
 *
 * Begreppet local är jag osäker på?
 * 
 * 
 */
public class CatalogueSearchCommand extends AbstractSimpleFastSearchCommand {

    private static final Logger LOG = Logger.getLogger(CatalogueSearchCommand.class);

    private boolean ignoreGeoNav = false;

    private boolean isLocal;

    private boolean isTop3 = false;

    private boolean ypkeywordsgeo = false;

    private StringBuilder filterBuilder = null;

    boolean exactCompany;
    boolean companyRank = false;

    private boolean correct = false;

    /** Creates a new catalogue search command.
     * TODO. Rewrite from scratch. This is insane.
     **/
    public CatalogueSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    /** TODO comment me. **/
    protected Map getNavigators() {

        if (ignoreGeoNav && super.getNavigators() != null) {
            final Map m = new HashMap();
            m.putAll(super.getNavigators());
            m.remove("geographic");
            return m;
        }

        return super.getNavigators();
    }

    /** TODO comment me. **/
    public SearchResult execute() {
        LOG.error("Execute");

        boolean viewAll = false;

        if (getParameters().containsKey("ypviewall")) {
            viewAll = true;
        }


        if (isLocalSearch() && !viewAll) {
            correct = false;
            ignoreGeoNav = true;
            isLocal = false;
            ypkeywordsgeo = true;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            correct = false;
            ignoreGeoNav = false;
            isTop3 = true;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;

            // Perform local search.
            correct = true;
            ignoreGeoNav = false;
            isLocal = true;
            final FastSearchResult localResult = (FastSearchResult) super.execute();

            final YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, isLocalSearch() && !viewAll);

            final String yprank = companyRank ? "company" : "default";
            result.addField("yprank", yprank);

            return result;
        } else if (!viewAll) {
            isLocal = false;
            isTop3 = true;
            correct = false;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;
            ypkeywordsgeo = true;
            correct = true;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            final YellowSearchResult result = new YellowSearchResult(this, null, nationalHits, top3, false);
            
            fetchProducts(result);
            final String yprank = companyRank ? "company" : "default";
            result.addField("yprank", yprank);

            return result;
        } else {
            correct = true;
            ypkeywordsgeo = false;
            isLocal = true;
            ignoreGeoNav = true;
            final FastSearchResult localResult = (FastSearchResult) super.execute();
            ignoreGeoNav = false;
            isLocal = false;

            isTop3 = true;
            correct = false;
            final FastSearchResult top3 = (FastSearchResult) super.execute();
            isTop3 = false;

            isLocal = false;
            ypkeywordsgeo = true;
            correct = false;
            final FastSearchResult nationalHits = (FastSearchResult) super.execute();
            ypkeywordsgeo = false;

            final String yprank = companyRank ? "company" : "default";

            final YellowSearchResult result = new YellowSearchResult(this, localResult, nationalHits, top3, false);

            result.addField("yprank", yprank);
            return result;
        }
    }

    private boolean isLocalSearch() {
        return getRunningQuery().getGeographicMatches().size() > 0;
    }


    private TokenMatch getLastGeoMatch() {
        final List<TokenMatch> matches = getRunningQuery().getGeographicMatches();

        if (matches.size() > 0) {
            return matches.get(matches.size() - 1);
        } else {
            return null;
        }
    }


    /** TODO comment me. **/
    public String getTransformedQuery() {

        final String t = super.getTransformedQuery();

        final TokenEvaluationEngine engine = context.getTokenEvaluationEngine();

        exactCompany = engine.evaluateQuery(TokenPredicate.EXACT_COMPANYRANK, context.getQuery());

        companyRank = exactCompany && !isTop3 && !getParameter("yprank").equals("standard") || getParameter("yprank").equals("company");

        if (companyRank) {
            return t.replaceAll("yellowphon", "yellownamephon");
        }

        if (isTop3) {
            return t.replaceAll("yellowphon:", "").replaceAll("-", " ");
        }

        if (isLocal) {
            return t.replaceAll("-", " ");
        } else {
            return t.replaceAll("yellowphon", "yellowgeophon").replaceAll("-", " ");
        }
    }


    /** TODO comment me. **/
    protected boolean isCorrectionEnabled() {
        return correct;
    }

    /** TODO comment me. **/
    protected int getResultsToReturn() {
        if (isTop3) {
            return 3;
        } else {
            return super.getResultsToReturn();
        }
    }

    /** TODO comment me. **/
    protected String getAdditionalFilter() {

        synchronized (this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuilder(super.getAdditionalFilter());
            }

            return ypkeywordsgeo && getLastGeoMatch() != null
                    ? filterBuilder.toString() + " +ypkeywordsgeo:" + getLastGeoMatch().getMatch()
                    : filterBuilder.toString();
        }
    }

    /** TODO comment me. **/
    protected String getSortBy() {

        final TokenEvaluationEngine engine = context.getTokenEvaluationEngine();

        if (engine.evaluateQuery(TokenPredicate.EXACT_COMPANYRANK, context.getQuery())) {
            return "yellowname";
        }

        return (isLocal ? "yellowpages2new +ypnavn" : "yellowpages2geo +ypnavn");
    }

    /** TODO comment me. **/
    public String getQueryInfo() {
        return getTransformedQuery() + " " + getSortBy() + " " + getAdditionalFilter();
    }


    // Query Builder

    private static final String PREFIX_INTEGER="yellowpages:";
    private static final String PREFIX_PHONETIC="yellowphon:";

    /**
     * Adds non phonetic prefix to integer terms.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final IntegerClause clause) {
        if (!getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER);
        }

        super.visitImpl(clause);
    }

    /**
     * Adds non phonetic prefix to phone number terms.
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final PhoneNumberClause clause) {
        if (!getTransformedTerm(clause).equals("")) {
            appendToQueryRepresentation(PREFIX_INTEGER);
        }
        super.visitImpl(clause);
    }

    /**
     * Adds phonetic prefix to a leaf clause.
     * Remove dots from words. (people, street, suburb, or city names do not have dots.)
     *
     * @param clause The clause to prefix.
     */
    protected void visitImpl(final LeafClause clause) {
        
        if (clause.getField() == null) {
            if (!getTransformedTerm(clause).equals("")) {
                appendToQueryRepresentation(PREFIX_PHONETIC 
                        + getTransformedTerm(clause).replaceAll("\\.", " "));
            }
            
        }else if(null == getFieldFilter(clause)){
            
            if (!getTransformedTerm(clause).equals("")) {
                // we also accept terms with fields that haven't been permitted for the searchConfiguration
                appendToQueryRepresentation(PREFIX_PHONETIC 
                        + clause.getField() + "\\:" + getTransformedTerm(clause).replaceAll("\\.", " "));
                
            }

        }
    }

    /**
     * An implementation that ignores phrase searches.
     *
     * Visits only the left clause, unless that clause is a clause, in which
     * case only the right clause is visited. Phrase searches are not possible
     * against the yellow index.
     */
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {
        
        // If we have a match on an international phone number, but it is not recognized as
        // a local phone number, force it to use the original number string.
        if (clause.getHint() == XorClause.Hint.PHONE_NUMBER_ON_LEFT
                && !clause.getFirstClause().getKnownPredicates().contains(TokenPredicate.PHONENUMBER)) {
            
            clause.getSecondClause().accept(visitor);
            
        } else if(XorClause.Hint.PHRASE_ON_LEFT == clause.getHint()){
            clause.getSecondClause().accept(visitor);
            
        } else {
            super.visitXorClause(visitor, clause);
        }
    }
    
    /**
     * This function gets all products for the resultList.
     * 
     * @param result
     */
    private void fetchProducts(YellowSearchResult result) {
        LOG.info("fetchProducts");
    			
		if(result == null)
    		return;
    	
		
		List<CatalogueSearchResultItem> nyResultListe = new ArrayList<CatalogueSearchResultItem>();
		
    	//TODO: get all keys to lookup and execute one call instead of iterating like this...
    	Iterator iter = result.getResults().listIterator();
    	
		//placeholder for products
		ProductResult products = new ProductSearchResult();    	
    	
    	while (iter.hasNext()) {
    		BasicSearchResultItem basicResultItem = (BasicSearchResultItem) iter.next();

    		CatalogueSearchResultItem resultItem = new CatalogueSearchResultItem();
    		for(Object o : basicResultItem.getFieldNames()){
    			String s = (String) o;
    			String v = basicResultItem.getField(s);
    			resultItem.addField(s,v);
    		}
    		
    		nyResultListe.add(resultItem);
    		
    		//jdbc version
    		Connection con = null;
    		try {
    			InitialContext ctxt = new InitialContext();
        		DataSource ds = (DataSource) ctxt.lookup("java:ag");
        		
        		con = ds.getConnection();
				
				PreparedStatement stmt = con
						.prepareStatement("select S.companyId, C.organizationNo, C.businessName, S.textShort, S.text1" +
										  " from AG_SALE S, AG_COMPANY C " +
										  " where C.companyId = S.companyId and C.organizationNo = ? and toDate > now() and S.text1 is not null");
				stmt.setString(1,resultItem.getField("yporgnummer"));
				LOG.info("Hent produkter for firma med organisasjonsnr. "+resultItem.getField("yporgnummer"));
				
				ResultSet res = stmt.executeQuery();
				
				if(res.next()){
					
					//listingProducts goes on the result listing page
					if(res.getString("textShort") != null){
						ProductResultItem listingProduct = new ProductSearchResultItem();
						listingProduct.addField("shortText", res.getString("textShort"));
						products.addListingResult(listingProduct);
					}

					//infoPageProducts goes on the infopage
					if(res.getString("text1") != null){
						ProductResultItem infoPageProduct = new ProductSearchResultItem();
						infoPageProduct.addField("text1", res.getString("text1"));
						products.addInfoPageResult(infoPageProduct);
					}

					//add the products to the searchResult
					if(products.hasInfoPageProducts() || products.hasListingProducts())
						resultItem.addProducts(products);

				}else{
			        LOG.info("ingen produkter funnet for firma.");
				}
				
				
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			} finally{
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
    		
			
		}
    	
    	// bytt ut den gamle mot den nye listen.
    	result.getResults().clear();
    	result.getResults().addAll(nyResultListe);
    }
     

}
