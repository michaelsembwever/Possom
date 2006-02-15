package no.schibstedsok.front.searchportal.command;

import com.thoughtworks.xstream.XStream;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.configuration.OverturePPCConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.executor.ParallelSearchCommandExecutor;
import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.InfrastructureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.util.Properties;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.site.Site;

/**
 * @author <a href="mailto:lars@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision$</tt>
 */
public class OverturePPCCommand extends AbstractSearchCommand {

    private static Log log = LogFactory.getLog(OverturePPCCommand.class);
    HTTPClient client = HTTPClient.instance("overture_ppc", SearchConstants.OVERTURE_PPC_HOST, 80);

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public OverturePPCCommand(final Context cxt, Map parameters) {
        super(cxt, parameters);
    }

    public SearchResult execute() {
        String query = getTransformedQuery();

        query = query.replace(' ', '+');

        StringBuffer url = new StringBuffer("/d/search/p/schibstedsok/xml/no/");

        try {
            url.append("?mkt=no&adultFilter=clean&Partner=schibstedsok_xml_no_searchbox_imp1");
            url.append("&Keywords=").append(URLEncoder.encode(query, "iso-8859-1"));
            url.append("&maxCount=").append(getSearchConfiguration().getResultsToReturn());
        } catch (UnsupportedEncodingException e) {
            throw new InfrastructureException(e);
        }

        log.debug("URI is " + url.toString());

        Document doc = null;
        try {
            doc = client.getXmlDocument("overture_ppc", url.toString());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        BasicSearchResult searchResult = new BasicSearchResult(this);

        if (doc != null) {
            Element resultElement = doc.getDocumentElement();

            NodeList list = resultElement.getElementsByTagName(SearchConstants.OVERTURE_PPC_ELEMENT);

            log.debug("Found " + list.getLength() + " of " + SearchConstants.OVERTURE_PPC_ELEMENT);

            for (int i = 0; i < list.getLength(); i++) {

                Element ppcListing = (Element) list.item(i);


                BasicSearchResultItem item = new BasicSearchResultItem();

                item.addField("title", ppcListing.getAttribute("title"));
                item.addField("description", ppcListing.getAttribute("description"));
                item.addField("siteHost", ppcListing.getAttribute("siteHost"));

                NodeList click = ppcListing.getElementsByTagName("ClickUrl");

                if (click.getLength() > 0) {
                    item.addField("clickURL", click.item(0).getChildNodes().item(0).getNodeValue());
                }

                searchResult.addResult(item);
                log.debug(item.getField("clickURL"));
            }
        }
        return searchResult;
    }


}


