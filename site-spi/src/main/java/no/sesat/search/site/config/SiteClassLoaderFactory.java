/*
 * SiteClassLoaderFactory.java
 */

package no.sesat.search.site.config;

import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This provides class loaders capable of loading classes from the skins.
 *
 * @author magnuse
 * @version $Id$
 */
public final class SiteClassLoaderFactory implements SiteKeyedFactory {

    /** The context needed. */
    public interface Context extends BytecodeContext, SiteContext, SpiContext {}

    private static final String CREATING_CLASS_LOADER = "Creating new class loader for ";
    private static final String CLASS_LOADER_REMOVED = "Class loader removed for: ";
    private static final String CLASS_LOADER_WANTED = "Looking for an existing class loader for ";

    private static final Map<Site, Map<Spi, SiteClassLoaderFactory>> INSTANCES
            = new HashMap<Site, Map<Spi, SiteClassLoaderFactory>>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(SiteConfiguration.class);

    /** The actual class loader for the (site, SPI)-pair */
    private final ClassLoader classLoader;

    private SiteClassLoaderFactory(final Context context) {

        final SpiClassLoader.Context classLoaderContext = new SpiClassLoader.Context() {
            public BytecodeLoader newBytecodeLoader(final SiteContext siteCxt, final String cName, final String jar) {
                return context.newBytecodeLoader(siteCxt, cName, jar);
            }
            public Site getSite() {
                return context.getSite();
            }

            public Spi getSpi() {
                return context.getSpi();
            }
        };

        classLoader = new SpiClassLoader(classLoaderContext);
    }

    /**
     * Returns a class loader factory for a site.
     *
     * @param context The site and bytecode loader.
     * @return a class loader factory for the site.
     */
    public static SiteClassLoaderFactory valueOf(final Context context) {

        final Site site = context.getSite();

        LOG.trace(CLASS_LOADER_WANTED + site);

        try {
            INSTANCES_LOCK.readLock().lock();

            if (null == INSTANCES.get(site) || null == INSTANCES.get(site).get(context.getSpi())) {
                try {
                    INSTANCES_LOCK.readLock().unlock();
                    INSTANCES_LOCK.writeLock().lock();

                    if (null == INSTANCES.get(site)) {
                        LOG.info(CREATING_CLASS_LOADER + site);
                        final Map<Spi, SiteClassLoaderFactory> spis = new HashMap<Spi, SiteClassLoaderFactory>();
                        INSTANCES.put(site, spis);
                    }

                    INSTANCES.get(site).put(context.getSpi(), new SiteClassLoaderFactory(context));
                } finally {
                    INSTANCES_LOCK.readLock().lock();
                    INSTANCES_LOCK.writeLock().unlock();
                }
            }

            return INSTANCES.get(site).get(context.getSpi());
        } finally {

            INSTANCES_LOCK.readLock().unlock();
        }
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
     * {@inheritDoc}
     */
    public boolean remove(final Site site) {
        try {
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        } finally {
            INSTANCES_LOCK.writeLock().unlock();
            LOG.info(CLASS_LOADER_REMOVED);
        }
    }
}
