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
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;


/**
 * Calculate Age.
 *
 *
 * @version <tt>$Id$</tt>
 */
@Controller("AgeCalculatorResultHandler")
public final class AgeCalculatorResultHandlerConfig extends AbstractResultHandlerConfig {

    private String targetField;
    private String sourceField;
    private String recursiveField;
    private String ageFormatKey = "age";
    private Boolean asDate = Boolean.FALSE;

    private static final Logger LOG = Logger.getLogger(AgeCalculatorResultHandlerConfig.class);


    /**
     * @return
     */
    public String getRecursiveField() {
        return recursiveField;
    }

    /**
     * @param recursiveField
     */
    public void setRecursiveField(String recursiveField) {
        this.recursiveField = recursiveField;
    }


    /**
     * @return
     */
    public String getTarget() {
        return targetField;
    }


    /**
     * @param targetField
     */
    public void setTarget(final String targetField) {
        this.targetField = targetField;
    }


    /**
     * @param string
     */
    public void setSource(final String string) {
        sourceField = string;
    }

    /**
     * @return
     */
    public String getSource() {
        return sourceField;
    }

    /**
     * @param asDate
     */
    public void setAsDate(final Boolean asDate) {
        this.asDate = asDate;
    }

    /**
     * @return
     */
    public Boolean getAsDate() {
        return asDate;
    }

    public String getAgeFormatKey() {
        return ageFormatKey;
    }

    public void setAgeFormatKey(String ageFormatKey) {
        this.ageFormatKey = ageFormatKey;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);

        setTarget(element.getAttribute("target"));
        setSource(element.getAttribute("source"));
        AbstractDocumentFactory.fillBeanProperty(this, null, "asDate", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, null, "recursiveField", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(this, null, "ageFormatKey", ParseType.String, element, "age");
        return this;
    }


}
