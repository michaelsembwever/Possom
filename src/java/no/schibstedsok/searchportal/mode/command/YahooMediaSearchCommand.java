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
import no.schibstedsok.searchportal.query.XorClause;

import java.util.Map;
import java.text.MessageFormat;
import java.net.URLEncoder;
import java.net.URL;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Command for searching images and videos using Yahoo! as a provider.
 *
 * @version $Id$
 */
public class YahooMediaSearchCommand extends AbstractYahooSearchCommand {
    // Query Language Operators and Terms
    private static final String QL_AND = " AND ";
    private static final String QL_OR = " OR ";
    private static final String QL_ANDNOT = " ANDNOT ";
    private static final String QL_TRUE = "#";

    private static final String COMMAND_URL_PATTERN =
            "/std_xmls_a00?type=adv&query={0}&custid1={1}&hits={2}&ocr={3}&catalog={4}";
    private static final String ERR_FAILED_CREATING_URL = "Failed to encode URL";
    private static final String RESULT_HEADER_ELEMENT = "GRP";
    private static final String TOTAL_HITS_ATTR = "TOT";
    private static final String RESULT_ELEMENT = "RES";

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

        try {
            String url = MessageFormat.format(
                    COMMAND_URL_PATTERN,
                    cfg.getPartnerId(),
                    cfg.getResultsToReturn(),
                    cfg.getOcr(),
                    cfg.getCatalog(),
                    URLEncoder.encode(query, "UTF-8"));

            return getSiteRestriction() == null ? url : url + "&rurl=" + getSiteRestriction();
        } catch (final UnsupportedEncodingException e) {
            throw new InfrastructureException(ERR_FAILED_CREATING_URL, e);
        }
    }

    /**
     * Getter for property 'siteRestriction'.
     *
     * @return Value for property 'siteRestriction'.
     */
    private URL getSiteRestriction() {
        return null;
    }

    /** {@inheritDoc} */
    public SearchResult execute() {
        try {

            final Document doc = getXmlResult();
            final SearchResult searchResult = new BasicSearchResult(this);

            if (doc != null) {
                final Element searchResponseE = doc.getDocumentElement();
                final Element resultHeaderE = (Element) searchResponseE.getElementsByTagName(RESULT_HEADER_ELEMENT).item(0);

                searchResult.setHitCount(Integer.parseInt(resultHeaderE.getAttribute(TOTAL_HITS_ATTR)));

                final NodeList list = searchResponseE.getElementsByTagName(RESULT_ELEMENT);

                for (int i = 0; i < list.getLength(); ++i) {
                    final Element listing = (Element) list.item(i);
                    searchResult.addResult(createResultItem(listing));
                }
            }

            return searchResult;
        } catch (final IOException e) {
            throw new InfrastructureException(e);
        } catch (final SAXException e) {
            throw new InfrastructureException(e);
        }
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
     * True if no term has been emitted yet *
     */
    private boolean firstTerm = true;

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
            final String transformedTerm = getTransformedTerm(clause);
            if (transformedTerm != null && transformedTerm.length() > 0) {
                appendToQueryRepresentation(transformedTerm);
                firstTerm = false;
            }
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
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);

        appendToQueryRepresentation(QL_OR);

        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(")");
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);

        final boolean hasEmptyLeaf = isEmptyLeaf(clause.getFirstClause()) || isEmptyLeaf(clause.getSecondClause());

        if (!(hasEmptyLeaf || clause.getSecondClause() instanceof NotClause)) {
            appendToQueryRepresentation(QL_AND);
        }

        clause.getSecondClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final NotClause clause) {
        // This must be extended to handle more cases
        // and not just the start of the query, e.g. first operand of an or operator.
        if (firstTerm) {
            appendToQueryRepresentation(QL_TRUE);
        }
        appendToQueryRepresentation(QL_ANDNOT);
        clause.getFirstClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final AndNotClause clause) {
        appendToQueryRepresentation(QL_ANDNOT);
        clause.getFirstClause().accept(this);
    }

    /**
     * Returns true iff the clause is a leaf clause and if it will not produce any output in the query representation.
     *
     * @param clause The clause to examine.
     *
     * @return true iff leaf is empty.
     */
    private boolean isEmptyLeaf(final Clause clause) {

        final YahooMediaSearchConfiguration cfg = (YahooMediaSearchConfiguration) context.getSearchConfiguration();

        return clause instanceof LeafClause
                && cfg.getFieldFilters().keySet().contains(((LeafClause)clause).getField()); 
    }
}
