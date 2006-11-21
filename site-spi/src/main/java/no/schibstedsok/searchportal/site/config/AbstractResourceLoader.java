
/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractResourceLoader.java
 *
 * Created on 23 January 2006, 10:57
 *
 */

package no.schibstedsok.searchportal.site.config;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Utility class to handle loading different types of resources in a background thread.
 * This avoids the problem of having to order loading of applications in the container because of static initialisers
 *  using resources from the search-front-config application.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
abstract class AbstractResourceLoader
        implements Runnable, DocumentLoader, PropertiesLoader {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final Logger LOG = Logger.getLogger(AbstractResourceLoader.class);
    private static final String DEBUG_POOL_COUNT = "Pool size: ";

    private final SiteContext context;
    private volatile String resource;
    private volatile Future future;

    /** the properties resource holder. **/
    protected Properties props;
    /** DocumentBuilder builder **/
    protected DocumentBuilder builder;
    /** Document **/
    protected Document document;

    private static final String ERR_MUST_USE_PROPS_INITIALISER = "Must use properties initialiser to use this method!";
    private static final String ERR_MUST_USE_XSTREAM_INITIALISER = "Must use xstream initialiser to use this method!";
    private static final String ERR_ONE_USE_ONLY = "This AbstractResourceLoader instance already in use!";
    private static final String ERR_MUST_USE_CONTEXT_CONSTRUCTOR = "Must use constructor that supplies a context!";
    private static final String ERR_INTERRUPTED_WAITING_4_RSC_2_LOAD = "Interrupted waiting for resource to load";
    private static final String ERR_NOT_INITIALISED = "This AbstractResourceLoader has not been initialised. Nothing to wait for!";
    private static final String WARN_USING_FALLBACK = "Falling back to default version for resource ";
    private static final String FATAL_RESOURCE_NOT_LOADED = "Resource not found ";
    private static final String WARN_PARENT_SITE = "Parent site is: ";
    
    /** Illegal Constructor. Must use AbstractResourceLoader(SiteContext). */
    private AbstractResourceLoader() {
        throw new IllegalArgumentException(ERR_MUST_USE_CONTEXT_CONSTRUCTOR);
    }

    /** Creates a new instance of AbstractResourceLoader.
     *@param cxt the context that we supply us with which site we are dealing with.
     */
    protected AbstractResourceLoader(final SiteContext cxt) {
        context = cxt;
    }
    
    public abstract boolean urlExists(String url);
    
    protected abstract String getResource(final Site site);
    
    protected abstract String getUrlFor(final String resource);
    
    protected abstract InputStream getInputStreamFor(final String resource);

    /** Get the SiteContext.
     *@return the SiteContext.
     **/
    protected SiteContext getContext() {
        return context;
    }

    /** Get the resource name/path this class is responsible for retrieving.
     *@return the resource name/path.
     **/
    protected String getResource() {
        return resource;
    }

    /** {@inheritDoc}
     */
    public Properties getProperties() {
        if (props == null) {
            throw new IllegalStateException(ERR_MUST_USE_PROPS_INITIALISER);
        }
        return props;
    }

    /** {@inheritDoc}
     */
    public Document getDocument() {
        if (builder == null) {
            throw new IllegalStateException(ERR_MUST_USE_XSTREAM_INITIALISER);
        }
        return document;
    }


    /** {@inheritDoc}
     */
    public void init(final String resource, final Properties props) {

        if (future != null && !future.isDone()) {
            throw new IllegalStateException(ERR_ONE_USE_ONLY);
        }
        this.resource = resource;
        this.props = props;
        future = EXECUTOR.submit(this);
        
        if(LOG.isDebugEnabled() && EXECUTOR instanceof ThreadPoolExecutor){
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)EXECUTOR;
            LOG.debug(DEBUG_POOL_COUNT + tpe.getActiveCount() + '/' + tpe.getPoolSize());
        }
    }


    /** {@inheritDoc}
     */
    public void init(final String resource, final DocumentBuilder builder) {

        if (future != null && !future.isDone()) {
            throw new IllegalStateException(ERR_ONE_USE_ONLY);
        }
        this.resource = resource;
        this.builder = builder;
        future = EXECUTOR.submit(this);
        
        if(LOG.isDebugEnabled() && EXECUTOR instanceof ThreadPoolExecutor){
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)EXECUTOR;
            LOG.debug(DEBUG_POOL_COUNT + tpe.getActiveCount() + '/' + tpe.getPoolSize());
        }
    }

    /** {@inheritDoc}
     */
    public void abut() {

        if (future == null) {
            throw new IllegalStateException(ERR_NOT_INITIALISED);
        }
        try {
            future.get();
        } catch (InterruptedException ex) {
            LOG.error(ERR_INTERRUPTED_WAITING_4_RSC_2_LOAD, ex);
        } catch (ExecutionException ex) {
            LOG.error(ERR_INTERRUPTED_WAITING_4_RSC_2_LOAD, ex);
        }
    }

    /** {@inheritDoc}
     */
    public void run() {
        if(props != null){
            // Properties inherent through the fallback process. Keys are *not* overridden.

            for(Site site = getContext().getSite(); site != null; site = site.getParent()){
                loadResource(getResource(site));
            }
            
        }else{
            // Default behavour: only load first found resource
            Site site = getContext().getSite();

            do {
                if (loadResource(getResource(site))) {
                    break;
                } else {
                    site = site.getParent();
                    if( null != site ){
                        LOG.warn(WARN_USING_FALLBACK + getResource(site));
                        LOG.warn(WARN_PARENT_SITE + site.getParent());
                    }
                }
            } while (site != null);

            if (site == null) {
                LOG.fatal(FATAL_RESOURCE_NOT_LOADED);
            }
        }
    }

    private boolean loadResource(final String resource) {

        boolean success = false;

        if(urlExists(resource)){
            
            final InputStream is = getInputStreamFor(resource);

            try {

                if (props != null) {
                    // only add properties that don't already exist!
                    // allows us to inherent back through the fallback process.
                    final Properties newProps = new Properties();
                    newProps.load(is);
                    
                    props.put(context.getSite().getName(), getUrlFor(resource));
                    
                    for(Object p : newProps.keySet()){

                        if(!props.containsKey(p)){
                            final String prop = (String)p;
                            props.setProperty(prop, newProps.getProperty(prop));
                        }
                    }
                }
                if (builder != null) {
                    document = builder.parse( new InputSource(new InputStreamReader(is)) );
                }

                LOG.info(readResourceDebug(resource));
                success = true;

            } catch (NullPointerException e) {
                LOG.warn(readResourceDebug(resource), e);

            } catch (IOException e) {
                LOG.warn(readResourceDebug(resource), e);
                
            } catch (SAXParseException e) {
                throw new ResourceLoadException(
                        readResourceDebug(resource) + " at " + e.getLineNumber() + ":" + e.getColumnNumber(), e);
                
            } catch (SAXException e) {
                throw new ResourceLoadException(readResourceDebug(resource), e);
                
            }finally{
                if( null != is ){
                    try{
                        is.close();
                    }catch(IOException ioe){
                        LOG.warn(readResourceDebug(resource), ioe);
                    }
                }
            }
        }
        return success;
    }
    
    protected String readResourceDebug(final String resource){
        
        return "Read Configuration from " + resource;
    }
}
