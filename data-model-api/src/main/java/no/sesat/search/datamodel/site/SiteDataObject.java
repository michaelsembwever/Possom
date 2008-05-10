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
 *
 * SiteDataObject.java
 *
 * Created on 23 January 2007, 13:57
 *
 */

package no.sesat.search.datamodel.site;

import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.SiteConfiguration;

import java.io.Serializable;

/**
 * The SiteDataObject is the datamodel's container for Site related classes.
 * These objects are intended to be shared instances corresponding to each site and therefore thread-safe.
 *
 *
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface SiteDataObject extends Serializable, SiteContext {

    /**
     * The Site class is a key used by other classes that return site-dependent results. *
     */
    Site getSite();

    /**
     * The SiteConfiguration class contains properties from the site's configuration.properties. *
     */
    SiteConfiguration getSiteConfiguration();
    //Channels getChannels();
    //TextMessage getTextMessage();
    //LinkPulse getLinkPulse(); // XXX is this a bean or a service
}

