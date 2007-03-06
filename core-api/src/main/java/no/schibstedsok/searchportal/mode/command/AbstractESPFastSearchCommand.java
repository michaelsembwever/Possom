/* Copyright (2005-2007) Schibsted Søk AS
 *
 * AbstractESPFastSearchCommand.java
 *
 * Created on 14 March 2006, 19:51
 *
 */

package no.schibstedsok.searchportal.mode.command;

import com.fastsearch.esp.search.ConfigurationException;
import com.fastsearch.esp.search.SearchEngineException;
import com.fastsearch.esp.search.SearchFactory;
import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.query.Query;
import com.fastsearch.esp.search.query.SearchParameter;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IQueryResult;
import com.fastsearch.esp.search.view.ISearchView;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.ESPFastSearchConfiguration;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.site.Site;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;

/**
 *
 * Base class for commands querying a FAST EPS Server.
 */
public abstract class AbstractESPFastSearchCommand extends AbstractSearchCommand {


    // Attributes ----------------------------------------------------
    private final ESPFastSearchConfiguration cfg;
    private final String queryServer;
    private final ISearchView searchView;
    private IQueryResult result;

    // Static --------------------------------------------------------

    private static final Map<String,ISearchView> SEARCH_VIEWS = new HashMap<String,ISearchView>();

    private final static String FACTORY_PROPERTY = "com.fastsearch.esp.search.SearchFactory";
    private final static String HTTP_FACTORY = "com.fastsearch.esp.search.http.HttpSearchFactory";
    private final static String QR_SERVER_PROPERTY = "com.fastsearch.esp.search.http.qrservers";
    private final static String ENCODER_PROPERTY = "com.fastsearch.esp.search.http.encoderclass";
    private final static String ENCODER_CLASS = "com.fastsearch.esp.search.http.DSURLUTF8Encoder";

    private final static String COLLAPSE_PARAMETER = "collapse";

    private static final Logger LOG = Logger.getLogger(AbstractESPFastSearchCommand.class);
    private static final String ERR_CALL_SET_VIEW = "setView() must be called prior to calling this method";

    private enum ReservedWord {
        AND("and"),
        OR("or"),
        ANDNOT("andnot");

        private String word;

        ReservedWord(final String word) {
            this.word = word;
        }

        public final String getWord() {
            return word;
        }
    }

    // Constructors --------------------------------------------------

    /**
     * Creates new instance of search command.
     *
     * @param cxt The context to work in.
     * @param parameters The command parameters to use.
     */
    public AbstractESPFastSearchCommand(
                    final Context cxt,
            final DataModel datamodel) {

        super(cxt, datamodel);

        cfg = (ESPFastSearchConfiguration) getSearchConfiguration();
        final SiteConfiguration siteConf = datamodel.getSite().getSiteConfiguration();
        queryServer = siteConf.getProperty(cfg.getQueryServer());
        searchView = initialiseSearchView();
    }

    // Public --------------------------------------------------------

