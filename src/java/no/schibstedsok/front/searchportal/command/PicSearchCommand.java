package no.schibstedsok.front.searchportal.command;

import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.net.URLEncoder;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PicSearchCommand extends AbstractSearchCommand {

    private static Log log = LogFactory.getLog(PicSearchCommand.class);
    HTTPClient client = HTTPClient.instance("picture_search", SearchConstants.PIC_SEARCH_HOST, 80);

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public PicSearchCommand(final SearchCommand.Context cxt, Map parameters) {
        super(cxt, parameters);
    }

    public SearchResult execute() {

        String query = getTransformedQuery();

        query = query.replace(' ', '+');

        try {
            query = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String url = "/query?ie=UTF-8&tldb=no&custid=558735&version=2.6&thumbs=" + getSearchConfiguration().getResultsToReturn() + "&q=" + query + "&start=" + getCurrentOffset(0);

        if (log.isDebugEnabled()) {
            log.debug("Using url " + url);
        }

        Document doc = doSearch(url);

        BasicSearchResult searchResult = new BasicSearchResult(this);
        Element resultElement = doc.getDocumentElement();

        if (doc != null) {

            searchResult.setHitCount(Integer.parseInt(resultElement.getAttribute("hits")));

            NodeList list = resultElement.getElementsByTagName("image");
            for (int i = 0; i < list.getLength(); i++) {

                Element picture = (Element) list.item(i);

                BasicSearchResultItem item = new BasicSearchResultItem();

                for (Iterator iterator = getSearchConfiguration().getResultFields().iterator(); iterator.hasNext();) {
                    String fieldName = (String) iterator.next();

                    item.addField(fieldName, picture.getAttribute(fieldName));
                }
                searchResult.addResult(item);

            }
        }
        return searchResult;
    }

    private Document doSearch(String url) {
        try {
            return client.getXmlDocument("picture_search", url);
        } catch (HttpException e) {
            log.error("Unable to connect to " + url, e);
        } catch (IOException e) {
            log.error("Problems with connection to " + url, e);
        } catch (SAXException e) {
            log.error("XML Parse error " + url , e);
        }
        return null;
    }
}
