/* Copyright (2007) Schibsted Søk AS
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

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface StringDataObject extends Serializable{
    /**
     * 
     * @return 
     */
    @AccessDisallow({VIEW_CONSTRUCTION})
    String getString();
    /**
     * 
     * @return 
     */
    String getUtf8UrlEncoded();
    /**
     * 
     * @return 
     */
    String getIso88591UrlEncoded();
    /**
     * 
     * @return 
     */
    String getXmlEscaped();
}
