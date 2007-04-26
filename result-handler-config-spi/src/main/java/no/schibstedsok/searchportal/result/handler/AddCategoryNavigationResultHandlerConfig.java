// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
@Controller("AddCategoryNavigationResultHandler")
public final class AddCategoryNavigationResultHandlerConfig extends AbstractResultHandlerConfig {

    private static final Logger LOG = Logger.getLogger(AddCategoryNavigationResultHandlerConfig.class);
    private String categoriesXml = "categories.xml";
    private String categoryFields;

    /**
     * @return
     */
    public String getCategoriesXml() {
        return categoriesXml;
    }

    /**
     * @param categoriesXml
     */
    public void setCategoriesXml(String categoriesXml) {
        this.categoriesXml = categoriesXml;
    }

    /**
     * @return
     */
    public String getCategoryFields() {
        return categoryFields;
    }

    /**
     * @param categoryFields
     */
    public void setCategoryFields(String categoryFields) {
        this.categoryFields = categoryFields;
    }

    public AddCategoryNavigationResultHandlerConfig readResultHandler(Element element) {

        super.readResultHandler(element);
        setCategoryFields(element.getAttribute("category-fields"));
        final String categoryXml = element.getAttribute("categories-xml");
        if (categoryXml != null && categoryXml.length() > 0) {
            setCategoriesXml(categoryXml);
        }
        return this;
    }

}
