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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("FieldChooser")
public final class FieldChooserResultHandlerConfig extends AbstractResultHandlerConfig {

    private final List<String> fields = new ArrayList<String>();
    private String targetField;
    private String defaultValue;
    private String recursiveField;

    /**
     * @param fieldName
     */
    public void addField(final String fieldName) {
        fields.add(fieldName);
    }
    
    public void addFields(final String[] fieldNames) {
        for (int i = 0; i < fieldNames.length; i++) {
            addField(fieldNames[i]);
        }
    }

    /**
     * @return
     */
    public List<String> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * @param fieldName
     */
    public void setTarget(final String fieldName) {
        targetField = fieldName;
    }

    /**
     * @return
     */
    public String getTarget() {
        return targetField;
    }


    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRecursiveField() {
        return recursiveField;
    }

    public void setRecursiveField(String recursiveField) {
        this.recursiveField = recursiveField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);

        setTarget(element.getAttribute("target"));
        String optAttr = element.getAttribute("recursive-field");
        if (optAttr != null && optAttr.length() > 0) {
            recursiveField = optAttr;
        }
        optAttr = element.getAttribute("default-value");
        if (optAttr != null && optAttr.length() > 0) {
            defaultValue = optAttr;
        }
        final String[] fields = element.getAttribute("fields").split(",");
        for (String field : fields) {
            addField(field);
        }

        return this;
    }


}
