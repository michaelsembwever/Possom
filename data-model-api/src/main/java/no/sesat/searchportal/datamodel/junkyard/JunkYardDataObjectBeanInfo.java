/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * JunkYardDataObjectBeanInfo.java
 *
 * Created on 30 January 2007, 20:51
 *
 */

package no.sesat.searchportal.datamodel.junkyard;

import no.sesat.searchportal.datamodel.request.*;
import no.sesat.searchportal.datamodel.generic.*;
import java.beans.PropertyDescriptor;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class JunkYardDataObjectBeanInfo extends MapDataObjectBeanInfo{

    // Constants -----------------------------------------------------
    
    private static final Class BEAN_CLASS = JunkYardDataObject.class;

    private static final Logger LOG = Logger.getLogger(JunkYardDataObjectBeanInfo.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of MapDataObjectBeanInfo */
    public JunkYardDataObjectBeanInfo() {
    }

    // Public --------------------------------------------------------

    @Override
    public PropertyDescriptor[] getPropertyDescriptors(){

        return MapDataObjectBeanInfo.addSingleMappedPropertyDescriptor("value", BEAN_CLASS);
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