    /** {@inheritDoc} */
    public SearchResult execute() {

        try {

            final StringBuilder filterBuilder = new StringBuilder();

            if (getFilter() != null) {
                filterBuilder.append(getFilter());
                filterBuilder.append(' ');
            }

            if (getAdditionalFilter() != null) {
                filterBuilder.append(getAdditionalFilter());
                filterBuilder.append(' ');
            }


            final String transformedQuery = getTransformedQuery();

            LOG.debug("Transformed query is " + transformedQuery);

            final String collapseId = getParameter(COLLAPSE_PARAMETER);

            final IQuery query = new Query(transformedQuery);

            if (cfg.isCollapsingEnabled()) {
                if (collapseId == null || "".equals(collapseId)) {
                    if (cfg.isCollapsingRemoves()) {
                        query.setParameter(new SearchParameter("collapseon", "batvcollapseid"));
                    }
                } else {
                    filterBuilder.append("+collapseid:").append(collapseId);
                }
            }
            
            //check sortby
            String sortBy =cfg.getSortBy();
            if (getParameters().containsKey("userSortBy")) {

                final String userSortBy = getParameter("userSortBy");
                LOG.debug("execute: SortBy " + userSortBy);

                if ("standard".equals(userSortBy)) {
                    sortBy ="freshnessprofile";
                } else if ("datetime".equals(userSortBy)) {
                    sortBy = "publishedtime";
                }
            }
            
            query.setParameter(new SearchParameter(BaseParameter.OFFSET, getCurrentOffset(0)));
            query.setParameter(new SearchParameter(BaseParameter.HITS, cfg.getResultsToReturn()));
            query.setParameter(new SearchParameter(BaseParameter.SORT_BY, sortBy));
            query.setParameter(new SearchParameter(BaseParameter.FILTER, filterBuilder.toString()));

            if (! (this instanceof NavigatableESPFastCommand)) {
                query.setParameter(new SearchParameter(BaseParameter.NAVIGATION, 0));
            }

            if (! "".equals(cfg.getQtPipeline())) {
                query.setParameter(new SearchParameter(BaseParameter.QT_PIPELINE, cfg.getQtPipeline()));
            }


            LOG.info(query);

            result = searchView.search(query);

            final FastSearchResult searchResult = new FastSearchResult(this);

            final int cnt = getCurrentOffset(0);
            final int maxIndex = getMaxDocIndex(result, cnt, cfg);

            searchResult.setHitCount(result.getDocCount());

            for (int i = cnt; i < maxIndex; i++) {
                try {
                    final IDocumentSummary document = result.getDocument(i + 1);
                    searchResult.addResult(createResultItem(document));

                } catch (NullPointerException e) { // THe doc count is not 100% accurate.
                    LOG.debug("Error finding document " + e);
                    return searchResult;
                }
            }

            if (cfg.isCollapsingEnabled() && cfg.isExpansionEnabled()) {
                if (collapseId != null && !collapseId.equals("")) {
                    if (searchResult.getResults().size() > 0) {
                        final SearchResultItem itm = searchResult.getResults().get(0);
                        final URL url = new URL(itm.getField("url"));
                        searchResult.addField("collapsedDomain", url.getHost());
                    }
                }
            }

            return searchResult;

        } catch (SearchEngineException ex) {
            LOG.error(ex.getMessage() + ' ' + ex.getCause());
            return new FastSearchResult(this);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new InfrastructureException(ex);
        }
    }


    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /** {@inheritDoc} */
    protected String escapeFieldedLeaf(final LeafClause clause) {

        return '"' + (null != clause.getField() ? clause.getField() + ':' : "") + clause.getTerm() + '"';
    }

    /**
     * {@inheritDoc}
     */
    protected final String escapeTerm(final String term) {
        for (ReservedWord word : ReservedWord.values()) {
            // Term might already be prefixed by the TermPrefixTransformer.
            if (term.contains(":") && term.endsWith(':' + word.getWord()) || term.equals(word.getWord())) {
                return term.replace(word.getWord(), '"' + word.getWord() + '"');
            }
        }

        return term;
    }

    // Generate query in FQL.
    /** {@inheritDoc} */
    protected void visitImpl(final AndClause clause) {
        // The leaf clauses might not produce any output. For example terms
        // having a site: field. In these cases we should not output the
        // operator keyword.
        boolean hasEmptyLeaf = false;

        hasEmptyLeaf |= isEmptyLeaf(clause.getFirstClause());
        hasEmptyLeaf |= isEmptyLeaf(clause.getSecondClause());

        clause.getFirstClause().accept(this);

        if (! hasEmptyLeaf)
            appendToQueryRepresentation(" and ");

        clause.getSecondClause().accept(this);
    }

