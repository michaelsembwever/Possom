/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractResourceLoader.java
 *
 * Created on 23 January 2006, 10:57
 *
 */

package no.schibstedsok.front.searchportal.configuration.loaders;

import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.site.SiteContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/** Utility class to handle loading different types of resources in a background thread.
 * This avoids the problem of having to order loading of applications in the container because of static initialisers
 *  using resources from the search-front-config application.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractResourceLoader 
        extends Thread 
        implements DocumentLoader, PropertiesLoader, XStreamLoader {

    private static final Log LOG = LogFactory.getLog(AbstractResourceLoader.class);

    private final SiteContext context;
    private volatile String resource;

    /** the properties resource holder. **/
    protected Properties props;
    /** the XStream resource holder. **/
    protected XStream xstream;
    /** the xstream resource result. **/
    protected Object xstreamResult;
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

    /** Load the resource. **/
    public abstract void run();

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
    public Object getXStreamResult() {
        if (xstream == null) {
            throw new IllegalStateException(ERR_MUST_USE_XSTREAM_INITIALISER);
        }
        return xstreamResult;
    }
    
    /** {@inheritDoc}
     */
    public Document getDocument(){
        if (builder == null) {
            throw new IllegalStateException(ERR_MUST_USE_XSTREAM_INITIALISER);
        }
        return document;
    }


    /** {@inheritDoc}
     */
    public void init(final String resource, final Properties props) {
        if (isAlive() || this.resource != null) {
            throw new IllegalStateException(ERR_ONE_USE_ONLY);
        }
        this.resource = resource;
        this.props = props;
        start();
    }


    /** {@inheritDoc}
     */
    public void init(final String resource, final XStream xstream) {
        if (isAlive()  || this.resource != null) {
            throw new IllegalStateException(ERR_ONE_USE_ONLY);
        }
        this.resource = resource;
        this.xstream = xstream;
        start();
    }
    
    /** {@inheritDoc}
     */
    public void init(final String resource, final DocumentBuilder builder) {
        if (isAlive()  || this.resource != null) {
            throw new IllegalStateException(ERR_ONE_USE_ONLY);
        }
        this.resource = resource;
        this.builder = builder;
        start();
    }
    
    /** {@inheritDoc}
     */
    public void abut() {
        if (this.resource == null) {
            throw new IllegalStateException(ERR_NOT_INITIALISED);
        }
        try {
            this.join();
        } catch (InterruptedException ex) {
            LOG.error(ERR_INTERRUPTED_WAITING_4_RSC_2_LOAD, ex);
        }
    }

}
