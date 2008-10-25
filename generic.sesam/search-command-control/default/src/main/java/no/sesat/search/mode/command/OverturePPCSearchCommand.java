/*
 * Copyright (2005-2008) Schibsted Søk AS
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;

import no.sesat.search.mode.config.OverturePpcCommandConfig;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.query.token.TokenPredicateUtility;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.OvertureSearchResult;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This command gets the overture ads to display. It also does some analysis of
 * the query to decide if it is a query that yields a high click frequency for
 * the ads. This is done by evaluating the predicate "exact_ppctoplist".
 *
 * @version $Id$
 */
public class OverturePPCSearchCommand extends AbstractYahooSearchCommand {

    private static final String OVERTURE_PPC_ELEMENT = "Listing";
    // Old school sitesearches
    private static final String SITE_SEARCH_OVERTURE_PARTNER_ID = "schibstedsok_xml_no_searchbox_imp2";

    private static final Logger LOG = Logger.getLogger(OverturePPCSearchCommand.class);

    private boolean top = false;

    /**
     * Create new overture command.
     *
     * @param cxt the context
     */
    public OverturePPCSearchCommand(final Context cxt) {
        super(cxt);
    }

    /**
     * Execute the command.
     *
     * @return the search result
     */
    public ResultList<? extends ResultItem> execute() {
        // Need to rerun the token evaluation stuff on the transformed query
        // The transformed query does not contain site: and nyhetskilde: which
        // could have prevented exact matching in the previous evaluation.
        final ReconstructedQuery rq = createQuery(getTransformedQuery());

        top = rq.getEngine().evaluateQuery(TokenPredicateUtility.getTokenPredicate("PPCTOPLIST").exactPeer(), rq.getQuery());

        try {
            final Document doc = getXmlResult();
            LOG.debug(doc.toString());
            final OvertureSearchResult<ResultItem> searchResult = new OvertureSearchResult<ResultItem>(top);
            searchResult.setHitCount(0);
            if (doc != null) {
                final Element elem = doc.getDocumentElement();
                final NodeList list = elem.getElementsByTagName(OVERTURE_PPC_ELEMENT);

                for (int i = 0; i < list.getLength(); ++i) {
                    final Element listing = (Element) list.item(i);
                    final BasicResultItem item = createItem(listing);
                    searchResult.addResult(item);
                }
                final NodeList resultSetList = elem.getElementsByTagName("ResultSet");
                if(resultSetList.getLength()>0){
                    searchResult.setHitCount(Integer.parseInt(
                            ((Element)resultSetList.item(0)).getAttribute("numResults")));
                }
            }

            return (ResultList<? extends ResultItem>) searchResult;

        } catch (SocketTimeoutException ste) {

            LOG.error(getSearchConfiguration().getId() +  " --> " + ste.getMessage());
            return new BasicResultList<ResultItem>();

        } catch (IOException e) {
            throw new SearchCommandException(e);

        } catch (SAXException e) {
            throw new SearchCommandException(e);
        }
    }

    /**
     * @return Returns the request url used for the ppc ads.
     */
    protected String createRequestURL() {

        final OverturePpcCommandConfig ppcConfig
                = (OverturePpcCommandConfig) context.getSearchConfiguration();

        final StringBuilder url = new StringBuilder(ppcConfig.getUrl());

        try {
            url.append("&Partner=" + getPartnerId());

            if (null != ppcConfig.getType() && ppcConfig.getType().length() > 0) {
                url.append("&type=" + ppcConfig.getType());
            }

            final String serveUrl = "http://" + datamodel.getSite().getSite().getName() + "search/";

            url.append("&Keywords=");
            url.append(URLEncoder.encode(getTransformedQuery().replace(' ', '+'), ppcConfig.getEncoding()));
            url.append("&maxCount=");
            url.append(getResultsToReturn());
            url.append("&serveUrl=");
            url.append(URLEncoder.encode(serveUrl.toString(), "UTF-8"));

            url.append("&" + getAffilDataParameter());

        }  catch (UnsupportedEncodingException e) {
            throw new SearchCommandException(e);
        }

        return url.toString();
    }

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    @Override
    protected void visitImpl(final NotClause clause) {}

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    @Override
    protected void visitImpl(final AndNotClause clause) {}

    /** TODO comment me. **/
    protected BasicResultItem createItem(final Element ppcListing) {
        final BasicResultItem item = new BasicResultItem();
        final NodeList click = ppcListing.getElementsByTagName("ClickUrl");

        item.addField("title", ppcListing.getAttribute("title"));
        item.addField("description", ppcListing.getAttribute("description"));
        item.addField("siteHost", ppcListing.getAttribute("siteHost"));

        if (click.getLength() > 0) {
            item.addField("clickURL", click.item(0).getFirstChild().getNodeValue());
        }

        return item;
    }

    /** TODO comment me. **/
    @Override
    protected String getPartnerId(){
        // FIXME. When the site searches have their own context
        // remove this and use the property partnerId of OverturePPCConfiguration
        // instead.
        return null != getParameter("ss")
                ? SITE_SEARCH_OVERTURE_PARTNER_ID
                : super.getPartnerId();
    }

}


