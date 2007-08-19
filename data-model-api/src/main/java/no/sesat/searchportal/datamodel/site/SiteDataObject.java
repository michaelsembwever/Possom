/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * SiteDataObject.java
 *
 * Created on 23 January 2007, 13:57
 *
 */

package no.sesat.searchportal.datamodel.site;

import no.sesat.searchportal.datamodel.generic.DataObject;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.SiteContext;
import no.sesat.searchportal.site.config.SiteConfiguration;

import java.io.Serializable;

/**
 * The SiteDataObject is the datamodel's container for Site related classes.
 * These objects are intended to be shared instances corresponding to each site and therefore thread-safe.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
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

