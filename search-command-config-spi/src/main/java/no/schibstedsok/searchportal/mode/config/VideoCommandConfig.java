package no.schibstedsok.searchportal.mode.config;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;


@Controller("VideoSearchCommand")
public class VideoCommandConfig extends AbstractXmlSearchConfiguration {

    private static final Logger LOG = Logger.getLogger(VideoCommandConfig.class);
    private String customerId;
    /**
     * Returns the customer id to use for picsearch queries associated with this configuration.
     *
     * @return The customer id.
     */
    public String getCustomerId() {
        return customerId;
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
