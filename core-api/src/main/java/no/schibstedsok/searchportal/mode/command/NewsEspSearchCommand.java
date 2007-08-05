package no.schibstedsok.searchportal.mode.command;

import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.query.SearchParameter;
import com.fastsearch.esp.search.result.EmptyValueException;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IQueryResult;
import com.fastsearch.esp.search.result.IllegalType;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.mode.config.NewsEspCommandConfig;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public class NewsEspSearchCommand extends NavigatableESPFastCommand {
    /**
     *
     */
    public static final String PARAM_NEXT_OFFSET = "nextOffset";
    private static final Logger LOG = Logger.getLogger(NewsEspSearchCommand.class);

    /**
     * @param cxt
     */
    public NewsEspSearchCommand(Context cxt) {
        super(cxt);
    }

    /**
     * @param nextOffset
     * @param searchResult
     */
    protected static void addNextOffsetField(int nextOffset, ResultList<? extends ResultItem> searchResult) {

        searchResult.addField(NewsEspSearchCommand.PARAM_NEXT_OFFSET, Integer.toString(nextOffset));
    }

    @Override
    protected void modifyQuery(IQuery query) {
        super.modifyQuery(query);
        final NewsEspCommandConfig config = getSearchConfiguration();

        // Because of a bug in FAST ESP5 related to collapsing and sorting, we must use sort direcetion,
        // and not the +fieldname syntax
        final StringDataObject sort = datamodel.getParameters().getValue(config.getUserSortParameter());
        String sortType;
        if (sort != null) {
            sortType = sort.getString();
        } else {
            sortType = config.getDefaultSort();
        }
        if (sortType.equals("relevance")) {
            query.setParameter(BaseParameter.SORT_BY, config.getRelevanceSortField());
            query.setParameter(BaseParameter.SORT_DIRECTION, "descending");
        } else {
            query.setParameter(BaseParameter.SORT_BY, config.getSortField());
            query.setParameter(BaseParameter.SORT_DIRECTION, sortType);
        }

        query.setParameter(BaseParameter.HITS, Math.max(config.getCollapsingMaxFetch(), config.getResultsToReturn()));
        if (config.isIgnoreOffset()) {
            query.setParameter(new SearchParameter(BaseParameter.OFFSET, 0));
        }
    }

    @Override
    protected FastSearchResult<ResultItem> createSearchResult(final IQueryResult result) throws IOException {
        final NewsEspCommandConfig config = getSearchConfiguration();
        try {
            return createCollapsedResults(config, getOffset(), result);
        } catch (IllegalType e) {
            LOG.error("Could not convert result", e);
        } catch (EmptyValueException e) {
            LOG.error("Could not convert result", e);
        }
        return super.createSearchResult(result);
    }

    /**
     * @return
     */
    protected int getOffset() {
        int offset = 0;
        if (datamodel.getJunkYard().getValue("offset") != null) {
            offset = Integer.parseInt((String) datamodel.getJunkYard().getValue("offset"));
        }
        return offset;
    }

    private void addMedium(Clause clause) {
        if (getQuery().getRootClause() == clause) {
            NewsEspCommandConfig config = getSearchConfiguration();
            String medium = (String) datamodel.getJunkYard().getValue(config.getMediumParameter());
            if (medium == null || medium.length() == 0) {
                medium = config.getDefaultMedium();
            }
            if (!NewsEspCommandConfig.ALL_MEDIUMS.equals(medium)) {
                if (getQueryRepresentationLength() > 0) {
                    insertToQueryRepresentation(0, "and(");
                    appendToQueryRepresentation(',');
                    appendToQueryRepresentation(config.getMediumPrefix());
                    appendToQueryRepresentation(':');
                    appendToQueryRepresentation(medium);
                    appendToQueryRepresentation(')');
                    LOG.debug("Added medium");
                    return;
                } else if (getQuery().getQueryString() != null && getQuery().getQueryString().trim().equals("*")) {
                    appendToQueryRepresentation(config.getMediumPrefix());
                    appendToQueryRepresentation(':');
                    appendToQueryRepresentation(medium);
                    LOG.debug("Added medium");
                    return;
                }
            }
            LOG.debug("Did not add medium on rootclause: medium=" + medium + ", queryLength=" + getQueryRepresentationLength());
        }
    }

    /**
     * @param clause
     */
    @Override
    protected void visitImpl(final Object clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        if (clause instanceof Clause) {
            addMedium((Clause) clause);
        }
    }

    /**
     * @param clause
     */
    protected void visitImpl(final Clause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        addMedium(clause);
    }


    @Override
    protected void visitImpl(final LeafClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final OperationClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final AndClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final OrClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final DefaultOperatorClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final NotClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final AndNotClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitImpl(clause);
        addMedium(clause);
    }


    @Override
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {
        LOG.debug("Visit xorClause called with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause) + ", rootClause=" + getQuery().getRootClause());
        super.visitXorClause(visitor, clause);
        addMedium(clause);
    }

    @Override
    public NewsEspCommandConfig getSearchConfiguration() {
        return (NewsEspCommandConfig) super.getSearchConfiguration();
    }

    /**
     * @param config
     * @param offset
     * @param result
     * @return
     * @throws com.fastsearch.esp.search.result.IllegalType
     *
     * @throws com.fastsearch.esp.search.result.EmptyValueException
     *
     */
    protected FastSearchResult<ResultItem> createCollapsedResults(
            final NewsEspCommandConfig config,
            final int offset,
            final IQueryResult result) throws IllegalType, EmptyValueException {

        final FastSearchResult<ResultItem> searchResult = new FastSearchResult<ResultItem>();
        final Map<String, ResultList<ResultItem>> collapseMap = new HashMap<String, ResultList<ResultItem>>();
        searchResult.setHitCount(result.getDocCount());
        int collectedHits = 0;
        int analyzedHits = 0;
        final int firstHit = config.isIgnoreOffset() ? 0 : offset;
        for (int i = firstHit; i < result.getDocCount() && analyzedHits < config.getCollapsingMaxFetch(); i++) {
            try {
                final IDocumentSummary document = result.getDocument(i + 1);
                final String collapseId = document.getSummaryField("collapseid").getStringValue();
                ResultList<ResultItem> parentResult = collapseMap.get(collapseId);
                if (parentResult == null) {
                    if (collapseMap.size() < config.getResultsToReturn()) {
                        parentResult = addResult(config, searchResult, document);
                        parentResult.setHitCount(1);
                        collapseMap.put(collapseId, parentResult);
                        collectedHits++;
                    }
                } else {
                    addResult(config, parentResult, document);
                    parentResult.setHitCount(parentResult.getHitCount() + 1);
                    collectedHits++;
                }
                analyzedHits++;
            } catch (NullPointerException e) {
                // The doc count is not 100% accurate.
                LOG.debug("Error finding document ", e);
                break;
            }
        }
        if (offset + collectedHits < result.getDocCount()) {
            addNextOffsetField(offset + collectedHits, searchResult);
        }
        return searchResult;
    }

    /**
     * @param config
     * @param searchResult
     * @param document
     * @return
     */
    protected static ResultList<ResultItem> addResult(
            final NewsEspCommandConfig config,
            final ResultList<ResultItem> searchResult,
            final IDocumentSummary document) {

        ResultList<ResultItem> newResult = new BasicResultList<ResultItem>();

        for (final Map.Entry<String, String> entry : config.getResultFields().entrySet()) {
            final IDocumentSummaryField summary = document.getSummaryField(entry.getKey());
            if (summary != null && !summary.isEmpty()) {
                newResult = newResult.addField(entry.getValue(), summary.getStringValue().trim());
            }
        }
        searchResult.addResult(newResult);
        return newResult;
    }

}
