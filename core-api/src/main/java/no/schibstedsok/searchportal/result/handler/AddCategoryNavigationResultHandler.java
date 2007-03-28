// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddCategoryNavigationResultHandler implements ResultHandler {
    private static final String CMD_ELEMENT_CATEGORY = "category";
    private static final String CMD_ATTR_DISPLAY_NAME = "display-name";
    private static final String CMD_ATTR_ID = "id";

    private static final Logger LOG = Logger.getLogger(AddCategoryNavigationResultHandler.class);
    private String categoriesXml = "conf/categories.xml";
    private String categoryFields;

    public void handleResult(Context cxt, DataModel datamodel) {
        try {
            if (cxt.getSearchResult() instanceof FastSearchResult) {
                addCategoryNavigators(datamodel, (FastSearchResult) cxt.getSearchResult(), parseCategories(datamodel));
            } else {
                LOG.error("Can not use " + AddCategoryNavigationResultHandler.class.getName() + " on a generic searchResult. Must be a " + FastSearchResult.class.getName());
            }
        } catch (IOException e) {
            LOG.error("Could not parse categories.", e);
        } catch (JDOMException e) {
            LOG.error("Could not parse categories.", e);
        }
    }

    private void addCategoryNavigators(DataModel datamodel, FastSearchResult searchResult, List<Category> categoryList) {
        final String[] categoryFields = getCategoryFieldArray();
        LOG.debug("Adding category navigators: categoryFields=" + Arrays.toString(categoryFields));
        if (categoryFields.length > 0) {
            LOG.debug("categoryList=" + categoryList);
            for (String categoryField : categoryFields) {
                Category selectedCategory = null;
                StringDataObject selectedFieldData = datamodel.getParameters().getValue(categoryField);
                LOG.debug("selectedFieldData=" + selectedFieldData);
                for (Category category : categoryList) {
                    searchResult.addModifier(categoryField, new Modifier(category.getDisplayName(), -1, null));
                    LOG.debug("Adding modifier name=" + categoryField + ", " + category.getDisplayName());
                    if (selectedFieldData != null && selectedFieldData.getString().equals(category.getDisplayName())) {
                        selectedCategory = category;
                        LOG.debug("selectedCategory=" + selectedCategory);
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

    private List<Category> parseCategories(DataModel datamodel) throws IOException, JDOMException {

        final URL url = new URL("http://" + datamodel.getSite().getSite().getName() + datamodel.getSite().getSite().getConfigContext() + categoriesXml);
        final HTTPClient client = HTTPClient.instance(url.getHost(), url.getHost(), url.getPort());
        BufferedInputStream in = null;
        try {
            in = client.getBufferedStream(url.getHost(), url.getPath());
            Document doc = getDocument(in);
            final Element root = doc.getRootElement();
            return parseCategories(root);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // Ignoring
                }
            }
        }
    }

    private Document getDocument(InputStream inputStream) throws JDOMException, IOException {
        final SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setValidation(false);
        saxBuilder.setIgnoringElementContentWhitespace(true);
        return saxBuilder.build(inputStream);
    }

    public List<Category> parseCategories(Element categoriesElement) {
        //noinspection unchecked
        List<Element> categoryElements = categoriesElement.getChildren(CMD_ELEMENT_CATEGORY);
        return parseCategories(categoryElements);
    }

    private List<Category> parseCategories(List<Element> categoryElements) {
        if (categoryElements != null && categoryElements.size() > 0) {
            LOG.debug("Parsing categoryList size = " + categoryElements.size());
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
        LOG.debug("Parsing category:" + categoryElement);
        String id = categoryElement.getAttributeValue(CMD_ATTR_ID);
        String displayName = categoryElement.getAttributeValue(CMD_ATTR_DISPLAY_NAME);
        //noinspection unchecked
        List<Category> subCategories = parseCategories(categoryElement.getChildren(CMD_ELEMENT_CATEGORY));
        return new Category(id, displayName, subCategories);
    }

    private String[] getCategoryFieldArray() {
        return StringUtils.split(categoryFields, ',');
    }

    public String getCategoriesXml() {
        return categoriesXml;
    }

    public void setCategoriesXml(String categoriesXml) {
        this.categoriesXml = categoriesXml;
    }

    public String getCategoryFields() {
        return categoryFields;
    }

    public void setCategoryFields(String categoryFields) {
        this.categoryFields = categoryFields;
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
