/* Copyright (2006-2012) Schibsted ASA
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
package no.sesat.search.result.handler;


import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;


/**
 *
 * @version $Id$
 */
@Controller("FieldEscapeHandler")
public final class FieldEscapeResultHandlerConfig extends AbstractResultHandlerConfig {

    private String sourceField;
    private String targetField;

    /**
     *
     * @return
     */
    public String getSourceField() {
        return sourceField;
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
    public String getTargetField() {
        return targetField;
    }

    /**
     *
     * @param targetField
     */
    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);


        setSourceField(element.getAttribute("source-field"));
        setTargetField(element.getAttribute("target-field"));

        return this;
    }



}
