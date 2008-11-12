/* Copyright (2006-2008) Schibsted Søk AS
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
import no.sesat.search.mode.command.querybuilder.FilterBuilder;
import no.sesat.search.query.Visitor;
import no.sesat.search.query.XorClause;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

/**
 *
 * A search command that uses the picsearch API.
 *
 *
 * @version <tt>$Id$</tt>
 */
public class PicSearchCommand extends AbstractXmlSearchCommand {

    private static final Logger LOG = Logger.getLogger(PicSearchCommand.class);
    private final int port;
    private static final String REQ_URL_FMT = "/query?ie=UTF-8&tldb={0}&filter={1}&custid={2}&version=2.6"
            + "&thumbs={3}&q={4}&start={5}&site={6}&color={7}&size={8}";

    /**
     * Creates a new command in given context.
     *
     * @param cxt Context to run in.
     */
    public PicSearchCommand(final Context cxt) {

        super(cxt);

        setXmlRestful(
                new AbstractXmlRestful(cxt) {
                    public String createRequestURL() {


                        final PictureCommandConfig cfg = (PictureCommandConfig) cxt.getSearchConfiguration();

                        try {

                            final String query
                                    = URLEncoder.encode(PicSearchCommand.this.getTransformedQuery(), "utf-8");

                            final String color = null != PicSearchCommand.this.getParameter("color")
                                    ? PicSearchCommand.this.getParameter("color")
                                    : "";

                            final String size = null != PicSearchCommand.this.getParameter("size")
                                    ? PicSearchCommand.this.getParameter("size")
                                    : "";

                            final String urlBoost = PicSearchCommand.this.getFilterBuilder().getFilter("tldb")
                                    .replace('=', ':')
                                    .replace(' ', ',');

                            if(null != cfg.getSite() && cfg.getSite().length() > 0){
                                PicSearchCommand.this.getFilterBuilder().addFilter("site", cfg.getSite());
                            }

                            // The boost can eiter be from the URL or from the configuration.
                            final String boost = URLEncoder.encode(urlBoost.length() > 0
                                    ? urlBoost
                                    : cfg.getDomainBoost(), "utf-8");

                            return MessageFormat.format(REQ_URL_FMT,
                                    boost,
                                    cfg.getFilter(),
                                    cfg.getCustomerId(),
                                    cfg.getResultsToReturn(),
                                    query,
                                    PicSearchCommand.this.getOffset()+1,
                                    PicSearchCommand.this.getFilterBuilder().getFilter("site").replace(' ', ','),
                                    color,
                                    size);

                        } catch (UnsupportedEncodingException e) {
                           throw new SearchCommandException(e);
                        }

                    }
        });

        final SiteConfiguration siteConfig = datamodel.getSite().getSiteConfiguration();
        final PictureCommandConfig psConfig = (PictureCommandConfig) context.getSearchConfiguration();

        port = Integer.parseInt(siteConfig.getProperty(psConfig.getPort()));

    }

    public ResultList<ResultItem> execute() {

            final BasicResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

            if (port > 0){
                try {

                    final Document doc = getXmlRestful().getXmlResult();
                    if (doc != null) {
                        final Element resultElement = doc.getDocumentElement();
                        searchResult.setHitCount(Integer.parseInt(resultElement.getAttribute("hits")));
                        final NodeList list = resultElement.getElementsByTagName("image");
                        for (int i = 0; i < list.getLength(); i++) {
                            searchResult.addResult(createItem((Element) list.item(i)));
                        }
                    }

                } catch (IOException ex) {
                    throw new SearchCommandException(ex);
                } catch (SAXException ex) {
                    throw new SearchCommandException(ex);
                }
            }

            return searchResult;

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

    @Override
    protected ResultItem createItem(final Element picture) {

        final BasicResultItem item = new BasicResultItem();
        for (final Map.Entry<String, String> entry : getSearchConfiguration().getResultFieldMap().entrySet()) {
            item.addField(entry.getValue(), picture.getAttribute(entry.getKey()));
        }
        return item;

    }

    @Override
    protected String getParameter(final String key){
        return super.getParameter(key);
    }

    @Override
    protected FilterBuilder getFilterBuilder(){
        return super.getFilterBuilder();
    }

}
