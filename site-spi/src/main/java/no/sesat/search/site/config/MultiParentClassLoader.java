/* Copyright (2007) Schibsted Søk AS
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
 */
package no.sesat.search.site.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

/**
 *
 */
public abstract class MultiParentClassLoader extends ClassLoader {

    private final List<ClassLoader> parents = new ArrayList<ClassLoader>();

    public MultiParentClassLoader() {
    }

    public MultiParentClassLoader(ClassLoader ...  parents) {
        this.parents.addAll(Arrays.asList(parents));
    }

    public final synchronized void addParentClassLoader(final ClassLoader parent) {
        parents.add(parent);
    }

    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException
    {
        // First, check if the class has already been loaded
        Class c = findLoadedClass(name);

        if (c == null) {

            // ...then try all of the parents halting if found.
            for (final Iterator<ClassLoader> iterator = parents.iterator(); iterator.hasNext() && c == null;) {
                try {
                    c = iterator.next().loadClass(name);
                } catch (ClassNotFoundException e) {
                    //
                }
            }

            // ....and finally try this class loader.
            if (c == null) {
                c = findClass(name);
            }

            if (resolve) {
                resolveClass(c);
            }

        }

        return c;
    }
}
