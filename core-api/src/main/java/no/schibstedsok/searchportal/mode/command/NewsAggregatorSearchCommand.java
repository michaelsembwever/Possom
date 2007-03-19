// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.mode.config.NewsAggregatorSearchConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

/**
 * Search command that will try to get pregenerated clusters from xml files. If the xml file is not available it will
 * fall back to a search.
 */
public class NewsAggregatorSearchCommand extends NavigatableESPFastCommand {

    private final static Logger LOG = Logger.getLogger(NewsAggregatorSearchCommand.class);
    private static final String PARAM_GEONAV = "geonav";
    private static final String PARAM_CLUSTER_ID = "clusterId";

    /**
     * @param cxt       The context to execute in.
     * @param dataModel The dataModel to use.
     */
    public NewsAggregatorSearchCommand(final Context cxt) {
        super(cxt);
    }

    public SearchResult execute() {
        LOG.debug("News aggregator search executed with: " + getParameters());
        LOG.debug("News aggregator search executed with: " + datamodel.getParameters());

        NewsAggregatorSearchConfiguration config = (NewsAggregatorSearchConfiguration) getSearchConfiguration();
        LOG.debug("Loading xml file at: " + config.getXmlSource());
        LOG.debug("Update interval: " + config.getUpdateIntervalMinutes());

        StringDataObject geoNav = datamodel.getParameters().getValue(PARAM_GEONAV);
        StringDataObject clusterId = datamodel.getParameters().getValue(PARAM_CLUSTER_ID);

        String xmlFile = geoNav == null ? config.getXmlMainFile() : geoNav.getString();

        if (clusterId == null) {
            return getPageResult(config, xmlFile);
        } else {
            return getClusterResult(config, clusterId, xmlFile);
        }
    }

    private SearchResult getClusterResult(NewsAggregatorSearchConfiguration config, StringDataObject clusterId, String xmlFile) {
        try {
            final NewsAggregatorXmlParser newsAggregatorXmlParser = new NewsAggregatorXmlParser();
            final InputStream inputStream = getInputStream(config, xmlFile);
            return newsAggregatorXmlParser.parseCluster(config, inputStream, clusterId.getString(), this);
        } catch (IOException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        } catch (JDOMException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        }
        return search(config, clusterId.getString());
    }

    private SearchResult search(NewsAggregatorSearchConfiguration config, String clusterId) {
        LOG.debug("------ Running search to get clusters ---------");
        LOG.debug("clusterId=" + clusterId);
        LOG.debug("result-fields=" + config.getResultFields());
        LOG.debug("query-server=" + config.getQueryServer());
        LOG.debug("-----------------------------------------------");
        SearchResult searchResult = super.execute();
        return searchResult;
    }

    private SearchResult getPageResult(NewsAggregatorSearchConfiguration config, String xmlFile) {
        final NewsAggregatorXmlParser newsAggregatorXmlParser = new NewsAggregatorXmlParser();
        try {
            return newsAggregatorXmlParser.parseFullPage(config, getInputStream(config, xmlFile), this);
        } catch (JDOMException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        } catch (IOException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        }
        return search(config, null);
    }

    private InputStream getInputStream(NewsAggregatorSearchConfiguration config, String xmlFile) throws IOException {
        final URL url = new URL(config.getXmlSource() + xmlFile);
// ---------
//        Can not use HTTPClient in this case since it ignores protocol of the url.
//  --------
//        final HTTPClient client = HTTPClient.instance(url.getHost(), url.getHost(), url.getPort());
//        return client.getBufferedStream(url.getHost(), url.getPath());
        final URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(1000);
        urlConnection.setReadTimeout(1000);
        return new BufferedInputStream(urlConnection.getInputStream());
    }

