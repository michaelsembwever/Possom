/*
 * Copyright (2005-2008) Schibsted Søk AS
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
