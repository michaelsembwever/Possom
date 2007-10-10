/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * AbstractRunningQuery.java
 *
 * Created on 16 February 2006, 19:49
 *
 */

package no.sesat.search.run;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractRunningQuery implements RunningQuery {

    private static final Logger LOG = Logger.getLogger(AbstractRunningQuery.class);

    protected final Context context;

    /** Creates a new instance of AbstractRunningQuery */
    protected AbstractRunningQuery(final Context cxt) {
        context = cxt;
    }


    /**
     * Remote duplicate spaces. Leading and trailing spaces will
     * be preserved
     * @param query that may conaint duplicate spaces
     * @return string with duplicate spaces removed
     */
    protected static String trimDuplicateSpaces(final String query){

        LOG.trace("trimDuplicateSpaces(" + query + ")");

        return query == null
                ? null
                : query.replaceAll("\\s+", " ").trim();
    }

}
