// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.result.EmptyValueException;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IQueryResult;
import com.fastsearch.esp.search.result.IllegalType;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.mode.config.ClusteringEspFastCommandConfig;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public class ClusteringESPFastCommand extends NewsEspSearchCommand {

    private static final Logger LOG = Logger.getLogger(ClusteringESPFastCommand.class);

    /**
     * @param cxt
     */
    public ClusteringESPFastCommand(Context cxt) {
        super(cxt);
    }

    /**
     * Modifies several queryParameters depending on situation.
     *
     * @param query the FAST IQuery to modify
     */
    @Override
    protected void modifyQuery(IQuery query) {
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
    protected FastSearchResult<ResultItem> createSearchResult(final IQueryResult result) throws IOException {
        try {
            final ClusteringEspFastCommandConfig config = getSearchConfiguration();
            StringDataObject clusterId = datamodel.getParameters().getValue(config.getClusterIdParameter());
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
            LOG.error("Could not convert result", e);
        } catch (EmptyValueException e) {
            LOG.error("Could not convert result", e);
        } catch (RuntimeException e) {
            LOG.error("Could not convert result", e);
        }
        // Falling back to super implementation, because this one does not work.
        return super.createSearchResult(result);
    }

    private FastSearchResult<ResultItem> createClusteredSearchResult(
            final ClusteringEspFastCommandConfig config,
            final int offset,
            final IQueryResult result) throws IllegalType, EmptyValueException {

        final String clusterField = config.getClusterField();
//        final String nestedResultsField = config.getNestedResultsField();
        // Following will throw either ClassCastException or NPE of navigators are used
        final FastSearchResult<ResultItem> searchResult = new FastSearchResult<ResultItem>(null);
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

    public ClusteringEspFastCommandConfig getSearchConfiguration() {
        return (ClusteringEspFastCommandConfig) super.getSearchConfiguration();
    }


}
