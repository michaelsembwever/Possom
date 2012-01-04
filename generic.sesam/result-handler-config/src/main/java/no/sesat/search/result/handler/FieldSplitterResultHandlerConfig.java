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
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Config for the result handler to split a field into an array.
 *
 * @version $Id$
 */
@Controller("FieldSplitter")
public final class FieldSplitterResultHandlerConfig extends AbstractResultHandlerConfig {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FieldSplitterResultHandlerConfig.class);

    private static final long serialVersionUID = -4594292275796458399L;

    private static final String DEFAULT_SEPARATOR = "fastpbfast";

    // Attributes ----------------------------------------------------

    private String fromField;

    private String toField;

    private String separator;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        super.readResultHandler(element);

        fromField = element.getAttribute("from-field");
        toField = element.getAttribute("to-field");

        if (element.getAttribute("separator") != null && element.getAttribute("separator").length() > 0) {
            separator = element.getAttribute("separator");
        } else {
            separator = DEFAULT_SEPARATOR;
        }

        return this;
    }

    // Getters / Setters ---------------------------------------------

    /** The field whose content will be split.
     * @return the fromField
     */
    public String getFromField() {
        return fromField;
    }

    /** @see #getFromField()
     *
     * @param from  Set from field.
     */
    public void setFromField(final String from) {
        fromField = from;
    }

    /** The field where the array resulting from the split will end up in.
     * @return the toField
     */
    public String getToField() {
        return toField;
    }

    /** @see #getToField()
     *
     * @param to set to field.
     */
    public void setToField(final String to) {
        toField = to;
    }

    /** The separator to split upon.
     * @return the separator
     */
    public String getSeparator() {
        return separator;
    }

    /** @see #getSeparator()
     *
     * @param s separator to use.
     */
    public void setSeparator(final String s) {
        separator = s;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
