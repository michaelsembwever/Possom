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
 * ResourceLoadException.java
 *
 * Created on October 23, 2006, 12:31 PM
 */

package no.sesat.search.site.config;

/**
 *
 *
 */
public final class ResourceLoadException extends RuntimeException{ // TODO this is not a RuntimeException!

    /** Creates a new instance of ResourceLoadException */
    private ResourceLoadException() {
    }

    /** Creates a new instance of ResourceLoadException */
    public ResourceLoadException(final String msg) {
        super(msg);
    }

    /** Creates a new instance of ResourceLoadException */
    public ResourceLoadException(final String msg, final Throwable th) {
        super(msg, th);
    }

}
