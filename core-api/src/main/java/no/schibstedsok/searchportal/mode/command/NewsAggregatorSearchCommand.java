// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.mode.config.NewsAggregatorCommandConfig;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.Navigator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * Search command that will try to get pregenerated clusters from xml files. If the xml file is not available it will
 * fall back to a search.
 * 
 * @author
 * @version $Id$
 */
public final class NewsAggregatorSearchCommand extends ClusteringESPFastCommand {

    private static final Logger LOG = Logger.getLogger(NewsAggregatorSearchCommand.class);
    private static final String PARAM_CLUSTER_ID = "clusterId";

    /**
     * @param cxt The context to execute in.
     */
    public NewsAggregatorSearchCommand(final Context cxt) {
        super(cxt);
    }

    public ResultList<? extends ResultItem> execute() {
        NewsAggregatorCommandConfig config = getSearchConfiguration();
        StringDataObject clusterId = datamodel.getParameters().getValue(PARAM_CLUSTER_ID);
        String xmlFile = getXmlFileName(datamodel, config);
        LOG.debug("Loading xml file at: " + config.getXmlSource() + xmlFile);
        if (clusterId == null) {
            return getPageResult(config, xmlFile);
        } else {
            return getClusterResult(config, clusterId, xmlFile);
        }
    }

    private String getXmlFileName(final DataModel dataModel, final NewsAggregatorCommandConfig config) {
        String geographic = "main";
        String category = "main";
        String[] geographicFields = config.getGeographicFieldArray();
        for (String geographicField : geographicFields) {
            StringDataObject geo = dataModel.getParameters().getValue(geographicField);
            if (geo != null) {
                geographic = formatToConvention(geo.getString());
                break;
            }
        }

        for (String categoryField : config.getCategoryFieldArray()) {
            StringDataObject cat = dataModel.getParameters().getValue(categoryField);
            if (cat != null) {
                category = formatToConvention(cat.getString());
                break;
            }
        }
        StringBuilder sb = new StringBuilder("fp_");
        sb.append(category).append('_').append(geographic).append(".xml");
        return sb.toString();
    }

    private String formatToConvention(String replaceString) {
        String newString = StringUtils.replaceChars(replaceString.toLowerCase(), "\u00E6", "ae");
        newString = StringUtils.replaceChars(newString, '\u00F8', 'o');
        newString = StringUtils.replaceChars(newString, '\u00E5', 'a');
        newString = StringUtils.replaceChars(newString, ' ', '_');
        return newString;
    }

    private ResultList<? extends ResultItem> getClusterResult(
            final NewsAggregatorCommandConfig config, 
            final StringDataObject clusterId, 
            final String xmlFile) {
        
        ResultList<? extends ResultItem> searchResult;
        try {
            final NewsAggregatorXmlParser newsAggregatorXmlParser = new NewsAggregatorXmlParser();
            final InputStream inputStream = getInputStream(config, xmlFile);
            final StringDataObject sortObject = datamodel.getParameters().getValue(config.getUserSortParameter());
            final String sort = sortObject == null ? null : sortObject.getString();
            searchResult = newsAggregatorXmlParser.parseCluster(config, inputStream, clusterId.getString(), getOffset(), sort, this);
            addSortModifiers((FastSearchResult) searchResult, config.getUserSortParameter(), "relevance", "descending", "ascending");
            if (searchResult != null && searchResult.getHitCount() > 0) {
                return searchResult;
            }
        } catch (IOException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        } catch (JDOMException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        }
        searchResult = search(config, clusterId.getString());
        if (searchResult instanceof FastSearchResult) {
            addSortModifiers((FastSearchResult) searchResult, config.getUserSortParameter(), "descending", "ascending");
        }
        return searchResult;
    }

    private void addSortModifiers(
            final FastSearchResult searchResult, 
            final String id, 
            final String... modifierNames) {
        
        for (String modifierName : modifierNames) {
            searchResult.addModifier(id, new Modifier(modifierName, -1, null));
        }
    }

    private ResultList<? extends ResultItem> search(
            final NewsAggregatorCommandConfig config, 
            final String clusterId) {
        
        LOG.debug("------ Running search to get clusters ---------");
        LOG.debug("clusterId=" + clusterId);
        LOG.debug("result-fields=" + config.getResultFields());
        LOG.debug("query-server=" + config.getQueryServer());
        LOG.debug("-----------------------------------------------");
        return super.execute();
    }

    @Override
    public NewsAggregatorCommandConfig getSearchConfiguration() {
        return (NewsAggregatorCommandConfig) super.getSearchConfiguration();
    }


