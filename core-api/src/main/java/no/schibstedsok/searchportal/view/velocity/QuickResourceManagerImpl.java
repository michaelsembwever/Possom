/* Copyright (2005-2007) Schibsted S¿k AS
 *
 * Created on May 11, 2007, 10:15:37 PM
 */
package no.schibstedsok.searchportal.view.velocity;

import org.apache.velocity.runtime.resource.ResourceManagerImpl;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.text.MessageFormat;

/**
 * A quicker replacement for {@link org.apache.velocity.runtime.resource.ResourceManagerImpl} that avoids
 * synchronization on every call. It will acquire an exclusive lock once every modification check (as specified by the
 * property <tt>url.resource.loader.modificationCheckInterval</tt>)
 *
 * @author Magnus Eklund
 */
public final class QuickResourceManagerImpl extends ResourceManagerImpl {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final String RESOURCE_NOT_FOUND = "ResourceManager :'{0}'' not found in any resource loader.";
    private static final String RESOURCE_PARSE_EXCEPTION = "ResourceManager.getResource() parse exception";
    private static final String RESOURCE_EXCEPTION = "ResourceManager.getResource() exception new";

    /**
     * {@inheritDoc}
     */
    public Resource getResource(final String resourceName, final int resourceType, final String encoding)
            throws Exception {

        final String resourceKey = resourceType + resourceName;

        Resource resource;

        try {

            lock.readLock().lock();
            resource = globalCache.get(resourceKey);

            if (resource != null) {
                // Resource is cached. Check if it needs an age check.
                if (resource.requiresChecking()) {
                    try {
                        lock.readLock().unlock();
                        lock.writeLock().lock();
                        refreshResource(resource, encoding);
                    }
                    catch (ResourceNotFoundException rnfe) {
                        globalCache.remove(resourceKey);
                        return getResource(resourceName, resourceType, encoding);
                    } finally {
                        // Downgrade to read lock to restore the locking state of the outer read lock.
                        lock.writeLock().unlock();
                        lock.readLock().lock();
                    }
                }
            } else {
                try {
                    // Resource needs to be loaded. Acquire exclusive lock.
                    lock.readLock().unlock();
                    lock.writeLock().lock();

                    resource = loadResource(resourceName, resourceType, encoding);

                    if (resource.getResourceLoader().isCachingOn()) {
                        globalCache.put(resourceKey, resource);
                    }
                } catch (ResourceNotFoundException rnfe) {
                    log.error(MessageFormat.format(RESOURCE_NOT_FOUND, resourceName));
                    throw rnfe;
                } catch (ParseErrorException pee) {
                    log.error(RESOURCE_PARSE_EXCEPTION, pee);
                    throw pee;
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    log.error(RESOURCE_EXCEPTION, e);
                    throw e;
                } finally {
                    // Downgrade to read lock to restore the locking state of the outer read lock.
                    lock.writeLock().unlock();
                    lock.readLock().lock();
                }

            }
            return resource;
        } finally {
            lock.readLock().unlock();
        }
    }
}
