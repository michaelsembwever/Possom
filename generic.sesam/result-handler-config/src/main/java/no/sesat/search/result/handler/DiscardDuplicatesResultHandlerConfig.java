/* Copyright (2006-2008) Schibsted ASA
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
 *
 */
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/** Removes from a ResultList duplicate ResultItems.
 * Duplicates are identified via the values of a specified field.
 * The check may be made case-insensitive.
 *
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Id$</tt>
 */
@Controller("DiscardDuplicatesResultHandler")
public final class DiscardDuplicatesResultHandlerConfig extends AbstractResultHandlerConfig {

    private String field;

    private boolean ignoreCase;

    /** The field to check uniqueness against.
     *
     * @param string
     */
    public void setField(final String string) {
        field = string;
    }

    /** @see #setField(java.lang.String)
     *
     * @return field name
     */
    public String getField() {
        return field;
    }

    /** @see #setIgnoreCase(boolean)
     *
     * @return
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /** Is the uniqueness case-insensitive.
     * Default is false.
     * @param ignoreCase true if case insensitive
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);

        AbstractDocumentFactory.fillBeanProperty(this, null, "field", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(this, null, "ignoreCase", ParseType.Boolean, element, "false");

        return this;
    }
}