// Copyright (2006) Schibsted Søk AS
/*
 * AbstractYahooSearchConfiguration.java
 *
 * Created on June 12, 2006, 10:58 AM
 *
 */

package no.schibstedsok.searchportal.mode.config;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractYahooSearchConfiguration extends AbstractSearchConfiguration {


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    private String partnerId;

    /**
     * Holds value of property host.
     */
    private String host;

    /**
     * Holds value of property encoding.
     */
    private String encoding;

    /**
     * Holds value of property port.
     */
    private int port;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of AbstractYahooSearchConfiguration
     */
    public AbstractYahooSearchConfiguration() {
        super(null);
    }

    public AbstractYahooSearchConfiguration(final SearchConfiguration asc){
        super(asc);
        if(asc != null && asc instanceof AbstractYahooSearchConfiguration){
            final AbstractYahooSearchConfiguration osc = (AbstractYahooSearchConfiguration) asc;
            encoding = osc.encoding;
            host = osc.host;
            partnerId = osc.partnerId;
            port = osc.port;
        }
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------


    public String getPartnerId() {
        return partnerId;
    }


    /**
     * Setter for property partnerId.
     * @param partnerId New value of property partnerId.
     */
    public void setPartnerId(final String partner) {
        partnerId = partner;
    }



    /**
     * Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(final String host) {
        this.host = host;
    }



    /**
     * Getter for property encoding.
     * @return Value of property encoding.
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Setter for property encoding.
     * @param encoding New value of property encoding.
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }



    /**
     * Getter for property port.
     * @return Value of property port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Setter for property port.
     * @param port New value of property port.
     */
    public void setPort(final int port) {
        this.port = port;
    }

}
