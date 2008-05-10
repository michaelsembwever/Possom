/* Copyright (2007-2008) Schibsted Søk AS
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
 */
package no.sesat.search.mode.command;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.mode.config.NewsEspCommandConfig;
import no.sesat.search.query.AndClause;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.UrlClause;
import no.sesat.search.query.Visitor;
import no.sesat.search.query.XorClause;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

import org.apache.log4j.Logger;

import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.query.SearchParameter;
import com.fastsearch.esp.search.result.EmptyValueException;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IQueryResult;
import com.fastsearch.esp.search.result.IllegalType;

/** Navigatable ESP search command for news.
 * @todo documentation what additional functionality actually amounts to it benefitting news verticals.
 *
 *
 * @version $Id$
 */
public class NewsEspSearchCommand extends NavigatableESPFastCommand {

    private static final String PARAM_NEXT_OFFSET = "nextOffset";
    private static final Logger LOG = Logger.getLogger(NewsEspSearchCommand.class);

    private static final String FAST_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public NewsEspSearchCommand(final Context cxt) {
        super(cxt);
    }

    /** Add the next offset field.
     * @param nextOffset
     * @param searchResult
     */
    protected static void addNextOffsetField(
            final int nextOffset,
            final ResultList<? extends ResultItem> searchResult) {

        searchResult.addField(NewsEspSearchCommand.PARAM_NEXT_OFFSET, Integer.toString(nextOffset));
    }

    @Override
    protected void modifyQuery(final IQuery query) {

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

        if (config.getMaxAge() != null) {
            final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            final int age = config.getMaxAgeAmount();

            switch (config.getAgeSymbol()) {
                case 'w':
                    cal.add(Calendar.WEEK_OF_YEAR, -age);
                    break;
                case 'M':
                    cal.add(Calendar.MONTH, -age);
                    break;
                case 'd':
                    cal.add(Calendar.DATE, -age);
                    break;
                case 'h':
                    cal.add(Calendar.HOUR, -age);
                    break;
                case 'm':
                    cal.add(Calendar.MINUTE, -age);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown age symbol " + config.getAgeSymbol());

            }

            final String latestDate = new SimpleDateFormat(FAST_DATE_FORMAT).format(cal.getTime());

            final StringBuilder q = new StringBuilder();

            if (query.getQueryString().length() > 0) {
                q.append("and(").append(query.getQueryString()).append(",");
            }

            q.append("filter(").append(config.getAgeField()).append(":range(").append(latestDate).append(",max))");

            if (query.getQueryString().length() > 0) {
                q.append(")");
            }
            query.setQueryString(q.toString());
        }
    }

    @Override
    protected FastSearchResult<ResultItem> createSearchResult(final IQueryResult result) throws IOException {

        final NewsEspCommandConfig config = getSearchConfiguration();
        try {
            return createCollapsedResults(config, getOffset(), result);
        } catch (final IllegalType e) {
            LOG.error("Could not convert result", e);
        } catch (final EmptyValueException e) {
            LOG.error("Could not convert result", e);
        }
        return super.createSearchResult(result);
    }

    private void addMedium(final Clause clause) {

        if (getQuery().getRootClause() == clause) {
            final NewsEspCommandConfig config = getSearchConfiguration();
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
                    appendToQueryRepresentation("\"" + medium + "\"");
                    appendToQueryRepresentation(')');
                    LOG.debug("Added medium");
                    return;
                } else if (getQuery().getQueryString() != null && getQuery().getQueryString().trim().equals("*")) {
                    appendToQueryRepresentation(config.getMediumPrefix());
                    appendToQueryRepresentation(':');
                    appendToQueryRepresentation("\"" + medium + "\"");
                    LOG.debug("Added medium");
                    return;
                }
            }
            LOG.debug("Did not add medium on rootclause: medium=" + medium
                    + ", queryLength=" + getQueryRepresentationLength());
        }
    }

    /**
     * @param clause
     */
    @Override
    protected void visitImpl(final Object clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitImpl(clause);
        if (clause instanceof Clause) {
            addMedium((Clause) clause);
        }
    }

    /**
     * @param clause
     */
    protected void visitImpl(final Clause clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitImpl(clause);
        addMedium(clause);
    }


    @Override
    protected void visitImpl(final LeafClause clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final OperationClause clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final AndClause clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final OrClause clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final DefaultOperatorClause clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final NotClause clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause="
                + getQuery().getRootClause());

        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final AndNotClause clause) {

        LOG.debug("Visiting me with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitImpl(clause);
        addMedium(clause);
    }


    @Override
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {

        LOG.debug("Visit xorClause called with: " + clause
                + ", isroot=" + (getQuery().getRootClause() == clause)
                + ", rootClause=" + getQuery().getRootClause());

        super.visitXorClause(visitor, clause);
        addMedium(clause);
    }

    @Override
    public NewsEspCommandConfig getSearchConfiguration() {
        return (NewsEspCommandConfig) super.getSearchConfiguration();
    }

    /** Create the collapsed result list.
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
        final int firstHit = offset;
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
            } catch (final NullPointerException e) {
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

    /** Add a result (document) as a new child result list to an existing result list.
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
