/* Copyright (2005-2006) Schibsted Søk AS
 * ResourceContext.java
 *
 * Created on 23 January 2006, 13:54
 *
 */

package no.schibstedsok.front.searchportal.configuration.loader;

import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.BaseContext;

/** Defines the utility context for consumers of all types of ResourceLoaders.
 * Since the file format a configuration resource exists in is really an implementation detail
 * it is not really wise to use the exact Resource context but this instead.
 * This gives the freedom for configuration files to change format at will.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface ResourceContext extends BaseContext,DocumentContext,PropertiesContext,XStreamContext{
}
