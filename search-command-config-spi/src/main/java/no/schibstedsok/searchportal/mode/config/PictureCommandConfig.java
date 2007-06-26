// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("PicSearchCommand")
public final class PictureCommandConfig extends CommandConfig {

    private static final Logger LOG = Logger.getLogger(PictureCommandConfig.class);

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
    private String site;
    private String domainBoost;

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
     * @deprecated use domainBoost instead. A boost of 50 is equivalent to setting the country parameter
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

    /**
     * Returns the site to which searches should be restricted.
     *
     * @return the site to which searches are restricted.
     */
    public String getSite() {
        return site;
    }

    /**
     * Set this to restrict the searches to <tt>site</tt>
     *
     * @param site to restrict searches to (e.g. dn.se).
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * Returns the domain boost to use.
     *
     * @return the domain boost.
     */
    public String getDomainBoost() {
        return domainBoost;
    }

    /**
     * Sets the domain boost. Example of domain boost string: se=100,nu=100,dk=50.
     *
     * @param domainBoost the domain boost string.
     */
    public void setDomainBoost(String domainBoost) {
        this.domainBoost = domainBoost;
    }

    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {

        super.readSearchConfiguration(element, inherit);

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "queryServerHost", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "queryServerPort", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "country", ParseType.String, element, "no");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "filter", ParseType.String, element, "medium");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "customerId", ParseType.String, element, "558735");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "site", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "domainBoost", ParseType.String, element, "");

        LOG.debug("customerid " + getCustomerId());

        return this;
    }

}
