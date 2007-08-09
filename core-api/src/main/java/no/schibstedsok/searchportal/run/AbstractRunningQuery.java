/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * AbstractRunningQuery.java
 *
 * Created on 16 February 2006, 19:49
 *
 */

package no.schibstedsok.searchportal.run;

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
