// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.command;

import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.OverturePPCSearchConfiguration;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.OvertureSearchResult;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.InfrastructureException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import no.schibstedsok.front.searchportal.configuration.AbstractYahooSearchConfiguration;

/**
 *
 * This command gets the overture ads to display. It also does some analysis of
 * the query to decide if it is a query that yields a high click frequency for
 * the ads. This is done by evaluating the predicate "exact_ppctoplist".
 */
public final class OverturePPCSearchCommand extends AbstractYahooSearchCommand {

    public static final String OVERTURE_PPC_ELEMENT = "Listing";

    private static final String SITE_SEARCH_OVERTURE_PARTNER_ID = "schibstedsok_xml_no_searchbox_sitesearch";

    private static final Logger LOG = Logger.getLogger(OverturePPCSearchCommand.class);

    private boolean top = false;

    /**
     * Create new overture command.
     *
     * @param query
     * @param configuration
     * @param parameters
     */
    public OverturePPCSearchCommand(final Context cxt,
                             final Map parameters) {
        super(cxt, parameters);

    }
    /**
     * Execute the command.
     *
     * @return
     */
    public SearchResult execute() {
        // Need to rerun the token evaluation stuff on the transformed query
        // The transformed query does not contain site: and nyhetskilde: which
        // could have prevented exact matching in the previous evaluation.
        final TokenEvaluatorFactoryImpl.Context tokenEvalFactoryCxt = ContextWrapper.wrap(
                TokenEvaluatorFactoryImpl.Context.class,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return getTransformedQuery();
                        }
                    },
                    context
        );

        final TokenEvaluatorFactory tokenEvaluatorFactory = new TokenEvaluatorFactoryImpl(tokenEvalFactoryCxt);

        top = TokenPredicate.EXACT_PPCTOPLIST.evaluate(tokenEvaluatorFactory);

        try {
            final Document doc = getXmlResult();
            final OvertureSearchResult searchResult = new OvertureSearchResult(this, top);

            if (doc != null) {
                final Element elem = doc.getDocumentElement();
                final NodeList list = elem.getElementsByTagName(OVERTURE_PPC_ELEMENT);

                for (int i = 0; i < list.getLength(); ++i) {
                    final Element listing = (Element) list.item(i);
                    final BasicSearchResultItem item = createItem(listing);
                    searchResult.addResult(item);
                }
                searchResult.setHitCount(Integer.parseInt(
                        ((Element)elem.getElementsByTagName("ResultSet").item(0)).getAttribute("numResults")));
            }
            
            return searchResult;
        } catch (IOException e) {
            throw new InfrastructureException(e);
        } catch (SAXException e) {
            throw new InfrastructureException(e);
        }
    }

    protected final String createRequestURL() {

        final OverturePPCSearchConfiguration ppcConfig
                = (OverturePPCSearchConfiguration) context.getSearchConfiguration();

        final StringBuilder url = new StringBuilder(ppcConfig.getUrl());

        try {
            url.append("&Partner=" + getPartnerId());
            url.append("&Keywords=");
            url.append(URLEncoder.encode(getTransformedQuery().replace(' ', '+'), ppcConfig.getEncoding()));
            url.append("&maxCount=");
            url.append(getResultsToReturn());
        }  catch (UnsupportedEncodingException e) {
            throw new InfrastructureException(e);
        }
        return url.toString();
    }


    protected int getResultsToReturn(){

        int resultsToReturn = super.getResultsToReturn();
        final int resultsToShow = context.getRunningQuery().getSearchTab().getAdLimit();
        final int resultsOnTop = context.getRunningQuery().getSearchTab().getAdOnTop();

        if (top && (!getParameters().containsKey("ss") && !isVgSiteSearch())) {
            resultsToReturn += resultsOnTop;
        } else {
            if (resultsToReturn >= resultsToShow - resultsOnTop){
                resultsToReturn = resultsToShow - resultsOnTop;
            }else{
                resultsToReturn += resultsOnTop;
            }
        }
        return resultsToReturn;
    }

    protected String getPartnerId(){

        final AbstractYahooSearchConfiguration conf
                = (AbstractYahooSearchConfiguration)context.getSearchConfiguration();

        // FIXME. When vg and the other site searches have their own context
        // remove this and use the property partnerId of OverturePPCConfiguration
        // instead.
        return getParameters().containsKey("ss") || isVgSiteSearch()
                ? SITE_SEARCH_OVERTURE_PARTNER_ID
                : conf.getPartnerId();
    }

    /**
     **/
    protected BasicSearchResultItem createItem(final Element ppcListing) {

        final BasicSearchResultItem item = new BasicSearchResultItem();
        final NodeList click = ppcListing.getElementsByTagName("ClickUrl");

        item.addField("title", ppcListing.getAttribute("title"));
        item.addField("description", ppcListing.getAttribute("description"));
        item.addField("siteHost", ppcListing.getAttribute("siteHost"));

        if (click.getLength() > 0) {
            item.addField("clickURL", click.item(0).getFirstChild().getNodeValue());
        }

        return item;
    }


}


