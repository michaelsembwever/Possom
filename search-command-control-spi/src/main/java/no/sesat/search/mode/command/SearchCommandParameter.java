/*
* Copyright (2008) Schibsted ASA
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
*
*/
package no.sesat.search.mode.command;

/**
 * A SearchCommandParameter are options that a search command provides to its users.
 * The best two examples are the offset parameter and the userSortBy parameter.
 * These are infact part of SearchCommand's default API. <br/>
 *
 * From {@link http://sesat.no/new-design-proposal-for-searchcommand-and-abstractsearchcommand.html}  <br/> <br/>
 *
 * Search Command Parameters typically have three sources,
 * and they use the first found.  <br/>
 * For example: a url parameter, a user parameter, the command's configured parameter.  <br/> <br/>
 *
 * Sometimes (eg userSortBy and pagination) the configuration actually comes from the presentation layer.
 * Here the command's configuration here must simply point to where in the presentation layer
 * this configuration can be found.
 * Strictly speaking the domain layer should be isolated from the presentation layer
 * but here we access only the presentation layer's configuration through the datamodel. <br/> <br/>
 *
 * ResultToReturn is an interesting example.
 * It should be both overridable from url and user parameters.
 * But the configuration exist in both the presentation layer and the domain layer.
 * The domain layer's only responsibility is to ensure at least the amount of results are returned
 * that the presentation layer wants.
 * Up until now its just been presumed that the command's configuration
 * is hardcoded to a value larger than any possible presentation value.
 *
 * @version $Id$
 */
interface SearchCommandParameter{

    /** The parameter name.
     *
     * @return the parameter name.
     */
    String getName();

    /** Whether the parameter is active.
     *
     * @return true is parameter is in use.
     */
    boolean isActive();

    /** Get the parameter's current value.
     *
     * @return the parameters current value. can be null is isActive() is false.
     */
    String getValue();
}

