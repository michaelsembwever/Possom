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
 * PlatefoodSearchResult.java
 *
 * Created on 24. august 2006, 15:23
 *
 */

package no.sesat.search.result;

import no.sesat.search.result.*;

/**
 *
 * @param T
 *
 * @version $Id$
 */
public final class PlatefoodSearchResult<T extends ResultItem> extends BasicResultList<T> {

    private boolean ppcTopListQuery = false;

    /**
     *
     * @param cmd
     * @param top
     */
    public PlatefoodSearchResult( final boolean top) {
        super();
        ppcTopListQuery = top;
    }

    /**
     *
     * @return
     */
    public boolean isTopListQuery() {
        return ppcTopListQuery;
    }

}
