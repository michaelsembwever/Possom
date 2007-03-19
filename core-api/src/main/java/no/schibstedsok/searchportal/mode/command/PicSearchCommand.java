// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.mode.config.PicSearchConfiguration;
import no.schibstedsok.searchportal.query.NotClause;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.net.URLEncoder;
import java.text.MessageFormat;

/**
 *
 * A search command that uses the picsearch API.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class PicSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(PicSearchCommand.class);
    private final HTTPClient client;
    private final int port;
    private static final String REQ_URL_FMT = "/query?ie=UTF-8&tldb={0}&filter={1}" +
            "&custid={2}&version=2.6&thumbs={3}&q={4}&start={5}";

    /**
     * Creates a new command in given context.
     *
     * @param cxt Context to run in.
     * @param parameters Command parameters.
     */
    public PicSearchCommand(final Context cxt) {

        super(cxt);

        final SiteConfiguration siteConfig = datamodel.getSite().getSiteConfiguration();
        final PicSearchConfiguration psConfig = (PicSearchConfiguration) context.getSearchConfiguration();

        final String host = siteConfig.getProperty(psConfig.getQueryServerHost());
        port = Integer.parseInt(siteConfig.getProperty(psConfig.getQueryServerPort()));
        client = HTTPClient.instance("picture_search", host, port);
    }

    /** {@inheritDoc} */
    public SearchResult execute() {

        String query = getTransformedQuery();
        final PicSearchConfiguration psConfig = (PicSearchConfiguration) context.getSearchConfiguration();

        try {
            query = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }


        final String url = MessageFormat.format(REQ_URL_FMT,
                psConfig.getCountry(),
                psConfig.getFilter(),
                psConfig.getCustomerId(),
                psConfig.getResultsToReturn(),
                query,
                getCurrentOffset(1));

        LOG.info("Using " + url);

        final BasicSearchResult searchResult = new BasicSearchResult(this);

        if( port > 0 ){

            final Document doc = doSearch(url);

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
        }
        return searchResult;
    }

    /**
     * Picsearch uses the - notation for NOT.
     *
     * @param clause The not clause to generate representation of.
     */
    protected void visitImpl(final NotClause clause) {
        final String childsTerm = getTransformedTerm(clause.getFirstClause());

        if (childsTerm != null && childsTerm.length() > 0) {
            appendToQueryRepresentation("-");
            clause.getFirstClause().accept(this);
        }
    }

    private Document doSearch(final String url) {

        try {
            return client.getXmlDocument("picture_search", url);
        } catch (IOException e) {
            LOG.error("Problems with connection to " + url, e);
        } catch (SAXException e) {
            LOG.error("XML Parse error " + url , e);
        }
        return null;
    }
}
