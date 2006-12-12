/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractESPFastSearchCommand.java
 *
 * Created on 14 March 2006, 19:51
 *
 */

package no.schibstedsok.searchportal.mode.command;

import com.fastsearch.esp.search.SearchEngineException;
import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.query.Query;
import com.fastsearch.esp.search.query.SearchParameter;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IQueryResult;
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
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import no.schibstedsok.searchportal.query.Visitor;

/**
 *
 * Base class for commands querying a FAST EPS Server.
 */
public abstract class AbstractESPFastSearchCommand extends AbstractSearchCommand {

    private final static String COLLAPSE_PARAMETER = "collapse";

    private static final Logger LOG = Logger.getLogger(AbstractESPFastSearchCommand.class);

    // Attributes ----------------------------------------------------
    private final ESPFastSearchConfiguration cfg;
    private IQueryResult result;
    
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates new instance of search command.
     *
     * @param cxt The context to work in.
     * @param parameters The command parameters to use.
     */
    public AbstractESPFastSearchCommand(
                    final Context cxt,
                    final Map<String, Object> parameters) {

        super(cxt, parameters);

        cfg = (ESPFastSearchConfiguration) getSearchConfiguration();
    }

    // Public --------------------------------------------------------
    /** {@insheritDoc} */
    public SearchResult execute() {

        try {
                                                  
            final StringBuilder filterBuilder = new StringBuilder();

            if (getFilter() != null) {
                filterBuilder.append(getFilter());
                filterBuilder.append(" ");
            }

            if (getAdditionalFilter() != null) {
                filterBuilder.append(getAdditionalFilter());
                filterBuilder.append(" ");
            }


            final String transformedQuery = getTransformedQuery();

            LOG.debug("Transformed query is " + transformedQuery);

            final String collapseId = getParameter(COLLAPSE_PARAMETER);

            final IQuery query = new Query(transformedQuery);

            if (cfg.isCollapsingEnabled()) {
                if (collapseId == null || collapseId.equals("")) {
                    if (cfg.isCollapsingRemoves()) {
                        query.setParameter(new SearchParameter("collapseon", "batvcollapseid"));
                    }
                } else {
                    filterBuilder.append("+collapseid:").append(collapseId);
                }
            }

            query.setParameter(new SearchParameter(BaseParameter.OFFSET, getCurrentOffset(0)));
            query.setParameter(new SearchParameter(BaseParameter.HITS, cfg.getResultsToReturn()));
            query.setParameter(new SearchParameter(BaseParameter.SORT_BY, cfg.getSortBy()));
            query.setParameter(new SearchParameter(BaseParameter.FILTER, filterBuilder.toString()));

            if (! (this instanceof NavigatableESPFastCommand)) {
                query.setParameter(new SearchParameter(BaseParameter.NAVIGATION, 0));
            }

            if (! cfg.getQtPipeline().equals("")) {
                query.setParameter(new SearchParameter(BaseParameter.QT_PIPELINE, cfg.getQtPipeline()));
            }


            LOG.debug("Query is " + query);

            result = cfg.getSearchView().search(query);

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
            LOG.error(ex.getMessage() + " " + ex.getCause());
            return new FastSearchResult(this);
        } catch (IOException ex) {
            LOG.error("exeute ", ex);
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