    @SuppressWarnings({"unchecked"})
    public static class NewsAggregatorXmlParser {
        private static final String ELEMENT_CLUSTER = "cluster";
        private static final String ELEMENT_ENTRY_COLLECTION = "entryCollection";
        private static final String ATTRIBUTE_FULL_COUNT = "fullcount";
        private static final String ATTRIBUTE_CLUSTERID = "id";
        private static final String ELEMENT_RELATED = "related";
        private static final String ATTRIBUTE_TYPE = "type";
        private static final String ELEMENT_CATEGORY = "category";
        private static final String ELEMENT_COLLAPSEID = "collapseid";
        private static final String ELEMENT_GEONAVIGATION = "geonavigation";
        private static final String ATTRIBUTE_NAME = "name";
        private static final String ATTRIBUTE_XML = "xml";

        private Document getDocument(InputStream inputStream) throws JDOMException, IOException {
            final SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setValidation(false);
            saxBuilder.setIgnoringElementContentWhitespace(true);

            return saxBuilder.build(inputStream);
        }

        public SearchResult parseCluster(NewsAggregatorSearchConfiguration config, InputStream inputStream, String clusterId, NewsAggregatorSearchCommand searchCommand) throws JDOMException, IOException {
            try {
                final FastSearchResult searchResult = new FastSearchResult(searchCommand);
                final Document doc = getDocument(inputStream);
                final Element root = doc.getRootElement();
                List<Element> clusters = root.getChildren(ELEMENT_CLUSTER);
                for (Element cluster : clusters) {
                    if (cluster.getAttributeValue(ATTRIBUTE_CLUSTERID).equals(clusterId)) {
                        handleFlatCluster(cluster, searchCommand, searchResult);
                        handleRelated(config, cluster.getChild(ELEMENT_RELATED), searchResult);
                        break;
                    }
                }
                handleGeoNav(root.getChild(ELEMENT_GEONAVIGATION), searchResult);
                return searchResult;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        // Ignoring
                    }
                }
            }
        }

        public SearchResult parseFullPage(NewsAggregatorSearchConfiguration config, InputStream inputStream, SearchCommand searchCommand) throws JDOMException, IOException {
            try {

                final FastSearchResult searchResult = new FastSearchResult(searchCommand);
                final Document doc = getDocument(inputStream);
                final Element root = doc.getRootElement();

                handleClusters(root.getChildren(ELEMENT_CLUSTER), searchResult, searchCommand);
                handleRelated(config, root.getChild(ELEMENT_RELATED), searchResult);
                handleGeoNav(root.getChild(ELEMENT_GEONAVIGATION), searchResult);
                return searchResult;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        // Ignoring
                    }
                }
            }
        }

        private void handleGeoNav(Element geonavElement, FastSearchResult searchResult) {
            final List<Element> geoNavElements = geonavElement.getChildren();
            for (Element geoNavElement : geoNavElements) {
                String navigationType = geoNavElement.getAttributeValue(ATTRIBUTE_TYPE);
                Navigator nav = new Navigator(geoNavElement.getAttributeValue(ATTRIBUTE_XML), null, geoNavElement.getAttributeValue(ATTRIBUTE_NAME), null);
                Modifier modifier = new Modifier(geoNavElement.getAttributeValue(ATTRIBUTE_NAME), -1, nav);
                searchResult.addModifier(navigationType, modifier);
            }
        }

        private void handleRelated(NewsAggregatorSearchConfiguration config, Element relatedElement, FastSearchResult searchResult) {
            final List<Element> categoryElements = relatedElement.getChildren(ELEMENT_CATEGORY);
            for (Element categoryElement : categoryElements) {
                final String categoryType = categoryElement.getAttributeValue(ATTRIBUTE_TYPE);

                final List<Modifier> relatedList = searchResult.getModifiers(categoryType);
                int categoryCount = 0;
                if (relatedList != null) {
                    categoryCount = relatedList.size();
                }
                if (categoryCount < config.getRelatedMaxCount()) {
                    final Modifier modifier = new Modifier(categoryElement.getTextTrim(), -1, null);
                    searchResult.addModifier(categoryType, modifier);
                }
            }
        }

        private void handleClusters(List<Element> clusters, SearchResult searchResult, SearchCommand searchCommand) {
            for (Element cluster : clusters) {
                handleCluster(cluster, searchCommand, searchResult);
            }
        }

        private void handleFlatCluster(Element cluster, SearchCommand searchCommand, SearchResult searchResult) {
            final Element entryCollectionElement = cluster.getChild(ELEMENT_ENTRY_COLLECTION);
            final List<Element> entryList = entryCollectionElement.getChildren();

            final HashMap<String, SearchResultItem> collapseMap = new HashMap<String, SearchResultItem>();
            for (Element entry : entryList) {
                final SearchResultItem searchResultItem = new BasicSearchResultItem();
                handleEntry(entry, searchResultItem);
                addResult(searchResultItem, searchResult, searchCommand, collapseMap);
            }
        }

        private void handleCluster(Element cluster, SearchCommand searchCommand, SearchResult searchResult) {
            final SearchResultItem searchResultItem = new BasicSearchResultItem();
            searchResultItem.addField("size", Integer.toString(Integer.parseInt(cluster.getAttributeValue(ATTRIBUTE_FULL_COUNT)) - 1));
            searchResultItem.addField(PARAM_CLUSTER_ID, cluster.getAttributeValue(ATTRIBUTE_CLUSTERID));

            final Element entryCollectionElement = cluster.getChild(ELEMENT_ENTRY_COLLECTION);
            final List<Element> entryList = entryCollectionElement.getChildren();
            final BasicSearchResult nestedSearchResult = new BasicSearchResult(searchCommand);

            for (int i = 0; i < entryList.size(); i++) {
                Element nestedEntry = entryList.get(i);
                if (i == 0) {
                    // First element is main result
                    handleEntry(nestedEntry, searchResultItem);
                } else {
                    SearchResultItem nestedResultItem = new BasicSearchResultItem();
                    handleEntry(nestedEntry, nestedResultItem);
                    addResult(nestedResultItem, nestedSearchResult, searchCommand);
                }
            }
            searchResultItem.addNestedSearchResult("entries", nestedSearchResult);
            searchResult.addResult(searchResultItem);
        }

        private void addResult(SearchResultItem nestedResultItem,
                               SearchResult nestedSearchResult,
                               SearchCommand searchCommand) {
            addResult(nestedResultItem, nestedSearchResult, searchCommand, null);
        }


        private void addResult(SearchResultItem nestedResultItem,
                               SearchResult nestedSearchResult,
                               SearchCommand searchCommand,
                               HashMap<String, SearchResultItem> collapseMap) {
            // Check if entry is duplicate and should be a subresult
            SearchResultItem collapseParent = null;
            String collapseId = nestedResultItem.getField(ELEMENT_COLLAPSEID);
            if (collapseMap != null) {
                collapseParent = collapseMap.get(collapseId);
            }
            if (collapseParent == null) {
                // No duplicate in results or should not be collapsed
                nestedSearchResult.addResult(nestedResultItem);
                if (collapseMap != null) {
                    collapseMap.put(collapseId, nestedResultItem);
                }
            } else {
                // duplicate item, adding as a subresult to first item.
                SearchResult collapsedResults = collapseParent.getNestedSearchResult("entries");
                if (collapsedResults == null) {
                    collapsedResults = new BasicSearchResult(searchCommand);
                    collapseParent.addNestedSearchResult("entries", collapsedResults);
                }
                collapsedResults.addResult(nestedResultItem);
            }
        }

        private void handleEntry(Element entryElement, SearchResultItem searchResultItem) {
            final List<Element> entrySubElements = entryElement.getChildren();
            for (Element entrySubElement : entrySubElements) {
                if (entrySubElement.getText() != null && entrySubElement.getTextTrim().length() > 0) {
                    searchResultItem.addField(entrySubElement.getName(), entrySubElement.getTextTrim());
                }
            }
        }


    }

}
