/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * Created on May 11, 2007, 8:16:37 PM
 */
package no.sesat.search.view.velocity;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceCache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A faster implementation of the resource cache than the default. The default implementation uses a fully synchronized
 * LRUMap.
 * <p> This implementation only supports an unbounded cache size and will throw <tt>IllegalArgumentException</tt> if
 * {@link org.apache.velocity.app.Velocity} is set to anything other than zero.
 *
 * @see org.apache.velocity.runtime.resource.ResourceCacheImpl
 *
 * @author Magnus Eklund
 */
public final class QuickResourceCacheImpl implements ResourceCache {

    /** Property name for controlling the initial size of the cache. */
    public static final String INITIAL_SIZE_PROPERTY = "resource.manager.quickcache.size";

    private Map<Object, Resource> cache;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final String BOUNDED_CACHE_NOT_SUPPORTED =
            "This implementatation only supports an unbounded cache. Change impl using property ";

    /**
     * {@inheritDoc}
     */
    public void initialize(final RuntimeServices svc) {
        if (svc.getInt(RuntimeConstants.RESOURCE_MANAGER_DEFAULTCACHE_SIZE, 0) != 0) {
            throw new IllegalArgumentException(BOUNDED_CACHE_NOT_SUPPORTED + Velocity.RESOURCE_MANAGER_CLASS);
        }

        // Writes are rare once SESAT has inititalized a site so a HashMap should be faster than a ConcurrentHashMap.
        cache = new HashMap<Object, Resource>(svc.getInt(INITIAL_SIZE_PROPERTY, 100));
    }

    /**
     * {@inheritDoc}
     */
    public Resource get(final Object o) {
        try {
            lock.readLock().lock();
            return cache.get(o);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Resource put(final Object o, final Resource resource) {
        try {
            lock.writeLock().lock();
            return cache.put(o, resource);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Resource remove(final Object o) {
        try {
            lock.writeLock().lock();
            return cache.remove(o);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator enumerateKeys() {
        try {
            lock.readLock().lock();
            return cache.keySet().iterator();
        } finally {
            lock.readLock().unlock();
        }
    }
}
