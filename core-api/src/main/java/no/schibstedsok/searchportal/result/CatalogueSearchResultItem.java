/*
 * $Id:$
 */
// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

/**
 * A simple implementation of a search result item that may contain products.
 *
 * @author <a href="mailto:lars.johansson@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
public class CatalogueSearchResultItem extends BasicSearchResultItem {

	/**
	 * TODO: javadoc.
	 */
    private ProductResult products = null;

    /**
     * TODO javadoc.
     * @param products
     */
    public void addProducts(final ProductResult products) {
    	this.products = products;
    }
    
    /**
     * TODO javadoc.
     * @return
     */
    public boolean hasProducts(){
    	return this.products != null ? true : false;
    }
    
    /**
     * TODO  javadoc.
     * @return
     */
    public ProductResult getProducts(){
    	return this.products;
    }
    
    /**
     * Utility method for checking if the entry belongs to a paying customer.
     * @return true if the result item is an entry for a paying customer.
     */
    public boolean isCommercial(){
    	String productPackage = getField("iyppakke");
    	if(productPackage != null && productPackage.length() > 0){
    		return true;
    	}
    	return false;
    }
    
    /**
     * Responsible for getting the phone number to display for this result item.
     * @return The phone number to display for this result item.
     */
    public String getPhoneNumber(){
    	String phoneNumber = getField("iypnrtelefon");
    	if(phoneNumber != null && phoneNumber.length() > 0){
    		return phoneNumber;
    	}
    	return null;
    }
    
    /**
     * Returns a description of the result item. 
     * @return  a description of the result item.
     */
    public String getCompanyDescription(){
    	if(!isCommercial()){
    		return null;
    	}
    	
    	String description = getField("iyplogotekst");
    	
    	if(description != null && description.length() > 0){
    		return description;
    	}
    	return "en lang fortelling på mer en 40 tegn blir nok choppet etter en stund.";
    }
    
    /**
     * Returns the url for the logo to display for this result item.
     * @return URL for the logo to display.
     */
    public String getLogoURL(){
    	//TODO: check product
    	if(!isCommercial()){
    		return null;
    	}
    	
    	final String logoURL = getField("iyplogourl");
    	if(logoURL != null && logoURL.length() > 0){
    		return logoURL;
    	}
    	return null;
    }
    
    
    /**
     * Returns the address for for this result item.
     * @return The address in format: street, zipcode city.
     */
    public String getAddress(){
       
    	String address = getField("iypadresse");
    	String zipCode = getField("iyppostnr");
    	String city = getField("iyppoststed");
    	
    	StringBuffer compositAddress = new StringBuffer();
    	if(address != null && address.length() > 0){
    		compositAddress.append(address);
    		compositAddress.append(", ");
    	}
    	
    	if(zipCode != null && zipCode.length() > 0){
    		compositAddress.append(zipCode);
    	}
    	
    	if(city != null && city.length() > 0){
    		compositAddress.append(" ");
    		compositAddress.append(city);
    	}
    	
    	if(compositAddress.length() > 0){
    		return compositAddress.toString();
    	}
    	return null;
    }
    
    public String getEmailAddress(){
    	//TODO: check product.
    	if(!isCommercial()){
    		return null;
    	}
    	
    	String email = getField("iypepost");
    	if(email != null && email.length() > 0){
    		return email;
    	}
    	return null;
    }
    
    /**
     * Returns the url of the homepage for this result item.
     * @return The URL of the homepage for a paying customer.
     */
    public String getHomePageURL(){
    	if(!isCommercial()){
    		return null;
    	}
    	//TODO: check product.
    	String homePageURL = getField("iypurl");
    	if(homePageURL != null && homePageURL.length() > 0){
    		return homePageURL;
    	}
    	return null;
    }
    
    
    /**
     * Checks if the result item should be rendered with a bold title.
     * @return true if the product has bold title.
     */
    public boolean isTitleBold(){
    	String productPackage = getField("iyppakke");
    	if(productPackage != null && productPackage.length() > 0){
    		try{
    			//TODO: move bold indicator to index to avoid hardcoding of product and rules.
    			int packageId = Integer.parseInt(productPackage);
    			if(packageId == 3 || packageId == 116 || packageId == 120  ||packageId ==121 || packageId ==122 || packageId == 123 ){
    				return true;
    			}
    		}catch(NumberFormatException e){
    			return false;
    		}
    	}
    	return false;
    }
}
