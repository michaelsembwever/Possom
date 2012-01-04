/* Copyright (2012) Schibsted ASA
 *   This file is part of Possom.
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
 * LocalEntityResolver.java
 *
 * Created on 4 April 2006, 14:42
 *
 */
package no.sesat.search.site.config;

import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

final class LocalEntityResolver implements EntityResolver {

    private static final Logger LOG = Logger.getLogger(LocalEntityResolver.class);
    private static final String INFO_LOADING_DTD = "Loading local DTD ";


    public InputSource resolveEntity(final String publicId, final String systemId) {

        // the latter is only for development purposes when dtds have't been published to production yet
        if (systemId.startsWith("http://sesam.no/dtds/") || systemId.startsWith("http://localhost")) {

            final String rsc = systemId.substring(systemId.lastIndexOf('/'));
            LOG.info(INFO_LOADING_DTD + rsc);
            return new InputSource(getClass().getResourceAsStream(rsc));

        } else {
            // use the default behaviour
            return null;
        }
    }

}