    private ResultList<? extends ResultItem> getPageResult(
            final NewsAggregatorCommandConfig config, 
            final String xmlFile) {
        
        final NewsAggregatorXmlParser newsAggregatorXmlParser = new NewsAggregatorXmlParser();
        ResultList<? extends ResultItem> searchResult;
        try {
            searchResult = newsAggregatorXmlParser.parseFullPage(config, getOffset(), getInputStream(config, xmlFile), this);
            addSortModifiers((FastSearchResult) searchResult, config.getUserSortParameter(), "descending", "ascending");
            if (searchResult != null && searchResult.getHitCount() > 0) {
                return searchResult;
            }
        } catch (JDOMException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        } catch (IOException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        }
        searchResult = search(config, null);
        if (searchResult instanceof FastSearchResult) {
            addSortModifiers((FastSearchResult) searchResult, config.getUserSortParameter(), "descending", "ascending");
        }
        return searchResult;
    }

    private InputStream getInputStream(
            final NewsAggregatorCommandConfig config, 
            final String xmlFile) throws IOException {
        
        final URL url = new URL(config.getXmlSource() + xmlFile);

        final HTTPClient client = HTTPClient.instance(url.getHost(), url.getPort());
        try{
            return client.getBufferedStream(url.getPath());
            
        }catch (IOException ex) {
            throw client.interceptIOException(ex);
        }
    }

    /**
     * 
     */
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
        private static final String ATTRIBUTE_TIMESTAMP = "timestamp";
        private static final String ATTRIBUTE_NAME = "name";
        private static final String ATTRIBUTE_XML = "xml";
        private static final String ELEMENT_COUNTS = "counts";
        private static final String ATTRIBUTE_ENTRY_COUNT = "entries";
        private static final String ATTRIBUTE_CLUSTER_COUNT = "clusters";

        private Document getDocument(final InputStream inputStream) throws JDOMException, IOException {
            final SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setValidation(false);
            saxBuilder.setIgnoringElementContentWhitespace(true);

            return saxBuilder.build(inputStream);
        }

