/* Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 * QueryStringContext.java
 *
 * Created on 23 January 2006, 14:02
 *
 */

package no.sesat.search.query;

import no.sesat.commons.ioc.BaseContext;

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
