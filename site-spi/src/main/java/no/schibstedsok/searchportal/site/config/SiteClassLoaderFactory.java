/*
 * SiteClassLoaderFactory.java
 *
 * Created on 20 December 2006, 16:22
 *
 */

package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This provides class loaders capable of loading classes from the skins. 
 *
 * @author magnuse
 */
public final class SiteClassLoaderFactory implements SiteKeyedFactory {

    public interface Context extends BytecodeContext, SiteContext {}
    
    private static final String CREATING_CLASS_LOADER = "Creating new class loader for ";
    private static final String CLASS_LOADER_REMOVED = "Class loader removed for: ";
    private static final String CLASS_LOADER_WANTED = "Looking for an existing class loader for ";

    private static final Map<Site, SiteClassLoaderFactory> INSTANCES = new HashMap<Site, SiteClassLoaderFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(SiteConfiguration.class);

    /** The actual class loader for the site */
    private final ClassLoader classLoader;

    /**
     * Creates a new class loader for site.
     *
     * @param context The context.
     * @param parentClassLoader Classloader of parent site.
     */
    private SiteClassLoaderFactory(final Context context, final ClassLoader parentClassLoader) {
        final ResourceClassLoader.Context classLoaderContext = new ResourceClassLoader.Context() {
            public Site getSite() {
                return context.getSite();
            }

            public BytecodeLoader newBytecodeLoader(SiteContext siteCxt, String className) {
                return context.newBytecodeLoader(siteCxt, className);
            }
        };

        classLoader = new ResourceClassLoader(classLoaderContext, parentClassLoader);
    }

    /**
     * Returns a class 
     *
     * @param context The site and bytecode loader.
     * @return a class loader factory for the site.
     */
    public static SiteClassLoaderFactory valueOf(final Context context) {

        final Site site = context.getSite();

        if (LOG.isTraceEnabled()) {
            LOG.trace(CLASS_LOADER_WANTED + site);
        }

        try {
            INSTANCES_LOCK.readLock().lock();
            if (null == INSTANCES.get(site)) {
                try {
                    INSTANCES_LOCK.readLock().unlock();
                    INSTANCES_LOCK.writeLock().lock();

                    if (null == INSTANCES.get(site)) {
                        LOG.info(CREATING_CLASS_LOADER + site);
                        INSTANCES.put(site, new SiteClassLoaderFactory(context, getParentClassLoader(context)));
                    }
                } finally {
                    INSTANCES_LOCK.readLock().lock();
                    INSTANCES_LOCK.writeLock().unlock();
                }
            }
            return INSTANCES.get(site);
        } finally {
            INSTANCES_LOCK.readLock().unlock();
        }
    }

    /**
     * Returns the class loader of the parent site. If the site does not have a parent, the standard class loader is
     * returned.
     *
     * @param context The context containing site.
     * @return the parents class loader.
     */
    private static ClassLoader getParentClassLoader(final Context context) {
        final Site site = context.getSite();

        final Context parentContext = new Context() {
            public BytecodeLoader newBytecodeLoader(SiteContext siteCxt, String className) {
                return context.newBytecodeLoader(siteCxt, className);
            }

            public Site getSite() {
                return context.getSite().getParent();
            }
        };

        return null != site.getParent() ? valueOf(parentContext).getClassLoader() : site.getClass().getClassLoader();
    }

    /**
     * Returns a class loader for the site.
     *
     * @return a classloader for the site.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }


    /**
     * @{inheritDoc}
     */
    public final boolean remove(final Site site) {
        try {
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        } finally {
            INSTANCES_LOCK.writeLock().unlock();
            LOG.info(CLASS_LOADER_REMOVED);
        }
    }
}