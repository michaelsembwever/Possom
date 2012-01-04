/*
 * Copyright (2005-2012) Schibsted ASA
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
 *
 */
package no.sesat.search.mode.command;

import org.apache.log4j.Logger;

/** Fast 4 search command.
 *
 * @version <tt>$Id$</tt>
 */
public class Fast4SearchCommand extends AbstractFast4SearchCommand {

    private static final Logger LOG = Logger.getLogger(Fast4SearchCommand.class);

    /** Creates a new instance of Fast4SearchCommand
     *
     * @param cxt Search command context.
     */
    public Fast4SearchCommand(final Context cxt) {

        super(cxt);
    }

}