        /**
         * 
         * @param config 
         * @param inputStream 
         * @param clusterId 
         * @param offset 
         * @param sort 
         * @param searchCommand 
         * @return 
         * @throws org.jdom.JDOMException 
         * @throws java.io.IOException 
         */
        public FastSearchResult parseCluster(
                final NewsAggregatorCommandConfig config, 
                final InputStream inputStream, 
                final String clusterId, 
                final int offset, 
                final String sort, 
                final NewsAggregatorSearchCommand searchCommand) throws JDOMException, IOException {
            
            try {
                LOG.debug("Parsing cluster: " + clusterId);
                // following will either throw a ClassCastException or NPE
                final FastSearchResult<ResultItem> searchResult = new FastSearchResult<ResultItem>(null);
                final Document doc = getDocument(inputStream);
                final Element root = doc.getRootElement();
                List<Element> clusters = root.getChildren(ELEMENT_CLUSTER);
                for (Element cluster : clusters) {
                    if (cluster.getAttributeValue(ATTRIBUTE_CLUSTERID).equals(clusterId)) {
                        handleFlatCluster(config, cluster, searchCommand, searchResult, offset, sort);
                        handleRelated(config, cluster.getChild(ELEMENT_RELATED), searchResult);
                        break;
                    }
                }
//                handleGeoNav(root.getChild(ELEMENT_GEONAVIGATION), searchResult);
                return searchResult;
                
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        LOG.trace(e.getMessage(), e);
                    }
                }
            }
        }

        /**
         * 
         * @param config 
         * @param offset 
         * @param inputStream 
         * @param searchCommand 
         * @return 
         * @throws org.jdom.JDOMException 
         * @throws java.io.IOException 
         */
        public FastSearchResult parseFullPage(
                final NewsAggregatorCommandConfig config, 
                final int offset, 
                final InputStream inputStream, 
                final SearchCommand searchCommand) throws JDOMException, IOException {
            
            try {

                // following will throw a ClassCastException or NPE
                final FastSearchResult<ResultItem> searchResult = new FastSearchResult<ResultItem>(null);
                final Document doc = getDocument(inputStream);
                final Element root = doc.getRootElement();

                handleClusters(config, offset, root.getChildren(ELEMENT_CLUSTER), searchResult, searchCommand);
                handleCounts(config, root.getChild(ELEMENT_COUNTS), offset, searchResult);
                handleRelated(config, root.getChild(ELEMENT_RELATED), searchResult);
//                handleGeoNav(root.getChild(ELEMENT_GEONAVIGATION), searchResult);
                return searchResult;
                
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        LOG.trace(e.getMessage(), e);
                    }
                }
            }
        }

        private void handleCounts(
                final NewsAggregatorCommandConfig config, 
                final Element countsElement, 
                final int offset, 
                final FastSearchResult searchResult) {
            
            if (countsElement != null) {
                final String entries = countsElement.getAttributeValue(ATTRIBUTE_ENTRY_COUNT);
                if (entries != null && entries.length() > 0) {
                    searchResult.setHitCount(Integer.parseInt(entries));
                }
                final String clusters = countsElement.getAttributeValue(ATTRIBUTE_CLUSTER_COUNT);
                if (clusters != null && clusters.length() > 0) {
                    if (offset + config.getResultsToReturn() < Integer.parseInt(clusters)) {
                        addNextOffsetField(offset + config.getResultsToReturn(), searchResult);
                    }
                }
            }
        }

        private void handleGeoNav(final Element geonavElement, final FastSearchResult searchResult) {
            if (geonavElement != null) {
                final List<Element> geoNavElements = geonavElement.getChildren();
                for (Element geoNavElement : geoNavElements) {
                    String navigationType = geoNavElement.getAttributeValue(ATTRIBUTE_TYPE);
                    Navigator nav = new Navigator(geoNavElement.getAttributeValue(ATTRIBUTE_XML), null, geoNavElement.getAttributeValue(ATTRIBUTE_NAME), null);
                    Modifier modifier = new Modifier(geoNavElement.getAttributeValue(ATTRIBUTE_NAME), -1, nav);
                    searchResult.addModifier(navigationType, modifier);
                }
            }
        }

        private void handleRelated(
                final NewsAggregatorCommandConfig config, 
                final Element relatedElement, 
                final FastSearchResult searchResult) {
            
            if (relatedElement != null) {
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
        }

        private void handleClusters(
                final NewsAggregatorCommandConfig config, 
                final int offset, 
                final List<Element> clusters, 
                final ResultList<ResultItem> searchResult, 
                final SearchCommand searchCommand) {
            
            int maxOffset = offset + config.getResultsToReturn();
            for (int i = offset; i < clusters.size() && i < maxOffset; i++) {
                Element cluster = clusters.get(i);
                handleCluster(config, cluster, searchCommand, searchResult);
            }
        }

        private void handleFlatCluster(
                final NewsAggregatorCommandConfig config, 
                final Element cluster, 
                final SearchCommand searchCommand, 
                final ResultList<ResultItem> searchResult, 
                int offset, 
                final String sort) {
            
            if (cluster != null) {
                final Element entryCollectionElement = cluster.getChild(ELEMENT_ENTRY_COLLECTION);
                if (entryCollectionElement != null) {
                    final List<Element> entryList = entryCollectionElement.getChildren();
                    searchResult.setHitCount(entryList.size());
                    final Map<String, ResultList<ResultItem>> collapseMap 
                            = new HashMap<String, ResultList<ResultItem>>();
                    
                    final ResultList<ResultItem> tmpSearchResult = new BasicSearchResult<ResultItem>();
                    // Collecting all results from xml. (This must be done if we want correct collpsing funtionality
                    for (Element entry : entryList) {
                        final ResultList<ResultItem> searchResultItem = new BasicSearchResult<ResultItem>();
                        handleEntry(entry, searchResultItem);
                        addResult(config, searchResultItem, tmpSearchResult, collapseMap, true);
                    }
                    sortResults(tmpSearchResult, sort);
                    offset = config.isIgnoreOffset() ? 0 : offset;
                    int lastIndex = Math.min(tmpSearchResult.getResults().size(), offset + config.getResultsToReturn());
                    for (int i = offset; i < lastIndex; i++) {
                        searchResult.addResult(tmpSearchResult.getResults().get(i));
                    }

                    if ((offset + config.getResultsToReturn()) < tmpSearchResult.getResults().size()) {
                        addNextOffsetField(offset + config.getResultsToReturn(), searchResult);
                    }
                }
            }
        }

        private void sortResults(final ResultList<ResultItem> searchResult, final String sort) {
            
            if ("ascending".equals(sort)) {
                searchResult.sortResults(DateFieldSearchResultComparator.getInstance());
                
            } else if ("descending".equals(sort)) {
                
                searchResult.sortResults(Collections.reverseOrder(DateFieldSearchResultComparator.getInstance()));
            }
        }

        private int handleCluster(
                final NewsAggregatorCommandConfig config, 
                final Element cluster, 
                final SearchCommand searchCommand, 
                final ResultList<ResultItem> searchResult) {
            
            if (cluster != null) {
                ResultList<ResultItem> searchResultItem = new BasicSearchResult<ResultItem>();
                searchResultItem = searchResultItem.addField(
                        "size", 
                        Integer.toString(Integer.parseInt(cluster.getAttributeValue(ATTRIBUTE_FULL_COUNT)) - 1));
                
                searchResultItem = searchResultItem.addField(
                        PARAM_CLUSTER_ID, 
                        cluster.getAttributeValue(ATTRIBUTE_CLUSTERID));

                final Element entryCollectionElement = cluster.getChild(ELEMENT_ENTRY_COLLECTION);
                final List<Element> entryList = entryCollectionElement.getChildren();
                final ResultList<ResultItem> nestedSearchResult = new BasicSearchResult<ResultItem>();

                for (int i = 0; i < entryList.size(); i++) {
                    final Element nestedEntry = entryList.get(i);
                    if (i == 0) {
                        // First element is main result
                        // First element is main result
                        searchResultItem = (ResultList<ResultItem>) handleEntry(nestedEntry, searchResultItem);
                    } else {
                        ResultList<ResultItem> nestedResultItem = new BasicSearchResult<ResultItem>();
                        nestedResultItem = (ResultList<ResultItem>) handleEntry(nestedEntry, nestedResultItem);
                        addResult(config, nestedResultItem, nestedSearchResult);
                    }
                }
                
                searchResultItem.addResult(nestedSearchResult);
                
                searchResult.addResult(searchResultItem);
                nestedSearchResult.setHitCount(entryList.size());
                return entryList.size();
            }
            return 0;
        }


        private ResultItem handleEntry(final Element entryElement, ResultItem searchResultItem) {
            
            final List<Element> entrySubElements = entryElement.getChildren();
            for (Element entrySubElement : entrySubElements) {
                if (entrySubElement.getText() != null && entrySubElement.getTextTrim().length() > 0) {
                    searchResultItem 
                            = searchResultItem.addField(entrySubElement.getName(), entrySubElement.getTextTrim());
                }
            }
            return searchResultItem;
        }

        private void addResult(
                final NewsAggregatorCommandConfig config, 
                final ResultList<ResultItem> nestedResultItem,
                final ResultList<ResultItem> nestedSearchResult) {
            
            addResult(config, nestedResultItem, nestedSearchResult, null, false);
        }


        private boolean addResult(final NewsAggregatorCommandConfig config,
                                  final ResultList<ResultItem> nestedResultItem,
                                  final ResultList<ResultItem> nestedSearchResult,
                                  final Map<String, ResultList<ResultItem>> collapseMap,
                                  final boolean noMax) {
            
            // Check if entry is duplicate and should be a subresult
            ResultList<ResultItem> collapseParent = null;
            String collapseId = nestedResultItem.getField(ELEMENT_COLLAPSEID);
            if (collapseMap != null) {
                collapseParent = collapseMap.get(collapseId);
            }
            if (collapseParent == null) {
                // Skipping add if max returned results has been reached.
                if (noMax || nestedSearchResult.getResults().size() < config.getResultsToReturn()) {
                    // No duplicate in results or should not be collapsed
                    nestedSearchResult.addResult(nestedResultItem);
                    if (collapseMap != null) {
                        collapseMap.put(collapseId, nestedResultItem);
                    }
                    return true;
                }
                return false;
            } else {
                // duplicate item, adding as a subresult to first item.
                ResultList<ResultItem> collapsedResults = collapseParent;
                
                if (collapsedResults == null) {
                    collapsedResults = new BasicSearchResult<ResultItem>();
                    collapseParent.addResult(collapsedResults);
                }
                collapsedResults.addResult(nestedResultItem);
                return true;
            }
        }
    }

    private static final class DateFieldSearchResultComparator implements Comparator<ResultItem> {
        
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        private static final String DATE_FIELD_NAME = NewsAggregatorXmlParser.ATTRIBUTE_TIMESTAMP;
        private static DateFieldSearchResultComparator myInstance = new DateFieldSearchResultComparator();

        public static DateFieldSearchResultComparator getInstance() {
            return myInstance;
        }

        private DateFieldSearchResultComparator() {
        }

        public int compare(final ResultItem resultItem1, final ResultItem resultItem2) {
            
            final String dateField1 = resultItem1.getField(DATE_FIELD_NAME);
            final String dateField2 = resultItem2.getField(DATE_FIELD_NAME);
            if (dateField1 == null || dateField1.length() == 0) {
                if (dateField2 == null || dateField2.length() == 0) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                if (dateField2 == null || dateField2.length() == 0) {
                    return 1;
                } else {
                    try {
                        final Date date1 = sdf.parse(dateField1);
                        final Date date2 = sdf.parse(dateField2);
                        if (date1.before(date2)) {
                            return -1;
                        } else if (date1.after(date2)) {
                            return 1;
                        }
                    } catch (ParseException e) {
                        LOG.error("Could not parse date field, sort will not work.", e);
                    }
                    return 0;
                }
            }
        }
    }
}
