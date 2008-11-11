/*
 * Copyright (2008) Schibsted Søk AS
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
 */
package no.sesat.search.mode.command.querybuilder;

/** An extension to a QueryBuilder for building the associated filter string.
 *
 * @version $Id$
 */
public interface FilterBuilder extends QueryBuilder {

    /** Add a filter. Where field is blank an anonymous filter will be added.
     *
     * @param field
     * @param value
     */
    void addFilter(String field, String value);

    /** The Filter String built from the Query's transformed clauses.
     *
     * The filter string consists of key-value pairs usually represented key:value.
     * The Query parser refers to these pairs as fielded clauses.
     *
     * <b>By default will delegate to getQueyString()</b>
     *
     * @return string built from the Query's transformed clauses, or "".
     */
    String getFilterString();

    /** Find a particular filter value.
     *
     * @param string the filter (or field) name
     * @return the filter's (or field's) value. space separated if multiple values exist.
     */
    String getFilter(String string);

}
