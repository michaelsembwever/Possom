/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * DataModelAccessException.java
 * 
 * Created on 24/09/2007, 11:15:39
 * 
 */

package no.sesat.search.datamodel.access;

import java.lang.reflect.Method;

/**
 *
 * @author mick
 * @version $Id$
 */
public final class DataModelAccessException extends IllegalAccessException {
    
    private static final String ERR_DENIED = "Failure to honour the Access control annotations on ";

    /**
     * Constructs an instance of <code>DataModelAccessException</code> with a specified detail message against the
     * noted method that access was attempted to against the noted level.
     * @param msg the detail message.
     */
    public DataModelAccessException(final Method method, final ControlLevel level) {
        super(ERR_DENIED + method.getName() + " against " + level);
    }
}
