/* Copyright (2006-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.result;

import no.schibstedsok.searchportal.result.*;
import org.apache.log4j.Logger;

/**
 * A simple implementation of a search result item that may contain products.
 *
 * @author <a href="mailto:lars.johansson@conduct.no">Lars Johansson</a>
 * @version <tt>$Id$</tt>
 */
public final class CatalogueSearchResultItem extends BasicResultItem {
	
    private static final Logger LOG = Logger.getLogger(CatalogueSearchResultItem.class);
    private static final String SEPARATOR = ",";
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
     * Responsible for building html meta description content.
     * <meta name='description' content="data from method." />
     * 
     * @return Comma separated values for content attribute.
     */
    public String getHtmlMetaDescriptionContent(){
    	StringBuffer metaDataDescrption = new StringBuffer();
    	
    	if(getDescription() != null){
    		metaDataDescrption.append(getDescription());
    		metaDataDescrption.append(SEPARATOR);
    	}
    	
    	if(getMarketName() != null){
    		metaDataDescrption.append(getMarketName());
    		metaDataDescrption.append(SEPARATOR);
    	}
    	
    	metaDataDescrption.append(getField("iypurl"));
    	metaDataDescrption.append(SEPARATOR);
    	metaDataDescrption.append("Adresse: " + getField("iypadresse"));
    	metaDataDescrption.append(SEPARATOR);
    	metaDataDescrption.append(getField("iyppostnr"));
    	metaDataDescrption.append(SEPARATOR);
    	metaDataDescrption.append(getField("iyppoststed"));
    	metaDataDescrption.append(SEPARATOR);
    	metaDataDescrption.append(getField("iypkommune"));
    	return metaDataDescrption.toString();
    }
    
    /**
     * Responsible for building the value of the keyword metadata content attribute.
     * <meta name='keywords' content='result from this method' />
     * @return Comma separated values for keywords.
     */
    public String getHtmlMetaKeywordsContent(){
    	StringBuffer metaDataKeywords = new StringBuffer();
    	if(getMarketName() != null){
    		metaDataKeywords.append(getMarketName());
    		metaDataKeywords.append(SEPARATOR);
    	}
    	
    	if(getDescription() != null){
    		metaDataKeywords.append(getDescription());
    		metaDataKeywords.append(SEPARATOR);
    	}
    	
    	metaDataKeywords.append(getField("iypurl"));
    	metaDataKeywords.append(SEPARATOR);
    	
    	metaDataKeywords.append("Tlf:"+getPhoneNumber());
    	metaDataKeywords.append(SEPARATOR);
    	if(getFax() != null){
    		metaDataKeywords.append("Fax:" + getFax());
    		metaDataKeywords.append(SEPARATOR);
    	}

    	metaDataKeywords.append("Adresse: " + getField("iypadresse"));
    	metaDataKeywords.append(SEPARATOR);
    	metaDataKeywords.append(getField("iyppostnr"));
    	metaDataKeywords.append(SEPARATOR);
    	metaDataKeywords.append(getField("iyppoststed"));
    	metaDataKeywords.append(SEPARATOR);
    	metaDataKeywords.append(getField("iypkommune"));
    	
    	
    	if(getKeywords() != null){
    		metaDataKeywords.append(SEPARATOR);
    		metaDataKeywords.append(getKeywords());
    	}
    	
    	return metaDataKeywords.toString();
    }
    
    /**
     * Util method for getting keywords from search result.
     * @return Comma separated keywords.
     */
    private String getKeywords() {
    	if(products != null && products.getInfoPageProducts() != null && products.getInfoPageProducts().size() > 0){
    		return products.getInfoPageProducts().get(0).getField("keywords");
    	}
    	
    	return null;
	}

    
    private String getDescription(){
    	if(products != null && products.getInfoPageProducts() != null && products.getInfoPageProducts().size() > 0){
    		return products.getInfoPageProducts().get(0).getField("textShort");
    	}
    	return null;
    }
    
    private String getMarketName(){
    	if(products != null && products.getInfoPageProducts() != null && products.getInfoPageProducts().size() > 0){
    		return products.getInfoPageProducts().get(0).getField("marketname");
    	}
    	return null;
    }
    
    private String getFax(){
    	if(products != null && products.getInfoPageProducts() != null && products.getInfoPageProducts().size() > 0){
    		return products.getInfoPageProducts().get(0).getField("fax");
    	}
    	
    	return null;
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
    	String description = getField("iyplogotekst");
        
    	if(isCommercial() && (description != null && description.length() > 0)){
    		return description;
    	}

        return null;
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
    		return "http://"+homePageURL;
    	}
    	return null;
    }
    
    public String imagePart(final String stringToSplit){
     
        if(stringToSplit == null || stringToSplit.length() < 1 || !stringToSplit.contains(";")){
            return null;
        }
        
        String[] imageAndUrl = stringToSplit.split(";");
        return imageAndUrl[0];
    }
    
    public String urlPart(final String stringToSplit){
        if(stringToSplit == null || stringToSplit.length() < 1 || !stringToSplit.contains(";")){
            return null;
        }
        
        String[] imageAndUrl = stringToSplit.split(";");
        return imageAndUrl[1];
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
    			if(packageId == 3 || packageId == 116 || packageId == 119 || packageId == 120  ||packageId ==121 || packageId ==122 || packageId == 123 ){
    				return true;
    			}
    		}catch(NumberFormatException e){
    			return false;
    		}
    	}
    	return false;
    }
}
