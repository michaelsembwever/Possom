/*
 * SiteDataObject.java
 *
 * Created on 23 January 2007, 13:57
 *
 */

package no.schibstedsok.searchportal.datamodel.site;

import java.io.Serializable;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface SiteDataObject extends Serializable {

    Site getSite();
    SiteConfiguration getSiteConfiguration();
    //Channels getChannels();
    //TextMessage getTextMessage();
    //LinkPulse getLinkPulse(); // XXX is this a bean or a service
}

