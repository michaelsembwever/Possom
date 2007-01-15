// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.ProductResult;
import no.schibstedsok.searchportal.result.ProductResultItem;
import no.schibstedsok.searchportal.result.ProductSearchResult;
import no.schibstedsok.searchportal.result.ProductSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 3436 $</tt>
 */
public final class CatalogueResultHandler implements ResultHandler {
    
    private static final Logger LOG = Logger.getLogger(CatalogueResultHandler.class);
    
    public void handleResult(final Context cxt, final Map parameters) {
    	LOG.info("Starter Catalogue ResultHandler.");

    	final SearchResult result = cxt.getSearchResult();

        fetchProducts(result);
        
    }

    
    /**
     * This function gets all products for the resultList.
     * 
     * @param result
     */
    private void fetchProducts(SearchResult result) {
        LOG.info("fetchProducts");
		
        // if result is missing, just exit this method
        if(result == null)
    		return;
    	
        
        //jdbc objects.
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet res = null;
        
    	Iterator iter = result.getResults().listIterator();
    	
		//placeholder for products
		ProductResult products = new ProductSearchResult();    	
		try {
			InitialContext ctxt = new InitialContext();
    		DataSource ds = (DataSource) ctxt.lookup("java:comp/env/jdbc/catalogue");
    		
    		con = ds.getConnection();

			stmt = con.prepareStatement("select S.companyId, C.organizationNo, C.businessName, S.textShort, S.text1" +
					  " from AG_SALE S, AG_COMPANY C " +
					  " where C.companyId = S.companyId and C.organizationNo = ? and toDate > now() and S.text1 is not null");
    		
	    	while (iter.hasNext()) {
	    		CatalogueSearchResultItem resultItem = (CatalogueSearchResultItem) iter.next();
	
					
					stmt.setString(1,resultItem.getField("iyporgnr"));
					LOG.info("Hent produkter for firma med organisasjonsnr> "+resultItem.getField("iyporgnr"));
					
					res = stmt.executeQuery();
					LOG.info("sql er kjørt");
					
					if(res.next()){
						LOG.info("Finne produkter for firma, les inn data.");
						ProductResultItem infoPageProduct = new ProductSearchResultItem();
						
	
						//infoPageProducts goes on the infopage
						if(res.getString("text1") != null){
							infoPageProduct.addField("text1", res.getString("text1"));
							LOG.info("lagt inn text1 felt i produktet: "+infoPageProduct.getField("text1"));
						}
						//infoPageProducts goes on the infopage
						if(res.getString("textShort") != null){
							infoPageProduct.addField("textShort", res.getString("textShort"));
							LOG.info("lagt inn textShort felt i produktet: "+infoPageProduct.getField("textShort"));
						}
	
						products.addInfoPageResult(infoPageProduct);
	
						//add the products to the searchResult
						if(products.hasInfoPageProducts() || products.hasListingProducts())
							resultItem.addProducts(products);
	
					}else{
				        LOG.info("ingen produkter funnet for firma.");
					}
					
	    		} // end while.
				
			} catch (SQLException e) {
				LOG.error("SQLException, Feil ved uthenting av produkter",e);

			} catch (NamingException e) {
				LOG.error("NamingException, Feil ved uthenting av produkter",e);
			}finally {
			      try {if (res != null) res.close();} catch (SQLException e) { LOG.error("Could not close ResultSet",e);}
			      try {if (stmt != null) stmt.close();} catch (SQLException e) { LOG.error("Could not close Statement",e);}
			      try {if (con != null) con.close();} catch (SQLException e) { LOG.error("Could not close Connection",e);}
			    
			}
    		
			
    	
    }
         
    
    
}
