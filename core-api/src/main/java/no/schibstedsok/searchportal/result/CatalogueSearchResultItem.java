// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

/**
 * A simple implementation of a search result item that may contain products.
 *
 * @author <a href="mailto:lars.johansson@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
public class CatalogueSearchResultItem extends BasicSearchResultItem {

    private ProductResult products = null;

    public void addProducts(ProductResult products) {
    	this.products = products;
    }
    
    public boolean hasProducts(){
    	return this.products != null ? true : false;
    }
    
    public ProductResult getProducts(){
    	return this.products;
    }
    
}
