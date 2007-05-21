// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.mode.config.PictureCommandConfig;
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
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 *
 * A search command that uses the picsearch API.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class PicSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(PicSearchCommand.class);
    private final HTTPClient client;
    private final int port;
    private static final String REQ_URL_FMT
            = "/query?ie=UTF-8&tldb={0}&filter={1}&custid={2}&version=2.6&thumbs={3}&q={4}&start={5}";

    private String fieldFilter;

    /**
     * Creates a new command in given context.
     *
     * @param cxt Context to run in.
     */
    public PicSearchCommand(final Context cxt) {

        super(cxt);

        final SiteConfiguration siteConfig = datamodel.getSite().getSiteConfiguration();
        final PictureCommandConfig psConfig = (PictureCommandConfig) context.getSearchConfiguration();

        final String host = siteConfig.getProperty(psConfig.getQueryServerHost());
        port = Integer.parseInt(siteConfig.getProperty(psConfig.getQueryServerPort()));
        client = HTTPClient.instance(host, port);

        fieldFilter = psConfig.getSite();
    }

    /** {@inherit} */
    public ResultList<? extends ResultItem> execute() {

        String query = getTransformedQuery();
        final PictureCommandConfig psConfig = (PictureCommandConfig) context.getSearchConfiguration();

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

        final BasicSearchResult<ResultItem> searchResult = new BasicSearchResult<ResultItem>();

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

    /**
     * @param visitor
     * @param clause
     */
    protected void visitXorClause(final Visitor visitor, final XorClause clause){

        // determine which branch in the query-tree we want to use.
        //  Both branches to a XorClause should never be used.
        switch(clause.getHint()){
            case FULLNAME_ON_LEFT:
                clause.getSecondClause().accept(this);
                break;
            default:
                super.visitXorClause(visitor, clause);
        }
    }

    /**
     * Extract the field filter site into the <tt>siteFilter</tt> if it exists.
     * Only done if no static site filter is specified by {@link no.schibstedsok.searchportal.mode.config.PictureCommandConfig#getSite()}
     *
     * @param clause The clause.
     */
    protected void visitImpl(final LeafClause clause) {
        final PictureCommandConfig psConfig = (PictureCommandConfig) context.getSearchConfiguration();

        // Do not care about site in query if a static site filter was specified in the configuaration.
        if (getFieldFilter(clause) != null && "".equals(fieldFilter) && "site".equals(clause.getField())) {
            fieldFilter = clause.getTerm();
        } else {
            super.visitImpl(clause);
        }
    }

    private Document doSearch(final String url) {

        try {
            return client.getXmlDocument(url);
        } catch (IOException e) {
            LOG.error("Problems with connection to " + url, e);
        } catch (SAXException e) {
            LOG.error("XML Parse error " + url , e);
        }
        return null;
    }
}
