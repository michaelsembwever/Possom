// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.mode.config.NewsAggregatorSearchConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.NewsAggregatorSearchResult;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.HashMap;
import java.net.URL;
import no.schibstedsok.searchportal.http.HTTPClient;


public class NewsAggregatorSearchCommand extends AbstractSearchCommand {

    private final static Logger LOG = Logger.getLogger(NewsAggregatorSearchCommand.class);

    /**
     * @param cxt        The context to execute in.
     * @param dataModel  The dataModel to use.
     */
    public NewsAggregatorSearchCommand(Context cxt, DataModel dataModel) {
        super(cxt, dataModel);
    }

    public SearchResult execute() {
        LOG.debug("News aggregator search executed with: " + getParameters());
        LOG.debug("News aggregator search executed with: " + datamodel.getParameters());

        NewsAggregatorSearchConfiguration config = (NewsAggregatorSearchConfiguration) getSearchConfiguration();
        LOG.debug("Loading xml file at: " + config.getXmlSource());
        LOG.debug("Update interval: " + config.getUpdateIntervalMinutes());

        StringDataObject geoNav = datamodel.getParameters().getValue("geonav");
        if (geoNav == null) {
            return getFrontPageResult(config);
        } else {
            return getPageResult(config, geoNav.getString());
        }
    }


    private SearchResult getFrontPageResult(NewsAggregatorSearchConfiguration config) {
        return getPageResult(config, config.getXmlMainFile());
    }
    private SearchResult getPageResult(NewsAggregatorSearchConfiguration config, String xmlFile) {
        try {
            final NewsAggregatorXmlParser newsAggregatorXmlParser = new NewsAggregatorXmlParser();

            final URL url = new URL(config.getXmlSource() + xmlFile);
            
            final HTTPClient client = HTTPClient.instance(url.getHost(), url.getHost(), url.getPort());
            
            return newsAggregatorXmlParser.parse(client.getBufferedStream(url.getHost(), url.getPath()), this);
            
        } catch (JDOMException e) {
            LOG.error("Could not parse xml: " + config.getXmlSource(), e);
        } catch (IOException e) {
            LOG.error("Could not parse xml: " + config.getXmlSource(), e);
        }
        return new BasicSearchResult(null);
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

        public SearchResult parse(InputStream inputStream, SearchCommand searchCommand) throws JDOMException, IOException {
            try {
                final NewsAggregatorSearchResult searchResult = new NewsAggregatorSearchResult(searchCommand);

                final SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setValidation(false);
                saxBuilder.setIgnoringElementContentWhitespace(true);

                final Document doc = saxBuilder.build(inputStream);
                final Element root = doc.getRootElement();

                handleClusters(root.getChildren(ELEMENT_CLUSTER), searchResult, searchCommand);
                handleRelated(root.getChild(ELEMENT_RELATED), searchResult);
                handleGeoNav(root.getChild(ELEMENT_GEONAVIGATION), searchResult);
                return searchResult;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch(Exception e) {
                        // Ignoring
                    }
                }
            }
        }

        private void handleGeoNav(Element geonavElement, NewsAggregatorSearchResult searchResult) {
            final List<Element> geoNavElements = geonavElement.getChildren();
            for (Element geoNavElement : geoNavElements) {
                String navigationType = geoNavElement.getAttributeValue(ATTRIBUTE_TYPE);
                NewsAggregatorSearchResult.Navigation navigation = new NewsAggregatorSearchResult.Navigation(
                        navigationType,
                        geoNavElement.getAttributeValue(ATTRIBUTE_NAME),
                        geoNavElement.getAttributeValue(ATTRIBUTE_XML));
                searchResult.addNavigation(navigationType, navigation);
            }
        }

        private void handleRelated(Element relatedElement, NewsAggregatorSearchResult searchResult) {
            final List<Element> categoryElements = relatedElement.getChildren(ELEMENT_CATEGORY);
            for (Element categoryElement : categoryElements) {
                final String categoryType = categoryElement.getAttributeValue(ATTRIBUTE_TYPE);
                final SearchResultItem searchResultItem = new BasicSearchResultItem();
                searchResultItem.addField(ATTRIBUTE_NAME, categoryElement.getTextTrim());
                searchResult.addRelatedResultItem(categoryType, searchResultItem);
            }            
        }

        private void handleClusters(List<Element> clusters, SearchResult searchResult, SearchCommand searchCommand) {
            for (Element cluster : clusters) {
                final SearchResultItem searchResultItem = new BasicSearchResultItem();
                searchResultItem.addField("size", cluster.getAttributeValue(ATTRIBUTE_FULL_COUNT));
                searchResultItem.addField("clusterId", cluster.getAttributeValue(ATTRIBUTE_CLUSTERID));

                final Element entryCollectionElement = cluster.getChild(ELEMENT_ENTRY_COLLECTION);

                final HashMap<String, SearchResultItem> collapseMap = new HashMap<String, SearchResultItem>();
                final List<Element> entryList = entryCollectionElement.getChildren();
                final BasicSearchResult nestedSearchResult = new BasicSearchResult(searchCommand);
                for (int i = 0; i < entryList.size(); i++) {
                    Element nestedEntry =  entryList.get(i);
                    if (i == 0) {
                        // First element is main result
                        handleEntry(nestedEntry, searchResultItem);
                        
                    } else {
                        SearchResultItem nestedResultItem = new BasicSearchResultItem();
                        handleEntry(nestedEntry, nestedResultItem);
                        addResult(nestedResultItem, collapseMap, nestedSearchResult, searchCommand);
                    }
                }
                searchResultItem.addNestedSearchResult("entries", nestedSearchResult);
                searchResult.addResult(searchResultItem);
            }
        }

        private void addResult(SearchResultItem nestedResultItem, HashMap<String, SearchResultItem> collapseMap, SearchResult nestedSearchResult, SearchCommand searchCommand) {
            // Check if entry is duplicate and should be a subresult
            String collapseId = nestedResultItem.getField(ELEMENT_COLLAPSEID);
            SearchResultItem collapseParent = collapseMap.get(collapseId);
            if (collapseParent == null) {
                // No duplicate in results
                nestedSearchResult.addResult(nestedResultItem);
                collapseMap.put(collapseId, nestedResultItem);
            } else {
                // duplicat item, adding as a subresult to first item.
                SearchResult collapsedResults = collapseParent.getNestedSearchResult("collapsedResults");
                if (collapsedResults == null) {
                    collapsedResults = new BasicSearchResult(searchCommand);
                    collapseParent.addNestedSearchResult("collapsedResults", collapsedResults);
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
