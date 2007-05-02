package no.schibstedsok.searchportal.mode.command;

import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.query.SearchParameter;
import com.fastsearch.esp.search.result.EmptyValueException;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IQueryResult;
import com.fastsearch.esp.search.result.IllegalType;
import no.schibstedsok.searchportal.mode.config.NewsEspCommandConfig;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewsEspSearchCommand extends NavigatableESPFastCommand {
    public static final String PARAM_NEXT_OFFSET = "nextOffset";
    private static final Logger LOG = Logger.getLogger(NewsEspSearchCommand.class);

    public NewsEspSearchCommand(Context cxt) {
        super(cxt);
    }

    protected static void addNextOffsetField(int nextOffset, SearchResult searchResult) {
        searchResult.addField(NewsEspSearchCommand.PARAM_NEXT_OFFSET, Integer.toString(nextOffset));
    }

    @Override
    protected void modifyQuery(IQuery query) {
        super.modifyQuery(query);
        final NewsEspCommandConfig config = getSearchConfiguration();
        query.setParameter(BaseParameter.HITS, Math.max(config.getCollapsingMaxFetch(), config.getResultsToReturn()));
        if (config.isIgnoreOffset()) {
            query.setParameter(new SearchParameter(BaseParameter.OFFSET, 0));
        }
    }

    @Override
    protected FastSearchResult createSearchResult(final IQueryResult result) throws IOException {
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
            if (!NewsEspCommandConfig.ALL_MEDIUMS.equals(medium) && getTransformedQuery().length() > 0) {
                if (medium == null || medium.length() == 0) {
                    medium = config.getDefaultMedium();
                }
                insertToQueryRepresentation(0, "and(");
                appendToQueryRepresentation(',');
                appendToQueryRepresentation(config.getMediumPrefix());
                appendToQueryRepresentation(':');
                appendToQueryRepresentation(medium);
                appendToQueryRepresentation(')');
                LOG.debug("Added medium");
            }

        }
    }

    @Override
    protected void visitImpl(final Object clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
    }

    protected void visitImpl(final Clause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final LeafClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final OperationClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final AndClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final OrClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final DefaultOperatorClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final NotClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final AndNotClause clause) {
        LOG.debug("Visiting me with: " + clause + ", isroot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    public NewsEspCommandConfig getSearchConfiguration() {
        return (NewsEspCommandConfig) super.getSearchConfiguration();
    }

    protected FastSearchResult createCollapsedResults(NewsEspCommandConfig config, int offset, IQueryResult result) throws IllegalType, EmptyValueException {
        final String nestedResultsField = config.getNestedResultsField();
        final FastSearchResult searchResult = new FastSearchResult(this);
        final HashMap<String, SearchResultItem> collapseMap = new HashMap<String, SearchResultItem>();
        searchResult.setHitCount(result.getDocCount());
        int collectedHits = 0;
        int analyzedHits = 0;
        final int firstHit = config.isIgnoreOffset() ? 0 : offset;
        for (int i = firstHit; i < result.getDocCount() && analyzedHits < config.getCollapsingMaxFetch(); i++) {
            try {
                final IDocumentSummary document = result.getDocument(i + 1);
                final String collapseId = document.getSummaryField("collapseid").getStringValue();
                SearchResultItem parentResult = collapseMap.get(collapseId);
                if (parentResult == null) {
                    if (collapseMap.size() < config.getResultsToReturn()) {
                        parentResult = addResult(config, searchResult, document);
                        collapseMap.put(collapseId, parentResult);
                        collectedHits++;
                    }
                } else {
                    SearchResult nestedResult = parentResult.getNestedSearchResult(nestedResultsField);
                    if (nestedResult == null) {
                        nestedResult = new BasicSearchResult(this);
                        parentResult.addNestedSearchResult(nestedResultsField, nestedResult);
                        nestedResult.setHitCount(1);
                    }
                    addResult(config, nestedResult, document);
                    nestedResult.setHitCount(nestedResult.getHitCount() + 1);
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

    protected static SearchResultItem addResult(NewsEspCommandConfig config, SearchResult searchResult, IDocumentSummary document) {
        SearchResultItem searchResultItem = new BasicSearchResultItem();

        for (final Map.Entry<String, String> entry : config.getResultFields().entrySet()) {
            final IDocumentSummaryField summary = document.getSummaryField(entry.getKey());
            if (summary != null && !summary.isEmpty()) {
                searchResultItem.addField(entry.getValue(), summary.getStringValue().trim());
            }
        }
        searchResult.addResult(searchResultItem);
        return searchResultItem;
    }

}