    /** {@inheritDoc} */
    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation(" (");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" or ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(") ");
    }
    /** {@inheritDoc} */
    protected void visitImpl(final DefaultOperatorClause clause) {
        boolean hasEmptyLeaf = false;

        hasEmptyLeaf |= isEmptyLeaf(clause.getFirstClause());
        hasEmptyLeaf |= isEmptyLeaf(clause.getSecondClause());

        clause.getFirstClause().accept(this);

        if (! hasEmptyLeaf)
            appendToQueryRepresentation(" and ");

        clause.getSecondClause().accept(this);
    }
    /** {@inheritDoc} */
    protected void visitImpl(final NotClause clause) {
        appendToQueryRepresentation(" not ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");

    }
    /** {@inheritDoc} */
    protected void visitImpl(final AndNotClause clause) {
        appendToQueryRepresentation("andnot ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");
    }

    private boolean isEmptyLeaf(final Clause clause) {
        if (clause instanceof LeafClause) {
            final LeafClause leaf = (LeafClause) clause;
            return null != leaf.getField() && null != getFieldFilter(leaf);
        }

        return false;
    }
    /**
     *
     * @param clause The clause to examine.
     */
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {
        switch(clause.getHint()){
            case PHRASE_ON_LEFT:
                // Web searches should use phrases over separate words.
                clause.getFirstClause().accept(visitor);
                break;
            default:
                // All other high level clauses are ignored.
                clause.getSecondClause().accept(visitor);
                break;
        }
    }


    /**
     * Returns the fast search result
     *
     * @return The fast search result.
     */
    protected IQueryResult getIQueryResult() {
        return result;
    }

    // Private -------------------------------------------------------

    private ISearchView initialiseSearchView() {


        final String view = cfg.getView();

        if (view == null) {
            throw new IllegalStateException(ERR_CALL_SET_VIEW);
        }

        final String searchViewKey = queryServer + "/" + view;

        // XXX There is no synchronisation around this static map.
        //   Not critical as any clashing threads will just override the values,
        //    and the cost of the occasional double-up creation probably doesn't compare
        //    to the synchronisation overhead.
        ISearchView searchView = SEARCH_VIEWS.get(searchViewKey);

        if( null == searchView ){
            final Properties props = new Properties();

            props.setProperty(FACTORY_PROPERTY, HTTP_FACTORY);
            props.setProperty(QR_SERVER_PROPERTY, queryServer);
            props.setProperty(ENCODER_PROPERTY, ENCODER_CLASS);

            try {
                searchView = SearchFactory.newInstance(props).getSearchView(view);

                // Force server address since we want to use the hardware load balancer.
                // This also enables us to do tunneling.
                final String serverName = queryServer.substring(0, queryServer.indexOf(':'));
                final String serverPort = queryServer.substring(queryServer.indexOf(':') + 1);
                searchView.setServerAddress(serverName, Integer.parseInt(serverPort), false);

            } catch (ConfigurationException e) {
                throw new InfrastructureException(e);
            } catch (SearchEngineException e) {
                throw new InfrastructureException(e);
            }
            SEARCH_VIEWS.put(searchViewKey, searchView);
        }

        LOG.debug("Using searchView: " + searchViewKey);

        return searchView;
    }

    private int getMaxDocIndex(
            final IQueryResult iQueryResult,
            final int cnt,
            final ESPFastSearchConfiguration fastSearchConfiguration)
    {
        return Math.min(cnt + fastSearchConfiguration.getResultsToReturn(), iQueryResult.getDocCount());
    }

    private SearchResultItem createResultItem(final IDocumentSummary document) {

        final SearchResultItem item = new BasicSearchResultItem();

        for (final Map.Entry<String,String> entry : cfg.getResultFields().entrySet()) {

            final IDocumentSummaryField summary = document.getSummaryField(entry.getKey());

            if (summary != null && !summary.isEmpty())
                item.addField(entry.getValue(), summary.getStringValue().trim());
        }

        if (cfg.isCollapsingEnabled()) {
            final String currCollapseId = getParameter(COLLAPSE_PARAMETER);

            if (currCollapseId == null || currCollapseId.equals("")) {

                if (! document.getSummaryField("fcocount").isEmpty() && Integer.parseInt(document.getSummaryField("fcocount").getStringValue()) > 1) {
                    item.addField("moreHits", "true");
                    item.addField("collapseParameter", COLLAPSE_PARAMETER);
                    item.addField("collapseId", document.getSummaryField("collapseid").getStringValue());
                }
            }
        }
        return item;
    }


    // Inner classes -------------------------------------------------
}

