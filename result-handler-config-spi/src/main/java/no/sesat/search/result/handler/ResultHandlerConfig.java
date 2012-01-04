/* Copyright (2012) Schibsted ASA
 *   This file is part of Possom.
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
 *
 * ResultHandlerConfig.java
 *
 * Created on 26 March 2007, 17:10
 *
 */

package no.sesat.search.result.handler;

import java.io.Serializable;
import org.w3c.dom.Element;

/**
 *
 *
 * @version <tt>$Id$</tt>
 */
public interface ResultHandlerConfig extends Serializable {
    /**
     *
     * @param element
     * @return
     */
    ResultHandlerConfig readResultHandler(Element element);
}
