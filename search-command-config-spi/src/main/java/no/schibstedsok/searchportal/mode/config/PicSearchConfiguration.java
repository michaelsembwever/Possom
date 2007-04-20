// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("PicSearchCommand")
public final class PicSearchConfiguration extends AbstractSearchConfiguration {

    /**
     * Holds value of property key for the queryServerHost.
     */
    private String queryServerHost;
    
    /**
     * Holds value of property key for the queryServerPort.
     */
    private String queryServerPort;
    
    private String country;
    private String filter;
    private String customerId;

    /**
     * 
     */
    public PicSearchConfiguration(){
        super(null);
    }
    
    /**
     * 
     * @param asc 
     */
    public PicSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
    
    /**
     * Getter for property key for queryServerUrl.
     * @return Value of property queryServerUrl.
     */
    public String getQueryServerHost() {
        return queryServerHost;
    }

    /**
     * Setter for property key for queryServerHost.
     * @param queryServerHost New value of property queryServerHost.
     */
    public void setQueryServerHost(final String queryServerHost) {
        this.queryServerHost = queryServerHost;
    }

    /**
     * Getter for property key for queryServerPort.
     * @return Value of property queryServerPort.
     */
    public String getQueryServerPort() {
        return queryServerPort;
    }

    /**
     * Setter for property key for queryServerPort.
     * @param queryServerPort New value of property queryServerPort.
     */
    public void setQueryServerPort(final String queryServerPort) {
        this.queryServerPort = queryServerPort;
    }

    /**
     * 
     * @return 
     */
    public String getCountry() {
        return country;
    }

    /**
     * 
     * @param country 
     */
    public void setCountry(final String country) {
        this.country = country;
    }

    /**
     * Returns the offensive content filtering level.
     *
     * @return Filtering level.
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Returns the customer id to use for picsearch queries associated with this configuration.
     *
     * @return The customer id.
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets property filter.
     *
     * @param filter New value for filter.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * Sets property customerId
     *
     * @param customerId New value for customerId
     */
    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }
}
