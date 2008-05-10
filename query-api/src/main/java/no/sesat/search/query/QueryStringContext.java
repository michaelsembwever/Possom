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
 * QueryStringContext.java
 *
 * Created on 23 January 2006, 14:02
 *
 */

package no.sesat.search.query;

import no.schibstedsok.commons.ioc.BaseContext;

/** Used when the query string is a requirement of the Context and neither the Query object or datamodel are available.
 *
 * @version $Id$
 *
 */
public interface QueryStringContext extends BaseContext{
    /** Get the original query string.
     *
     * @return the original query string.
     */
    String getQueryString();
}
