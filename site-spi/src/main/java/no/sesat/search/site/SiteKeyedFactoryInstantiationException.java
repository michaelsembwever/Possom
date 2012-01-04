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
 * SiteKeyedFactoryInstantiationException.java
 *
 * Created on 23 January 2007, 19:07
 *
 */

package no.sesat.search.site;

/**
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class SiteKeyedFactoryInstantiationException extends Exception{

    public SiteKeyedFactoryInstantiationException(final String msg, final Exception cause){
        super(msg, cause);
    }
}
