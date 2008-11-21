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
 *
 * PlatefoodPPCSearchCommand.java
 *
 * Created on 24. august 2006, 10:00
 *
 */
package no.sesat.search.mode.command;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import no.sesat.search.mode.config.PlatefoodPpcCommandConfig;
import no.sesat.search.query.token.Categories;
import no.sesat.search.query.token.TokenPredicateUtility;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.PlatefoodSearchResult;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.config.SiteConfiguration;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 *
 * @version $Id$
 */
public class PlatefoodPPCSearchCommand extends AbstractYahooSearchCommand {

    private static final Logger LOG = Logger.getLogger(PlatefoodPPCSearchCommand.class);

    /** Constant that is used as partnerId on the gift page. */
    private static final String GIFT_PAGE_ID = "wipgift";

    /** RegEx pattern used to get a base url from a url. */
    private static final Pattern BASE_URL_PATTERN = Pattern.compile("(https?://)?(.*)");

    private boolean top = false;

    private String top3BackfillBlock;

    /**
     * Create new Platefood command.
     *
     * @param cxt
     */
    public PlatefoodPPCSearchCommand(final Context cxt) {

        super(cxt);

        setXmlRestful(
                new AbstractXmlRestful(cxt) {
                    public String createRequestURL() {
                        final PlatefoodPpcCommandConfig ppcConfig
                                = (PlatefoodPpcCommandConfig) cxt.getSearchConfiguration();
                        final String partnerId = PlatefoodPPCSearchCommand.this.getPartnerId();
                        final StringBuilder url = new StringBuilder(ppcConfig.getUrl());

                        try {
                            url.append("&channelName=" + partnerId);

                            if (partnerId != null && partnerId.equals(GIFT_PAGE_ID)) {
                                url.append("&searchTerm=");
                                url.append(URLEncoder.encode("send gave", ppcConfig.getEncoding()));

                                // Finding location, using that as an extra parameter
                                final String location = PlatefoodPPCSearchCommand.this.getParameter("ywpoststed");
                                if (location != null && location.length() > 0) {
                                    url.append("&locationTerm=");
                                    url.append(URLEncoder.encode(location, ppcConfig.getEncoding()));
                                }
                            } else {
                                url.append("&searchTerm=");
                                url.append(URLEncoder.encode(
                                        PlatefoodPPCSearchCommand.this.getTransformedQuery(),
                                        ppcConfig.getEncoding()));
                            }

                            url.append("&page=1");
                        }  catch (UnsupportedEncodingException e) {
                            throw new SearchCommandException(e);
                        }

                        return url.toString();
                    }
            });

        final PlatefoodPpcCommandConfig conf = (PlatefoodPpcCommandConfig)cxt.getSearchConfiguration();

        final SiteConfiguration siteConf = cxt.getDataModel().getSite().getSiteConfiguration();

        top3BackfillBlock = siteConf.getProperty(conf.getTop3BackfillBlock());
    }

    /**
     * Execute the command.
     *
     * @return the search result
     */
    public ResultList<ResultItem> execute() {

        // Need to rerun the token evaluation stuff on the transformed query
        // The transformed query does not contain site: and nyhetskilde: which
        // could have prevented exact matching in the previous evaluation.
        final ReconstructedQuery rq = createQuery(getTransformedQuery());

        final PlatefoodPpcCommandConfig ppcConfig
            = (PlatefoodPpcCommandConfig) context.getSearchConfiguration();

        // TODO smelling of non-sesat business logic here. AND presentation logic. move out.
        top = rq.getEngine().evaluateQuery(Categories.LOAN_TRIGGER, rq.getQuery());
        top |= rq.getEngine().evaluateQuery(Categories.SUDOKU_TRIGGER, rq.getQuery());
        top &= rq.getEngine().evaluateQuery(TokenPredicateUtility.getTokenPredicate("PPCTOPLIST").exactPeer(), rq.getQuery());

        try {
            final Document doc = getXmlRestful().getXmlResult();
            final PlatefoodSearchResult<ResultItem> searchResult = new PlatefoodSearchResult<ResultItem>(top);

            if (doc != null) {
                final Element elem = doc.getDocumentElement();
                final NodeList list = elem.getElementsByTagName("chan:impression");
                int numberOfAds = ppcConfig.getResultsToReturn();

                if (list.getLength() < numberOfAds) {
                    numberOfAds = list.getLength();
                }
                int numberOfTop3AdsToDisplay=numberOfAds;

                for (int i = 0; i < numberOfAds; ++i) {
                    final Element listing = (Element) list.item(i);
                    final BasicResultItem item = createItem(listing);
                    searchResult.addResult(item);
                }
                searchResult.setHitCount(numberOfTop3AdsToDisplay);
            }
            searchResult.addField("top3BackfillBlock",top3BackfillBlock);

            return searchResult;

        } catch (SocketTimeoutException ste) {

            LOG.error(getSearchConfiguration().getId() +  " --> " + ste.getMessage());
            return new BasicResultList<ResultItem>();

        } catch (IOException e) {
            throw new SearchCommandException(e);
        } catch (SAXException e) {
            throw new SearchCommandException(e);
        }
    }

