/*
 * Copyright (2012) Schibsted ASA
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
package no.sesat.mojo.modes;

/**
 * Abstract configuration object eventually written to schema.
 *
 * @version $Id$
 */
public class AbstractConfig {
    private String doc;
    private String name;

    protected AbstractConfig(final String name){
        this.name =name;
    }

    /**
     * @param name
     *            Name of this attribute.
     * @param doc
     *            Doc for this attribute.
     */
    protected AbstractConfig(final String name, final String doc) {
        this.name = name;
        this.doc = doc;
    }

    /**
     * @return true if it has documentation.
     */
    public boolean hasDoc() {
        return (doc != null && !doc.trim().isEmpty());
    }

    protected String getDoc(){
        return doc;
    }

    protected final void setDoc(final String doc){
        this.doc = doc;
    }

    protected String getName(){
        return name;
    }

    protected final void setName(final String name){
        this.name = name;
    }
}
