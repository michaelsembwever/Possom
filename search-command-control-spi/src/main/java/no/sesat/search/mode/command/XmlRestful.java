/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
