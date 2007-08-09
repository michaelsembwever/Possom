/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
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
