/*
 * LocationDataObject.java
 *
 * Created on 23 January 2007, 12:37
 *
 */

package no.schibstedsok.searchportal.datamodel.user;

import java.io.Serializable;
import no.schibstedsok.searchportal.datamodel.generic.DataNode;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataNode
public interface LocationDataObject extends Serializable {

    AddressDataObject getAddress();

    AddressDataObject instantiateAddress();

    BandwidthDataObject getBandwith();

    BandwidthDataObject instantiateBandwith();
}
