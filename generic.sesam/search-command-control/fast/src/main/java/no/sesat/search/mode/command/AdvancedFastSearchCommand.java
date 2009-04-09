/* Copyright (2008) Schibsted ASA
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
package no.sesat.search.mode.command;

/**
 * @deprecated use instead the Fast4SearchCommand with infix-query-builder
 *
 */
public class AdvancedFastSearchCommand extends AbstractAdvancedFastSearchCommand {

    /** Creates a new instance of an AdvancedFastSearchCommand.
     *
     * @param cxt Search command context.
     */
    public AdvancedFastSearchCommand(final Context cxt) {

        super(cxt);
    }

}
