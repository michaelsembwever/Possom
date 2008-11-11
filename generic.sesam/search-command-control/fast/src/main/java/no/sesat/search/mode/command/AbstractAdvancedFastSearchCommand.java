/* Copyright (2008) Schibsted Søk AS
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
 * Command producing queries in advanced query syntax for Fast FDS4.
 * @deprecated use instead the FastSearchCommand with a infix-query-builder configured.
 * An example is set up and ready to use.
 * see generic.sesam/war/src/main/conf/modes.xml id="default-fast4-command-advanced-query"
 */
public abstract class AbstractAdvancedFastSearchCommand extends AbstractSimpleFastSearchCommand {

    /**
     * Creates new advanced commmand.
     *
     * @param cxt        The context.
     */
    public AbstractAdvancedFastSearchCommand(final Context cxt) {

        super(cxt);
    }

    @Override
    protected synchronized String getQueryRepresentation() {

        final String query = super.getQueryRepresentation();

        return query.trim().startsWith("ANDNOT ")
                ? '#' + query
                : query;
    }

    // protected ----------------------------------------------



}
