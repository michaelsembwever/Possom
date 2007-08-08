package no.schibstedsok.searchportal.site.config;


import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author magnuse
 * @version $Id$
 */
public abstract class ResourceClassLoader extends ClassLoader {

    /**  Context needed by this class. */
    public interface Context extends BytecodeContext, SiteContext {}

    private final Context context;

    private Collection<String> notFound = Collections.synchronizedCollection(new HashSet<String>());
    private ReadWriteLock notFoundLock = new ReentrantReadWriteLock();

    /**
     * Creates a new resource class loader for a site.
     *
     * @param context the context.
     */
    public ResourceClassLoader(final Context context) {
        this.context = context;
    }

    public ResourceClassLoader(final Context context, final ClassLoader parent) {
        super(parent);
        this.context = context;
    }

    /**
     * Returns the jar file the class must be contained in. If null, all jar-files and classes available to the class
     * loader of the resource servlet are searched.
     *
     * @return the name of the jar file.
     */
    protected abstract String getJarName();

    /**
     * Finds classes using a {@link BytecodeLoader}.
     *
     * @param className the clas to find
     * @return the class.
     * @throws ClassNotFoundException if the class cannot be found in this class loader.
     */
    @Override
    protected Class<?> findClass(final String className) throws ClassNotFoundException {

        try {
            // Optimization. Do not try to load class if it has not been found before.
            notFoundLock.readLock().lock();
            if (notFound.contains(className)) {
                throw new ClassNotFoundException(className + " not found");
            }
        } finally {
            notFoundLock.readLock().unlock();
        }

        final BytecodeLoader loader = context.newBytecodeLoader(context, className, getJarName());
        loader.abut();

        final byte[] bytecode = loader.getBytecode();

        // Resource loader loaded empty result means class was not found.
        if (bytecode.length == 0) {
            try {
                notFoundLock.writeLock().lock();
                notFound.add(className);
            } finally {
                notFoundLock.writeLock().unlock();
            }
            throw new ClassNotFoundException(className + " not found");
        }

        return defineClass(className, bytecode, 0, bytecode.length);
    }
}
