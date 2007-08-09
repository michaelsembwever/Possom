/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 * ResourceContext.java
 *
 * Created on 23 January 2006, 13:54
 *
 */

package no.schibstedsok.searchportal.site.config;


import no.schibstedsok.commons.ioc.BaseContext;

/** Defines the utility context for consumers of all types of ResourceLoaders.
 * Since the file format a configuration resource exists in is really an implementation detail
 * it is not really wise to use the exact Resource context but this instead.
 * This gives the freedom for configuration files to change format at will.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface ResourceContext extends BaseContext, DocumentContext, PropertiesContext, BytecodeContext {
}
