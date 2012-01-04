/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.transform;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import org.w3c.dom.Element;

/**
 *
 *
 */
@Controller("ExactFieldFilterQueryTransformer")
public class ExactFieldFilterQueryTransformerConfig extends AbstractQueryTransformerConfig {
    private String filterField;
    private String filterParameter;
    private boolean useEquals;

    public String getFilterField() {
        return filterField;
    }

    public void setFilterField(String filterField) {
        this.filterField = filterField;
    }

    public String getFilterParameter() {
        return filterParameter;
    }

    public void setFilterParameter(String filterParameter) {
        this.filterParameter = filterParameter;
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    @Override
    public ExactFieldFilterQueryTransformerConfig readQueryTransformer(final Element element) {
        AbstractDocumentFactory.fillBeanProperty(this, null, "filterField", AbstractDocumentFactory.ParseType.String, element, "url");
        AbstractDocumentFactory.fillBeanProperty(this, null, "filterParameter", AbstractDocumentFactory.ParseType.String, element, "url");
        AbstractDocumentFactory.fillBeanProperty(this, null, "useEquals", AbstractDocumentFactory.ParseType.Boolean, element, "false");
        return this;
    }

}