    protected BasicResultItem createItem(final Element ppcListing) {

        final BasicResultItem item = new BasicResultItem();
        final NodeList clickUrl = ppcListing.getElementsByTagName("chan:trackURL");
        final NodeList displayUrl = ppcListing.getElementsByTagName("chan:displayURL");
        final NodeList title = ppcListing.getElementsByTagName("chan:title");
        final NodeList desc1 = ppcListing.getElementsByTagName("chan:desc");
        final NodeList desc2 = ppcListing.getElementsByTagName("chan:line2");
        final NodeList imageUrl = ppcListing.getElementsByTagName("chan:line1");
        final NodeList phone = ppcListing.getElementsByTagName("chan:phoneNumber");

        final String place = ppcListing.getParentNode().getParentNode()
                .getAttributes().getNamedItem("id").getNodeValue();

        LOG.debug("T3X: "+ place);

        if (title.getLength() > 0) {
            item.addField("title", title.item(0).getFirstChild().getNodeValue());
        }
        if (desc1.getLength() > 0) {
            String sDesc1 = desc1.item(0).getFirstChild().getNodeValue();
            if (sDesc1.matches(".*@\\..*")) {
                String media[] = sDesc1.split("@\\.");
                if (media.length >= 2 ) {
                    if(media[1].trim().length()>0) {
                        item.addField("imageUrl", "http://sesam.se/export/t3/"+media[1].trim());
                    }
                    if (media.length >= 4) {
                        if(media[2].trim().length()>0) {
                            item.addField("flashUrl", "http://sesam.se/export/t3/"+media[2].trim());
                        }
                    }
                }
                item.addField("description1", sDesc1.replaceAll("@\\..*@\\.", ""));
            } else {
                item.addField("description1", sDesc1);
            }
        }
        if (desc2.getLength() > 0) {
            item.addField("description2", desc2.item(0).getFirstChild().getNodeValue());
        }
        if (displayUrl.getLength() > 0) {
            item.addField("siteHost", displayUrl.item(0).getFirstChild().getNodeValue());
        }
        if (clickUrl.getLength() > 0) {
            item.addField("clickURL", clickUrl.item(0).getFirstChild().getNodeValue());
        }
        if (displayUrl.getLength() > 0) {
            item.addField("displayUrl", displayUrl.item(0).getFirstChild().getNodeValue());
            item.addField("displayUrlBase", getBaseUrl(displayUrl.item(0).getFirstChild().getNodeValue()));
        }
        if (imageUrl.getLength() > 0) {
            item.addField("imageUrl", imageUrl.item(0).getFirstChild().getNodeValue());
        }
        if (phone.getLength() > 0) {
            item.addField("phone", phone.item(0).getFirstChild().getNodeValue());
        }
        item.addField("place", place);

        return item;
    }

    /**
     * Returns a striped base url from a given url.
     *
     * @param url the url to strip
     * @return the base url
     */
    private String getBaseUrl(final String url) {

        if (url == null || url.length() == 0) {
            return null;
        }

        try {
            final Matcher matcher = BASE_URL_PATTERN.matcher(url);
            matcher.matches();
            return matcher.group(2);
        } catch (IllegalStateException e) {
            LOG.warn("Failed to get base url from gift url: " + url);
            return null;
        }
    }

    @Override
    protected String getParameter(String paramName) {
        return super.getParameter(paramName);
    }

}
