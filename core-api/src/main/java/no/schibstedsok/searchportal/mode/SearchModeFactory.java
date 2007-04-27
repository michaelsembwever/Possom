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
    private static final String ERR_ONLY_ONE_CHILD_NAVIGATOR_ALLOWED
            = "Each FastNavigator is only allowed to have one child. Parent was ";
    private static final String ERR_FAST_EPS_QR_SERVER =
            "Query server address cannot contain the scheme (http://): ";
    private static final String INFO_PARSING_MODE = "Parsing mode ";
    private static final String INFO_PARSING_CONFIGURATION = " Parsing configuration ";
    private static final String INFO_PARSING_NAVIGATOR = "  Parsing navigator ";
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
                    final Element commandE = (Element)commandN;

                    if(searchConfigurationFactory.supported(commandE.getTagName(), context.getSite())){
                        
                        final SearchConfiguration sc = CommandTypes.DUMMY.parseSearchConfiguration(context, commandE, mode);
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

    /**
     * @deprecated we'll use SearchCommandFactory directly soon.
     **/
    private enum CommandTypes {DUMMY;

//        private final Class<? extends SearchConfiguration> clazz;
        private final String xmlName;
//        private final SearchCommandFactory searchConfigurationFactory = new SearchCommandFactory();
//        private final QueryTransformerFactory queryTransformerFactory = new QueryTransformerFactory();
//        private final ResultHandlerFactory resultHandlerFactory = new ResultHandlerFactory();

        CommandTypes(/*final Class<? extends SearchConfiguration> clazz*/) {
//            this.clazz = clazz;
            xmlName = name().replaceAll("_", "-").toLowerCase();
        }

        public String getXmlName() {
            return xmlName;
        }

        public SearchConfiguration parseSearchConfiguration(
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

                final SearchConfiguration sc 
                        = searchConfigurationFactory.parseSearchConfiguration(commandE, inherit, cxt.getSite());
                
                if (sc instanceof FastCommandConfig) {
                    final FastCommandConfig fsc = (FastCommandConfig) sc;
                    final FastCommandConfig fscInherit = inherit instanceof FastCommandConfig
                            ? (FastCommandConfig) inherit
                            : null;
                    fillBeanProperty(sc, inherit, "clustering", ParseType.Boolean, commandE, "false");
                    fillBeanProperty(sc, inherit, "collapsing", ParseType.Boolean, commandE, "false");
                    fillBeanProperty(sc, inherit, "expansion", ParseType.Boolean, commandE, "false");

                    if (commandE.getAttribute("collections").length() > 0) {
                        fsc.getCollections().clear();
                        final String[] collections = commandE.getAttribute("collections").split(",");
                        for (String collection : collections) {
                            fsc.addCollection(collection);
                        }
                    }

                    fillBeanProperty(sc, inherit, "filter", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "project", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "project", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "filtertype", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "ignoreNavigation", ParseType.Boolean, commandE, "false");
                    fillBeanProperty(sc, inherit, "offensiveScoreLimit", ParseType.Int, commandE, "-1");
                    fillBeanProperty(sc, inherit, "qtPipeline", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "queryServerUrl", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "relevantQueries", ParseType.Boolean, commandE, "false");
                    fillBeanProperty(sc, inherit, "sortBy", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "spamScoreLimit", ParseType.Int, commandE, "-1");
                    fillBeanProperty(sc, inherit, "spellcheck", ParseType.Boolean, commandE, "false");
                    fillBeanProperty(sc, inherit, "spellchecklanguage", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "lemmatise", ParseType.Boolean, commandE, "false");

                    if (fsc.getQueryServerUrl() == null || "".equals(fsc.getQueryServerUrl())) {
                        LOG.debug("queryServerURL is empty for " + fsc.getName());
                    }

                    // navigators
                    if (fscInherit != null && fscInherit.getNavigators() != null) {
                        for (final Map.Entry<String, Navigator> nav : fscInherit.getNavigators().entrySet()) {
                            fsc.addNavigator(nav.getValue(), nav.getKey());
                        }
                    }

                    final NodeList nList = commandE.getElementsByTagName("navigators");

                    for (int i = 0; i < nList.getLength(); ++i) {
                        final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
                        for (Navigator navigator : navigators) {
                            fsc.addNavigator(navigator, navigator.getId());
                        }

                    }
                }
                if (sc instanceof EspFastCommandConfig) {
                    final EspFastCommandConfig esc = (EspFastCommandConfig) sc;

                    final EspFastCommandConfig ascInherit = inherit instanceof EspFastCommandConfig
                            ? (EspFastCommandConfig) inherit
                            : null;

                    fillBeanProperty(sc, inherit, "view", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "sortBy", ParseType.String, commandE, "default");
                    fillBeanProperty(sc, inherit, "collapsingRemoves", ParseType.Boolean, commandE, "false");
                    fillBeanProperty(sc, inherit, "collapsingEnabled", ParseType.Boolean, commandE, "false");
                    fillBeanProperty(sc, inherit, "expansionEnabled", ParseType.Boolean, commandE, "false");
                    fillBeanProperty(sc, inherit, "qtPipeline", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "queryServer", ParseType.String, commandE, "");

                    if (null != esc.getQueryServer() && esc.getQueryServer().startsWith("http://")) {
                        throw new IllegalArgumentException(ERR_FAST_EPS_QR_SERVER + esc.getQueryServer());
                    }

                    // navigators
                    final NodeList nList = commandE.getElementsByTagName("navigators");
                    for (int i = 0; i < nList.getLength(); ++i) {
                        final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
                        for (Navigator navigator : navigators) {
                            esc.addNavigator(navigator, navigator.getId());
                        }

                    }
                }

                if (sc instanceof NavigatableEspFastCommandConfig) {
                    final NavigatableEspFastCommandConfig nasc = (NavigatableEspFastCommandConfig) sc;
                    // navigators
                    final NodeList nList = commandE.getElementsByTagName("navigators");
                    for (int i = 0; i < nList.getLength(); ++i) {
                        final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
                        for (Navigator navigator : navigators) {
                            nasc.addNavigator(navigator, navigator.getId());
                        }
                    }
                }
                if (sc instanceof HittaCommandConfig) {
                    fillBeanProperty(sc, inherit, "catalog", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "key", ParseType.String, commandE, "");
                }

                if (sc instanceof PrisjaktCommandConfig) {
                }

                if (sc instanceof BlocketCommandConfig) {
                    final BlocketCommandConfig bsc = (BlocketCommandConfig) sc;

                    /**
                     * Read blocket.se's around 400 most commonly used search phrases excluding vehicle oriented stuff, from blocket_search_words.xml.
                     */
                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setValidating(false);
                    final DocumentBuilder builder = factory.newDocumentBuilder();
                    DocumentLoader loader = cxt.newDocumentLoader(cxt, bsc.getBlocketConfigFileName(), builder);
                    loader.abut();

                    final Map<String, String> blocketmap = new HashMap<String, String>();
                    final Document doc = loader.getDocument();
                    final Element root = doc.getDocumentElement();

                    final NodeList wordList = root.getElementsByTagName("word");

                    // loop through words.
                    for (int i = 0; i < wordList.getLength(); ++i) {
                        final Element wordElement = (Element) wordList.item(i);
                        final String cid = wordElement.getAttribute("category-id");
                        final String catName = wordElement.getAttribute("category");
                        final String word = wordElement.getTextContent();
                        // Put words into a map
                        blocketmap.put(word, cid + ":" + catName);
                    }
                    bsc.setBlocketMap(blocketmap);
                }

                if (sc instanceof VehicleCommandConfig) {
                    final VehicleCommandConfig vsc = (VehicleCommandConfig) sc;

                    /**
                     * Read vehicle specific properties for bytbil.com and blocket.se
                     */
                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setValidating(false);
                    final DocumentBuilder builder = factory.newDocumentBuilder();
                    DocumentLoader loader = cxt.newDocumentLoader(cxt, vsc.getAccessoriesFileName(), builder);
                    loader.abut();

                    final Set<String> accessoriesSet = new HashSet<String>();
                    final Document doc = loader.getDocument();
                    final Element root = doc.getDocumentElement();

                    final NodeList accList = root.getElementsByTagName("accessory");

                    /**
                     * Put car accessory search words from xml in a set
                     */
                    for (int i = 0; i < accList.getLength(); ++i) {
                        final Element wordElement = (Element) accList.item(i);
                        final String acc = wordElement.getTextContent();
                        accessoriesSet.add(acc);
                    }
                    vsc.setAccessoriesSet(accessoriesSet);


                    final Map<String, String> carMap = new HashMap<String, String>();
                    final DocumentBuilder builder2 = factory.newDocumentBuilder();
                    DocumentLoader carLoader = cxt.newDocumentLoader(cxt, vsc.getCarsPropertiesFileName(), builder2);
                    carLoader.abut();

                    final Document doc2 = carLoader.getDocument();
                    final Element root2 = doc2.getDocumentElement();

                    /**
                     * Put car words from xml into a map
                     */
                    final NodeList carList = root2.getElementsByTagName("car");

                    for (int i = 0; i < carList.getLength(); ++i) {
                        final Element wordElement = (Element) carList.item(i);
                        final String brand = wordElement.getAttribute("brand");
                        final String model = wordElement.getAttribute("model");
                        final String car = wordElement.getTextContent();
                        carMap.put(car, brand + ";" + model);   // "volvo p 1800" , "volvo;p 1800"
                    }
                    vsc.setCarsMap(carMap);
                }

                if (sc instanceof AbstractYahooSearchConfiguration) {
                    fillBeanProperty(sc, inherit, "encoding", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "partnerId", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "host", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "port", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "hostHeader", ParseType.String, commandE, "");
                }
                if (sc instanceof YahooMediaCommandConfig) {

                    fillBeanProperty(sc, inherit, "catalog", ParseType.String, commandE,
                            YahooMediaCommandConfig.DEFAULT_CATALOG);
                    fillBeanProperty(sc, inherit, "ocr", ParseType.String, commandE,
                            YahooMediaCommandConfig.DEFAULT_OCR);
                    fillBeanProperty(sc, inherit, "site", ParseType.String, commandE, "");
                }
                if (sc instanceof OverturePpcCommandConfig) {
                    fillBeanProperty(sc, inherit, "url", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "type", ParseType.String, commandE, "");
                }
                if (sc instanceof PlatefoodPpcCommandConfig) {
                    fillBeanProperty(sc, inherit, "url", ParseType.String, commandE, "");
                }
                if (sc instanceof YahooIdpCommandConfig) {
                    fillBeanProperty(sc, inherit, "database", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "dateRange", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "filter", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "hideDomain", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "language", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "languageMix", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "region", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "regionMix", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "spellState", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "unique", ParseType.String, commandE, "");
                }
                if (sc instanceof PictureCommandConfig) {

                    fillBeanProperty(sc, inherit, "queryServerHost", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "queryServerPort", ParseType.String, commandE, "");
                    fillBeanProperty(sc, inherit, "country", ParseType.String, commandE, "no");
                    fillBeanProperty(sc, inherit, "filter", ParseType.String, commandE, "medium");
                    fillBeanProperty(sc, inherit, "customerId", ParseType.String, commandE, "558735");

                    LOG.debug("Inherited customerid " + ((PictureCommandConfig) sc).getCustomerId());

                }
                if (sc instanceof MobileCommandConfig) {
                    final MobileCommandConfig msc = (MobileCommandConfig) sc;

                    // TODO use fillBeanProperty pattern instead
                    msc.setPersonalizationGroup(commandE.getAttribute("personalization-group"));
                    // TODO use fillBeanProperty pattern instead
                    msc.setTelenorPersonalizationGroup(commandE.getAttribute("telenor-personalization-group"));
                    // TODO use fillBeanProperty pattern instead
                    msc.setSortBy(commandE.getAttribute("sort-by"));
                    // TODO use fillBeanProperty pattern instead
                    msc.setSource(commandE.getAttribute("source"));
                    // TODO use fillBeanProperty pattern instead
                    msc.setFilter(commandE.getAttribute("filter"));
                }
                if (sc instanceof BlendingNewsCommandConfig) {
                    final BlendingNewsCommandConfig bnsc = (BlendingNewsCommandConfig) sc;

                    final String[] filters = commandE.getAttribute("filters").split(",");

                    final List<String> filterList = new ArrayList<String>();

                    for (int i = 0; i < filters.length; i++) {
                        filterList.add(filters[i].trim());
                    }
                    // TODO use fillBeanProperty pattern instead
                    bnsc.setFiltersToBlend(filterList);
                    // TODO use fillBeanProperty pattern instead
                    bnsc.setDocumentsPerFilter(Integer.parseInt(commandE.getAttribute("documentsPerFilter")));
                }

                if (sc instanceof StormweatherCommandConfig) {
                    final StormweatherCommandConfig swsc = (StormweatherCommandConfig) sc;
                    if (commandE.getAttribute("xml-elements").length() > 0) {
                        final String[] elms = commandE.getAttribute("xml-elements").split(",");
                        for (String elm : elms) {
                            swsc.addElementValue(elm.trim());
                        }
                    }

                    // Add inherited xml elemts.
                    if (inherit instanceof StormweatherCommandConfig) {
                        final StormweatherCommandConfig swsi = (StormweatherCommandConfig) inherit;
                        for (String elm : swsi.getElementValues()) {
                            swsc.addElementValue(elm);
                        }
                    }
                }

                if (sc instanceof TvsearchCommandConfig) {
                    final TvsearchCommandConfig tssc = (TvsearchCommandConfig) sc;
                    final String[] defaultChannels = commandE.getAttribute("default-channels").split(",");
                    for (String channel : defaultChannels) {
                        tssc.addDefaultChannel(channel.trim());
                    }
                    // TODO use fillBeanProperty pattern instead
                    tssc.setResultsToFetch(Integer.parseInt(commandE.getAttribute("results-to-fetch")));

                }

                if (sc instanceof TvwaitsearchCommandConfig) {
                    final TvwaitsearchCommandConfig twsc = (TvwaitsearchCommandConfig) sc;
                    fillBeanProperty(twsc, inherit, "index", ParseType.Int, commandE, "0");
                    fillBeanProperty(twsc, inherit, "waitOn", ParseType.String, commandE, null);
                    fillBeanProperty(twsc, inherit, "useMyChannels", ParseType.Boolean, commandE, "false");
                }

                if (sc instanceof TvenrichCommandConfig) {
                    final TvenrichCommandConfig tesc = (TvenrichCommandConfig) sc;
                    fillBeanProperty(tesc, inherit, "waitOn", ParseType.String, commandE, null);
                }

                if (sc instanceof CatalogueCommandConfig) {
                    final CatalogueCommandConfig csc = (CatalogueCommandConfig) sc;
                    fillBeanProperty(csc, inherit, "queryParameterWhere", ParseType.String, commandE, "");
                    fillBeanProperty(csc, inherit, "searchBy", ParseType.String, commandE, "");
                    fillBeanProperty(csc, inherit, "split", ParseType.Boolean, commandE, "false");
                }
                if (sc instanceof CatalogueAdsCommandConfig) {
                    final CatalogueAdsCommandConfig casc = (CatalogueAdsCommandConfig) sc;
                    fillBeanProperty(casc, inherit, "queryParameterWhere", ParseType.String, commandE, "");
                }

                if (sc instanceof CatalogueBannersCommandConfig) {
                    final CatalogueBannersCommandConfig cbsc = (CatalogueBannersCommandConfig) sc;
                    fillBeanProperty(cbsc, inherit, "queryParameterWhere", ParseType.String, commandE, "");
                }

                if (sc instanceof NewsEspCommandConfig) {
                    final NewsEspCommandConfig nesc = (NewsEspCommandConfig) sc;
                    fillBeanProperty(nesc, inherit, "mediumPrefix", ParseType.String, commandE, "medium");
                    fillBeanProperty(nesc, inherit, "defaultMedium", ParseType.String, commandE, "webnewsarticle");
                    fillBeanProperty(nesc, inherit, "mediumParameter", ParseType.String, commandE, "medium");
                }

                if (sc instanceof ClusteringEspFastCommandConfig) {
                    final ClusteringEspFastCommandConfig cefc = (ClusteringEspFastCommandConfig) sc;
                    fillBeanProperty(cefc, inherit, "clusterIdParameter", ParseType.String, commandE, "clusterId");
                    fillBeanProperty(cefc, inherit, "resultsPerCluster", ParseType.Int, commandE, "");
                    fillBeanProperty(cefc, inherit, "clusterField", ParseType.String, commandE, "cluster");
                    fillBeanProperty(cefc, inherit, "clusterMaxFetch", ParseType.Int, commandE, "10");
                    fillBeanProperty(cefc, inherit, "nestedResultsField", ParseType.String, commandE, "entries");
                    fillBeanProperty(cefc, inherit, "sortField", ParseType.String, commandE, "publishedtime");
                    fillBeanProperty(cefc, inherit, "defaultSort", ParseType.String, commandE, "descending");
                    fillBeanProperty(cefc, inherit, "userSortParameter", ParseType.String, commandE, "sort");
                }

                if (sc instanceof NewsAggregatorCommandConfig) {
                    final NewsAggregatorCommandConfig nasc = (NewsAggregatorCommandConfig) sc;
                    fillBeanProperty(nasc, inherit, "xmlSource", ParseType.String, commandE, "");
                    fillBeanProperty(nasc, inherit, "xmlMainFile", ParseType.String, commandE, "fp_main_main.xml");
                    fillBeanProperty(nasc, inherit, "geographicFields", ParseType.String, commandE, "");
                    fillBeanProperty(nasc, inherit, "categoryFields", ParseType.String, commandE, "");
                }

                if (sc instanceof NewsMyNewsCommandConfig) {

                }

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
                }else if(null!=inherit){
                    // inherit all
                    for(QueryTransformerConfig qtc : inherit.getQueryTransformers()){
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
                }else if(null!=inherit){
                    // inherit all
                    for(ResultHandlerConfig rhc : inherit.getResultHandlers()){
                        sc.addResultHandler(rhc);
                    }
                }

                return sc;

            } catch (SecurityException ex) {
                throw new InfrastructureException(ex);
            } catch (IllegalArgumentException ex) {
                throw new InfrastructureException(ex);
            } catch (ParserConfigurationException e) {
                throw new InfrastructureException(e);
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

        private Collection<Navigator> parseNavigators(final Element navsE) {

            final Collection<Navigator> navigators = new ArrayList<Navigator>();
            final NodeList children = navsE.getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                final Node child = children.item(i);
                if (child instanceof Element && "navigator".equals(((Element) child).getTagName())) {
                    final Element navE = (Element) child;
                    final String id = navE.getAttribute("id");
                    final String name = navE.getAttribute("name");
                    final String sortAttr = navE.getAttribute("sort") != null && navE.getAttribute("sort").length() > 0
                            ? navE.getAttribute("sort").toUpperCase() : "COUNT";
                    LOG.info(INFO_PARSING_NAVIGATOR + id + " [" + name + "]" + ", sort=" + sortAttr);
                    final Navigator.Sort sort = Navigator.Sort.valueOf(sortAttr);

                    final Navigator nav = new Navigator(
                            name,
                            navE.getAttribute("field"),
                            navE.getAttribute("display-name"),
                            sort);
                    nav.setId(id);
                    final Collection<Navigator> childNavigators = parseNavigators(navE);
                    if (childNavigators.size() > 1) {
                        throw new IllegalStateException(ERR_ONLY_ONE_CHILD_NAVIGATOR_ALLOWED + id);
                    } else if (childNavigators.size() == 1) {
                        nav.setChildNavigator(childNavigators.iterator().next());
                    }
                    navigators.add(nav);
                }
            }

            return navigators;
        }
    }

    private static final class SearchCommandFactory extends AbstractFactory<SearchConfiguration> {

        SearchCommandFactory() {
        }

        SearchConfiguration parseSearchConfiguration(
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
