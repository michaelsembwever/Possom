/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.mode;


import no.sesat.search.site.config.AbstractConfigFactory;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.query.transform.QueryTransformerConfig;
import no.sesat.search.result.handler.ResultHandlerConfig;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactory;
import no.sesat.search.site.config.Spi;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.site.config.DocumentLoader;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author <a href="mailto:mick@wever.org>mick</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchModeFactory extends AbstractDocumentFactory implements SiteKeyedFactory {

    /**
     * The context any SearchModeFactory must work against. *
     */
    public interface Context extends ResourceContext, AbstractConfigFactory.Context {}

    // Constants -----------------------------------------------------

    private static final Map<Site, SearchModeFactory> INSTANCES = new HashMap<Site, SearchModeFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();


    private static final SearchCommandFactory SEARCH_CONFIGURATION_FACTORY = new SearchCommandFactory();
    private static final QueryTransformerFactory QUERY_TRANSFORMER_FACTORY = new QueryTransformerFactory();
    private static final ResultHandlerFactory RESULT_HANDLER_FACTORY = new ResultHandlerFactory();


    /**
     * The name of the modes configuration file.
     */
    public static final String MODES_XMLFILE = "modes.xml";


    private static final Map<SearchMode, Map<String, SearchConfiguration>> COMMANDS
            = new HashMap<SearchMode, Map<String, SearchConfiguration>>();
    private static final ReentrantReadWriteLock COMMANDS_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(SearchModeFactory.class);
    private static final String ERR_DOC_BUILDER_CREATION
            = "Failed to DocumentBuilderFactory.newInstance().newDocumentBuilder()";
    private static final String INFO_PARSING_MODE = "Parsing mode ";
    private static final String INFO_PARSING_CONFIGURATION = " Parsing configuration ";
    private static final String ERR_PARENT_COMMAND_NOT_FOUND = "Parent command {0} not found for {1} in mode {2}";


    // Attributes ----------------------------------------------------

    private final Map<String, SearchMode> modes = new HashMap<String, SearchMode>();
    private final ReentrantReadWriteLock modesLock = new ReentrantReadWriteLock();

    private final DocumentLoader loader;
    private final Context context;

    // Static --------------------------------------------------------

    /**
     * Returns the factory for the site in the context.
     *
     * @param cxt The context.
     * @return the factory for the context.
     */
    public static SearchModeFactory valueOf(final Context cxt) {

        final Site site = cxt.getSite();

        SearchModeFactory instance;
        try {
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        } finally {
            INSTANCES_LOCK.readLock().unlock();
        }

        if (instance == null) {
            try {
                instance = new SearchModeFactory(cxt);
            } catch (ParserConfigurationException ex) {
                LOG.error(ERR_DOC_BUILDER_CREATION, ex);
            }
        }
        return instance;
    }

    /**
     * Removes mode factory associated with site causing configuration to be reloaded.
     *
     * @param site The site to remove.
     * @return true if the site existed.
     */
    public boolean remove(final Site site) {

        try {
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        } finally {
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of ModeFactoryImpl
     */
    private SearchModeFactory(final Context cxt)
            throws ParserConfigurationException {

        LOG.trace("ModeFactory(cxt)");
        try {
            INSTANCES_LOCK.writeLock().lock();

            context = cxt;

            // configuration files
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            loader = context.newDocumentLoader(cxt, MODES_XMLFILE, builder);

            // update the store of factories
            INSTANCES.put(context.getSite(), this);
            // start initialisation
            init();

        } finally {
            INSTANCES_LOCK.writeLock().unlock();
        }

    }

    // Public --------------------------------------------------------

    /**
     * Returns the mode with given identifier.
     *
     * @param id The mode identifier.
     * @return The mode.
     */
    public SearchMode getMode(final String id) {

        LOG.trace("getMode(" + id + ")");

        SearchMode mode = getModeImpl(id);
        if (mode == null && id != null && id.length() > 0 && context.getSite().getParent() != null) {
            // not found in this site's modes.xml. look in parent's site.
            final SearchModeFactory factory = valueOf(ContextWrapper.wrap(
                    Context.class,
                    new SiteContext() {
                        public Site getSite() {
                            return context.getSite().getParent();
                        }
                    },
                    context
            ));
            mode = factory.getMode(id);
        }
        return mode;
    }

    // Package protected ---------------------------------------------

    /* Test use it. **/

    /**
     * Return the modes created by the factory.
     *
     * @return a map of the modes. Mode id is the key.
     */
    Map<String, SearchMode> getModes() {

        return Collections.unmodifiableMap(modes);
    }

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void init() throws ParserConfigurationException {

        loader.abut();
        LOG.debug("Parsing " + MODES_XMLFILE + " started");
        final Document doc = loader.getDocument();
        final Element root = doc.getDocumentElement();

        if (null != root) {
            // loop through modes.
            final NodeList modeList = root.getElementsByTagName("mode");

            for (int i = 0; i < modeList.getLength(); ++i) {
                final Element modeE = (Element) modeList.item(i);
                final String id = modeE.getAttribute("id");

                LOG.info(INFO_PARSING_MODE + modeE.getLocalName() + " " + id);
                final SearchMode inherit = getMode(modeE.getAttribute("inherit"));

                final SearchMode mode = new SearchMode(inherit);

                mode.setId(id);
                mode.setExecutor(parseExecutor(
                        modeE.getAttribute("executor"),
                        inherit != null ? inherit.getExecutor() : SearchMode.SearchCommandExecutorConfig.SEQUENTIAL));

                fillBeanProperty(mode, inherit, "analysis", ParseType.Boolean, modeE, "false");

                // setup new commands list for this mode
                final Map<String, SearchConfiguration> modesCommands = new HashMap<String, SearchConfiguration>();
                try {
                    COMMANDS_LOCK.writeLock().lock();
                    COMMANDS.put(mode, modesCommands);
                } finally {
                    COMMANDS_LOCK.writeLock().unlock();
                }


                final NodeList childNodes = modeE.getChildNodes();

                for (int j = 0; j < childNodes.getLength(); ++j) {
                    final Node childNode = childNodes.item(j);
                    if (!(childNode instanceof Element)) {
                        continue;
                    }
                    final Element childElement = (Element) childNode;

                    if(SEARCH_CONFIGURATION_FACTORY.supported(childElement.getTagName(), context)){

                        // commands
                        final SearchConfiguration sc
                                = SEARCH_CONFIGURATION_FACTORY.parseSearchConfiguration(context, childElement, mode);

                        modesCommands.put(sc.getName(), sc);
                        mode.addSearchConfiguration(sc);

//                    }else if("navigation".equals(childElement.getTagName())){
//                        // navigation
//                        assert null == mode.getNavigationConfiguration() : "NavigationConfiguration already set!";
//
//                        final NodeList navigationElements = childElement.getElementsByTagName("navigation");
//
//                        mode.setNavigationConfiguration(parseNavigation(id, navigationElements));
                    }
                }

                // add mode
                try {
                    modesLock.writeLock().lock();
                    modes.put(id, mode);
                } finally {
                    modesLock.writeLock().unlock();
                }
            }
        }

        // finished
        LOG.debug("Parsing " + MODES_XMLFILE + " finished");

    }

    private static SearchMode.SearchCommandExecutorConfig parseExecutor(
            final String name,
            final SearchMode.SearchCommandExecutorConfig def) {

        try {
            if (0 < name.length()) {
                return SearchMode.SearchCommandExecutorConfig.valueOf(name.toUpperCase());
            }

        } catch (IllegalArgumentException iae) {
            LOG.error("Unparsable executor " + name, iae);
        }
        return def;
    }

    private SearchMode getModeImpl(final String id) {

        try {
            modesLock.readLock().lock();
            return modes.get(id);

        } finally {
            modesLock.readLock().unlock();
        }
    }

    // Inner classes -------------------------------------------------


    private static final class SearchCommandFactory extends AbstractConfigFactory<SearchConfiguration> {

        SearchCommandFactory() {}

        SearchConfiguration parseSearchConfiguration(
                final Context cxt,
                final Element commandE,
                final SearchMode mode) throws ParserConfigurationException {

            final String parentName = commandE.getAttribute("inherit");
            final String id = commandE.getAttribute("id");

            final SearchConfiguration inherit = findParent(parentName, mode);

            if (!"".equals(parentName) && inherit == null) {

                throw new IllegalArgumentException(
                        MessageFormat.format(ERR_PARENT_COMMAND_NOT_FOUND, parentName, id, mode.getId()));
            }

            LOG.info(INFO_PARSING_CONFIGURATION + commandE.getLocalName() + " " + id);

            try {

                final SearchConfiguration sc = parseSearchConfigurationImpl(commandE, inherit, cxt);

                // query transformers
                NodeList qtNodeList = commandE.getElementsByTagName("query-transformers");
                final Element qtRootElement = (Element) qtNodeList.item(0);
                if (qtRootElement != null) {
                    qtNodeList = qtRootElement.getChildNodes();

                    // clear all inherited query-transformers
                    sc.clearQueryTransformers();

                    for (int i = 0; i < qtNodeList.getLength(); i++) {
                        final Node node = qtNodeList.item(i);
                        if (!(node instanceof Element)) {
                            continue;
                        }
                        final Element qt = (Element) node;
                        if (QUERY_TRANSFORMER_FACTORY.supported(qt.getTagName(), cxt)) {
                            sc.addQueryTransformer(QUERY_TRANSFORMER_FACTORY.parseQueryTransformer(qt, cxt));
                        }
                    }
                } else if (null != inherit) {
                    // inherit all
                    for (QueryTransformerConfig qtc : inherit.getQueryTransformers()) {
                        sc.addQueryTransformer(qtc);
                    }
                }

                // result handlers
                NodeList rhNodeList = commandE.getElementsByTagName("result-handlers");
                final Element rhRootElement = (Element) rhNodeList.item(0);
                if (rhRootElement != null) {
                    rhNodeList = rhRootElement.getChildNodes();

                    // clear all inherited result handlers
                    sc.clearResultHandlers();

                    for (int i = 0; i < rhNodeList.getLength(); i++) {
                        final Node node = rhNodeList.item(i);
                        if (!(node instanceof Element)) {
                            continue;
                        }
                        final Element rh = (Element) node;
                        if (RESULT_HANDLER_FACTORY.supported(rh.getTagName(), cxt)) {
                            sc.addResultHandler(RESULT_HANDLER_FACTORY.parseResultHandler(rh, cxt));
                        }
                    }
                } else if (null != inherit) {
                    // inherit all
                    for (ResultHandlerConfig rhc : inherit.getResultHandlers()) {
                        sc.addResultHandler(rhc);
                    }
                }

                return sc;

            } catch (SecurityException ex) {
                throw new ParserConfigurationException(ex.getMessage());
            } catch (IllegalArgumentException ex) {
                throw new ParserConfigurationException(ex.getMessage());
            }
        }

        private SearchConfiguration findParent(
                final String id,
                final SearchMode mode) {

            SearchMode m = mode;
            SearchConfiguration config;
            do {
                final Map<String, SearchConfiguration> configs;
                try {
                    COMMANDS_LOCK.readLock().lock();
                    configs = COMMANDS.get(m);
                } finally {
                    COMMANDS_LOCK.readLock().unlock();
                }
                config = configs.get(id);
                m = m.getParentSearchMode();

            } while (config == null && m != null);

            return config;
        }

        private SearchConfiguration parseSearchConfigurationImpl(
                final Element element,
                final SearchConfiguration inherit,
                final Context context) {

            final SearchConfiguration sc = construct(element, context);

            assert null == inherit || inherit.getClass().isAssignableFrom(sc.getClass())
                    : "Can only inherit from same or superclass configuration. "
                    + element.getAttribute("id") + '(' + sc.getClass().getSimpleName() + ')'
                    + " trying to inherit from " + inherit.getName() + '(' + inherit.getClass().getSimpleName() + ')';

            return sc.readSearchConfiguration(element, inherit);
        }

        protected Class<SearchConfiguration> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass " + className);

            final String classNameFQ = "no.sesat.search.mode.config."+ className+ "Config";
            final Class<SearchConfiguration> clazz = loadClass(context, classNameFQ, Spi.SEARCH_COMMAND_CONFIG);

            LOG.debug("Found class " + clazz.getName());
            return clazz;
        }
    }

    private static final class QueryTransformerFactory extends AbstractConfigFactory<QueryTransformerConfig> {

        QueryTransformerFactory() {
        }

        QueryTransformerConfig parseQueryTransformer(final Element qt, final Context context) {
            return construct(qt, context).readQueryTransformer(qt);
        }

        protected Class<QueryTransformerConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass " + className);

            final String classNameFQ = "no.sesat.search.query.transform."
                    + className
                    + "QueryTransformerConfig";
            final Class<QueryTransformerConfig> clazz = loadClass(context, classNameFQ, Spi.QUERY_TRANSFORM_CONFIG);

            LOG.debug("Found class " + clazz.getName());

            return clazz;
        }
    }

    private static final class ResultHandlerFactory extends AbstractConfigFactory<ResultHandlerConfig> {

        ResultHandlerFactory() {
        }

        ResultHandlerConfig parseResultHandler(final Element rh, final Context context) {
            return construct(rh, context).readResultHandler(rh);
        }

        protected Class<ResultHandlerConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass " + className);

            final String classNameFQ = "no.sesat.search.result.handler."
                    + className
                    + "ResultHandlerConfig";

            final Class<ResultHandlerConfig> clazz = loadClass(context, classNameFQ, Spi.RESULT_HANDLER_CONFIG);
            LOG.info("Found class " + clazz.getName());
            return clazz;
        }
    }
}
