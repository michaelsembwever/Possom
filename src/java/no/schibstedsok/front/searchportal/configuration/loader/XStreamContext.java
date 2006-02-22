/* Copyright (2005-2006) Schibsted Søk AS
 *
 * Created on 23 January 2006, 13:54
 */

package no.schibstedsok.front.searchportal.configuration.loader;

import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.BaseContext;

/** Defines the context for consumers of XStreamLoaders.
 *
 * @version $Id: ResourceContext.java 2045 2006-01-25 12:10:01Z mickw $
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface XStreamContext extends BaseContext {

    /** Create a new XStreamLoader for the given resource name/path and load it with the given XStream.
     * @param resource the resource name/path.
     * @param xstream the xstream to deserialise the resource with.
     * @return the new PropertiesLoader to use.
     **/
    XStreamLoader newXStreamLoader(String resource, XStream xstream);

}
