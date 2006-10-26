/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 * PlatefoodPPCSearchCommand.java
 *
 * Created on 24. august 2006, 10:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package no.schibstedsok.searchportal.mode.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.PlatefoodPPCSearchConfiguration;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.PlatefoodSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author SSTHKJER
 */
public final class PlatefoodPPCSearchCommand extends AbstractYahooSearchCommand {

    private static final Logger LOG = Logger.getLogger(PlatefoodPPCSearchCommand.class);

    /** Constant that is used as partnerId on the gift page. */
    private static final String GIFT_PAGE_ID = "wipgift";

    private boolean top = false;

    /**
     * Create new Platefood command.
     *
     * @param query
     * @param configuration
     * @param parameters
     */
    public PlatefoodPPCSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);

    }

    /**
     * Execute the command.
     *
     * @return the search result
     */
    public SearchResult execute() {
        // Need to rerun the token evaluation stuff on the transformed query
        // The transformed query does not contain site: and nyhetskilde: which
        // could have prevented exact matching in the previous evaluation.
        final ReconstructedQuery rq = createQuery(getTransformedQuery());

        final PlatefoodPPCSearchConfiguration ppcConfig
                = (PlatefoodPPCSearchConfiguration) context.getSearchConfiguration();


        top = rq.getEngine().evaluateQuery(TokenPredicate.LOAN_TRIGGER, rq.getQuery());
        top |= rq.getEngine().evaluateQuery(TokenPredicate.SUDOKU_TRIGGER, rq.getQuery());
        top &= rq.getEngine().evaluateQuery(TokenPredicate.EXACT_PPCTOPLIST, rq.getQuery());

        try {
            final Document doc = getXmlResult();
            final PlatefoodSearchResult searchResult = new PlatefoodSearchResult(this, top);
            if (doc != null) {
                final Element elem = doc.getDocumentElement();
                final NodeList list = elem.getElementsByTagName("chan:impression");
                int result = ppcConfig.getResultsToReturn();

                if (list.getLength() < result) {
                    result = list.getLength();
                }

                for (int i = 0; i < result; ++i) {
                    final Element listing = (Element) list.item(i);
                    final BasicSearchResultItem item = createItem(listing);
                    searchResult.addResult(item);
                }
                searchResult.setHitCount(result);
            }

            return searchResult;
        } catch (IOException e) {
            throw new InfrastructureException(e);
        } catch (SAXException e) {
            throw new InfrastructureException(e);
        }
    }

    /**
     * @return Return the request url used to get ads.
     **/
    protected String createRequestURL() {
        final PlatefoodPPCSearchConfiguration ppcConfig
                = (PlatefoodPPCSearchConfiguration) context.getSearchConfiguration();
        final String partnerId = ppcConfig.getPartnerId();
        final StringBuilder url = new StringBuilder(ppcConfig.getUrl());

        try {
            url.append("&channelName=" + partnerId);

            if (partnerId != null && partnerId.equals(GIFT_PAGE_ID)) {
                url.append("&searchTerm=");
                url.append(URLEncoder.encode("send gave", ppcConfig.getEncoding()));

                // Finding location, using that as an extra parameter
                final String location = getParameter("ywpoststed");
                if (location != null && location.length() > 0) {
                    url.append("&locationTerm=");
                    url.append(URLEncoder.encode(location, ppcConfig.getEncoding()));
                }

            } else {
                url.append("&searchTerm=");
                url.append(URLEncoder.encode(getTransformedQuery().replace(' ', '+'), ppcConfig.getEncoding()));
            }

            url.append("&page=1");
        }  catch (UnsupportedEncodingException e) {
            throw new InfrastructureException(e);
        }
        return url.toString();
    }


    /** TODO comment me. **/
    protected int getResultsToReturn(){
        final int resultsToShow = context.getRunningQuery().getSearchTab().getAdLimit();
        final int resultsOnTop = context.getRunningQuery().getSearchTab().getAdOnTop();

        if (top && (!getParameters().containsKey("ss") && !isVgSiteSearch())) {
            return resultsToShow + resultsOnTop;
        } else {
            return resultsToShow;
        }
    }

    /**
     **/
    protected BasicSearchResultItem createItem(final Element ppcListing) {
        final BasicSearchResultItem item = new BasicSearchResultItem();
        final NodeList clickUrl = ppcListing.getElementsByTagName("chan:trackURL");
        final NodeList displayUrl = ppcListing.getElementsByTagName("chan:displayURL");
        final NodeList title = ppcListing.getElementsByTagName("chan:title");
        final NodeList desc = ppcListing.getElementsByTagName("chan:desc");
        final NodeList imageUrl = ppcListing.getElementsByTagName("chan:line1");
        final String place =
            ppcListing.getParentNode().getParentNode().getAttributes().getNamedItem("id").getNodeValue();

        if (title.getLength() > 0) {
            item.addField("title", title.item(0).getFirstChild().getNodeValue());
        }
        if (desc.getLength() > 0) {
            item.addField("description", desc.item(0).getFirstChild().getNodeValue());
        }
        if (displayUrl.getLength() > 0) {
            item.addField("siteHost", displayUrl.item(0).getFirstChild().getNodeValue());
        }
        if (clickUrl.getLength() > 0) {
            item.addField("clickURL", clickUrl.item(0).getFirstChild().getNodeValue());
        }
        if (imageUrl.getLength() > 0) {
            item.addField("imageUrl", imageUrl.item(0).getFirstChild().getNodeValue());
        }
        item.addField("place", place);

        return item;
    }

}
