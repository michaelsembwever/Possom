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
 *
 * SearchCommandExecutorFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.sesat.search.mode.executor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.mode.SearchMode.SearchCommandExecutorConfig.Controller;

/** Obtain a working ResultHandler from a given ResultHandlerConfig.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchCommandExecutorFactory {

    // Constants -----------------------------------------------------

    private static final Map<Class<? extends SearchCommandExecutor>,SearchCommandExecutor> INSTANCES
                = new HashMap<Class<? extends SearchCommandExecutor>,SearchCommandExecutor>();

    private static final ReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of QueryTransformerFactory */
    private SearchCommandExecutorFactory() {
    }

    // Public --------------------------------------------------------

    /**
     *
     * @param config
     * @return
     */
    public static SearchCommandExecutor getController(final SearchMode.SearchCommandExecutorConfig config){

        try{
            
            final String name = "no.sesat.search.mode.executor."
                    + SearchMode.SearchCommandExecutorConfig.class.getDeclaredField(config.name())
                    .getAnnotation(Controller.class).value();

            @SuppressWarnings("unchecked")
            final Class<? extends SearchCommandExecutor> cls
                    = (Class<? extends SearchCommandExecutor>)config.getClass().getClassLoader().loadClass(name);
            
            SearchCommandExecutor result = null;
            
            try{
                INSTANCES_LOCK.readLock().lock();
                result = INSTANCES.get(cls);   
                
            } finally {
                INSTANCES_LOCK.readLock().unlock();
            }
            try{
                INSTANCES_LOCK.writeLock().lock();
            
                if(null == result){
                    final Constructor<? extends SearchCommandExecutor> constructor = cls.getConstructor();

                    result = constructor.newInstance();
                    
                    INSTANCES.put(cls, result);
                }
            }finally{
                INSTANCES_LOCK.writeLock().unlock();
            }
            
            return result;

        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        } catch (NoSuchFieldException ex) {
            throw new IllegalArgumentException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
