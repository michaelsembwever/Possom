// Copyright (2007) Schibsted Søk AS
/*
 * JunkYardDataObjectBeanInfo.java
 *
 * Created on 30 January 2007, 20:51
 *
 */

package no.schibstedsok.searchportal.datamodel.junkyard;

import no.schibstedsok.searchportal.datamodel.request.*;
import no.schibstedsok.searchportal.datamodel.generic.*;
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
