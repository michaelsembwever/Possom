/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
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
import org.w3c.dom.Element;

/**
 * DateFormatHandler is part of no.sesat.search.result
 * Transform fast inputdate to how it will be displayed in tv enrichment . Tv Enrichment
 * will most likely only display Hour of day.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
@Controller("DateFormatHandler")
public final class DateFormatResultHandlerConfig extends AbstractResultHandlerConfig {

    private String fieldPrefix = "";
    private String sourceField;

    /**
     *
     * @return
     */
    public String getFieldPrefix() {
        return fieldPrefix;
    }

    /**
     *
     * @param fieldPrefix
     */
    public void setFieldPrefix(final String fieldPrefix) {
        this.fieldPrefix = fieldPrefix;
    }

    /**
     *
     * @param sourceField
     */
    public void setSourceField(final String sourceField) {
       this.sourceField = sourceField;
    }

    /**
     *
     * @return
     */
    public String getSourceField() {
        return sourceField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);

        if (element.hasAttribute("prefix")) {
            setFieldPrefix(element.getAttribute("prefix"));
        }
        setSourceField(element.getAttribute("source"));

        return this;
    }


}
