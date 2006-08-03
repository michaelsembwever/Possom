
/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractResourceLoader.java
 *
 * Created on 23 January 2006, 10:57
 *
 */

package no.schibstedsok.searchportal.util.config;


import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/** Utility class to handle loading different types of resources in a background thread.
 * This avoids the problem of having to order loading of applications in the container because of static initialisers
 *  using resources from the search-front-config application.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractResourceLoader
        implements Runnable, DocumentLoader, PropertiesLoader {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final Logger LOG = Logger.getLogger(AbstractResourceLoader.class);

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

}
