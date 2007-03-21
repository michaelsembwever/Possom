// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NewsAggregatorSearchConfiguration extends NavigatableESPFastConfiguration {
    private final static Logger LOG = Logger.getLogger(NewsAggregatorSearchConfiguration.class);
    private static final String CMD_ELEMENT_CATEGORIES = "categories";
    private static final String CMD_ELEMENT_CATEGORY = "category";
    private static final String CMD_ATTR_DISPLAY_NAME = "display-name";
    private static final String CMD_ATTR_ID = "id";

    private String xmlSource;
    private String xmlMainFile;
    private String clusterField;
    private String nestedResultsField;
    private int relatedMaxCount = 30;
    private int resultsPerCluster;
    private int clusterMaxFetch;
    private List<Category> categories;
    private String geographicFields;
    private String categoryFields;

    public NewsAggregatorSearchConfiguration() {
        super(null);
    }

    public NewsAggregatorSearchConfiguration(SearchConfiguration sc) {
        super(sc);
        if (sc instanceof NewsAggregatorSearchConfiguration) {
            final NewsAggregatorSearchConfiguration nasc = (NewsAggregatorSearchConfiguration) sc;
            xmlSource = nasc.getXmlSource();
            xmlMainFile = nasc.getXmlMainFile();
            clusterField = nasc.getClusterField();
            nestedResultsField = nasc.getNestedResultsField();
            relatedMaxCount = nasc.getRelatedMaxCount();
            resultsPerCluster = nasc.getResultsPerCluster();
            clusterMaxFetch = nasc.getClusterMaxFetch();
            categories = nasc.getCategories();
            geographicFields = nasc.getGeographicFields();
            categoryFields = nasc.getCategoryFields();
        }
    }

    public void parseCategories(Element commandElement) {
        NodeList catsNodeList = commandElement.getElementsByTagName(CMD_ELEMENT_CATEGORIES);
        if (catsNodeList != null && catsNodeList.getLength() > 0) {
            NodeList categoryNodeList = catsNodeList.item(0).getChildNodes();
            categories = parseCategories(categoryNodeList);
        }
    }

    private List<Category> parseCategories(NodeList categoryElements) {
        if (categoryElements != null && categoryElements.getLength() > 0) {
            LOG.debug("Parsing categoryList size = " + categoryElements.getLength());
            List<Category> categoryList = null;
            for (int i = 0; i < categoryElements.getLength(); i++) {
                Node node = categoryElements.item(i);
                if (node instanceof Element && CMD_ELEMENT_CATEGORY.equals(node.getNodeName())) {
                    if (categoryList == null) {
                        categoryList = new ArrayList<Category>();
                    }
                    categoryList.add(parseCategory((Element) node));
                }
            }
            return categoryList;
        } else {
            return null;
        }
    }

    private Category parseCategory(Element categoryElement) {
        LOG.debug("Parsing category:" + categoryElement);
        String id = categoryElement.getAttribute(CMD_ATTR_ID);
        String displayName = categoryElement.getAttribute(CMD_ATTR_DISPLAY_NAME);
        List<Category> subCategories = parseCategories(categoryElement.getElementsByTagName(CMD_ELEMENT_CATEGORY));
        return new Category(id, displayName, subCategories);
    }

    public int getRelatedMaxCount() {
        return relatedMaxCount;
    }

    public void setRelatedMaxCount(int relatedMaxCount) {
        this.relatedMaxCount = relatedMaxCount;
    }

    public String getXmlSource() {
        return xmlSource;
    }

    public void setXmlSource(String xmlSource) {
        this.xmlSource = xmlSource;
    }

    public void setXmlMainFile(String xmlMainFile) {
        this.xmlMainFile = xmlMainFile;
    }

    public String getGeographicFields() {
        return geographicFields;
    }

    public void setGeographicFields(String geographicFields) {
        this.geographicFields = geographicFields;
    }

    public String[] getCategoryFieldArray() {
        return StringUtils.split(categoryFields, ',');
    }

    public String getCategoryFields() {
        return categoryFields;
    }

    public void setCategoryFields(String categoryFields) {
        this.categoryFields = categoryFields;
    }

    public String getXmlMainFile() {
        return xmlMainFile;
    }

    public String getClusterField() {
        return clusterField;
    }

    public void setClusterField(String clusterField) {
        this.clusterField = clusterField;
    }

    public String getNestedResultsField() {
        return nestedResultsField;
    }

    public void setNestedResultsField(String nestedResultsField) {
        this.nestedResultsField = nestedResultsField;
    }

    public void setResultsPerCluster(int resultsPerCluster) {
        this.resultsPerCluster = resultsPerCluster;
    }

    public int getResultsPerCluster() {
        return resultsPerCluster;
    }


    public int getClusterMaxFetch() {
        return clusterMaxFetch;
    }

    public void setClusterMaxFetch(int clusterMaxFetch) {
        this.clusterMaxFetch = clusterMaxFetch;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String[] getGeographicFieldArray() {
        return StringUtils.split(geographicFields, ',');
    }

    public static final class Category {
        private final String id;
        private final String displayName;
        private final List<Category> subCategories;

        public Category(String id, String displayName) {
            this(id, displayName, null);
        }

        public Category(String id, String displayName, List<Category> subCategories) {
            this.id = id;
            this.displayName = displayName;
            this.subCategories = subCategories;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<Category> getSubCategories() {
            return subCategories;
        }

        public String toString() {
            return "Category{" +
                    "id='" + id + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", hasSubCategories=" + (subCategories != null && subCategories.size() > 0) +
                    '}';
        }
    }

}
