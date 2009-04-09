/*
* Copyright (2008) Schibsted ASA
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
package no.sesat.search.mode.command;

import java.io.IOException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/** SearchCommands that are RESTful and return XML response.
 * http://en.wikipedia.org/wiki/Representational_State_Transfer
 *
 * It makes the presumption of working with JAXP's DOM.
 *
 * @version $Id$
 */
public interface XmlRestful extends Restful{

    /** Obtain the loaded Document, of the RESTful result.
     * Makes the presumption that the RESTful service returns an well formed xml response.
     *
     *
     * @return the w3c dom Document
     * @throws java.io.IOException failure to read the response (or sent the request)
     * @throws org.xml.sax.SAXException failure to parse/load the xml into a dom
     */
    Document getXmlResult() throws IOException, SAXException;
}
