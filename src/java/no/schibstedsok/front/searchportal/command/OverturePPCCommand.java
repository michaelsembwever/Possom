package no.schibstedsok.front.searchportal.command;

import com.thoughtworks.xstream.XStream;
import no.schibstedsok.front.searchportal.configuration.OverturePPCConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.executor.ParallelSearchCommandExecutor;
import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.query.RunningQuery;
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
import no.schibstedsok.front.searchportal.configuration.loaders.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.UrlResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.XStreamLoader;
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
    public OverturePPCCommand(RunningQuery query, SearchConfiguration configuration, Map parameters) {
        super(query, configuration, parameters);
    }

    public SearchResult execute() {
        String query = getTransformedQuery();

        query = query.replace(' ', '+');

        StringBuffer url = new StringBuffer("/d/search/p/schibstedsok/xml/no/");

        try {
            url.append("?mkt=no&adultFilter=clean&Partner=schibstedsok_xml_no_searchbox_imp1");
            url.append("&Keywords=").append(URLEncoder.encode(query, "iso-8859-1"));
            url.append("&maxCount=").append(configuration.getResultsToReturn());
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


    // TODO move this to a test class
    public static void main(String[] args) throws Exception {

        String query = "linux";

        final SearchMode mode = new SearchMode();
        mode.setExecutor(new ParallelSearchCommandExecutor());
        SearchConfiguration searchConfiguration = new OverturePPCConfiguration();
        searchConfiguration.setResultsToReturn(3);
        mode.addSearchConfiguration(searchConfiguration);

        final RunningQuery.Context rqCxt = new RunningQuery.Context(){
            public SearchMode getSearchMode() {
                return mode;
            }

            public PropertiesLoader newPropertiesLoader(String resource, Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this,resource, properties);
            }

            public XStreamLoader newXStreamLoader(String resource, XStream xstream) {
                return UrlResourceLoader.newXStreamLoader(this,resource, xstream);
            }

            public Site getSite() {
                return Site.DEFAULT; //FIXME implement me properly
            }
            
        };
        
        final RunningQuery runningQuery = new RunningQuery(rqCxt, query, new HashMap());

        runningQuery.run();

    }

}


