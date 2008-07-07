/* Copyright (2008) Schibsted Søk AS
 * This file is part of SESAT.
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
 */

package no.sesat.search.query.token;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.commons.ioc.BaseContext;
import no.sesat.search.query.QueryStringContext;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactory;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.site.config.SiteClassLoaderFactory;
import no.sesat.search.site.config.Spi;
import org.apache.log4j.Logger;

/** EvaluatorFactory responsible to finding appropriate factory subclass to use given the context.
 *
 * Any implementing EvaluatorFactory provides two main methods
 *  isResponsibleFor(token)
 *  getEvaluator(token)
 * which are used by primarily by the TokenEvaluationEngine to find applicable evaluators and to use them.
 *
 * SKER3540
 * @version $Id$
 */
public abstract class AbstractEvaluatorFactory implements SiteKeyedFactory {

    /**
     * The context the RegExpEvaluatorFactory must work against.
     */
    public interface Context extends BaseContext, ResourceContext, SiteContext, QueryStringContext {
        /** Fully qualified classname for the factory responsible for finding related evaluators.
         * @return the fully qualified factory classname.
         */
        String getEvaluatorFactoryClassName();
        /** A uniqueId for the request.
         * Is not mission critical, ie can be left leave blank.
         * @return the uniqueId
         */
        String getUniqueId();
    }

    // Constants -----------------------------------------------------

    private static final Map<Site,Map<String,AbstractEvaluatorFactory>> INSTANCES
            = new HashMap<Site,Map<String,AbstractEvaluatorFactory>>();

    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(AbstractEvaluatorFactory.class);
    private static final String ERR_MUST_USE_CONTEXT_CONSTRUCTOR = "Must use constructor that supplies a context!";

    // Attributes ----------------------------------------------------

    private final Context context;

    // Static --------------------------------------------------------

    /** find the appropriate factory subclass to use given the context.
     *
     * @param cxt supplied context.
     * @return the appropriate factory subclass.
     */
    public static final AbstractEvaluatorFactory instanceOf(final Context cxt) {

        final Site site = cxt.getSite();
        final String clsName = cxt.getEvaluatorFactoryClassName();

        checkSiteMapExists(site);

        AbstractEvaluatorFactory instance;
        try {
            INSTANCES_LOCK.readLock().lock();

            instance = INSTANCES.get(site).get(clsName);

        }finally {
            INSTANCES_LOCK.readLock().unlock();
        }
        if (null == instance) {

            try {
                INSTANCES_LOCK.writeLock().lock();

                final SiteClassLoaderFactory f = SiteClassLoaderFactory.instanceOf(createClassLoadingContext(cxt));

                final Class clazz = f.getClassLoader().loadClass(clsName);

                @SuppressWarnings("unchecked")
                final Constructor<? extends AbstractEvaluatorFactory> s = clazz.getConstructor(Context.class);

                instance = s.newInstance(cxt);
                INSTANCES.get(site).put(clsName, instance);

            }catch (InstantiationException ex) {
                throw new IllegalArgumentException("Unable to construct AbstractEvaluatorFactory: " + clsName, ex);
            }catch (IllegalAccessException ex) {
                throw new IllegalArgumentException("Unable to construct AbstractEvaluatorFactory: " + clsName, ex);
            }catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unable to construct AbstractEvaluatorFactory: " + clsName, ex);
            }catch (InvocationTargetException ex) {
                throw new IllegalArgumentException("Unable to construct AbstractEvaluatorFactory: " + clsName, ex);
            }catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("Unable to construct AbstractEvaluatorFactory: " + clsName, ex);
            }catch (SecurityException ex) {
                throw new IllegalArgumentException("Unable to construct AbstractEvaluatorFactory: " + clsName, ex);
            }catch(ClassNotFoundException ex){
                throw new IllegalArgumentException("Unable to construct AbstractEvaluatorFactory: " + clsName, ex);
            }finally{
                INSTANCES_LOCK.writeLock().unlock();
            }
        }
        return instance;
    }

    // Constructors --------------------------------------------------

    /**
     * Illegal Constructor. Must use AbstractEvaluatorFactory(SiteContext).
     */
    private AbstractEvaluatorFactory() {
        throw new IllegalArgumentException(ERR_MUST_USE_CONTEXT_CONSTRUCTOR);
    }

    /** All factory implementations must super to this in their constructor.
     * @param context the supplied context to work within.
     * @throws SiteKeyedFactoryInstantiationException  if factory construction fails.
     */
    protected AbstractEvaluatorFactory(final Context context) throws SiteKeyedFactoryInstantiationException{

        this.context = context;
    }

    // Public --------------------------------------------------------

    /**
     * Is the factory responsibe for evaluation of this TokenPredicate.
     * Default implementation checks that getEvaluator(..) does not return ALWAYS_FALSE_EVALUATOR
     * @param token the TokenPredicate we're checking
     * @return true if factory is responsible for evaluating the given TokenPredicate.
     */
    public boolean isResponsibleFor(final TokenPredicate token){

        try {
            return TokenEvaluationEngineImpl.ALWAYS_FALSE_EVALUATOR != getEvaluator(token);

        }catch (EvaluationException ex) {
            LOG.error("failed trying to find evaluator", ex);
            return false;
        }
    }

    /**
     * If the evaluator is not found in this site it will fallback and look in the parent site.
     * @param token the predicate the evaluator is to be used for
     * @return the TokenEvaluator to use. Or TokenEvaluationEngineImpl.ALWAYS_FALSE_EVALUATOR if not found.
     * @throws EvaluationException
     */
    public abstract TokenEvaluator getEvaluator(final TokenPredicate token) throws EvaluationException;

    public boolean remove(final Site site) {

        try {
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }
        finally {
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /** Obtain the context we're working within.
     *
     * @return the context.
     */
    protected final Context getContext(){
        return context;
    }

    // Private -------------------------------------------------------

    private static SiteClassLoaderFactory.Context createClassLoadingContext(final Context context) {

        return new no.sesat.search.site.config.SiteClassLoaderFactory.Context() {
            public BytecodeLoader newBytecodeLoader(SiteContext siteContext, String className, String jarFileName) {
                return context.newBytecodeLoader(siteContext, className, jarFileName);
            }

            public Site getSite() {
                return context.getSite();
            }

            public Spi getSpi() {
                return Spi.QUERY_EVALUATION;
            }
        };
    }

    private static void checkSiteMapExists(final Site site){

        final Map<String,AbstractEvaluatorFactory> factories;
        try {
            INSTANCES_LOCK.readLock().lock();

            factories = INSTANCES.get(site);

        }finally {
            INSTANCES_LOCK.readLock().unlock();
        }
        if(null == factories){
            try {
                INSTANCES_LOCK.writeLock().lock();

                INSTANCES.put(site, new HashMap<String,AbstractEvaluatorFactory>());

            }finally{
                INSTANCES_LOCK.writeLock().unlock();
            }
        }

    }

    // Inner classes -------------------------------------------------
}
