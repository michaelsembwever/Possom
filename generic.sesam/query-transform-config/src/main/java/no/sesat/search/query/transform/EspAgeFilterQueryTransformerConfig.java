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

import org.w3c.dom.Element;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
/**
 * Agefilter QueryTransformer for esp5.
 *
 * @AUTHOR Geir H. Pettersen (T-Rank)
 */
@Controller("EspAgeFilterQueryTransformer")
public class EspAgeFilterQueryTransformerConfig extends AbstractQueryTransformerConfig {
    private int ageCount;
    private String ageSymbol;
    private String ageField;

    public int getAgeCount() {
        return ageCount;
    }

    public void setAgeCount(int ageCount) {
        this.ageCount = ageCount;
    }

    public String getAgeSymbol() {
        return ageSymbol;
    }

    public void setAgeSymbol(String ageSymbol) {
        this.ageSymbol = ageSymbol;
    }

    public String getAgeField() {
        return ageField;
    }

    public void setAgeField(String ageField) {
        this.ageField = ageField;
    }

    @Override
    public AbstractQueryTransformerConfig readQueryTransformer(final Element element) {
        AbstractDocumentFactory.fillBeanProperty(this, null, "ageCount", AbstractDocumentFactory.ParseType.Int, element, "1");
        AbstractDocumentFactory.fillBeanProperty(this, null, "ageSymbol", AbstractDocumentFactory.ParseType.String, element, "w");
        AbstractDocumentFactory.fillBeanProperty(this, null, "ageField", AbstractDocumentFactory.ParseType.String, element, "");
        return this;
    }
}
