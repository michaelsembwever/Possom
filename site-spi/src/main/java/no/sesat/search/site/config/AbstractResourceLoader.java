/* Copyright (2005-2007) Schibsted Søk AS
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
 *
 * AbstractResourceLoader.java
 *
 * Created on 23 January 2006, 10:57
 *
 */

package no.sesat.search.site.config;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
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
public abstract class AbstractResourceLoader
        implements Runnable, DocumentLoader, PropertiesLoader, BytecodeLoader {

    private enum Polymorphism{
        NONE,
        FIRST_FOUND,
        DOWN_HIERARCHY,
        UP_HEIRARCHY
    }
    
    private enum Resource{
        
        PROPERTIES(Polymorphism.UP_HEIRARCHY),
        DOM_DOCUMENT(Polymorphism.NONE),
        BYTECODE(Polymorphism.NONE);

        final private Polymorphism polymorphism;
        
        Resource(final Polymorphism polymorphism){
            this.polymorphism = polymorphism;
        }
        
        Polymorphism getPolymorphism(){
            return polymorphism;
        }
    }

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final Logger LOG = Logger.getLogger(AbstractResourceLoader.class);
    private static final String DEBUG_POOL_COUNT = "Pool size: ";

    private final SiteContext context;
    private String resource;
    private Future future;

    private Resource resourceType;
    /** the properties resource holder. **/
    protected Properties props;
    /** DocumentBuilder builder. **/
    protected DocumentBuilder builder;
    /** Document. **/
    protected Document document;

    /** Bytecode **/
    private byte[] bytecode;

    /** Name of jar to load classes from **/
    protected String jarFileName;

    private static final String ERR_MUST_USE_PROPS_INITIALISER = "Must use properties initialiser to use this method!";
    private static final String ERR_MUST_USE_XSTREAM_INITIALISER = "Must use xstream initialiser to use this method!";
    private static final String ERR_MUST_USE_BYTECODE_INITIALISER = "Must use bytecode initialiser to use this method";
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
    
    public abstract boolean urlExists(URL url);
    
    protected abstract URL getResource(final Site site);
    
    protected abstract InputStream getInputStreamFor(final URL resource);

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

    public byte[] getBytecode() {
        if (bytecode == null) {
            throw new IllegalStateException(ERR_MUST_USE_BYTECODE_INITIALISER);
        }

        return bytecode;
    }

    /** {@inheritDoc}
     */
    public void init(final String resource, final Properties props) {

        resourceType = Resource.PROPERTIES;
        preInit(resource);
        this.props = props;
        postInit();
    }


    /** {@inheritDoc}
     */
    public void init(final String resource, final DocumentBuilder builder) {

        resourceType = Resource.DOM_DOCUMENT;
        preInit(resource);
        this.builder = builder;
        postInit();
    }

    /** {@inheritDoc}
     */
    public void initBytecodeLoader(String className, String jarFileName) {
        resourceType = Resource.BYTECODE;
        this.jarFileName = jarFileName;
        preInit(className);
        postInit();
    }



    private void preInit(final String resource){
        
        if (future != null && !future.isDone()) {
            throw new IllegalStateException(ERR_ONE_USE_ONLY);
        }

        if (resourceType == Resource.BYTECODE) {
            // Convert package structure to path.
            if(!resource.endsWith(".jsp")){
                this.resource = resource.replace(".", "/") + ".class";
            }else{
                this.resource = resource;
            }
            
            if (jarFileName != null) {
                // Construct the path portion of a JarUrl.
                this.resource = jarFileName + "!/" + this.resource;
            }

        } else {
            this.resource = resource;
        }
    }
    
    private void postInit(){
        
        future = EXECUTOR.submit(this);
        
        if(LOG.isTraceEnabled() && EXECUTOR instanceof ThreadPoolExecutor){
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)EXECUTOR;
            LOG.trace(DEBUG_POOL_COUNT + tpe.getActiveCount() + '/' + tpe.getPoolSize());
        }
    }

    /** {@inheritDoc}
     */
    public void abut() {

        if (future == null) {
            throw new IllegalStateException(ERR_NOT_INITIALISED);
        }
        
        try {
            
            final long time = System.currentTimeMillis();
            future.get();
            LOG.debug("abut(" + (System.currentTimeMillis() - time) + "ms) for " + getResource(context.getSite()));
            

        } catch (InterruptedException ex) {
            LOG.error(ERR_INTERRUPTED_WAITING_4_RSC_2_LOAD, ex);
        } catch (ExecutionException ex) {
            LOG.error(ERR_INTERRUPTED_WAITING_4_RSC_2_LOAD, ex);
        }
    }

    /** {@inheritDoc}
     */
    public void run() {
        
        // Inheriting from Site & UniqueId from parent thread is meaningless in a thread pool.
        MDC.put(Site.NAME_KEY, context.getSite());
        MDC.remove("UNIQUE_ID");
        
        switch(resourceType.getPolymorphism()){
            case UP_HEIRARCHY:
                // Properties inherent through the fallback process. Keys are *not* overridden.
                boolean resourceFound = false;
                for(Site site = getContext().getSite(); site != null; site = site.getParent()){
                    resourceFound |= loadResource(getResource(site));
                }
                if (!resourceFound) {
                    throw new ResourceLoadException("Could not find resource " + getResource(context.getSite()));
                }
                break;
                
            case DOWN_HIERARCHY:
                throw new UnsupportedOperationException("Not yet implemented");
                
            case FIRST_FOUND:
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
                break;
                
            case NONE:
                // if the resource doesn't exist then fake an empty result.
                if (!loadResource(getResource(getContext().getSite()))) {
                    loadEmptyResource(getResource(getContext().getSite()));
                }
                break;
        }
    }

    private boolean loadEmptyResource(final URL url) {
        
        LOG.debug("Loading empty resource for " + resource);

        switch(resourceType){
            case PROPERTIES:
                props.put(context.getSite().getName(), url);
                break;

            case DOM_DOCUMENT:
                document = builder.newDocument();
                break;

            case BYTECODE:
                bytecode = new byte[0];
                break;
            
        }

        return true;
    }
    
    
    private boolean loadResource(final URL url) {

        boolean success = false;

        if(urlExists(url)){
            
            final InputStream is = getInputStreamFor(url);

            try {

                switch(resourceType){
                    
                    case PROPERTIES:
                        
                        // only add properties that don't already exist!
                        // allows us to inherent back through the fallback process.
                        final Properties newProps = new Properties();
                        newProps.load(is);
                        
                        props.put(context.getSite().getName(), url);
                        
                        for(Object p : newProps.keySet()){
                            
                            if(!props.containsKey(p)){
                                final String prop = (String)p;
                                props.setProperty(prop, newProps.getProperty(prop));
                            }
                        }
                        break;
                        
                    case DOM_DOCUMENT:
                        document = builder.parse( new InputSource(new InputStreamReader(is)) );
                        break;

                    case BYTECODE:
                        final ByteArrayOutputStream bytecodeOutputStream = new ByteArrayOutputStream();

                        for(int i = is.read(); i != -1; i = is.read()) {
                            bytecodeOutputStream.write(i);
                        }

                        bytecode = bytecodeOutputStream.toByteArray();
                        break;
                }

                LOG.info(readResourceDebug(url));
                success = true;

            } catch (NullPointerException e) {
                LOG.warn(readResourceDebug(url), e);

            } catch (IOException e) {
                LOG.warn(readResourceDebug(url), e);
                
            } catch (SAXParseException e) {
                throw new ResourceLoadException(
                        readResourceDebug(url) + " at " + e.getLineNumber() + ":" + e.getColumnNumber(), e);
                
            } catch (SAXException e) {
                throw new ResourceLoadException(readResourceDebug(url), e);
                
            }finally{
                if( null != is ){
                    try{
                        is.close();
                    }catch(IOException ioe){
                        LOG.warn(readResourceDebug(url), ioe);
                    }
                }
            }
        }
        return success;
    }
    
    protected String readResourceDebug(final URL url){
        
        return "Read Configuration from " + resource;
    }
}
