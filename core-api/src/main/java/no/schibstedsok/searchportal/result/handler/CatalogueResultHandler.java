// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;

import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.config.FastSearchConfiguration;
import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.ProductResult;
import no.schibstedsok.searchportal.result.ProductResultItem;
import no.schibstedsok.searchportal.result.ProductSearchResult;
import no.schibstedsok.searchportal.result.ProductSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;

import org.apache.log4j.Logger;


/**
 * Resulthandler to fetch sales from the catalogue sales system.
 * This class is called after a search command with <catalogue/> 
 * resulthandler tag is defined. It loops through all result
 * items and load data with sql/jdbc from the sales system.
 * 
 * @todo Replace all JDBC/SQL code with call to Webservice in
 * 		 sales system.
 * 
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Revision: 3436 $</tt>
 */
public final class CatalogueResultHandler implements ResultHandler {
    
    private static final Logger LOG = Logger.getLogger(CatalogueResultHandler.class);
    private String url = null;
    private String username = null;
    private String password = null;
    
    public void handleResult(final Context cxt, final Map parameters) {
    	LOG.info("Starter Catalogue ResultHandler.");
    	
        final SiteConfiguration siteConf
    	= SiteConfiguration.valueOf(ContextWrapper.wrap(SiteConfiguration.Context.class, cxt));

        url = siteConf.getProperty("catalogue_ds.db");
		username = siteConf.getProperty("catalogue_ds.username");
		password = siteConf.getProperty("catalogue_ds.password");

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
		
				
			Class.forName("com.mysql.jdbc.Driver");

	        con 
	      		= DriverManager.getConnection(url,username,password);    		
    		
			stmt = con.prepareStatement("select S.companyId, C.organizationNo, C.businessName, S.textShort, S.text1" +
					  " from AG_SALE S, AG_COMPANY C " +
					  " where C.companyId = S.companyId and C.organizationNo = ? and toDate > now() and S.text1 is not null");
    		
	    	while (iter.hasNext()) {
	    		CatalogueSearchResultItem resultItem = (CatalogueSearchResultItem) iter.next();
	
					
					stmt.setString(1,resultItem.getField("iyporgnr"));
					LOG.info("Hent produkter for firma med organisasjonsnr: "+resultItem.getField("iyporgnr"));
					
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
			}catch(ClassNotFoundException e){
				LOG.error("ClassNotFoundException, Feil ved uthenting av jdbc driver",e);
			}finally {
			      try {if (res != null) res.close();} catch (SQLException e) { LOG.error("Could not close ResultSet",e);}
			      try {if (stmt != null) stmt.close();} catch (SQLException e) { LOG.error("Could not close Statement",e);}
			      try {if (con != null) con.close();} catch (SQLException e) { LOG.error("Could not close Connection",e);}
			    
			}
    		
			
    	
    }
         
    
    
}
