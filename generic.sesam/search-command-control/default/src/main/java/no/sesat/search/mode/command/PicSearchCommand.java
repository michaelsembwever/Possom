/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.mode.command;


import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.http.HTTPClient;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.mode.config.PictureCommandConfig;
import no.sesat.search.query.NotClause;
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
import no.sesat.search.query.Visitor;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

/**
 *
 * A search command that uses the picsearch API.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class PicSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(PicSearchCommand.class);
    private final transient HTTPClient client;
    private final int port;
    private static final String REQ_URL_FMT = "/query?ie=UTF-8&tldb={0}&filter={1}&custid={2}&version=2.6"
            + "&thumbs={3}&q={4}&start={5}&site={6}&color={7}&size={8}";

    private String siteFilter;
    private final StringBuilder tldb = new StringBuilder();

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

        siteFilter = psConfig.getSite();
    }

    /** {@inheritDoc}
     */
    public ResultList<? extends ResultItem> execute() {

        final PictureCommandConfig cfg = (PictureCommandConfig) context.getSearchConfiguration();

        try {

            final String query = URLEncoder.encode(getTransformedQuery(), "utf-8");
            final String color = getParameter("color");
            final String size = getParameter("size");
            final String urlBoost = tldb.toString();

            // The boost can eiter be from the URL or from the configuration.
            final String boost = URLEncoder.encode(urlBoost.length() > 0 ? urlBoost : cfg.getDomainBoost(), "utf-8");

            final String url = MessageFormat.format(REQ_URL_FMT,
                    boost,
                    cfg.getFilter(),
                    cfg.getCustomerId(),
                    cfg.getResultsToReturn(),
                    query,
                    getCurrentOffset(1),
                    siteFilter,
                    color,
                    size);

            DUMP.info("Using " + url);

            final BasicResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

            if (port > 0){

                final Document doc = doSearch(url);

                if (doc != null) {

                    final Element resultElement = doc.getDocumentElement();

                    searchResult.setHitCount(Integer.parseInt(resultElement.getAttribute("hits")));

                    final NodeList list = resultElement.getElementsByTagName("image");
                    for (int i = 0; i < list.getLength(); i++) {

                        final Element picture = (Element) list.item(i);

                        final BasicResultItem item = new BasicResultItem();

                        for (final Map.Entry<String,String> entry : cfg.getResultFields().entrySet()) {

                            item.addField(entry.getValue(), picture.getAttribute(entry.getKey()));
                        }
                        searchResult.addResult(item);

                    }
                }
            }

            return searchResult;

        } catch (UnsupportedEncodingException e) {
           throw new SearchCommandException(e);
        }
    }

    /**
     * Picsearch uses the - notation for NOT.
     *
     * @param clause The not clause to generate representation of.
     */
    @Override
    protected void visitImpl(final NotClause clause) {
        final String childsTerm = getTransformedTerm(clause.getFirstClause());

        if (childsTerm != null && childsTerm.length() > 0) {
            appendToQueryRepresentation("-");
            clause.getFirstClause().accept(this);
        }
    }

    /**
     * Visits full names as separate words rather than as an entity.
     *
     * @param visitor The visitor.
     * @param clause the xor clause to examine.
     */
    @Override
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
     * Only done if no static site filter is specified by
     * {@link no.sesat.search.mode.config.PictureCommandConfig#getSite()}
     *
     * @param clause The clause.
     */
    @Override
    protected void visitImpl(final LeafClause clause) {


        // Temportary code to allow experimenting with the new picsearch API feature "flexible top level domain boost".
        if (clause.getField() != null && "tldb".equals(clause.getField())) {
            if (tldb.length() > 0) {
                tldb.append(',');
            }

            tldb.append(clause.getTerm().replace('=', ':'));
            return;
        }
        // End temporary code.

        // Do not care about site in query if a static site filter was specified in the configuaration.
        if (getFieldFilter(clause) != null && "".equals(siteFilter) && "site".equals(clause.getField())) {
            siteFilter = clause.getTerm();
        } else {
            super.visitImpl(clause);
        }
    }

    private Document doSearch(final String url) {

        try {
            return client.getXmlDocument(url);

        } catch (IOException e) {
            LOG.error("Problems with connection to " + url, e);
            throw new SearchCommandException(e);
            
        } catch (SAXException e) {
            LOG.error("XML Parse error " + url , e);
            throw new SearchCommandException(e);
        }
    }
}
