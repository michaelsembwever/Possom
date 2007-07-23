// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode;


import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.query.transform.QueryTransformerConfig;
import no.schibstedsok.searchportal.result.handler.ResultHandlerConfig;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.site.config.Spi;
import no.schibstedsok.searchportal.site.config.SiteClassLoaderFactory;
import no.schibstedsok.searchportal.site.config.*;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="mailto:mick@wever.org>mick</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchModeFactory extends AbstractDocumentFactory implements SiteKeyedFactory {

    /**
     * The context any SearchModeFactory must work against. *
     */
    public interface Context extends BaseContext, ResourceContext, SiteContext, BytecodeContext {
    }

    // Constants -----------------------------------------------------

    private static final Map<Site, SearchModeFactory> INSTANCES = new HashMap<Site, SearchModeFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();


    private static final SearchCommandFactory searchConfigurationFactory = new SearchCommandFactory();
    private static final QueryTransformerFactory queryTransformerFactory = new QueryTransformerFactory();
    private static final ResultHandlerFactory resultHandlerFactory = new ResultHandlerFactory();
    private static final NavFactory navFactory = new NavFactory();

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

    private static final String RESET_NAV_ELEMENT = "reset-nav";

    // Attributes ----------------------------------------------------

    private final Map<String, SearchMode> modes = new HashMap<String, SearchMode>();
    private final ReentrantReadWriteLock modesLock = new ReentrantReadWriteLock();

    private final DocumentLoader loader;
    private final Context context;

    // Static --------------------------------------------------------

    /**
     *
     * @param cxt
     * @return
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
     *
     * @param id
     * @return
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

    Map<String, SearchMode> getModes() {

        return Collections.unmodifiableMap(modes);
    }

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void init() {

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

                    if(searchConfigurationFactory.supported(childElement.getTagName(), context)){

                        // commands
                        final SearchConfiguration sc
                                = searchConfigurationFactory.parseSearchConfiguration(context, childElement, mode);

                        modesCommands.put(sc.getName(), sc);
                        mode.addSearchConfiguration(sc);

                    }else if("navigation".equals(childElement.getTagName())){
                        // navigation
                        assert null == mode.getNavigationConfiguration() : "NavigationConfiguration already set!";

                        final NodeList navigationElements = childElement.getElementsByTagName("navigation");

                        mode.setNavigationConfiguration(parseNavigation(id, navigationElements));
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

    private NavigationConfig parseNavigation(final String modeId, final NodeList navigationElements) {
        final NavigationConfig cfg = new NavigationConfig();

        for (int i = 0; i < navigationElements.getLength(); i++) {
            final Element navigationElement = (Element) navigationElements.item(i);
            final NavigationConfig.Navigation navigation = new NavigationConfig.Navigation(navigationElement);

            final NodeList navs = navigationElement.getChildNodes();

            for (int l = 0; l < navs.getLength(); l++) {
                final Node navNode = navs.item(l);

                if (navNode instanceof Element && !RESET_NAV_ELEMENT.equals(navNode.getNodeName())) {
                    NavigationConfig.Nav nav = navFactory.parseNav((Element) navNode,navigation,  context, null);
                    navigation.addNav(nav, cfg);
                }
            }

            for (int j = 0; j < navs.getLength(); j++) {
                final Node navElement = navs.item(j);

                if (RESET_NAV_ELEMENT.equals(navElement.getNodeName())) {
                    String resetNavId = ((Element)navElement).getAttribute("modeId");
                    if (modeId != null) {
                        NavigationConfig.Nav nav = navigation.getNavMap().get(resetNavId);
                        if (nav != null) {
                            navigation.addReset(nav);
                        } else {
                            LOG.error("Error in config, <reset-nav modeId=\"" + modeId + "\" />, in mode " + modeId + " not found");
                        }
                    }
                }
            }

            cfg.addNavigation(navigation);
        }
        return cfg;
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


    private static final class SearchCommandFactory extends AbstractFactory<SearchConfiguration> {

        SearchCommandFactory() {

        }

        SearchConfiguration parseSearchConfiguration(
                final Context cxt,
                final Element commandE,
                final SearchMode mode) {

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
                        if (queryTransformerFactory.supported(qt.getTagName(), cxt)) {
                            sc.addQueryTransformer(queryTransformerFactory.parseQueryTransformer(qt, cxt));
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
                        if (resultHandlerFactory.supported(rh.getTagName(), cxt)) {
                            sc.addResultHandler(resultHandlerFactory.parseResultHandler(rh, cxt));
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
                throw new InfrastructureException(ex);
            } catch (IllegalArgumentException ex) {
                throw new InfrastructureException(ex);
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

        @SuppressWarnings("unchecked")
        protected Class<SearchConfiguration> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass " + className);

            String classNameFQ = "no.schibstedsok.searchportal.mode.config."+ className+ "Config";
            final Class<SearchConfiguration> clazz = loadClass(context, classNameFQ, Spi.SEARCH_COMMAND_CONFIG);

            LOG.debug("Found class " + clazz.getName());
            return clazz;
        }
    }

    private static final class NavFactory extends AbstractFactory<NavigationConfig.Nav> {

        NavigationConfig.Nav parseNav(
                final Element element,
                final NavigationConfig.Navigation navigation,
                final Context context,
                final NavigationConfig.Nav parent) {
            try {
                final Class<NavigationConfig.Nav> clazz = findClass(element.getTagName(), context);

                final Constructor<NavigationConfig.Nav> c
                        = clazz.getConstructor(NavigationConfig.Nav.class, NavigationConfig.Navigation.class, Element.class);

                final NavigationConfig.Nav nav = c.newInstance(parent, navigation, element);

                final NodeList children = element.getChildNodes();

                for (int i = 0; i < children.getLength(); ++i) {
                    final Node navNode = children.item(i);

                    // TODO. Shoud be possible to remove these special cases by implementating static-parameter and option as navigation controller sub classses.
                    if (navNode instanceof Element
                            && ! ("static-parameter".equals(navNode.getNodeName()) || "option".equals(navNode.getNodeName()))) {
                        NavigationConfig.Nav n = parseNav((Element) navNode, navigation, context, nav);
                        nav.addChild(n);
                    }

                }
                return nav;
            } catch (InstantiationException ex) {
                throw new InfrastructureException(ex);
            } catch (IllegalAccessException ex) {
                throw new InfrastructureException(ex);
            } catch (ClassNotFoundException e) {
                LOG.error(e.getMessage(), e);
                return null;
            } catch (NoSuchMethodException e) {
                LOG.error(e.getMessage(), e);
                return null;
            } catch (InvocationTargetException e) {
                LOG.error(e.getMessage(), e);
                return null;
            }
        }

        protected Class<NavigationConfig.Nav> findClass(final String xmlName, final Context context) throws ClassNotFoundException {
            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass " + className);

            // Special case for "nav".
            String classNameFQ = xmlName.equals("nav") ?
                    NavigationConfig.Nav.class.getName() :
                    "no.schibstedsok.searchportal.mode.navigation."+ className+ "Navigation";

            final Class<NavigationConfig.Nav> clazz = loadClass(context, classNameFQ, Spi.SEARCH_COMMAND_CONFIG);

            LOG.debug("Found class " + clazz.getName());
            return clazz;
        }
    }

    private static final class QueryTransformerFactory extends AbstractFactory<QueryTransformerConfig> {

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

            final String classNameFQ = "no.schibstedsok.searchportal.query.transform."
                    + className
                    + "QueryTransformerConfig";
            final Class<QueryTransformerConfig> clazz = loadClass(context, classNameFQ, Spi.QUERY_TRANSFORM_CONFIG);

            LOG.debug("Found class " + clazz.getName());

            return clazz;
        }
    }

    private static final class ResultHandlerFactory extends AbstractFactory<ResultHandlerConfig> {

        ResultHandlerFactory() {
        }

        ResultHandlerConfig parseResultHandler(final Element rh, final Context context) {
            return construct(rh, context).readResultHandler(rh);
        }

        @SuppressWarnings("unchecked")
        protected Class<ResultHandlerConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass " + className);

            String classNameFQ = "no.schibstedsok.searchportal.result.handler."
                    + className
                    + "ResultHandlerConfig";

            final Class<ResultHandlerConfig> clazz = loadClass(context, classNameFQ, Spi.RESULT_HANDLER_CONFIG);
            LOG.info("Found class " + clazz.getName());
            return clazz;
        }
    }

    private abstract static class AbstractFactory<C>{

        private static final String INFO_CONSTRUCT = "  Construct ";

        AbstractFactory(){}

        boolean supported(final String xmlName, final Context context) {

            try {
                return null != findClass(xmlName, context);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        protected C construct(final Element element, final Context context) {

            final String xmlName = element.getTagName();
            LOG.debug(INFO_CONSTRUCT + xmlName);

            try {
                return findClass(xmlName, context).newInstance();
            } catch (InstantiationException ex) {
                throw new InfrastructureException(ex);
            } catch (IllegalAccessException ex) {
                throw new InfrastructureException(ex);
            } catch (ClassNotFoundException e) {
                LOG.error(e.getMessage(), e);
                return null;
            }
        }

        protected abstract Class<C> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException;

        @SuppressWarnings("unchecked")
        protected Class<C> loadClass(final Context context, final String classNameFQ, final Spi spi)
                throws ClassNotFoundException {
            final SiteClassLoaderFactory.Context c = new SiteClassLoaderFactory.Context() {
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }

                public Site getSite() {
                    return context.getSite();
                }

                public Spi getSpi() {
                    return spi;
                }
            };

            final ClassLoader classLoader = SiteClassLoaderFactory.valueOf(c).getClassLoader();

            return (Class<C>) classLoader.loadClass(classNameFQ);
        }
    }
}
