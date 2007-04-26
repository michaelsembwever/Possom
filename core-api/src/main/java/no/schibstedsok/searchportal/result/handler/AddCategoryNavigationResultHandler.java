// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public final class AddCategoryNavigationResultHandler implements ResultHandler {

    private static final String CMD_ELEMENT_CATEGORY = "category";
    private static final String CMD_ATTR_DISPLAY_NAME = "display-name";
    private static final String CMD_ATTR_ID = "id";

    private static final Logger LOG = Logger.getLogger(AddCategoryNavigationResultHandler.class);
    private static List<Category> categoryList;

    private final AddCategoryNavigationResultHandlerConfig config;

    /**
     * @param config
     */
    public AddCategoryNavigationResultHandler(final ResultHandlerConfig config) {
        this.config = (AddCategoryNavigationResultHandlerConfig) config;
    }


    /**
     * @param cxt
     * @param datamodel
     */
    public void handleResult(Context cxt, DataModel datamodel) {
        try {
            if (cxt.getSearchResult() instanceof FastSearchResult) {
                if (categoryList == null) {
                    // This could happen more than once, but synchronize overhead would be on every call, so it ok. 
                    categoryList = parseCategories(cxt, datamodel);
                }
                addCategoryNavigators(datamodel, (FastSearchResult) cxt.getSearchResult(), categoryList);
            } else {
                LOG.error("Can not use " + AddCategoryNavigationResultHandler.class.getName() + " on a generic searchResult. Must be a " + FastSearchResult.class.getName());
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Could not parse categories.", e);
        }
    }

    private void addCategoryNavigators(DataModel datamodel, FastSearchResult searchResult, List<Category> categoryList) {
        if (categoryList != null && categoryList.size() > 0) {
            final String[] categoryFields = getCategoryFieldArray();
            if (categoryFields.length > 0) {
                for (String categoryField : categoryFields) {
                    Category selectedCategory = null;
                    StringDataObject selectedFieldData = datamodel.getParameters().getValue(categoryField);
                    for (Category category : categoryList) {
                        searchResult.addModifier(categoryField, new Modifier(category.getDisplayName(), -1, null));
                        if (selectedFieldData != null && selectedFieldData.getString().equals(category.getDisplayName())) {
                            selectedCategory = category;
                        }
                    }
                    if (selectedCategory != null && selectedCategory.getSubCategories() != null) {
                        categoryList = selectedCategory.getSubCategories();
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private List<Category> parseCategories(Context cxt, DataModel datamodel) throws ParserConfigurationException {
        Document doc = getDocument(cxt, datamodel);
        final Element root = doc.getDocumentElement();
        return parseCategories(root);
    }

    private Document getDocument(Context cxt, DataModel dataModel) throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        DocumentLoader documentLoader = cxt.newDocumentLoader(dataModel.getSite(), config.getCategoriesXml(), builder);
        documentLoader.abut();
        return documentLoader.getDocument();
    }

    /**
     * @param categoriesElement
     * @return
     */
    public List<Category> parseCategories(Element categoriesElement) {
        List<Element> categoryElements = getDirectChildren(categoriesElement, CMD_ELEMENT_CATEGORY);
        return parseCategories(categoryElements);
    }

    private List<Category> parseCategories(List<Element> categoryElements) {
        if (categoryElements != null && categoryElements.size() > 0) {
            final List<Category> categoryList = new ArrayList<Category>();
            for (Element categoryElement : categoryElements) {
                categoryList.add(parseCategory(categoryElement));
            }
            return categoryList;
        } else {
            return null;
        }
    }

    private Category parseCategory(Element categoryElement) {
        String id = categoryElement.getAttribute(CMD_ATTR_ID);
        String displayName = categoryElement.getAttribute(CMD_ATTR_DISPLAY_NAME);
        List<Category> subCategories = parseCategories(getDirectChildren(categoryElement, CMD_ELEMENT_CATEGORY));
        return new Category(id, displayName, subCategories);
    }

    private List<Element> getDirectChildren(Element element, String elementName) {
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

    private String[] getCategoryFieldArray() {
        return StringUtils.split(config.getCategoryFields(), ',');
    }

    private static final class Category {
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
