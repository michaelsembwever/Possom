/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * Created on May 11, 2007, 10:15:37 PM
 */
package no.sesat.search.view.velocity;

import org.apache.velocity.runtime.resource.ResourceManagerImpl;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.log4j.Logger;
import org.apache.commons.lang.time.StopWatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.MessageFormat;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A quicker replacement for {@link org.apache.velocity.runtime.resource.ResourceManagerImpl} that avoids
 * doing resource loading and parsing while holding a global exclusive lock. This implementation creates new resource
 * instances instead of updating existing ones and loads and parses them in the background. This removes the need for
 * locking and the only synchronization done is whatever synchronization measures the cache is taking. Resources not yet
 * in the cache are loaded synchronously. The reduced locking is achieved at the expense of resources possibly getting
 * loaded several times during startup or reload.
 *
 * @author Magnus Eklund
 * @version $Id$
 */
public final class QuickResourceManagerImpl extends ResourceManagerImpl {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final String RESOURCE_NOT_FOUND = "ResourceManager :'{0}'' not found in any resource loader.";
    private static final String RESOURCE_PARSE_EXCEPTION = "ResourceManager.getResource() parse exception";
    private static final String RESOURCE_EXCEPTION = "ResourceManager.getResource() exception new";
    private static final String LOADED_VELOCITY_RESOURCE = "Loaded velocity resource {0} in {1}";
    private static final String CHECKED_MODIFICATION = "Checked modification of velocity resource {0} in {1}";

    private static final Logger LOG = Logger.getLogger(QuickResourceManagerImpl.class);
    private static final String DEBUG_POOL_COUNT = "Pool size: ";

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource(final String name, final int type, final String encoding) throws Exception {

        final Resource resource = globalCache.get(type + name);

        if (resource != null) {

            // Use cached resource for this invocation but also start a thread to update cache with a brand new
            // resource instance.
            if (resource.requiresChecking()) {
                
                // Touch the resource so that a closely following caller won't trigger an update thread.
                // Keeps updates of the same resource from piling up when traffic is high.
                resource.touch();
                EXECUTOR.submit(new Loader(resource, name, type, encoding));
            }

            return resource;
            
        } else {
            
            return new Loader(resource, name, type, encoding).load();
        }
    }

    /**
     * Class capable of loading a resource and updating the cache.
     */
    private final class Loader implements Runnable {

        private final String name;
        private final int type;
        private final String enc;
        private final String key;
        private final Resource oldResource;
        private boolean modified = false;

        /**
         * Creates a new resource loader. The supplied <tt>oldResource</tt> will not be refreshed but a new one will
         * replace it in the cache. This should be safe with regards to the rest of velocity since the default cache
         * implementation is backed by a LRUMap discarding resources at will.
         *
         * @param oldResource The resource being refreshed. Null if loaded for the first time.
         * @param name The name of the resource to load.
         * @param type The type of the resource to load.
         * @param enc The encoding of the resource.
         */
        private Loader(final Resource oldResource, final String name, final int type, final String enc) {
            
            this.oldResource = oldResource;
            this.name = name;
            this.type = type;
            this.enc = enc;

            key = type + name;
            
            if(LOG.isDebugEnabled() && EXECUTOR instanceof ThreadPoolExecutor){
                final ThreadPoolExecutor tpe = (ThreadPoolExecutor)EXECUTOR;
                LOG.debug(DEBUG_POOL_COUNT + tpe.getActiveCount() + '/' + tpe.getPoolSize());
            }
        }

        /**
         * Loads the resource if it has been modified since it was last loaded.
         *
         * @return the resource.
         * @throws ResourceNotFoundException if the resource wasn't found.
         * @throws ParseErrorException if the resource couldn't be parsed.
         * @throws Exception ?
         */
        private Resource load() throws Exception {

            final StopWatch stopWatch = new StopWatch();

            try {
                stopWatch.start();
                modified = oldResource == null || oldResource.isSourceModified();
                stopWatch.split();

                if (modified) {
                    final Resource newResource = loadResource(name, type, enc);

                    if (newResource.getResourceLoader().isCachingOn()) {
                        globalCache.put(key, newResource);
                    }
                    return newResource;
                } else {
                    return oldResource;
                }
            } catch (ResourceNotFoundException rnfe) {
                log.error(MessageFormat.format(RESOURCE_NOT_FOUND, name));
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

                stopWatch.stop();

                if (oldResource != null && LOG.isInfoEnabled()) {
                    LOG.info(MessageFormat.format(CHECKED_MODIFICATION, key, stopWatch.toSplitString()));
                }
                if (modified && LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(LOADED_VELOCITY_RESOURCE, key, stopWatch.toString()));
                }
            }
        }

        /**
         * Loads resource if it has been modified since it was last loaded.
         */
        public void run() {
            
            try {
                load();
                
            }  catch (ResourceNotFoundException rnfe) {
                log.error(MessageFormat.format(RESOURCE_NOT_FOUND, name));
            } catch (ParseErrorException pee) {
                log.error(RESOURCE_PARSE_EXCEPTION, pee);
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                log.error(RESOURCE_EXCEPTION, e);
            }
        }
    }
}
