// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.command;


import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.SiteConfiguration;
import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.configuration.PicSearchConfiguration;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.net.URLEncoder;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class PicSearchCommand extends AbstractSearchCommand {

    private static final Log LOG = LogFactory.getLog(PicSearchCommand.class);
    private final HTTPClient client;

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public PicSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
        final SiteConfiguration siteConfig
                = SiteConfiguration.valueOf(ContextWrapper.wrap(SiteConfiguration.Context.class, cxt));
        client = HTTPClient.instance(
                "picture_search",
                siteConfig.getProperty("picsearch.host"),
                Integer.valueOf(siteConfig.getProperty("picsearch.port")));
    }

    public SearchResult execute() {

        String query = getTransformedQuery().replace(' ', '+');
        final PicSearchConfiguration psConfig = (PicSearchConfiguration) context.getSearchConfiguration();

        try {
            query = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }

        final String url = "/query?ie=UTF-8&tldb=" + psConfig.getPicsearchCountry()
                + "&custid=558735&filter=medium&version=2.6&thumbs=" + psConfig.getResultsToReturn()
                + "&q=" + query
                + "&start=" + getCurrentOffset(0);

        LOG.info("Using " + url);

        final Document doc = doSearch(url);

        final BasicSearchResult searchResult = new BasicSearchResult(this);

        if (doc != null) {

            final Element resultElement = doc.getDocumentElement();

            searchResult.setHitCount(Integer.parseInt(resultElement.getAttribute("hits")));

            final NodeList list = resultElement.getElementsByTagName("image");
            for (int i = 0; i < list.getLength(); i++) {

                final Element picture = (Element) list.item(i);

                final BasicSearchResultItem item = new BasicSearchResultItem();

                for (final Map.Entry<String,String> entry : psConfig.getResultFields().entrySet()) {

                    item.addField(entry.getValue(), picture.getAttribute(entry.getKey()));
                }
                searchResult.addResult(item);

            }
        }
        return searchResult;
    }

    private Document doSearch(final String url) {
        try {
            return client.getXmlDocument("picture_search", url);
        } catch (HttpException e) {
            LOG.error("Unable to connect to " + url, e);
        } catch (IOException e) {
            LOG.error("Problems with connection to " + url, e);
        } catch (SAXException e) {
            LOG.error("XML Parse error " + url , e);
        }
        return null;
    }
}
