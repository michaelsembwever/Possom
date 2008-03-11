/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
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

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.http.HTTPClient;
import no.sesat.search.mode.config.NewsAggregatorCommandConfig;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.Modifier;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search command that will try to get pregenerated clusters from xml files. If the xml file is not available it will
 * fall back to a search.
 *
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public final class NewsAggregatorSearchCommand extends NewsClusteringESPFastCommand {

    private static final Logger LOG = Logger.getLogger(NewsAggregatorSearchCommand.class);
    private static final String PARAM_CLUSTER_ID = "clusterId";

    /**
     * @param cxt The context to execute in.
     */
    public NewsAggregatorSearchCommand(final Context cxt) {
        super(cxt);
    }

    public ResultList<? extends ResultItem> execute() {
        final NewsAggregatorCommandConfig config = getSearchConfiguration();
        final StringDataObject clusterId = datamodel.getParameters().getValue(PARAM_CLUSTER_ID);
        final String xmlUrl = getXmlUrlString(datamodel, config);
        LOG.debug("Loading xml file at: " + xmlUrl);
        if (clusterId == null) {
            return getPageResult(config, xmlUrl);
        } else {
            return getClusterResult(config, clusterId, xmlUrl);
        }
    }

    private String getXmlUrlString(final DataModel dataModel, final NewsAggregatorCommandConfig config) {
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
        StringBuilder sb = new StringBuilder(config.getXmlSource());
        sb.append("fp_");
        sb.append(category).append('_').append(geographic).append(".xml");
        return sb.toString();
    }

    private String formatToConvention(String replaceString) {
        String newString = StringUtils.replaceChars(replaceString.toLowerCase(), "\u00E6", "ae");
        newString = StringUtils.replaceChars(newString, '\u00F8', 'o');
        newString = StringUtils.replaceChars(newString, '\u00E5', 'a');
        newString = StringUtils.replaceChars(newString, "\u00E4", "ae");
        newString = StringUtils.replaceChars(newString, '\u00F6', 'o');
        newString = StringUtils.replaceChars(newString, ' ', '_');
        return newString;
    }

    private ResultList<? extends ResultItem> getClusterResult(
            final NewsAggregatorCommandConfig config,
            final StringDataObject clusterId,
            final String xmlUrl) {

        ResultList<? extends ResultItem> searchResult;
        try {
            final NewsAggregatorXmlParser newsAggregatorXmlParser = new NewsAggregatorXmlParser();
            final StringDataObject sortObject = datamodel.getParameters().getValue(config.getUserSortParameter());
            final String sort = sortObject == null ? null : sortObject.getString();
            searchResult = newsAggregatorXmlParser.parseCluster(config, xmlUrl, clusterId.getString(), getOffset(), sort);
            addSortModifiers((FastSearchResult) searchResult, config.getUserSortParameter(), "relevance", "descending", "ascending");
            if (searchResult != null && searchResult.getHitCount() > 0) {
                return searchResult;
            }
        } catch (IOException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        } catch (SAXException e) {
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
            final String xmlUrl) {

        final NewsAggregatorXmlParser newsAggregatorXmlParser = new NewsAggregatorXmlParser();
        ResultList<? extends ResultItem> searchResult;
        try {
            searchResult = newsAggregatorXmlParser.parseFullPage(config, getOffset(), xmlUrl);
            addSortModifiers((FastSearchResult) searchResult, config.getUserSortParameter(), "descending", "ascending");
            if (searchResult != null && searchResult.getHitCount() > 0) {
                return searchResult;
            }
        } catch (IOException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        } catch (SAXException e) {
            LOG.debug("Falling back to search instead of xml parse", e);
        }
        searchResult = search(config, null);
        if (searchResult instanceof FastSearchResult) {
            addSortModifiers((FastSearchResult) searchResult, config.getUserSortParameter(), "descending", "ascending");
        }
        return searchResult;
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
        private static final String ELEMENT_COUNTS = "counts";
        private static final String ATTRIBUTE_ENTRY_COUNT = "entries";
        private static final String ATTRIBUTE_CLUSTER_COUNT = "clusters";
        private static final String ELEMENT_ENTRY = "entry";

        private Document getDocument(String urlString) throws IOException, SAXException {
            final URL url = new URL(urlString);
            final HTTPClient httpClient = HTTPClient.instance(url);
            return httpClient.getXmlDocument(url.getPath());
        }

        /**
         * Parses a specific identified cluster
         *
         * @param config    the commandConfig
         * @param xmlUrl    the url to the xml containing the cluster
         * @param clusterId the id of the cluster to parse
         * @param offset    the offset into the cluster where to start returning results
         * @param sort      the sortdirection for the result
         * @return the parsed result
         * @throws org.xml.sax.SAXException if the undelying saxpaser throws an exception
         * @throws java.io.IOException      if the file could not be read for some reason
         */
        public FastSearchResult<ResultItem> parseCluster(
                final NewsAggregatorCommandConfig config,
                final String xmlUrl,
                final String clusterId,
                final int offset,
                final String sort) throws IOException, SAXException {

            LOG.debug("Parsing cluster: " + clusterId);
            // following will either throw a ClassCastException or NPE
            final FastSearchResult<ResultItem> searchResult = new FastSearchResult<ResultItem>();
            final Document doc = getDocument(xmlUrl);
            final Element root = doc.getDocumentElement();
            List<Element> clusters = getDirectChildren(root, ELEMENT_CLUSTER);
            for (Element cluster : clusters) {
                if (cluster.getAttribute(ATTRIBUTE_CLUSTERID).equals(clusterId)) {
                    handleFlatCluster(config, cluster, searchResult, offset, sort);
                    handleRelated(config, getFirstChild(cluster, ELEMENT_RELATED), searchResult);
                    break;
                }
            }
            return searchResult;
        }

        /**
         * Parses a full summary xml page.
         *
         * @param xmlUrl the urel to the page to parse
         * @param config the commandConfig
         * @param offset what cluster to start parsing at
         * @return the result of the parse
         * @throws org.xml.sax.SAXException if the undelying saxpaser throws an exception
         * @throws java.io.IOException      if the file could not be read for some reason
         */
        public FastSearchResult<ResultItem> parseFullPage(

                final NewsAggregatorCommandConfig config,
                final int offset,
                final String xmlUrl) throws IOException, SAXException {
            // following will throw a ClassCastException or NPE
            final FastSearchResult<ResultItem> searchResult = new FastSearchResult<ResultItem>();
            final Document doc = getDocument(xmlUrl);
            final Element root = doc.getDocumentElement();

            handleClusters(config, offset, getDirectChildren(root, ELEMENT_CLUSTER), searchResult);
            handleCounts(config, getFirstChild(root, ELEMENT_COUNTS), offset, searchResult);
            handleRelated(config, getFirstChild(root, ELEMENT_RELATED), searchResult);
            return searchResult;
        }

        private void handleCounts(
                final NewsAggregatorCommandConfig config,
                final Element countsElement,
                final int offset,
                final FastSearchResult searchResult) {

            if (countsElement != null) {
                final String entries = countsElement.getAttribute(ATTRIBUTE_ENTRY_COUNT);
                if (entries != null && entries.length() > 0) {
                    searchResult.setHitCount(Integer.parseInt(entries));
                }
                final String clusters = countsElement.getAttribute(ATTRIBUTE_CLUSTER_COUNT);
                if (clusters != null && clusters.length() > 0) {
                    if (offset + config.getResultsToReturn() < Integer.parseInt(clusters)) {
                        addNextOffsetField(offset + config.getResultsToReturn(), searchResult);
                    }
                }
            }
        }

        private void handleRelated(
                final NewsAggregatorCommandConfig config,
                final Element relatedElement,
                final FastSearchResult searchResult) {

            if (relatedElement != null) {
                final List<Element> categoryElements = getDirectChildren(relatedElement, ELEMENT_CATEGORY);
                for (Element categoryElement : categoryElements) {
                    final String categoryType = categoryElement.getAttribute(ATTRIBUTE_TYPE);

                    final List<Modifier> relatedList = searchResult.getModifiers(categoryType);
                    int categoryCount = 0;
                    if (relatedList != null) {
                        categoryCount = relatedList.size();
                    }
                    if (categoryCount < config.getRelatedMaxCount()) {
                        final Modifier modifier = new Modifier(categoryElement.getTextContent().trim(), -1, null);
                        searchResult.addModifier(categoryType, modifier);
                    }
                }
            }
        }

        private void handleClusters(
                final NewsAggregatorCommandConfig config,
                final int offset,
                final List<Element> clusters,
                final ResultList<ResultItem> searchResult) {

            int maxOffset = offset + config.getResultsToReturn();
            for (int i = offset; i < clusters.size() && i < maxOffset; i++) {
                Element cluster = clusters.get(i);
                handleCluster(config, cluster, searchResult);
            }
        }

        private void handleFlatCluster(
                final NewsAggregatorCommandConfig config,
                final Element cluster,
                final ResultList<ResultItem> searchResult,
                int offset,
                final String sort) {

            if (cluster != null) {
                final Element entryCollectionElement = getFirstChild(cluster, ELEMENT_ENTRY_COLLECTION);
                if (entryCollectionElement != null) {
                    final List<Element> entryList = getDirectChildren(entryCollectionElement, ELEMENT_ENTRY);
                    searchResult.setHitCount(entryList.size());
                    final Map<String, ResultList<ResultItem>> collapseMap
                            = new HashMap<String, ResultList<ResultItem>>();

                    final ResultList<ResultItem> tmpSearchResult = new BasicResultList<ResultItem>();
                    // Collecting all results from xml. (This must be done if we want correct collpsing funtionality
                    for (Element entry : entryList) {
                        final ResultList<ResultItem> searchResultItem = new BasicResultList<ResultItem>();
                        handleEntry(entry, searchResultItem);
                        addResult(config, searchResultItem, tmpSearchResult, collapseMap, true);
                    }
                    sortResults(tmpSearchResult, sort);
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
                final ResultList<ResultItem> searchResult) {

            if (cluster != null) {
                ResultList<ResultItem> clusterResult = new BasicResultList<ResultItem>();
                clusterResult = clusterResult.addField(
                        "size",
                        Integer.toString(Integer.parseInt(cluster.getAttribute(ATTRIBUTE_FULL_COUNT)) - 1));

                clusterResult = clusterResult.addField(
                        PARAM_CLUSTER_ID,
                        cluster.getAttribute(ATTRIBUTE_CLUSTERID));

                final Element entryCollectionElement = getFirstChild(cluster, ELEMENT_ENTRY_COLLECTION);
                if (entryCollectionElement != null) {
                    final List<Element> entryList = getDirectChildren(entryCollectionElement, ELEMENT_ENTRY);
                    for (int i = 0; i < entryList.size(); i++) {
                        final Element nestedEntry = entryList.get(i);
                        if (i == 0) {
                            // First element is main result
                            clusterResult = (ResultList<ResultItem>) handleEntry(nestedEntry, clusterResult);
                        } else {
                            ResultList<ResultItem> nestedResultItem = new BasicResultList<ResultItem>();
                            nestedResultItem = (ResultList<ResultItem>) handleEntry(nestedEntry, nestedResultItem);
                            addResult(config, nestedResultItem, clusterResult);
                        }
                    }
                    searchResult.addResult(clusterResult);
                    clusterResult.setHitCount(entryList.size());
                    return entryList.size();
                }
            }
            return 0;
        }


        private ResultItem handleEntry(final Element entryElement, ResultItem searchResultItem) {

            final List<Element> entrySubElements = getDirectChildren(entryElement);
            for (Element entrySubElement : entrySubElements) {
                if (entrySubElement.getTextContent() != null && entrySubElement.getTextContent().trim().length() > 0) {
                    searchResultItem = searchResultItem.addField(entrySubElement.getNodeName(), entrySubElement.getTextContent().trim());
                }
            }
            return searchResultItem;
        }

        private void addResult(
                final NewsAggregatorCommandConfig config,
                final ResultList<ResultItem> srcResult,
                final ResultList<ResultItem> targetResult) {
            addResult(config, srcResult, targetResult, null, false);
        }


        private boolean addResult(final NewsAggregatorCommandConfig config,
                                  final ResultList<ResultItem> srcResult,
                                  final ResultList<ResultItem> targetResult,
                                  final Map<String, ResultList<ResultItem>> collapseMap,
                                  final boolean noMax) {

            // Check if entry is duplicate and should be a subresult
            ResultList<ResultItem> collapseParent = null;
            String collapseId = srcResult.getField(ELEMENT_COLLAPSEID);
            if (collapseMap != null) {
                collapseParent = collapseMap.get(collapseId);
            }
            if (collapseParent == null) {
                // Skipping add if max returned results has been reached.
                if (noMax || targetResult.getResults().size() < config.getResultsToReturn()) {
                    // No duplicate in results or should not be collapsed
                    targetResult.addResult(srcResult);
                    if (collapseMap != null) {
                        collapseMap.put(collapseId, srcResult);
                    }
                    return true;
                }
                return false;
            } else {
                // duplicate item, adding as a subresult to first item.
                collapseParent.addResult(srcResult);
                return true;
            }
        }

        private static Element getFirstChild(Element element, String elementName) {
            if (element != null) {
                NodeList childNodes = element.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (childNode instanceof Element && childNode.getNodeName().equals(elementName)) {
                        return (Element) childNode;
                    }
                }
            }
            return null;
        }

        private static List<Element> getDirectChildren(Element element, String elementName) {
            ArrayList<Element> children = new ArrayList<Element>();
            if (element != null) {
                NodeList childNodes = element.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (childNode instanceof Element && childNode.getNodeName().equals(elementName)) {
                        children.add((Element) childNode);
                    }
                }
            }
            return children;
        }

        private static List<Element> getDirectChildren(Element element) {
            ArrayList<Element> children = new ArrayList<Element>();
            if (element != null) {
                NodeList childNodes = element.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (childNode instanceof Element) {
                        children.add((Element) childNode);
                    }
                }
            }
            return children;
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
