/* Copyright (2009) Schibsted Søk AS
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
 *
 */

package no.sesat.search.site;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;

/** Abstract implementation of a SiteKeyedFactory to help with
 * the INSTANCES map and locking pattern typically used around it using ReentrantReadWriteLocks.
 *
 * @version $Id$
 */
public abstract class AbstractSiteKeyedFactory implements SiteKeyedFactory{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractSiteKeyedFactory.class);

    private static final String ERR_DOC_BUILDER_CREATION = " failed to construct new factory instance for ";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    /** Handles the locking pattern around the INSTANCES map that's done with a ReentrantReadWriteLock.
     * @see http://permalink.gmane.org/gmane.comp.java.sesat.kernel.devel/228
     *
     * @param <T> the type of SiteKeyedFactory that can be constructed.
     * @param site the site this factory will answer to.
     * @param instances the map of factories of type T already in existence.
     * @param instancesLock the lock used around the instances map
     * @param constructor the wrapper around the constructor used to create factories of type T.
     * @return the singleton instance of factory of type T related to the given site.
     */
    protected static final <T extends AbstractSiteKeyedFactory> T instanceOf(
            final Site site,
            final Map<Site,T> instances,
            final ReentrantReadWriteLock instancesLock,
            final FactoryConstructor<T> constructor){

        try {
            instancesLock.readLock().lock();
            if (!instances.containsKey(site)) {
                // It is not possible to upgrade a read lock...
                instancesLock.readLock().unlock();
                instancesLock.writeLock().lock();
                try {
                    // ...so check the condition again.
                    if (!instances.containsKey(site)) {
                        instances.put(site, constructor.construct());
                    }
                } catch (SiteKeyedFactoryInstantiationException e) {
                    LOG.error(constructor + ERR_DOC_BUILDER_CREATION + site, e);
                } finally {
                    // Downgrade to a read lock.
                    instancesLock.readLock().lock();
                    instancesLock.writeLock().unlock();
                }
            }
            return instances.get(site);
        } finally {
            // Finally release the read lock.
            instancesLock.readLock().unlock();
        }
    }

    // Constructors --------------------------------------------------

    protected AbstractSiteKeyedFactory() {
    }

    // Public --------------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

    /** Wraps a constructor so that it may be delegated to from the instanceOf method.
     *
     * @param <T> the type of SiteKeyedFactory that can be constructed.
     */
    protected interface FactoryConstructor<T extends AbstractSiteKeyedFactory>{
        /** Wraps a constructor so that it may be delegated to from the instanceOf method.
         *
         * @return the new SiteKeyedFactory instance
         * @throws no.sesat.search.site.SiteKeyedFactoryInstantiationException any failure in construction.
         */
        T construct() throws SiteKeyedFactoryInstantiationException;
    }
}
