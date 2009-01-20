/* Copyright (2006-2008) Schibsted Søk AS
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
/*
 * WebSearchCommand.java
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.sesat.search.mode.command;

import no.sesat.commons.visitor.Visitor;
import no.sesat.search.query.XorClause;

/**
 *
 * A search command for the web search.
 *
 * @version $Id$
 */
public class WebSearchCommand extends Fast4SearchCommand {

    /** Creates a new instance of WebSearchCommand
     *
     * @param cxt Search command context.
     */
    public WebSearchCommand(final Context cxt) {

        super(cxt);
    }



    /**
     *
     * @param clause The clause to examine.
     */
    @Override
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {

        switch(clause.getHint()){
        case PHRASE_ON_LEFT:
        case FULLNAME_ON_LEFT:
            // Web searches should use phrases over separate words.
            clause.getFirstClause().accept(visitor);
            break;
        default:
            // All other high level clauses are ignored.
            clause.getSecondClause().accept(visitor);
            break;
        }
    }


}
