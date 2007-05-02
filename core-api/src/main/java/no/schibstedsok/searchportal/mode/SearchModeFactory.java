// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode;


import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.AbstractYahooSearchConfiguration;
import no.schibstedsok.searchportal.mode.config.BlendingNewsCommandConfig;
import no.schibstedsok.searchportal.mode.config.BlocketCommandConfig;
import no.schibstedsok.searchportal.mode.config.CatalogueAdsCommandConfig;
import no.schibstedsok.searchportal.mode.config.CatalogueBannersCommandConfig;
import no.schibstedsok.searchportal.mode.config.CatalogueCommandConfig;
import no.schibstedsok.searchportal.mode.config.ClusteringEspFastCommandConfig;
import no.schibstedsok.searchportal.mode.config.EspFastCommandConfig;
import no.schibstedsok.searchportal.mode.config.FastCommandConfig;
import no.schibstedsok.searchportal.mode.config.HittaCommandConfig;
import no.schibstedsok.searchportal.mode.config.MobileCommandConfig;
import no.schibstedsok.searchportal.mode.config.NavigatableEspFastCommandConfig;
import no.schibstedsok.searchportal.mode.config.NewsAggregatorCommandConfig;
import no.schibstedsok.searchportal.mode.config.NewsEspCommandConfig;
import no.schibstedsok.searchportal.mode.config.NewsMyNewsCommandConfig;
import no.schibstedsok.searchportal.mode.config.OverturePpcCommandConfig;
import no.schibstedsok.searchportal.mode.config.PictureCommandConfig;
import no.schibstedsok.searchportal.mode.config.PlatefoodPpcCommandConfig;
import no.schibstedsok.searchportal.mode.config.PrisjaktCommandConfig;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.mode.config.StormweatherCommandConfig;
import no.schibstedsok.searchportal.mode.config.TvenrichCommandConfig;
import no.schibstedsok.searchportal.mode.config.TvsearchCommandConfig;
import no.schibstedsok.searchportal.mode.config.TvwaitsearchCommandConfig;
import no.schibstedsok.searchportal.mode.config.VehicleCommandConfig;
import no.schibstedsok.searchportal.mode.config.YahooIdpCommandConfig;
import no.schibstedsok.searchportal.mode.config.YahooMediaCommandConfig;
import no.schibstedsok.searchportal.query.transform.QueryTransformerConfig;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.result.handler.ResultHandlerConfig;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.ResourceContext;
import no.schibstedsok.searchportal.site.config.SiteClassLoaderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author <a href="mailto:mick@wever.org>mick</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchModeFactory extends AbstractDocumentFactory implements SiteKeyedFactory {

    /**
     * The context any SearchModeFactory must work against. *
     */
    public interface Context extends BaseContext, ResourceContext, SiteContext {
    }

    // Constants -----------------------------------------------------

    private static final Map<Site, SearchModeFactory> INSTANCES = new HashMap<Site, SearchModeFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();


    private static final SearchCommandFactory searchConfigurationFactory = new SearchCommandFactory();
    private static final QueryTransformerFactory queryTransformerFactory = new QueryTransformerFactory();
    private static final ResultHandlerFactory resultHandlerFactory = new ResultHandlerFactory();

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
    private static final String INFO_PARSING_RESULT_HANDLER = "  Parsing result handler ";
    private static final String INFO_PARSING_QUERY_TRANSFORMER = "  Parsing query transformer ";
    private static final String DEBUG_PARSED_PROPERTY = "  Property property ";
    private static final String ERR_PARENT_COMMAND_NOT_FOUND = "Parent command {0} not found for {1} in mode {2}";

    // Attributes ----------------------------------------------------

    private final Map<String, SearchMode> modes = new HashMap<String, SearchMode>();
    private final ReentrantReadWriteLock modesLock = new ReentrantReadWriteLock();

    private final DocumentLoader loader;
    private final Context context;

    private String templatePrefix;

    // Static --------------------------------------------------------

    /**
     * TODO comment me. *
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
     * TODO comment me. *
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
     * TODO comment me. *
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
            templatePrefix = root.getAttribute("template-prefix");

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

                // commands
                final NodeList commandsList = modeE.getChildNodes();

                for (int j = 0; j < commandsList.getLength(); ++j) {
                    final Node commandN = commandsList.item(j);
                    if (!(commandN instanceof Element)) {
                        continue;
                    }
                    final Element commandE = (Element) commandN;

                    if(searchConfigurationFactory.supported(commandE.getTagName(), context.getSite())){

                        final SearchConfiguration sc 
                                = searchConfigurationFactory.parseSearchConfiguration(context, commandE, mode);

                        modesCommands.put(sc.getName(), sc);
                        mode.addSearchConfiguration(sc);
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
            
                final SearchConfiguration sc = parseSearchConfigurationImpl(commandE, inherit, cxt.getSite());

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
                        if (queryTransformerFactory.supported(qt.getTagName(), cxt.getSite())) {
                            sc.addQueryTransformer(queryTransformerFactory.parseQueryTransformer(qt, cxt.getSite()));
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
                        if (resultHandlerFactory.supported(rh.getTagName(), cxt.getSite())) {
                            sc.addResultHandler(resultHandlerFactory.parseResultHandler(rh, cxt.getSite()));
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
            SearchConfiguration config = null;
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
                final Site site) {

            return construct(element, site).readSearchConfiguration(element, inherit);
        }

        @SuppressWarnings("unchecked")
        protected Class<SearchConfiguration> findClass(final String xmlName, final Site site)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.info("findClass " + className);
            final Class<SearchConfiguration> clazz
                    = (Class<SearchConfiguration>) SiteClassLoaderFactory.valueOf(site).getClassLoader().loadClass(
                                "no.schibstedsok.searchportal.mode.config."
                                + className
                                + "Config");


            LOG.info("Found class " + clazz.getName());
            return clazz;
        }
    }

    private static final class QueryTransformerFactory extends AbstractFactory<QueryTransformerConfig> {

        QueryTransformerFactory() {
        }

        QueryTransformerConfig parseQueryTransformer(final Element qt, final Site site) {
            return construct(qt, site).readQueryTransformer(qt);
        }

        @SuppressWarnings("unchecked")
        protected Class<QueryTransformerConfig> findClass(final String xmlName, final Site site)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.info("findClass " + className);

            final Class<QueryTransformerConfig> clazz
                    = (Class<QueryTransformerConfig>) SiteClassLoaderFactory.valueOf(site).getClassLoader().loadClass(
                    "no.schibstedsok.searchportal.query.transform."
                            + className
                            + "QueryTransformerConfig");


            LOG.info("Found class " + clazz.getName());
            return clazz;
        }
    }

    private static final class ResultHandlerFactory extends AbstractFactory<ResultHandlerConfig> {

        ResultHandlerFactory() {
        }

        ResultHandlerConfig parseResultHandler(final Element rh, final Site site) {
            return construct(rh, site).readResultHandler(rh);
        }

        @SuppressWarnings("unchecked")
        protected Class<ResultHandlerConfig> findClass(final String xmlName, final Site site)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.info("findClass " + className);

            final Class<ResultHandlerConfig> clazz =
                    (Class<ResultHandlerConfig>) SiteClassLoaderFactory.valueOf(site).getClassLoader().loadClass(
                            "no.schibstedsok.searchportal.result.handler."
                            + className
                            + "ResultHandlerConfig");


            LOG.info("Found class " + clazz.getName());
            return clazz;
        }
    }

    private static abstract class AbstractFactory<C>{

        private static final String INFO_CONSTRUCT = "  Construct ";

        AbstractFactory(){}

        boolean supported(final String xmlName, final Site site) {

            try {
                return null != findClass(xmlName, site);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        protected C construct(final Element element, final Site site) {

            final String xmlName = element.getTagName();
            LOG.info(INFO_CONSTRUCT + xmlName);

            try {
                return findClass(xmlName, site).newInstance();
            } catch (InstantiationException ex) {
                throw new InfrastructureException(ex);
            } catch (IllegalAccessException ex) {
                throw new InfrastructureException(ex);
            } catch (ClassNotFoundException e) {
                LOG.error(e.getMessage(), e);
                return null;
            }
        }

        protected abstract Class<C> findClass(final String xmlName, final Site site) throws ClassNotFoundException;
    }
}
