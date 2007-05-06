// Copyright (2006-2007) Schibsted Søk AS
/*
 * AbstractYahooSearchConfiguration.java
 *
 * Created on June 12, 2006, 10:58 AM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractXmlSearchConfiguration extends CommandConfig {


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    /**
     * Holds value of property key for host.
     */
    private String host;

    /**
     * Holds value of property encoding.
     */
    private String encoding;

    /**
     * Holds value of property key for port.
     */
    private String port;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

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
    public String getPort() {
        return this.port;
    }

    /**
     * Setter for property port.
     * @param port New value of property port.
     */
    public void setPort(final String port) {
        this.port = port;
    }

    /**
     * Holds value of property hostHeader.
     */
    private String hostHeader;

    /**
     * Getter for property hostHeader.
     * @return Value of property hostHeader.
     */
    public String getHostHeader() {
        return this.hostHeader;
    }

    /**
     * Setter for property hostHeader.
     * @param hostHeader New value of property hostHeader.
     */
    public void setHostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
    }

    @Override
    public AbstractXmlSearchConfiguration readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "encoding", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "host", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "port", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "hostHeader", ParseType.String, element, "");

        return this;
    }



    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
    
}
