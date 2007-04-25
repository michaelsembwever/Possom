/*
 * SiteClassLoaderFactory.java
 *
 * Created on 20 December 2006, 16:22
 *
 */

package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This provides class loaders capable of loading classes from the skins. 
 *
 * @author magnuse
 */
public final class SiteClassLoaderFactory implements SiteKeyedFactory {
    public interface Context extends SiteContext {
    }

    private static final String LOADING_CLASS = "Loading class from URL: ";

    private static final String CREATING_CLASS_LOADER = "Creating new class loader for ";
    private static final String CLASS_LOADER_CREATED = "New URL class loader created. Loading from urls: ";
    private static final String CLASS_LOADER_REMOVED = "Class loader removed for: ";
    private static final String INVALID_URL = "Invalid URL";
    private static final String CLASS_LOADER_WANTED = "Looking for an existing class loader for ";

    private static final Map<Site, SiteClassLoaderFactory> INSTANCES = new HashMap<Site, SiteClassLoaderFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(SiteConfiguration.class);

    /** The site for which to load classes */
    private final Site site;

    /** The actual class loader for the site */
    private final ClassLoader classLoader;

    private class T extends ClassLoader {
        
    }

    /**
     * Creates a new class loader for site.
     *
     * @param site The site.
     * @param parentClassLoader Classloader of parent site.
     */
    private SiteClassLoaderFactory(final Site site, final ClassLoader parentClassLoader) {
        this.site = site;
        final URL[] urls = {getConnectionURL(getURLToLoadFrom())};
        classLoader = new URLClassLoader(urls, parentClassLoader);

        LOG.info(CLASS_LOADER_CREATED + StringUtils.join(urls, ','));
    }

    /**
     * Returns a class 
     *
     * @param site The site to get a class loader factory for.
     * @return a class loader factory for the site.
     */
    public static SiteClassLoaderFactory valueOf(final Site site) {

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
                        INSTANCES.put(site, new SiteClassLoaderFactory(site, getParentClassLoader(site)));
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
     * @param site The site.
     * @return the parents class loader.
     */
    private static ClassLoader getParentClassLoader(Site site) {
        return null != site.getParent() ? valueOf(site.getParent()).getClassLoader() : site.getClass().getClassLoader();
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

    private URL getURLToLoadFrom() {
        try {
            return new URL("http://"
                    + site.getName()
                    + site.getConfigContext()
                    + "classes/");
        } catch (MalformedURLException e) {
            LOG.error(INVALID_URL, e);
            return null;
        }
    }

    /**
     * This class creates a new URL form the given one where the host has been replaced with localhost. Any connections
     * initiated using the returned URL will instead use the host as a host header.
     *
     * @param url The url (e.g. http://sesam.no/pix.gif)
     * @return The new url (e.g. http://localhost/pix.gif and with a URLStreamHandler causing URLConnection to add
     *         sesam.no as a host header)
     */
    private URL getConnectionURL(final URL url) {

        LOG.info(LOADING_CLASS + url);

        try {

            final URLStreamHandler customHeaders = new URLStreamHandler() {
                protected URLConnection openConnection(final URL u) throws IOException {
                    final URL connectionURL = new URL(u.getProtocol(), "localhost", u.getPort(), u.getFile());
                    final URLConnection connection = connectionURL.openConnection();
                    connection.addRequestProperty("host", u.getHost());
                    return connection;
                }
            };

            return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), customHeaders);
        } catch (MalformedURLException e) {
            LOG.error(INVALID_URL, e);
            return null;
        }
    }
}