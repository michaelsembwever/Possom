package no.schibstedsok.front.searchportal.command;

import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.configuration.OverturePPCConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import no.schibstedsok.front.searchportal.executor.ParallelSearchCommandExecutor;
import no.schibstedsok.front.searchportal.http.HTTPClient;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.OvertureSearchResult;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.InfrastructureException;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;

/**
 *
 * This command gets the overture ads to display. It also does some analysis of
 * the query to decide if it is a query that yields a high click frequency for
 * the ads. This is done by evaluating the predicate "exact_ppctoplist".
 */
public class OverturePPCCommand extends AbstractSearchCommand {

    private static final String BASE_PATH = "/d/search/p/schibstedsok/xml/no/" +
            "?mkt=no&" +
            "adultFilter=clean" + "" +
            "&Partner=schibstedsok_xml_no_searchbox_imp1";
    private static final String OVERTURE_PARAMETER_ENCODING = "iso-8859-1";

    private static Log log = LogFactory.getLog(OverturePPCCommand.class);

    private HTTPClient client = HTTPClient.instance("overture_ppc",
            SearchConstants.OVERTURE_PPC_HOST, 80);

    /**
     * Create new overture command.
     *
     * @param query
     * @param configuration
     * @param parameters
     */
    public OverturePPCCommand(final Context cxt,
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
                    context,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return getTransformedQuery();
                        }
                    }
        );

        final TokenEvaluatorFactory tokenEvaluatorFactory = new TokenEvaluatorFactoryImpl(tokenEvalFactoryCxt);

        final boolean top = TokenPredicate.EXACT_PPCTOPLIST.evaluate(tokenEvaluatorFactory);
        
        final String query = getTransformedQuery().replace(' ', '+');
        final StringBuffer url = createRequestURL(query, top);

        try {
            final Document doc = getOvertureXmlResult(url);
            final OvertureSearchResult searchResult = new OvertureSearchResult(this, top);

            if (doc != null) {
                final Element elem = doc.getDocumentElement();
                final NodeList list = elem.getElementsByTagName(SearchConstants.OVERTURE_PPC_ELEMENT);

                for (int i = 0; i < list.getLength(); ++i) {
                    final Element listing = (Element) list.item(i);
                    final BasicSearchResultItem item = createItem(listing);
                    searchResult.addResult(item);
                }
            }
            return searchResult;
        } catch (IOException e) {
            throw new InfrastructureException(e);
        } catch (SAXException e) {
            throw new InfrastructureException(e);
        }
    }

    private TokenEvaluatorFactory getEvalFactory() {
        return context.getRunningQuery().getTokenEvaluatorFactory();
    }

    private StringBuffer createRequestURL(final String query, final boolean top)
    throws InfrastructureException {
        OverturePPCConfiguration ppcConfig = (OverturePPCConfiguration) context.getSearchConfiguration();

        StringBuffer url = new StringBuffer(BASE_PATH);

        int resultsToReturn = context.getSearchConfiguration().getResultsToReturn();

        if (top) {
            resultsToReturn += ppcConfig.getResultsOnTop();
        }

        try {
            url.append("&Keywords=");
            url.append(URLEncoder.encode(query, OVERTURE_PARAMETER_ENCODING));
            url.append("&maxCount=");
            url.append(resultsToReturn);
        } catch (UnsupportedEncodingException e) {
            throw new InfrastructureException(e);
        }
        return url;
    }

    private BasicSearchResultItem createItem(final Element ppcListing) {
        final BasicSearchResultItem item = new BasicSearchResultItem();
        final NodeList click = ppcListing.getElementsByTagName("ClickUrl");

        item.addField("title", ppcListing.getAttribute("title"));
        item.addField("description", ppcListing.getAttribute("description"));
        item.addField("siteHost", ppcListing.getAttribute("siteHost"));

        if (click.getLength() > 0) {
            item.addField("clickURL", click.item(0).getChildNodes().item(0).getNodeValue());
        }

        return item;
    }

    private Document getOvertureXmlResult(final StringBuffer url) throws IOException, SAXException {
        return client.getXmlDocument("overture_ppc", url.toString());
    }
}


