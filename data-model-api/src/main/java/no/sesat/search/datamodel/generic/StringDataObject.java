/* Copyright (2007) Schibsted ASA
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
 * StringDataObject.java
 *
 * Created on 23 January 2007, 12:43
 *
 */

package no.sesat.search.datamodel.generic;

import java.io.Serializable;
import static no.sesat.search.datamodel.access.ControlLevel.VIEW_CONSTRUCTION;
import no.sesat.search.datamodel.access.AccessDisallow;

/** DataObject wrapping a String providing getters for url encoded and xml escaped variants.
 *
 * String should not be used directly during the rendering stage.
 * Hence getString has the annontation <code>@AccessDisallow({VIEW_CONSTRUCTION})</code>.
 *
 *
 *
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface StringDataObject extends Serializable{
    /** The plain value of the string.
     * Disallowed during rendering for security reasons.
     * @return
     */
    @AccessDisallow({VIEW_CONSTRUCTION})
    String getString();
    /** The UTF8 url encoded variant of the string.
     * Encoding must meet those requirements stated in java.net.URLEncoder
     *
     * @return UTF8 url encoded variant of the string.
     */
    String getUtf8UrlEncoded();
    /** The ISO-88591 url encoded variant of the string.
     * Encoding must meet those requirements stated in java.net.URLEncoder
     *
     * @return ISO-88591 url encoded variant of the string.
     */
    String getIso88591UrlEncoded();
    /** The xml escaped variant of the string.
     *
     * @return xmlescaped variant of the string.
     */
    String getXmlEscaped();
}
