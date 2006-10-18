package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.mode.config.YahooMediaSearchConfiguration;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.LeafClause;

import java.util.Map;
import java.text.MessageFormat;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Command for searching images and videos using Yahoo! as a provider.
 *
 * Yahoo API documentation can be found here:
 * https://dev.schibstedsok.no/confluence/display/TECHDEV/Yahoo+Media+Search 
 *
 * @version $Id$
 */
public class YahooMediaSearchCommand extends AbstractYahooSearchCommand {
    // Query Language Operators.
    private static final String QL_AND = " AND ";
    private static final String QL_OR = " OR ";
    private static final String QL_ANDNOT = " ANDNOT ";

    private static final String COMMAND_URL_PATTERN =
            "/std_xmls_a00?type=adv&query={0}&offset={1}&custid1={2}&hits={3}&ocr={4}&catalog={5}&encoding=utf-8";

    private static final String ERR_FAILED_CREATING_URL = "Failed to encode URL";

    private static final String RESULT_HEADER_ELEMENT = "GRP";
    private static final String TOTAL_HITS_ATTR = "TOT";
    private static final String RESULT_ELEMENT = "RES";

    private static final String URL_ENCODING = "UTF-8";

    private static final String YAHOO_SIZE_PARAM = "dimensions";
    private static final String SIZE_PARAM = "sz";
    private static final String SITE_FILTER = "site";

    /**
     * provides a mapping betweeen sizes defined by us
     * and sizes defined by yahoo. Currently one to one.
     */
    private static enum ImageMapping {
        SMALL ("small"),
        MEDIUM ("medium"),
        LARGE ("large"),
        WALLPAPER ("wallpaper"),
        WIDEWALLPAPER ("widewallpaper");

        private final String sizes;

        ImageMapping(final String sizes) {
            this.sizes = sizes;
        }

        public String getSizes() {
            return sizes;
        }
    }

    /**
     * Create new yahoo media command.
     *
     * @param cxt Context to execute in.
     * @param parameters Command parameters to use.
     */
    public YahooMediaSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    /** {@inheritDoc} */
    protected String createRequestURL() {

        String query = getTransformedQuery();

        final YahooMediaSearchConfiguration cfg = (YahooMediaSearchConfiguration) context.getSearchConfiguration();

        if (cfg.getSite().length() > 0) {
            query += QL_AND + "site:" + cfg.getSite(); 
        }

        try {
            String url = MessageFormat.format(
                    COMMAND_URL_PATTERN,
                    URLEncoder.encode(query, URL_ENCODING),
                    getCurrentOffset(0),
                    cfg.getPartnerId(),
                    cfg.getResultsToReturn(),
                    cfg.getOcr(),
                    cfg.getCatalog());

            if (getSingleParameter(SIZE_PARAM) != null && !getSingleParameter(SIZE_PARAM).equals("")) {
                final ImageMapping mapping = ImageMapping.valueOf(getSingleParameter(SIZE_PARAM).toUpperCase());

                if (mapping != null)
                    url += "&" + YAHOO_SIZE_PARAM + "=" + mapping.getSizes();
            }

            return url;

        } catch (final UnsupportedEncodingException e) {
            throw new InfrastructureException(ERR_FAILED_CREATING_URL, e);
        }
    }

    /** {@inheritDoc} */
    public SearchResult execute() {
        try {

            final Document doc = getXmlResult();
            final SearchResult searchResult = new BasicSearchResult(this);

            if (doc != null) {

                searchResult.setHitCount(0);

                final Element searchResponseE = doc.getDocumentElement();
                final Element resultHeaderE = (Element) searchResponseE.getElementsByTagName(RESULT_HEADER_ELEMENT).item(0);

                if (resultHeaderE != null) {

                    searchResult.setHitCount(Integer.parseInt(resultHeaderE.getAttribute(TOTAL_HITS_ATTR)));

                    final NodeList list = searchResponseE.getElementsByTagName(RESULT_ELEMENT);

                    for (int i = 0; i < list.getLength(); ++i) {
                        final Element listing = (Element) list.item(i);
                        searchResult.addResult(createResultItem(listing));
                    }
                }
            }
            return searchResult;

        } catch (final IOException e) {
            throw new InfrastructureException(e);
        } catch (final SAXException e) {
            throw new InfrastructureException(e);
        }
    }


    private String pendingAnd = "";

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final DefaultOperatorClause clause) {

        Clause first = clause.getFirstClause();
        Clause second = clause.getSecondClause();
        
        // Make sure NotClauses are not emitted first. The query language only supports ANDNOT.
        // This has the side effect of rearringing other types of queries as well.
        // TODO. This should probably be applied to OR clauses as well but currently isn't.
        if (!(clause.getSecondClause() instanceof NotClause || clause.getFirstClause() instanceof LeafClause)) {
            first = clause.getSecondClause();
            second = clause.getFirstClause();
        }

        first.accept(this);

        if (! (isEmptyLeaf(first) || isEmptyLeaf(second)))
            pendingAnd = QL_AND; // Defer emission of QL_AND until we know right branch isn't a NotClause.
        
        second.accept(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final LeafClause clause) {

        final String transformedTerm = getTransformedTerm(clause);

        if (transformedTerm != null && transformedTerm.length() > 0) {
            appendToQueryRepresentation(pendingAnd);

            if (clause.getField() != null && clause.getField().equals(SITE_FILTER)) {
                appendToQueryRepresentation(clause.getField() + ":");
            }

            appendToQueryRepresentation(transformedTerm);
            pendingAnd = "";
        }
    }


    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(QL_AND);
        clause.getSecondClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation(pendingAnd);
        pendingAnd = "";

        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);

        appendToQueryRepresentation(QL_OR);

        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(")");
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final NotClause clause) {
        pendingAnd = "";
        appendToQueryRepresentation(QL_ANDNOT);
        clause.getFirstClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final AndNotClause clause) {
        pendingAnd = "";
        appendToQueryRepresentation(QL_ANDNOT);
        clause.getFirstClause().accept(this);
    }

    private SearchResultItem createResultItem(final Element listing) {
        final BasicSearchResultItem item = new BasicSearchResultItem();

        for (final Map.Entry<String,String> entry : context.getSearchConfiguration().getResultFields().entrySet()){

            final String field = listing.getAttribute(entry.getKey().toUpperCase());

            if (!"".equals(field)) {
                item.addField(entry.getValue(), field);
            }
        }

        return item;
    }

    /**
     * Returns true iff the clause is a leaf clause and if it will not produce any output in the query representation.
     *
     * @param clause The clause to examine.
     *
     * @return true iff leaf is empty.
     */
    private boolean isEmptyLeaf(final Clause clause) {
        return clause instanceof LeafClause && (getTransformedTerm(clause) == null || getTransformedTerm(clause).length() == 0); 
    }
}
