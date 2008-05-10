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
 * SiteContext.java
 *
 * Created on 23 January 2006, 13:55
 *
 */

package no.sesat.search.site;

import no.schibstedsok.commons.ioc.BaseContext;

/** Defines the context for consumers of Site objects.
 *
 * @version $Id$
 *
 */
public interface SiteContext extends BaseContext {
    /** What is the site to use.
     *<b>If you override this then you must also override any ResourceContexts!!</b>
     * @return the site.
     **/
    Site getSite();
}
