// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;


/**
 * @version <tt>$Id$</tt>
 * @author mick
 */
@Controller("HittaSearchCommand")
public final class HittaCommandConfig extends AbstractWebServiceSearchConfiguration {

    /**
     * 
     */
    public HittaCommandConfig(){
    }

    /**
     * 
     * @param asc 
     */
    public HittaCommandConfig(final SearchConfiguration asc){

        if(asc != null && asc instanceof HittaCommandConfig){
            final HittaCommandConfig hssc = (HittaCommandConfig)asc;

            key = hssc.key;
        }
    }

    /**
     * Holds value of property catalog.
     */
    private String catalog;

    /**
     * Getter for property catalog.
     * @return Value of property catalog.
     */
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * Setter for property catalog.
     * @param catalog New value of property catalog.
     */
    public void setCatalog(final String catalog) {
        this.catalog = catalog;
    }

    /**
     * Holds value of property key.
     */
    private String key;

    /**
     * Getter for property key.
     * @return Value of property key.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Setter for property key.
     * @param key New value of property key.
     */
    public void setKey(final String key) {
        this.key = key;
    }


}
