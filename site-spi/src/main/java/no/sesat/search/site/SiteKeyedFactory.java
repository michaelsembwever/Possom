/* Copyright (2006-2007) Schibsted ASA
 *   This file is part of SESAT.
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
 * SiteKeyedFactory.java
 *
 * Created on 5 May 2006, 07:27
 *
 */

package no.sesat.search.site;

/** Factories that have Site->Factory mappings should implement this interface
 * to ensure general behaviours.
 *  SiteKeyedFactories by default have a synchronised site to factory mapping.
 *  If this is not the case it must be clearly noted in the class's javadoc.
 *
 *
 * @version $Id$
 */
public interface SiteKeyedFactory {

    /** Remove the factory the maps to the given site. *
     * @param site remove factory corresponding to this site.
     * @return true if a factory was successfully removed.
     */
    boolean remove(Site site);

}
