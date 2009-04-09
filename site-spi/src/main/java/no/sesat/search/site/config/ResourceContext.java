/* Copyright (2005-2008) Schibsted ASA
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
 * ResourceContext.java
 *
 * Created on 23 January 2006, 13:54
 *
 */

package no.sesat.search.site.config;


import no.sesat.commons.ioc.BaseContext;

/** Defines the utility context for consumers of all types of ResourceLoaders.
 * Since the file format a configuration resource exists in is really an implementation detail
 * it is not really wise to use the exact Resource context but this instead.
 * This gives the freedom for configuration files to change format at will.
 *
 * @version $Id$
 *
 */
public interface ResourceContext extends BaseContext, DocumentContext, PropertiesContext, BytecodeContext {
}
