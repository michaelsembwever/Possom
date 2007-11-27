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
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
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

    @Override
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
