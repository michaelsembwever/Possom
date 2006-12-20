// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id $</tt>
 */
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
    
    
    public PicSearchConfiguration(){
        super(null);
    }
    
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
     * Setter for property key for queryServerURL.
     * @param queryServerURL New value of property queryServerURL.
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

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

}
