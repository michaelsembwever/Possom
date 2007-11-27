/* Copyright (2007) Schibsted Søk AS
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

import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.result.EmptyValueException;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IQueryResult;
import com.fastsearch.esp.search.result.IllegalType;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.mode.config.ClusteringEspFastCommandConfig;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

import java.io.IOException;

/** Search command to cluster results from a NewsEspSearchCommand.
 *
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public class NewsClusteringESPFastCommand extends NewsEspSearchCommand {

    private static final Logger LOG = Logger.getLogger(NewsClusteringESPFastCommand.class);

    private static final String ERR_CONVERT = "Could not convert result";

    public NewsClusteringESPFastCommand(final Context cxt) {
        super(cxt);
    }

    /**
     * Modifies several queryParameters depending on situation.
     *
     * @param query the FAST IQuery to modify
     */
    @Override
    protected void modifyQuery(final IQuery query) {

        super.modifyQuery(query);
        final ClusteringEspFastCommandConfig config = getSearchConfiguration();

        final StringDataObject clusterId = datamodel.getParameters().getValue(config.getClusterIdParameter());
        if (clusterId == null && ! config.isClusteringDisabled()) {
            LOG.debug("--- Modifying query. ---");
            final int resultsPerCluster = config.getResultsPerCluster();
            final int resultCount = config.getResultsToReturn() * resultsPerCluster;

            query.setParameter("collapseon", "batv" + config.getClusterField());
            query.setParameter("collapsenum", resultsPerCluster);
            query.setParameter(BaseParameter.HITS, Math.max(resultCount, config.getCollapsingMaxFetch()));
        }
    }

    /**
     * Creates a clustered FastSearchResult.
     *
     * @param result the clustered searchResult
     * @return the FAST IQueryReslt to make the searchResult from
     * @throws IOException
     */
    @Override
    protected FastSearchResult<ResultItem> createSearchResult(final IQueryResult result) throws IOException {

        try {
            final ClusteringEspFastCommandConfig config = getSearchConfiguration();
            final StringDataObject clusterId = datamodel.getParameters().getValue(config.getClusterIdParameter());
            if (config.isClusteringDisabled()) {
                FastSearchResult<ResultItem> searchResult = super.createSearchResult(result);
                int offset = getOffset();
                if (offset + config.getResultsToReturn() < result.getDocCount()) {
                    addNextOffsetField(offset + config.getResultsToReturn(), searchResult);
                }
                return searchResult;
            } else if (clusterId == null) {
                return createClusteredSearchResult(config, getOffset(), result);
            } else {
                return createCollapsedResults(config, getOffset(), result);
            }
        } catch (IllegalType e) {
            LOG.error(ERR_CONVERT, e);
        } catch (EmptyValueException e) {
            LOG.error(ERR_CONVERT, e);
        } catch (RuntimeException e) {
            LOG.error(ERR_CONVERT, e);
        }
        // Falling back to super implementation, because this one does not work.
        return super.createSearchResult(result);
    }

    private FastSearchResult<ResultItem> createClusteredSearchResult(
            final ClusteringEspFastCommandConfig config,
            final int offset,
            final IQueryResult result) throws IllegalType, EmptyValueException {

        final String clusterField = config.getClusterField();
        //  final String nestedResultsField = config.getNestedResultsField();
        // Following will throw either ClassCastException or NPE of navigators are used
        final FastSearchResult<ResultItem> searchResult = new FastSearchResult<ResultItem>();
        final int maxClusterCount = config.getResultsToReturn();

        IDocumentSummaryField currentClusterId;
        IDocumentSummaryField lastClusterId = null;
        int collectedClusters = 0;
        int collectedHits = 0;
        ResultList<ResultItem> clusterEntry = null;

        LOG.debug("HitCount=" + result.getDocCount() + ", clusterField=" + clusterField + ", offset=" + offset);
        final int firstHit = config.isIgnoreOffset() ? 0 : offset;

        for (int i = firstHit; i < result.getDocCount(); i++) {
            try {
                final IDocumentSummary document = result.getDocument(i + 1);
                currentClusterId = document.getSummaryField(clusterField);

                if (currentClusterId.isEmpty()
                        || lastClusterId == null
                        || lastClusterId.isEmpty()
                        || currentClusterId.getStringValue().equals("0")
                        || (!currentClusterId.getStringValue().equals(lastClusterId.getStringValue()))) {

                    LOG.debug("Adding new cluster: " + currentClusterId + ", count is: " + collectedClusters);
                    if (collectedClusters < maxClusterCount) {
                        clusterEntry = addResult(config, searchResult, document);
                        if (!currentClusterId.isEmpty()) {
                            clusterEntry = clusterEntry.addField(
                                    config.getClusterIdParameter(),
                                    currentClusterId.getStringValue());
                        }
                        final IDocumentSummaryField clusterHitCount = document.getSummaryField("fcocount");
                        if (!clusterHitCount.isEmpty()) {
                            clusterEntry.setHitCount(Integer.parseInt(clusterHitCount.getStringValue()));
                        }
                        lastClusterId = currentClusterId;
                    } else {
                        break;
                    }
                    collectedClusters++;
                } else {
                    LOG.debug("Adding subResult for: " + currentClusterId.getStringValue());
                    addResult(config, clusterEntry, document);
                }
                collectedHits++;

            } catch (NullPointerException e) {
                // The doc count is not 100% accurate.
                LOG.debug("Error finding document " + e);
                break;
            }
        }

        if (offset + collectedHits < result.getDocCount()) {
            addNextOffsetField(offset + collectedHits, searchResult);
        }

        searchResult.setHitCount(result.getDocCount());
        return searchResult;
    }

    @Override
    public ClusteringEspFastCommandConfig getSearchConfiguration() {
        return (ClusteringEspFastCommandConfig) super.getSearchConfiguration();
    }


}
