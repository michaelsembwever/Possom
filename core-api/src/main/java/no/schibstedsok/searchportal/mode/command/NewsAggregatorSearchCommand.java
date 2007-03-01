package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
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
import java.net.URL;
import java.net.URLConnection;

@SuppressWarnings({"unchecked"})
public class NewsAggregatorSearchCommand extends AbstractSearchCommand {

    private final static Logger log = Logger.getLogger(NewsAggregatorSearchCommand.class);

    /**
     * @param cxt        The context to execute in.
     * @param dataModel  The dataModel to use.
     */
    public NewsAggregatorSearchCommand(Context cxt, DataModel dataModel) {
        super(cxt, dataModel);
    }

    public SearchResult execute() {
        log.debug("News aggregator search executed with: " + getParameters());
        log.debug("News aggregator search executed with: " + datamodel.getParameters());

        NewsAggregatorSearchConfiguration config = (NewsAggregatorSearchConfiguration) getSearchConfiguration();
        log.debug("Loading xml file at: " + config.getXmlSource());
        log.debug("Update interval: " + config.getUpdateIntervalMinutes());

        return getFrontPageResult(config);
    }

    private SearchResult getFrontPageResult(NewsAggregatorSearchConfiguration config) {
        try {
            NewsAggregatorXmlParser newsAggregatorXmlParser = new NewsAggregatorXmlParser();

            URL url = new URL(config.getXmlSource());
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(1000);            
            return newsAggregatorXmlParser.parse(urlConnection.getInputStream(), this);
        } catch (JDOMException e) {
            log.error("Could not parse xml: " + config.getXmlSource(), e);
        } catch (IOException e) {
            log.error("Could not parse xml: " + config.getXmlSource(), e);
        }
        return new BasicSearchResult(null);
    }


    public static class NewsAggregatorXmlParser {
        private static final String ELEMENT_CLUSTER = "cluster";
        private static final String ELEMENT_ENTRY_COLLECTION = "entryCollection";
        private static final String ATTRIBUTE_FULL_COUNT = "fullcount";
        private static final String ATTRIBUTE_CLUSTERID = "id";
        private static final String ELEMENT_RELATED = "related";
        private static final String ATTRIBUTE_CATEGORY_TYPE = "type";
        private static final String ELEMENT_CATEGORY = "category";
        private static final String ATTRIBUTE_CATEGORY_ID = "id";

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

        private void handleRelated(Element relatedElement, NewsAggregatorSearchResult searchResult) {
            final List<Element> categoryElements = relatedElement.getChildren(ELEMENT_CATEGORY);
            for (Element categoryElement : categoryElements) {
                final String categoryType = categoryElement.getAttributeValue(ATTRIBUTE_CATEGORY_TYPE);
                final SearchResultItem searchResultItem = new BasicSearchResultItem();
                searchResultItem.addField("name", categoryElement.getTextTrim());
                searchResult.addRelatedResultItem(categoryType, searchResultItem);
            }            
        }

        private void handleClusters(List<Element> clusters, SearchResult searchResult, SearchCommand searchCommand) {
            for (Element cluster : clusters) {
                final SearchResultItem searchResultItem = new BasicSearchResultItem();
                searchResultItem.addField("size", cluster.getAttributeValue(ATTRIBUTE_FULL_COUNT));
                searchResultItem.addField("clusterId", cluster.getAttributeValue(ATTRIBUTE_CLUSTERID));

                final Element entryCollectionElement = cluster.getChild(ELEMENT_ENTRY_COLLECTION);
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
                        nestedSearchResult.addResult(nestedResultItem);
                    }
                }
                searchResultItem.addNestedSearchResult("entries", nestedSearchResult);
                searchResult.addResult(searchResultItem);
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
