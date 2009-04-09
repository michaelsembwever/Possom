/*
 * Copyright (2005-2007) Schibsted ASA
 *   This file is part of SESAT.
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
 * DataModelAccessException.java
 *
 * Created on 24/09/2007, 11:15:39
 *
 */

package no.sesat.search.datamodel.access;

import java.lang.reflect.Method;

/**
 *
 *
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
