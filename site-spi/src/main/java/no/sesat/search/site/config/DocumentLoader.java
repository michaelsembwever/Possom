/* Copyright (2005-2007) Schibsted Søk AS
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
 * XStreamLoader.java
 *
 * Created on 23 January 2006, 09:45
 *
 */

package no.sesat.search.site.config;

import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;



/** DocumentLoader to deal with xml document resources.
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface DocumentLoader extends ResourceLoader {
    /** initialise this resource loader with the resource name/path and the builder used to create the dom.
     *@param resource the name/path of the resource.
     *@param builder the document that will be used to hold the xml dom.
     **/
    void init(String resource, DocumentBuilder builder);
    /** get the Document.
     *@return the Document.
     **/
    Document getDocument();

}
