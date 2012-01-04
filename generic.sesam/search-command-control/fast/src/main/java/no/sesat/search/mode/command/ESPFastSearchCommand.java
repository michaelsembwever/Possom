/* Copyright (2012) Schibsted ASA
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
 */
/*
 * ESPFastSearchCommand.java
 *
 * Created on May 30, 2006, 3:01 PM
 *
 */

package no.sesat.search.mode.command;


/**
 * A search command used for querrying FAST ESP 5.0 query servers.
 */
public class ESPFastSearchCommand extends AbstractESPFastSearchCommand {

    /** Creates a new instance of FastSearchCommand
     *
     * @param cxt Search command context.
     */
    public ESPFastSearchCommand(final Context cxt) {

        super(cxt);
    }
}
