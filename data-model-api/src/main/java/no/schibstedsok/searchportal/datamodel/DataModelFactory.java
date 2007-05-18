// Copyright (2007) Schibsted Søk AS
/*
 * DataModelFactory.java
 *
 * Created on 23 January 2007, 09:27
 *
 */

package no.schibstedsok.searchportal.datamodel;

import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.schibstedsok.searchportal.site.config.PropertiesContext;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import org.apache.log4j.Logger;


/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public abstract class DataModelFactory implements SiteKeyedFactory{

    /**
     * The context any DataModelFactory must work within.
     */
    public interface Context extends BaseContext, SiteContext, PropertiesContext {
    }

   // Constants -----------------------------------------------------

    private static final Map<Site, DataModelFactory> INSTANCES = new HashMap<Site,DataModelFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    private static final String DATA_MODEL_FACTORY_IMPL = "sesam.datamodel.impl";

    private static final Logger LOG = Logger.getLogger(DataModelFactory.class);

    // Attributes ----------------------------------------------------

    private final Context context;

    // Static --------------------------------------------------------

    /**
     * 
     * @param cxt 
     * @return 
     * @throws no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException 
     */
    public static DataModelFactory valueOf(final Context cxt) throws SiteKeyedFactoryInstantiationException {

        final Site site = cxt.getSite();
        assert null != site;

        DataModelFactory instance;
        try{
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        }finally{
            INSTANCES_LOCK.readLock().unlock();
        }

        if (instance == null) {
            try{
                INSTANCES_LOCK.writeLock().lock();

                instance = newInstance(cxt);

                // update the store of DataModelFactory instances
                INSTANCES.put(cxt.getSite(), instance);

            }finally{
                INSTANCES_LOCK.writeLock().unlock();
            }
        }
        return instance;
    }

    /**
     * 
     * @param site 
     * @return 
     */
    public boolean remove(final Site site){

        try{
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }


    // Constructors --------------------------------------------------

    private DataModelFactory(){
        throw new IllegalArgumentException("Must use constructor with Context argument.");
    }

    /** Creates a new instance of DataModelToolkitFactory 
     * @param cxt 
     */
    protected DataModelFactory(final Context cxt) {

        context = cxt;
    }

    // Public --------------------------------------------------------

    /**
     * 
     * @return 
     */
    public abstract DataModel instantiate();

    /**
     * <pre>
     * Example usage:
     *     DataModelFactory dm = DataModelFactory.valueOf(null);
     *     BrowserDataObject bdo = dm.instantiate(
     *       BrowserDataObject.class,
     *       new DataObject.Property("locale", null),
     *       new DataObject.Property("supportedLocales", null));
     * </pre>
     ** @param cls 
     * @param properties 
     * @return 
     */
    public abstract <T> T instantiate(final Class<T> cls, DataObject.Property... properties);
    
    /** Lets the datamodel instance know that it has moved on and is now being accessed by the next level in the 
     * control process stack.
     ** @param datamodel 
     * @return 
     */
    public abstract DataModel incrementControlLevel(final DataModel datamodel);

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    /** Constructs an DataModelFactory implementation.
     * Matching the class as specified
     * by the value for DATA_MODEL_FACTORY_IMPL in the Site's configuration.properties.
     **/
    private static DataModelFactory newInstance(final Context cxt) throws SiteKeyedFactoryInstantiationException{

        try{

            final SiteConfiguration siteConf = SiteConfiguration.valueOf(
                    ContextWrapper.wrap(SiteConfiguration.Context.class, cxt));

            final String clsName = siteConf.getProperty(DATA_MODEL_FACTORY_IMPL);
            LOG.info("constructing for " + cxt.getSite() + " instance of " + clsName);
            final Class<DataModelFactory> cls = (Class<DataModelFactory>) Class.forName(clsName);

            return cls.getDeclaredConstructor(Context.class).newInstance(cxt);


        }catch (ClassNotFoundException ex) {
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch (NoSuchMethodException ex) {
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch (InstantiationException ex) {
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch(IllegalArgumentException ex){
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch(IllegalAccessException ex){
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }catch(InvocationTargetException ex){
            throw new SiteKeyedFactoryInstantiationException(ex.getMessage(), ex);

        }
    }

    // Inner classes -------------------------------------------------

}
