/* Copyright (2007) Schibsted Søk AS
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
 * StaticSearchCommand.java
 *
 * Created on May 18, 2006, 10:47 AM
 *
 */

package no.sesat.search.mode.command;

import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

/**
 * A search command that can be used to generate static HTML search results. No
 * search is done.
 *
 *
 * @version $Id$
 */
public class StaticSearchCommand extends AbstractSearchCommand {

    private static final ResultItem DUMMYITEM = new BasicResultItem();

    public StaticSearchCommand(final Context cxt) {

        super(cxt);
    }

    public ResultList<? extends ResultItem> execute() {

        final ResultList<ResultItem> result = new BasicResultList<ResultItem>();
        result.addResult(DUMMYITEM);
        result.setHitCount(1);
        return result;
    }
}
