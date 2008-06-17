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
package no.sesat.search.site.config;

/**
 * @author magnuse
 * @version $Id$
 */
public enum Spi {

    /** */
    SITE("site"),
    /** */
    QUERY_TRANSFORM_CONFIG("query-transform-config"),
    /** */
    QUERY_TRANSFORM_CONTROL("query-transform-control", QUERY_TRANSFORM_CONFIG),
    /** */
    RESULT("result"),
    /** */
    RESULT_HANDLER_CONFIG("result-handler-config", RESULT),
    /** */
    RESULT_HANDLER_CONTROL("result-handler-control", RESULT_HANDLER_CONFIG),
    /** */
    SEARCH_COMMAND_CONFIG("search-command-config", RESULT),
    /** */
    SEARCH_COMMAND_CONTROL("search-command-control", SEARCH_COMMAND_CONFIG),
    /** */
    VIEW_CONFIG("view-config", RESULT),
    /** */
    VIEW_CONTROL("view-control", VIEW_CONFIG),
    /** */
    RUN_HANDLER_CONFIG("run-handler-config"),
    /** */
    RUN_HANDLER_CONTROL("run-handler-control", RUN_HANDLER_CONFIG),
    /** */
    RUN_TRANSFORM_CONFIG("run-transform-config"),
    /** */
    RUN_TRANSFORM_CONTROL("run-transform-control", RUN_TRANSFORM_CONFIG),
    /** */
    SERVLET_HANDLER("servlet-handler"),
    /** */
    VELOCITY_DIRECTIVES("velocity-directives", RESULT);

    private final Spi parent;
    private final String canonicalName;

    Spi(final String canonicalName, final Spi parent) {
        this.canonicalName = canonicalName;
        this.parent = parent;
    }

    /**
     *
     */
    Spi(final String canonicalName) {
        parent = null;
        this.canonicalName = canonicalName;
    }

    /**
     *
     * @return
     */
    public Spi getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return canonicalName;
    }
}
