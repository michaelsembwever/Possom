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
import no.schibstedsok.searchportal.mode.config.ClusteringESPFastConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClusteringESPFastCommand extends NavigatableESPFastCommand {
    private static final Logger LOG = Logger.getLogger(ClusteringESPFastCommand.class);

    public ClusteringESPFastCommand(Context cxt) {
        super(cxt);
    }

    /**
     * Modifies several queryParameters depending on situation.
     *
     * @param query the FAST IQuery to modify
     */
    protected void modifyQuery(IQuery query) {
        final ClusteringESPFastConfiguration config = getSearchConfiguration();
        final StringDataObject clusterId = datamodel.getParameters().getValue(config.getClusterIdParameter());
        if (clusterId == null) {
            LOG.debug("--- Modifying query. ---");
            final int resultsPerCluster = config.getResultsPerCluster();
            final int resultCount = config.getResultsToReturn() * resultsPerCluster;

            query.setParameter("collapseon", "batv" + config.getClusterField());
            query.setParameter("collapsenum", resultsPerCluster);
            query.setParameter(BaseParameter.HITS, resultCount);
        } else {
            query.setParameter(BaseParameter.HITS, config.getClusterMaxFetch());
        }
    }

    /**
     * Creates a clustered FastSearchResult.
     *
     * @param result the clustered searchResult
     * @return the FAST IQueryReslt to make the searchResult from
     * @throws IOException
     */
    protected FastSearchResult createSearchResult(final IQueryResult result) throws IOException {
        try {
            final ClusteringESPFastConfiguration config = getSearchConfiguration();
            StringDataObject clusterId = datamodel.getParameters().getValue(config.getClusterIdParameter());
            if (clusterId == null) {
                return createClusteredSearchResult(config, result);
            } else {
                return createSingleClusterResults(config, result);
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


    private FastSearchResult createSingleClusterResults(ClusteringESPFastConfiguration config, IQueryResult result) throws IllegalType, EmptyValueException {
        final String nestedResultsField = config.getNestedResultsField();
        final FastSearchResult searchResult = new FastSearchResult(this);
        final HashMap<String, SearchResultItem> collapseMap = new HashMap<String, SearchResultItem>();
        searchResult.setHitCount(result.getDocCount());
        for (int i = 0; i < result.getDocCount(); i++) {
            try {
                final IDocumentSummary document = result.getDocument(i + 1);
                final String collapseId = document.getSummaryField("collapseid").getStringValue();
                SearchResultItem parentResult = collapseMap.get(collapseId);
                if (parentResult == null) {
                    parentResult = addResult(config, searchResult, document);
                    searchResult.addResult(parentResult);
                    collapseMap.put(collapseId, parentResult);
                } else {
                    SearchResult nestedResult = parentResult.getNestedSearchResult(nestedResultsField);
                    if (nestedResult == null) {
                        nestedResult = new BasicSearchResult(this);
                        parentResult.addNestedSearchResult(nestedResultsField, nestedResult);
                        nestedResult.setHitCount(1);
                    }
                    addResult(config, nestedResult, document);
                    nestedResult.setHitCount(nestedResult.getHitCount() + 1);
                }
            } catch (NullPointerException e) {
                // The doc count is not 100% accurate.
                LOG.debug("Error finding document ", e);
                break;
            }
        }
        return searchResult;
    }

    private FastSearchResult createClusteredSearchResult(ClusteringESPFastConfiguration config, IQueryResult result) throws IllegalType, EmptyValueException {
        final String clusterField = config.getClusterField();
        final String nestedResultsField = config.getNestedResultsField();
        final FastSearchResult searchResult = new FastSearchResult(this);
        final int maxClusterCount = config.getResultsToReturn();

        IDocumentSummaryField currentClusterId;
        IDocumentSummaryField lastClusterId = null;
        int collectedClusters = 0;
        int collectedHits = 0;
        SearchResultItem clusterEntry = null;
        SearchResult subResult = null;

        LOG.debug("HitCount=" + result.getDocCount() + ", clusterField=" + clusterField + ", nestedResultsField=" + nestedResultsField);

        for (int i = 0; i < result.getDocCount(); i++) {
            try {
                final IDocumentSummary document = result.getDocument(i + 1);
                currentClusterId = document.getSummaryField(clusterField);
                if (currentClusterId.isEmpty() ||
                        lastClusterId == null ||
                        lastClusterId.isEmpty() ||
                        currentClusterId.getStringValue().equals("0") ||
                        (!currentClusterId.getStringValue().equals(lastClusterId.getStringValue()))) {
                    collectedClusters++;
                    LOG.debug("Adding new cluster: " + currentClusterId + ", count is: " + collectedClusters);
                    if (collectedClusters < maxClusterCount) {
                        clusterEntry = addResult(config, searchResult, document);
                        if (!currentClusterId.isEmpty()) {
                            clusterEntry.addField(config.getClusterIdParameter(), currentClusterId.getStringValue());
                        }
                        lastClusterId = currentClusterId;
                    } else {
                        break;
                    }
                    subResult = null;
                } else {
                    LOG.debug("Adding subResult for: " + currentClusterId.getStringValue());
                    if (subResult == null) {
                        subResult = new BasicSearchResult(this);
                        IDocumentSummaryField clusterHitCount = document.getSummaryField("fcocount");
                        if (!clusterHitCount.isEmpty()) {
                            subResult.setHitCount(Integer.parseInt(clusterHitCount.getStringValue()));
                        }
                        clusterEntry.addNestedSearchResult(nestedResultsField, subResult);
                    }
                    addResult(config, subResult, document);
                }
                collectedHits++;
            } catch (NullPointerException e) {
                // The doc count is not 100% accurate.
                LOG.debug("Error finding document " + e);
                break;
            }
        }
        searchResult.setHitCount(collectedHits);
        return searchResult;
    }

    private static SearchResultItem addResult(ClusteringESPFastConfiguration config, SearchResult searchResult, IDocumentSummary document) {
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


    public ClusteringESPFastConfiguration getSearchConfiguration() {
        return (ClusteringESPFastConfiguration) super.getSearchConfiguration();
    }


}
