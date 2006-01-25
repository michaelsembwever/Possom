/* Copyright (2005-2006) Schibsted Søk AS
 * ResourceContext.java
 *
 * Created on 23 January 2006, 13:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.configuration.loaders;

import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;

/** Defines the context for consumers of Resources.
 * That is both properties and XStream resource.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface ResourceContext {
    /** Create a new PropertiesLoader for the given resource name/path and load it into the given properties.
     * @param resource the resource name/path.
     * @param properties the properties to hold the individual properties loaded.
     * @return the new PropertiesLoader to use.
     **/
    PropertiesLoader newPropertiesLoader(String resource, Properties properties);
    /** Create a new XStreamLoader for the given resource name/path and load it with the given XStream.
     * @param resource the resource name/path.
     * @param xstream the xstream to deserialise the resource with.
     * @return the new PropertiesLoader to use.
     **/
    XStreamLoader newXStreamLoader(String resource, XStream xstream);
    /** Create a new DocumentLoader for the given resource name/path and load it with the given DocumentBuilder.
     * @param resource the resource name/path.
     * @param builder the DocumentBuilder to build the DOM resource with.
     * @return the new DocumentLoader to use.
     **/
    DocumentLoader newDocumentLoader( String resource, DocumentBuilder builder);
}
