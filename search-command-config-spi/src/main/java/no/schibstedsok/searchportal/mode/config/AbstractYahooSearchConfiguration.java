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
public abstract class AbstractYahooSearchConfiguration extends AbstractXmlSearchConfiguration {


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    private String partnerId;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    /**
     * 
     * @return 
     */
    public String getPartnerId() {
        return partnerId;
    }


    /**
     * Setter for property partnerId.
     * @param partner New value of property partnerId.
     */
    public void setPartnerId(final String partner) {
        partnerId = partner;
    }

    @Override
    public AbstractYahooSearchConfiguration readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "partnerId", ParseType.String, element, "");

        return this;
    }



    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
    
}
