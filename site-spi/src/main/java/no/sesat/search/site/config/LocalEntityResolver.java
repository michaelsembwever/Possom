/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
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
