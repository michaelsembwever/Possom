/* Copyright (2005-2006) Schibsted Søk AS
 * XStreamLoader.java
 *
 * Created on 23 January 2006, 09:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.util.config;

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
