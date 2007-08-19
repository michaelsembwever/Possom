package no.sesat.search.site.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

/**
 * @author magnuse
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