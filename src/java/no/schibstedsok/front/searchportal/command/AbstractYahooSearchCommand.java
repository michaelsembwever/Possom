/*
 * AbstractYahooSearchCommand.java
 *
 * Created on June 12, 2006, 10:51 AM
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.AbstractYahooSearchConfiguration;
import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractYahooSearchCommand extends AbstractSearchCommand {
    
    
    // Constants -----------------------------------------------------
    
    private static final String BASE_PATH = "/d/search/p/schibstedsok/xml/no/?mkt=no&adultFilter=clean";

    private static final Logger LOG = Logger.getLogger(AbstractYahooSearchCommand.class);
    
    // Attributes ----------------------------------------------------
    
    private HTTPClient client;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    
    /**
     * Create new overture command.
     *
     * @param query
     * @param configuration
     * @param parameters
     */
    public AbstractYahooSearchCommand(
            final Context cxt,
            final Map parameters) {
        
        super(cxt, parameters);

        final AbstractYahooSearchConfiguration conf = (AbstractYahooSearchConfiguration)cxt.getSearchConfiguration();
        client = HTTPClient.instance(conf.getName(), conf.getHost(), conf.getPort());
    }
    
    // Public --------------------------------------------------------
    
    // Z implementation ----------------------------------------------
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    protected int getResultsToReturn(){
        
        return context.getSearchConfiguration().getResultsToReturn();
    }
    
    protected String getPartnerId(){
        
        final AbstractYahooSearchConfiguration conf 
                = (AbstractYahooSearchConfiguration)context.getSearchConfiguration();
        return conf.getPartnerId();
    }

    protected final StringBuilder createRequestURL(final String query) 
            throws InfrastructureException {
        
        final AbstractYahooSearchConfiguration ppcConfig 
                = (AbstractYahooSearchConfiguration) context.getSearchConfiguration();

        final StringBuilder url = new StringBuilder(BASE_PATH);

        try {
            url.append("&Partner=" + getPartnerId());
            url.append("&Keywords=");
            url.append(URLEncoder.encode(query, ppcConfig.getEncoding()));
            url.append("&maxCount=");
            url.append(getResultsToReturn());
        }  catch (UnsupportedEncodingException e) {
            throw new InfrastructureException(e);
        }
        return url;
    }

    /** Override if you want to add fields to the ResultItem from the element. 
     * Remember to use super.createItem(element) instead of creating a ResultItem from scratch.
     **/
    protected BasicSearchResultItem createItem(final Element ppcListing) {
        
        final BasicSearchResultItem item = new BasicSearchResultItem();
        final NodeList click = ppcListing.getElementsByTagName("ClickUrl");

        item.addField("title", ppcListing.getAttribute("title"));
        item.addField("description", ppcListing.getAttribute("description"));
        item.addField("siteHost", ppcListing.getAttribute("siteHost"));

        if (click.getLength() > 0) {
            item.addField("clickURL", click.item(0).getChildNodes().item(0).getNodeValue());
        }

        return item;
    }

    protected final Document getOvertureXmlResult(final String url) throws IOException, SAXException {
        
        return client.getXmlDocument(context.getSearchConfiguration().getName(), url);
    }

    protected final boolean isVgSiteSearch() {
        return context.getQuery().getQueryString().contains("site:vg.no");
    }  
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------


    

}
