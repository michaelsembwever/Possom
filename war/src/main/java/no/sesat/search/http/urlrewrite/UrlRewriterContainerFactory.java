/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * UrlRewriterContainerFactory.java
 *
 * Created on 19. april 2006, 20:48
 */

package no.sesat.search.http.urlrewrite;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import no.sesat.commons.ioc.BaseContext;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactory;
import no.sesat.search.site.config.DocumentContext;
import org.apache.log4j.Logger;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;
import org.w3c.dom.Document;

/** Provides a SiteKeyedFactory around urlrewrite.xml configurations instead of tuckey's default
 * of only loading WEB-INF/urlrewrite.xml
 *
 *
 * @version $Id$
 */
public final class UrlRewriterContainerFactory /*extends AbstractDocumentFactory*/ implements SiteKeyedFactory{

    /**
     * The context any SearchTabFactory must work against.
     */
    public interface Context extends BaseContext, DocumentContext, SiteContext {
        ServletContext getServletContext();
    }

   // Constants -----------------------------------------------------

    private static final Map<Site, UrlRewriterContainerFactory> INSTANCES
            = new HashMap<Site,UrlRewriterContainerFactory>();

    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    /**
     * The name of the skin's configuration file.
     */
    public static final String URLREWRITE_XMLFILE = "urlrewrite.xml";

    private static final String EMPTY_RULES = "<urlrewrite></urlrewrite>";

    private static final Logger LOG = Logger.getLogger(UrlRewriterContainerFactory.class);
    private static final String ERR_DOC_BUILDER_CREATION
            = "Failed to DocumentBuilderFactory.newInstance().newDocumentBuilder()";

    // Attributes ----------------------------------------------------

    private final UrlRewriter urlRewriter;
    private final DocumentLoader loader;
    private final Context context;

    // Static --------------------------------------------------------

    /** Return the factory in use for the skin defined within the context. *
     * @param cxt
     * @return
     */
    public static UrlRewriterContainerFactory instanceOf(final Context cxt) {

        final Site site = cxt.getSite();
        assert null != site;

        UrlRewriterContainerFactory instance;
        try{
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        }finally{
            INSTANCES_LOCK.readLock().unlock();
        }

        if (instance == null) {
            try {
                instance = new UrlRewriterContainerFactory(cxt);
            } catch (ParserConfigurationException ex) {
                LOG.error(ERR_DOC_BUILDER_CREATION,ex);
            }
        }
        return instance;
    }

    /** Remove the factory in use for the skin defined within the context. **/
    public boolean remove(final Site site){

        try{
            INSTANCES_LOCK.writeLock().lock();
            urlRewriter.destroy();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of ViewFactory */
    private UrlRewriterContainerFactory(final Context cxt) throws ParserConfigurationException {

        LOG.trace("UrlRewriterContainerFactory(cxt)");
        try{
            INSTANCES_LOCK.writeLock().lock();

            context = cxt;

            // configuration files
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            loader = context.newDocumentLoader(cxt, URLREWRITE_XMLFILE, builder);

            // start initialisation
            urlRewriter = initUrlRewriter();

            // update the store of factories
            INSTANCES.put(context.getSite(), this);

        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }

    }

    // Public --------------------------------------------------------

    /**
     * @return
     */
    public UrlRewriter getUrlRewriter(){

        LOG.trace("getUrlRewriter()");

        return urlRewriter;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------


    private UrlRewriter initUrlRewriter() {

        loader.abut();
        LOG.info("Parsing " + URLREWRITE_XMLFILE + " started");
        final Document doc = loader.getDocument();
        final String output = transformDocumentToString(doc).getBuffer().toString();
        // finished
        LOG.info("Parsing " + URLREWRITE_XMLFILE + " finished");

        byte[] byteArr = 0 < output.trim().length()
                ? output.getBytes()
                : EMPTY_RULES.getBytes();

        return new UrlRewriter(new Conf(
                context.getServletContext(),
                new ByteArrayInputStream(byteArr),
                URLREWRITE_XMLFILE,
                ""));
    }

    private static StringWriter transformDocumentToString(final Document xml){

        final StringWriter writer = new StringWriter();
        try{
            final Result res = new StreamResult(writer);
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT,  "no" );
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform( new DOMSource(xml), res );

        }catch(TransformerException te){
            LOG.error(te.getMessage(), te);
        }
        return writer;
    }

    // Inner classes -------------------------------------------------

}