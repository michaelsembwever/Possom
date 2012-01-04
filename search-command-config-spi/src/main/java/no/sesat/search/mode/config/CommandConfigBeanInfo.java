/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */

package no.sesat.search.mode.config;

import java.beans.IntrospectionException;

/**
 * BeanInfo for the CommnadConfig class
 *
 */
public class CommandConfigBeanInfo extends AbstractBeanInfoHelper {

    public CommandConfigBeanInfo() throws ClassNotFoundException, IntrospectionException {
        this(CommandConfig.class);
    }

    public CommandConfigBeanInfo(Class<?> clas) throws ClassNotFoundException, IntrospectionException {
        super(clas);
        add("resultFields", "getResultFields", "addResultFields");
    }
}
