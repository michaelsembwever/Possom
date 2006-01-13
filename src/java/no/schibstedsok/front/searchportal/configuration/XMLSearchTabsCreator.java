package no.schibstedsok.front.searchportal.configuration;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class XMLSearchTabsCreator implements SearchTabsCreator {

    final Properties properties = new Properties();

    private SearchTabs tabs;

    private static SearchTabsCreator instance;

    private static final Log LOG = LogFactory.getLog(XMLSearchTabsCreator.class);

    private XMLSearchTabsCreator() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: XMLSearchTabsCreator()");
        }

        try {
            properties.load(this.getClass().getResourceAsStream(
                    "/" + SearchConstants.CONFIGURATION_FILE));
            LOG.info("Read configuration from "
                    + SearchConstants.CONFIGURATION_FILE);
        } catch (IOException e) {
            LOG.error("XMLSearchTabsCreator When Reading Configuration from "
                    + SearchConstants.CONFIGURATION_FILE, e);
            throw new InfrastructureException("Unable to read properties from "
                    + SearchConstants.CONFIGURATION_FILE, e);
        }
    }

    public SearchTabs createSearchTabs() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: createSearchTabs()");
        }

        if (tabs == null) {
            XStream xstream = new XStream(new DomDriver());
            xstream
                    .alias(
                            "FastSearch",
                            no.schibstedsok.front.searchportal.configuration.FastConfiguration.class);
            xstream
                    .alias(
                            "YellowSearch",
                            no.schibstedsok.front.searchportal.configuration.YellowSearchConfiguration.class);
            xstream
                    .alias(
                            "PicSearch",
                            no.schibstedsok.front.searchportal.configuration.PicSearchConfiguration.class);
            xstream
                    .alias(
                            "tabs",
                            no.schibstedsok.front.searchportal.configuration.SearchTabs.class);
            xstream
                    .alias(
                            "OverturePPCSearch",
                            no.schibstedsok.front.searchportal.configuration.OverturePPCConfiguration.class);
            xstream
                    .alias(
                            "MathExpression",
                            no.schibstedsok.front.searchportal.configuration.MathExpressionConfiguration.class);
            tabs = (SearchTabs) xstream
                    .fromXML(new InputStreamReader(
                            this
                                    .getClass()
                                    .getResourceAsStream(
                                            "/"
                                                    + properties
                                                            .getProperty("tabs_configuration"))));
            LOG.info("Tabs created from "
                    + properties.getProperty("tabs_configuration"));
        }

        return tabs;
    }

    public Properties getProperties() {
        return properties;
    }

    public static synchronized SearchTabsCreator getInstance() {

        if (instance == null)
            instance = new XMLSearchTabsCreator();

        return instance;
    }

}
