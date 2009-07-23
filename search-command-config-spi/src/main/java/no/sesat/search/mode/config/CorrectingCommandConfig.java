/* Copyright (2009) Schibsted ASA
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
package no.sesat.search.mode.config;

/** Extended behaviour to any SearchCommand that allows it to re-run searches modifying the query each time
 * until the result list is desirable.
 * The search command itself is not re-run but rather a new instance of the search command is constructed and executed.
 * It is expected that the correctionCount is passed on from old command to the new.
 *
 * Any implementation must be able to correctQuery(..) if it wishes the command to run again. If this method returns
 * the same query string then the command is not executed again and the current result list deemed desirable.
 * This decision is also influenced by CorrectinCommandConfig.getCorrectingLimit.
 *
 * CorrectingSearchCommandUtility.correctQueryFromSpellingSuggestions(..) provides a helping implementation
 * that the correctQuery method can delegate to. It relies on the presense of SpellingSuggestions in the result list.
 *
 * CorrectingSearchCommandUtility.doCall(..) is a helper functor method to handle the looping execution process.
 * See CorrectingSolrSearchCommand.call(..) for example usage of it.
 *
 * @see CorrectingSearchCommand
 * @version $Id$
 */
public interface CorrectingCommandConfig extends SearchConfiguration{
    void setCorrectingLimit(int correctingLimit);
    int getCorrectingLimit();
}
