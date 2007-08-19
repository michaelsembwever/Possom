/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * XStreamLoader.java
 *
 * Created on 23 January 2006, 09:45
 *
 */

package no.sesat.searchportal.site.config;

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
