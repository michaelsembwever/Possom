// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.*;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.mode.config.SiteConfiguration;
import no.schibstedsok.searchportal.query.transform.MobileTvQueryTransformer;
import no.schibstedsok.searchportal.query.transform.RegExpTransformer;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.util.config.DocumentLoader;
import no.schibstedsok.searchportal.util.config.PropertiesLoader;
import no.schibstedsok.searchportal.util.config.ResourceContext;
import no.schibstedsok.searchportal.util.config.UrlResourceLoader;
import no.schibstedsok.searchportal.mode.executor.ParallelSearchCommandExecutor;
import no.schibstedsok.searchportal.mode.executor.SearchCommandExecutor;
import no.schibstedsok.searchportal.mode.executor.SequentialSearchCommandExecutor;
import no.schibstedsok.searchportal.query.transform.AgeFilterTransformer;
import no.schibstedsok.searchportal.result.handler.CombineNavigatorsHandler;
import no.schibstedsok.searchportal.result.handler.DataModelResultHandler;
import no.schibstedsok.searchportal.query.transform.TvSearchQueryTransformer;
import no.schibstedsok.searchportal.result.handler.FieldEscapeHandler;
import no.schibstedsok.searchportal.result.handler.NumberOperationHandler;
import no.schibstedsok.searchportal.result.handler.TvSearchSortingHandler;
import no.schibstedsok.searchportal.view.output.TextOutputResultHandler;
import no.schibstedsok.searchportal.view.output.VelocityResultHandler;
import no.schibstedsok.searchportal.view.output.XmlOutputResultHandler;
import no.schibstedsok.searchportal.query.transform.ExactTitleMatchTransformer;
import no.schibstedsok.searchportal.query.transform.InfopageQueryTransformer;
import no.schibstedsok.searchportal.query.transform.NewsTransformer;
import no.schibstedsok.searchportal.query.transform.TokenMaskTransformer;
import no.schibstedsok.searchportal.query.transform.QueryTransformer;
import no.schibstedsok.searchportal.query.transform.SimpleSiteSearchTransformer;
import no.schibstedsok.searchportal.query.transform.SynonymQueryTransformer;
import no.schibstedsok.searchportal.query.transform.TermPrefixTransformer;
import no.schibstedsok.searchportal.query.transform.WeatherQueryTransformer;
import no.schibstedsok.searchportal.query.transform.WeatherInfopageQueryTransformer;
import no.schibstedsok.searchportal.result.handler.AddDocCountModifier;
import no.schibstedsok.searchportal.result.handler.AgeCalculatorResultHandler;
import no.schibstedsok.searchportal.result.handler.CategorySplitter;
import no.schibstedsok.searchportal.result.handler.ContentSourceCollector;
import no.schibstedsok.searchportal.result.handler.DiscardDuplicatesResultHandler;
import no.schibstedsok.searchportal.result.handler.DiscardOldNewsResultHandler;
import no.schibstedsok.searchportal.result.handler.FieldChooser;
import no.schibstedsok.searchportal.result.handler.FindFileFormat;
import no.schibstedsok.searchportal.result.handler.ForecastWindHandler;
import no.schibstedsok.searchportal.result.handler.ImageHelper;
import no.schibstedsok.searchportal.result.handler.MapCoordHandler;
import no.schibstedsok.searchportal.result.handler.MultiValuedFieldCollector;
import no.schibstedsok.searchportal.result.handler.PhoneNumberChooser;
import no.schibstedsok.searchportal.result.handler.PhoneNumberFormatter;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.result.handler.SpellingSuggestionChooser;
import no.schibstedsok.searchportal.result.handler.ForecastDateHandler;
import no.schibstedsok.searchportal.result.handler.SumFastModifiers;
import no.schibstedsok.searchportal.result.handler.DateFormatHandler;
import no.schibstedsok.searchportal.result.handler.WeatherCelciusHandler;
import no.schibstedsok.searchportal.result.handler.WeatherDateHandler;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.util.config.AbstractDocumentFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:mick@wever.org>mick</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchModeFactory extends AbstractDocumentFactory implements SiteKeyedFactory{

    /**
     * The context any SearchModeFactory must work against. *
     */
    public interface Context extends BaseContext, ResourceContext, SiteContext {}

    // Constants -----------------------------------------------------

    private static final Map<Site, SearchModeFactory> INSTANCES = new HashMap<Site,SearchModeFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();

    /** TODO comment me. **/
    public static final String MODES_XMLFILE = "modes.xml";


    private static final Map<SearchMode,Map<String,SearchConfiguration>> COMMANDS
            = new HashMap<SearchMode,Map<String,SearchConfiguration>>();
    private static final ReentrantReadWriteLock COMMANDS_LOCK = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(SearchModeFactory.class);
    private static final String ERR_DOC_BUILDER_CREATION
            = "Failed to DocumentBuilderFactory.newInstance().newDocumentBuilder()";
    private static final String ERR_MISSING_IMPLEMENTATION = "Missing implementation case in CommandTypes";
    private static final String ERR_ONLY_ONE_CHILD_NAVIGATOR_ALLOWED
            = "Each FastNavigator is only allowed to have one child. Parent was ";
    private static final String ERR_FAST_EPS_QR_SERVER =
            "Query server adressen cannot contain the scheme (http://): ";
    private static final String INFO_PARSING_MODE = "Parsing mode ";
    private static final String INFO_PARSING_CONFIGURATION = " Parsing configuration ";
    private static final String INFO_PARSING_NAVIGATOR = "  Parsing navigator ";
    private static final String INFO_PARSING_RESULT_HANDLER = "  Parsing result handler ";
    private static final String INFO_PARSING_QUERY_TRANSFORMER = "  Parsing query transformer ";
    private static final String DEBUG_PARSED_PROPERTY = "  Property property ";

    // Attributes ----------------------------------------------------

    private final Map<String,SearchMode> modes = new HashMap<String,SearchMode>();
    private final ReentrantReadWriteLock modesLock = new ReentrantReadWriteLock();

    private final DocumentLoader loader;
    private final Context context;

    private String templatePrefix;

    // Static --------------------------------------------------------

    /** TODO comment me. **/
    public static SearchModeFactory valueOf(final Context cxt) {

        final Site site = cxt.getSite();

        INSTANCES_LOCK.readLock().lock();
        SearchModeFactory instance = INSTANCES.get(site);
        INSTANCES_LOCK.readLock().unlock();

        if (instance == null) {
            try {
                instance = new SearchModeFactory(cxt);
            } catch (ParserConfigurationException ex) {
                LOG.error(ERR_DOC_BUILDER_CREATION,ex);
            }
        }
        return instance;
    }

    /** TODO comment me. **/
    public boolean remove(final Site site){

        try{
            INSTANCES_LOCK.writeLock().lock();
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of ModeFactoryImpl */
    private SearchModeFactory(final Context cxt)
            throws ParserConfigurationException {

        LOG.trace("ModeFactory(cxt)");
        INSTANCES_LOCK.writeLock().lock();

        context = cxt;

        // configuration files
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        loader = context.newDocumentLoader(MODES_XMLFILE, builder);

        // update the store of factories
        INSTANCES.put(context.getSite(), this);
        // start initialisation
        init();
        INSTANCES_LOCK.writeLock().unlock();

    }

    // Public --------------------------------------------------------

    /** TODO comment me. **/
    public SearchMode getMode(final String id){

        LOG.trace("getMode(" + id + ")");

        SearchMode mode = getModeImpl(id);
        if(mode == null && id != null && id.length() >0 && context.getSite().getParent() != null){
            // not found in this site's modes.xml. look in parent's site.
            final SearchModeFactory factory = valueOf(ContextWrapper.wrap(
                    Context.class,
                    new SiteContext(){
                public Site getSite(){
                    return context.getSite().getParent();
                }
                public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                    return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
                }
                public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                    return UrlResourceLoader.newDocumentLoader(this, resource, builder);
                }
            },
                    context
                    ));
            mode = factory.getMode(id);
        }
        return mode;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void init(){

        loader.abut();
        LOG.debug("Parsing " + MODES_XMLFILE + " started");
        final Document doc = loader.getDocument();
        final Element root = doc.getDocumentElement();

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
            mode.setExecutor(parseExecutor(modeE.getAttribute("executor"),
                    inherit != null ? inherit.getExecutor() : new SequentialSearchCommandExecutor()));
            fillBeanProperty(mode, inherit, "analysis", ParseType.Boolean, modeE, "false");

            // setup new commands list for this mode
            final Map<String,SearchConfiguration> modesCommands = new HashMap<String,SearchConfiguration>();
            try{
                COMMANDS_LOCK.writeLock().lock();
                COMMANDS.put(mode, modesCommands);
            }finally{
                COMMANDS_LOCK.writeLock().unlock();
            }

            // now loop through commands
            for(CommandTypes commandType : CommandTypes.values()){
                final NodeList commandsList = modeE.getElementsByTagName(commandType.getXmlName());
                for (int j = 0; j < commandsList.getLength(); ++j) {
                    final Element commandE = (Element) commandsList.item(j);
                    final SearchConfiguration sc = commandType.parseSearchConfiguration(context, commandE, mode);
                    modesCommands.put(sc.getName(), sc);
                    mode.addSearchConfiguration(sc);
                }
            }
            // add mode
            try{
                modesLock.writeLock().lock();
                modes.put(id, mode);
            }finally{
                modesLock.writeLock().unlock();
            }
        }

        // finished
        LOG.debug("Parsing " + MODES_XMLFILE + " finished");

    }

    private static SearchCommandExecutor parseExecutor(final String name, final SearchCommandExecutor def){

        if("parallel".equalsIgnoreCase(name)){
            return new ParallelSearchCommandExecutor();
        }else if("sequential".equalsIgnoreCase(name)){
            return new SequentialSearchCommandExecutor();
        }
        return def;
    }

    private SearchMode getModeImpl(final String id){

        try{
            modesLock.readLock().lock();
            return modes.get(id);

        }finally{
            modesLock.readLock().unlock();
        }
    }

    // Inner classes -------------------------------------------------

    private enum CommandTypes {
        COMMAND(AbstractSearchConfiguration.class),
        ADVANCED_FAST_COMMAND(AdvancedFastSearchConfiguration.class),
        BLENDING_NEWS_COMMAND(BlendingNewsSearchConfiguration.class),
        FAST_COMMAND(FastSearchConfiguration.class),
        HITTA_COMMAND(HittaSearchConfiguration.class),
        MATH_COMMAND(MathExpressionSearchConfiguration.class),
        MOBILE_COMMAND(MobileSearchConfiguration.class),
        NEWS_COMMAND(NewsSearchConfiguration.class),
        OVERTURE_PPC_COMMAND(OverturePPCSearchConfiguration.class),
        PICTURE_COMMAND(PicSearchConfiguration.class),
        SENSIS_COMMAND(SensisSearchConfiguration.class),
        STATIC_COMMAND(StaticSearchConfiguration.class),
        STOCK_COMMAND(StockSearchConfiguration.class),
        STORMWEATHER_COMMAND(StormWeatherSearchConfiguration.class),
        TVSEARCH_COMMAND(TvSearchConfiguration.class),
        YAHOO_IDP_COMMAND(YahooIdpSearchConfiguration.class),
        YELLOWPAGES_COMMAND(YellowSearchConfiguration.class),
        WEB_COMMAND(WebSearchConfiguration.class),
        WHITEPAGES_COMMAND(WhiteSearchConfiguration.class),
        DAILY_WORD_COMMAND(DailyWordConfiguration.class),
        BLOG_COMMAND(BlogSearchConfiguration.class);


        private final Class<? extends SearchConfiguration> clazz;
        private final String xmlName;


        CommandTypes(final Class<? extends SearchConfiguration> clazz){
            this.clazz = clazz;
            xmlName = name().replaceAll("_","-").toLowerCase();
        }

        public String getXmlName(){
            return xmlName;
        }

        public SearchConfiguration parseSearchConfiguration(
                final Context cxt,
                final Element commandE,
                final SearchMode mode){

            final SearchConfiguration inherit = findParent(commandE.getAttribute("inherit"), mode);
            final String id = commandE.getAttribute("id");
            LOG.info(INFO_PARSING_CONFIGURATION + commandE.getLocalName() + " " + id);

            try {
                final Constructor<? extends SearchConfiguration> con;
                con = clazz.getConstructor(SearchConfiguration.class);
                final SearchConfiguration sc;
                sc = con.newInstance(inherit);
                fillBeanProperty(sc, inherit, "resultsToReturn", ParseType.Int, commandE, "-1");

                if(sc instanceof AbstractSearchConfiguration){
                    // everything extends AbstractSearchConfiguration
                    final AbstractSearchConfiguration asc = (AbstractSearchConfiguration) sc;

                    asc.setName(id);
                    fillBeanProperty(sc, inherit, "alwaysRun", ParseType.Boolean , commandE, "false");

                    if(commandE.getAttribute("field-filters").length() >0){
                        final String[] fieldFilters = commandE.getAttribute("field-filters").split(",");
                        for(String fieldFilter : fieldFilters){
                            if(fieldFilter.contains(" AS ")){
                                final String[] ff = fieldFilter.split(" AS ");
                                asc.addFieldFilter(ff[0].trim(), ff[1].trim());
                            }else{
                                asc.addFieldFilter(fieldFilter, fieldFilter);
                            }
                        }
                    }
                    fillBeanProperty(sc, inherit, "paging", ParseType.Boolean , commandE, "false");
                    fillBeanProperty(sc, inherit, "queryParameter", ParseType.String , commandE, "");

                    if(commandE.getAttribute("result-fields").length() >0){
                        final String[] resultFields = commandE.getAttribute("result-fields").split(",");
                        for(String resultField : resultFields){
                            asc.addResultField(resultField.trim().split(" AS "));
                        }
                    }

                    fillBeanProperty(sc, inherit, "statisticalName", ParseType.String , commandE, "");

                }
                if(sc instanceof FastSearchConfiguration){
                    final FastSearchConfiguration fsc = (FastSearchConfiguration) sc;
                    final FastSearchConfiguration fscInherit = inherit instanceof FastSearchConfiguration
                            ? (FastSearchConfiguration)inherit
                            : null;
                    fillBeanProperty(sc, inherit, "clustering", ParseType.Boolean , commandE, "false");
                    fillBeanProperty(sc, inherit, "collapsing", ParseType.Boolean , commandE, "false");
                    if(commandE.getAttribute("collections").length() >0){
                        fsc.getCollections().clear();
                        final String[] collections = commandE.getAttribute("collections").split(",");
                        for(String collection : collections){
                            fsc.addCollection(collection);
                        }
                    }
                    fillBeanProperty(sc, inherit, "filter", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "ignoreNavigation", ParseType.Boolean , commandE, "false");
                    fillBeanProperty(sc, inherit, "offensiveScoreLimit", ParseType.Int , commandE, "-1");
                    fillBeanProperty(sc, inherit, "qtPipeline", ParseType.String , commandE, "");
                    final String queryServerUrl = commandE.getAttribute("query-server-url");
                    // TODO use fillBeanProperty pattern instead
                    fsc.setQueryServerURL(parseProperty(cxt, queryServerUrl,
                            fscInherit != null ? fscInherit.getQueryServerURL() : null));
                    fillBeanProperty(sc, inherit, "relevantQueries", ParseType.Boolean , commandE, "false");
                    fillBeanProperty(sc, inherit, "sortBy", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "spamScoreLimit", ParseType.Int , commandE, "-1");
                    fillBeanProperty(sc, inherit, "spellcheck", ParseType.Boolean , commandE, "false");

                    // navigators
                    final NodeList nList = commandE.getElementsByTagName("navigators");
                    for(int i = 0; i < nList.getLength(); ++i){
                        final Collection<Navigator> navigators = parseNavigators((Element)nList.item(i));
                        for(Navigator navigator : navigators){
                            fsc.addNavigator(navigator, navigator.getId());
                        }

                    }
                }
                if (sc instanceof AdvancedFastSearchConfiguration) {
                    final AdvancedFastSearchConfiguration asc = (AdvancedFastSearchConfiguration) sc;
                    final AdvancedFastSearchConfiguration ascInherit = inherit instanceof AdvancedFastSearchConfiguration
                            ? (AdvancedFastSearchConfiguration) inherit
                            : null;
                    fillBeanProperty(sc, inherit, "view", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "sortBy", ParseType.String , commandE, "default");
                    // TODO use fillBeanProperty pattern instead
                    asc.setCollapsingEnabled(parseBoolean(commandE.getAttribute("collapsing"),
                            ascInherit != null ? ascInherit.isCollapsingEnabled() : false));
                    fillBeanProperty(sc, inherit, "collapseOnField", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "qtPipeline", ParseType.String , commandE, "");

                    final String qrServer = commandE.getAttribute("query-server");
                    final String qrServerValue = parseProperty(cxt, qrServer,
                            ascInherit != null ? ascInherit.getQueryServer() : null);

                    if (qrServerValue.startsWith("http://")) {
                        throw new IllegalArgumentException(ERR_FAST_EPS_QR_SERVER + qrServerValue);
                    }

                    asc.setQueryServer(qrServerValue);
                    // navigators
                    final NodeList nList = commandE.getElementsByTagName("navigators");
                    for(int i = 0; i < nList.getLength(); ++i){
                        final Collection<Navigator> navigators = parseNavigators((Element)nList.item(i));
                        for(Navigator navigator : navigators){
                            asc.addNavigator(navigator, navigator.getId());
                        }

                    }
                }
                if(sc instanceof HittaSearchConfiguration){
                    fillBeanProperty(sc, inherit, "catalog", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "key", ParseType.String , commandE, "");
                }
                if(sc instanceof MathExpressionSearchConfiguration){
                    final MathExpressionSearchConfiguration msc = (MathExpressionSearchConfiguration) sc;
                }
                if(sc instanceof NewsSearchConfiguration){
                }
                if(sc instanceof AbstractYahooSearchConfiguration){
                    fillBeanProperty(sc, inherit, "encoding", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "host", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "partnerId", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "port", ParseType.Int , commandE, "80");
                }
                if(sc instanceof OverturePPCSearchConfiguration){
                    fillBeanProperty(sc, inherit, "url", ParseType.String , commandE, "");
                }
                if(sc instanceof YahooIdpSearchConfiguration){
                    fillBeanProperty(sc, inherit, "database", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "dateRange", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "filter", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "hideDomain", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "region", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "regionMix", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "spellState", ParseType.String , commandE, "");
                    fillBeanProperty(sc, inherit, "unique", ParseType.String , commandE, "");
                }
                if(sc instanceof PicSearchConfiguration){
                    final PicSearchConfiguration psc = (PicSearchConfiguration) sc;
                    final PicSearchConfiguration pscInherit = inherit instanceof PicSearchConfiguration
                            ? (PicSearchConfiguration)inherit
                            : null;
                    final String queryServerHost = commandE.getAttribute("query-server-host");
                    // TODO use fillBeanProperty pattern instead
                    psc.setQueryServerHost(parseProperty(cxt, queryServerHost,
                            pscInherit != null ? pscInherit.getQueryServerHost() : null));
                    // TODO use fillBeanProperty pattern instead
                    final String queryServerPort = commandE.getAttribute("query-server-port");
                    // TODO use fillBeanProperty pattern instead
                    psc.setQueryServerPort(Integer.valueOf(parseProperty(cxt, queryServerPort,
                            pscInherit != null ? String.valueOf(pscInherit.getQueryServerPort()) : "0")));
                    fillBeanProperty(sc, inherit, "picsearchCountry", ParseType.String , commandE, "no");
                }
                if(sc instanceof SensisSearchConfiguration){
                    final SensisSearchConfiguration ssc = (SensisSearchConfiguration) sc;
                }
                if(sc instanceof StockSearchConfiguration){
                    final StockSearchConfiguration ssc = (StockSearchConfiguration) sc;
                }
                if(sc instanceof WebSearchConfiguration){
                    final WebSearchConfiguration wsc = (WebSearchConfiguration) sc;
                }
                if(sc instanceof WhiteSearchConfiguration){
                    final WhiteSearchConfiguration wsc = (WhiteSearchConfiguration) sc;
                }
                if(sc instanceof YellowSearchConfiguration){
                    final YellowSearchConfiguration ysc = (YellowSearchConfiguration) sc;
                }
                if (sc instanceof MobileSearchConfiguration) {
                    final MobileSearchConfiguration msc = (MobileSearchConfiguration) sc;

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
                if (sc instanceof BlendingNewsSearchConfiguration) {
                    final BlendingNewsSearchConfiguration bnsc = (BlendingNewsSearchConfiguration) sc;

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

                if (sc instanceof StormWeatherSearchConfiguration) {
                    final StormWeatherSearchConfiguration swsc = (StormWeatherSearchConfiguration) sc;
                    if(commandE.getAttribute("xml-elements").length() >0){
                        final String[] elms = commandE.getAttribute("xml-elements").split(",");
                        for(String elm : elms){
                            swsc.addElementValue(elm.trim());
                        }
                    }
                }

                if (sc instanceof TvSearchConfiguration) {
                    final TvSearchConfiguration tssc = (TvSearchConfiguration) sc;
                    final String[] defaultChannels = commandE.getAttribute("default-channels").split(",");
                    for (String channel : defaultChannels) {
                        tssc.addDefaultChannel(channel.trim());
                    }
                    // TODO use fillBeanProperty pattern instead
                    tssc.setResultsToFetch(Integer.parseInt(commandE.getAttribute("results-to-fetch")));

                }
                if (sc instanceof BlogSearchConfiguration) {
                    final BlogSearchConfiguration bsc = (BlogSearchConfiguration) sc;
                }

                // query transformers
                NodeList qtNodeList = commandE.getElementsByTagName("query-transformers");
                final Element qtRootElement = (Element) qtNodeList.item(0);
                if(qtRootElement != null){
                    qtNodeList = qtRootElement.getChildNodes();

                    // clear all inherited query-transformers
                    sc.clearQueryTransformers();

                    for(int i = 0; i < qtNodeList.getLength(); i++) {
                        final Node node = qtNodeList.item(i);
                        if (!(node instanceof Element)) {
                            continue;
                        }
                        final Element qt = (Element) node;
                        for (QueryTransformerTypes qtType : QueryTransformerTypes.values()) {
                            if (qt.getTagName().equals(qtType.getXmlName())) {
                                sc.addQueryTransformer(qtType.parseQueryTransformer(qt));
                            }
                        }
                    }
                }

                // result handlers
                NodeList rhNodeList = commandE.getElementsByTagName("result-handlers");
                final Element rhRootElement = (Element) rhNodeList.item(0);
                if(rhRootElement != null){
                    rhNodeList = rhRootElement.getChildNodes();

                    // clear all inherited result handlers
                    sc.clearResultHandlers();

                    for(int i = 0; i < rhNodeList.getLength(); i++) {
                        final Node node = rhNodeList.item(i);
                        if (!(node instanceof Element)) {
                            continue;
                        }
                        final Element rh = (Element) node;
                        for (ResultHandlerTypes rhType : ResultHandlerTypes.values()) {
                            if (rh.getTagName().equals(rhType.getXmlName())) {
                                sc.addResultHandler(rhType.parseResultHandler(rh));
                            }
                        }
                    }
                }

                return sc;

            } catch (InstantiationException ex) {
                throw new InfrastructureException(ex);
            } catch (IllegalAccessException ex) {
                throw new InfrastructureException(ex);
            } catch (SecurityException ex) {
                throw new InfrastructureException(ex);
            } catch (NoSuchMethodException ex) {
                throw new InfrastructureException(ex);
            } catch (IllegalArgumentException ex) {
                throw new InfrastructureException(ex);
            } catch (InvocationTargetException ex) {
                throw new InfrastructureException(ex);
            }
        }

        private SearchConfiguration findParent(
                final String id,
                final SearchMode mode){

            SearchMode m = mode;
            SearchConfiguration config = null;
            do{
                COMMANDS_LOCK.readLock().lock();
                final Map<String,SearchConfiguration> configs = COMMANDS.get(m);
                COMMANDS_LOCK.readLock().unlock();
                config = configs.get(id);
                m = m.getParentSearchMode();
            }while(config == null && m != null);

            return config;
        }

        private String parseProperty(final Context cxt, final String s, final String def){

            final String key = s.trim().length() == 0 ? def != null ? def : "" : s;
            final String value = SiteConfiguration.valueOf(
                    ContextWrapper.wrap(SiteConfiguration.Context.class, cxt))
                    .getProperty(key);
            final String result = value != null ? value : key;
            LOG.debug(DEBUG_PARSED_PROPERTY + key + " --> " + result);
            return result;
        }

        private Collection<Navigator> parseNavigators(final Element navsE){

            final Collection<Navigator> navigators = new ArrayList<Navigator>();
            final NodeList children = navsE.getChildNodes();
            for(int i = 0; i < children.getLength(); ++i){
                final Node child = children.item(i);
                if(child instanceof Element && "navigator".equals(((Element)child).getTagName())){
                    final Element navE = (Element)child;
                    final String id = navE.getAttribute("id");
                    final String name = navE.getAttribute("name");
                    LOG.info(INFO_PARSING_NAVIGATOR + id + " [" + name + "]");
                    final Navigator nav = new Navigator(
                            name,
                            navE.getAttribute("field"),
                            navE.getAttribute("display-name"));
                    nav.setId(id);
                    final Collection<Navigator> childNavigators = parseNavigators(navE);
                    if(childNavigators.size() > 1){
                        throw new IllegalStateException(ERR_ONLY_ONE_CHILD_NAVIGATOR_ALLOWED + id);
                    }else if(childNavigators.size() == 1){
                        nav.setChildNavigator(childNavigators.iterator().next());
                    }
                    navigators.add(nav);
                }
            }

            return navigators;
        }
    }

    private enum QueryTransformerTypes {
        AGEFILTER(AgeFilterTransformer.class),
        EXACT_TITLE_MATCH(ExactTitleMatchTransformer.class),
        INFOPAGE(InfopageQueryTransformer.class),
        // @deprecated see deprecated note in class
        MOBILE_TV(MobileTvQueryTransformer.class),
        NEWS(NewsTransformer.class),
        // @deprecated use token-remover match="prefix" instead.
        PREFIX_REMOVER(TokenMaskTransformer.class),
        REGEXP(RegExpTransformer.class),
        SIMPLE_SITE_SEARCH(SimpleSiteSearchTransformer.class),
        SYNONYM(SynonymQueryTransformer.class),
        TERM_PREFIX(TermPrefixTransformer.class),
        TOKEN_MASK(TokenMaskTransformer.class),
        TVSEARCH(TvSearchQueryTransformer.class),
        WEATHER(WeatherQueryTransformer.class),
        WEATHERINFOPAGE(WeatherInfopageQueryTransformer.class);

        private final Class<? extends QueryTransformer> clazz;
        private final String xmlName;

        QueryTransformerTypes(final Class<? extends QueryTransformer> c){
            clazz = c;
            xmlName = name().replaceAll("_","-").toLowerCase();
        }

        public String getXmlName(){
            return xmlName;
        }

        public QueryTransformer parseQueryTransformer(final Element qt){
            try {

                LOG.info(INFO_PARSING_QUERY_TRANSFORMER + xmlName);
                final QueryTransformer transformer = clazz.newInstance();
                switch(this){
                    case AGEFILTER:
                        final AgeFilterTransformer agft = (AgeFilterTransformer) transformer;
                        agft.setAgeField(qt.getAttribute("field"));
                        break;
                    case PREFIX_REMOVER:
                        final TokenMaskTransformer prqt = (TokenMaskTransformer) transformer;
                        prqt.addPredicates(qt.getAttribute("prefixes").split(","));
                        prqt.setMatch(TokenMaskTransformer.Position.PREFIX);
                        break;
                    case REGEXP:
                        fillBeanProperty(transformer, null, "regexp", ParseType.String, qt, "");
                        fillBeanProperty(transformer, null, "replacement", ParseType.String, qt, "");
                        break;
                    case SIMPLE_SITE_SEARCH:
                        final SimpleSiteSearchTransformer ssqt = (SimpleSiteSearchTransformer) transformer;
                        ssqt.setParameterName(qt.getAttribute("parameter"));
                        break;
                    case TERM_PREFIX:
                        fillBeanProperty(transformer, null, "prefix", ParseType.String, qt, "");
                        fillBeanProperty(transformer, null, "number-prefix", ParseType.String, qt, "");
                        break;
                    case TOKEN_MASK:
                        final TokenMaskTransformer trqt = (TokenMaskTransformer) transformer;
                        trqt.addPredicates(qt.getAttribute("predicates").split(","));
                        if(qt.getAttribute("match").length() > 0){
                            trqt.setMatch(
                                    TokenMaskTransformer.Position.valueOf(qt.getAttribute("position").toUpperCase()));
                        }
                        if(qt.getAttribute("mask").length() >0){
                            trqt.setMask(
                                    TokenMaskTransformer.Mask.valueOf(qt.getAttribute("mask").toUpperCase()));
                        }
                        break;
                    case TVSEARCH:
                        fillBeanProperty(transformer, null, "with-endtime", ParseType.Boolean, qt, "");
                        break;
                    case WEATHER:
                        final WeatherQueryTransformer wqt = (WeatherQueryTransformer) transformer;
                        wqt.setDefaultLocations(qt.getAttribute("default-locations").split(","));
                    default:
                        break;
                }
                return transformer;

            } catch (InstantiationException ex) {
                throw new InfrastructureException(ex);
            } catch (IllegalAccessException ex) {
                throw new InfrastructureException(ex);
            }
        }

    }

    private enum ResultHandlerTypes {
        ADD_DOC_COUNT(AddDocCountModifier.class),
        AGE_CALCULATOR(AgeCalculatorResultHandler.class),
        CATEGORY_SPLITTER(CategorySplitter.class),
        CONTENT_SOURCE_COLLECTOR(ContentSourceCollector.class),
        DATA_MODEL(DataModelResultHandler.class),
        DISCARD_OLD_NEWS(DiscardOldNewsResultHandler.class),
        DISCARD_DUPLICATES(DiscardDuplicatesResultHandler.class),
        FIELD_CHOOSER(FieldChooser.class),
        FIND_FILE_FORMAT(FindFileFormat.class),
        IMAGE_HELPER(ImageHelper.class),
        MULTIVALUED_FIELD_COLLECTOR(MultiValuedFieldCollector.class),
        NUMBER_OPERATION(NumberOperationHandler.class),
        PHONE_NUMBER_CHOOSER(PhoneNumberChooser.class),
        PHONE_NUMBER_FORMATTER(PhoneNumberFormatter.class),
        SPELLING_SUGGESTION_CHOOSER(SpellingSuggestionChooser.class),
        SUM(SumFastModifiers.class),
        DATE_FORMAT(DateFormatHandler.class),
        WEATHER_CELCIUS(WeatherCelciusHandler.class),
        WEATHER_DATE(WeatherDateHandler.class),
        FORECAST_DATE(ForecastDateHandler.class),
        FORECAST_WIND(ForecastWindHandler.class),
        MAP_COORD(MapCoordHandler.class),
        TVSEARCH_SORTING(TvSearchSortingHandler.class),
        COMBINE_NAVIGATORS(CombineNavigatorsHandler.class),
        TEXT_OUTPUT(TextOutputResultHandler.class),
        VELOCITY_OUTPUT(VelocityResultHandler.class),
        FIELD_ESCAPE(FieldEscapeHandler.class),
        XML_OUTPUT(XmlOutputResultHandler.class);


        private final Class<? extends ResultHandler> clazz;
        private final String xmlName;

        ResultHandlerTypes(final Class<? extends ResultHandler> c){
            clazz = c;
            xmlName = name().replaceAll("_","-").toLowerCase();
        }

        public String getXmlName(){
            return xmlName;
        }

        public ResultHandler parseResultHandler(final Element rh){

            try {
                LOG.info(INFO_PARSING_RESULT_HANDLER + xmlName);
                final ResultHandler handler = clazz.newInstance();
                switch(this){
                    case ADD_DOC_COUNT:
                        final AddDocCountModifier adc = (AddDocCountModifier) handler;
                        adc.setModifierName(rh.getAttribute("modifier"));
                        break;
                    case AGE_CALCULATOR:
                        final AgeCalculatorResultHandler ac = (AgeCalculatorResultHandler) handler;
                        ac.setTargetField(rh.getAttribute("target"));
                        ac.setSourceField(rh.getAttribute("source"));
                        break;
                    case FIELD_CHOOSER:
                        final FieldChooser fc = (FieldChooser) handler;
                        {
                            fc.setTargetField(rh.getAttribute("target"));
                            final String[] fields = rh.getAttribute("fields").split(",");
                            for(String field : fields){
                                fc.addField(field);
                            }
                        }
                        break;
                    case MULTIVALUED_FIELD_COLLECTOR:
                        final MultiValuedFieldCollector mvfc = (MultiValuedFieldCollector) handler;
                        {
                            if(rh.getAttribute("fields").length() >0){
                                final String[] fields = rh.getAttribute("fields").split(",");
                                for(String field : fields){
                                    if(field.contains(" AS ")){
                                        final String[] ff = field.split(" AS ");
                                        mvfc.addField(ff[0], ff[1]);
                                    }else{
                                        mvfc.addField(field, field);
                                    }
                                }
                            }
                        }
                        break;
                    case NUMBER_OPERATION:
                        final NumberOperationHandler noh = (NumberOperationHandler)handler;
                        {
                            if(rh.getAttribute("fields").length() >0){
                                final String[] fields = rh.getAttribute("fields").split(",");
                                for(String field : fields){
                                    noh.addField(field);
                                }
                            }
                            noh.setTarget(parseString(rh.getAttribute("target"), ""));
                            noh.setOperation(parseString(rh.getAttribute("operation"), ""));
                            noh.setMinDigits(parseInt(rh.getAttribute("min-digits"), 1));
                            noh.setMaxDigits(parseInt(rh.getAttribute("max-digits"), 99));
                            noh.setMinFractionDigits(parseInt(rh.getAttribute("min-fraction-digits"), 0));
                            noh.setMaxFractionDigits(parseInt(rh.getAttribute("max-fraction-digits"), 99));
                        }
                        break;
                    case IMAGE_HELPER:
                        final ImageHelper im = (ImageHelper) handler;
                        {
                            if(rh.getAttribute("fields").length() >0){
                                final String[] fields = rh.getAttribute("fields").split(",");
                                for(String field : fields){
                                    if(field.contains(" AS ")){
                                        final String[] ff = field.split(" AS ");
                                        im.addField(ff[0], ff[1]);
                                    }else{
                                        im.addField(field, field);
                                    }
                                }
                            }
                        }
                        break;
                    case SPELLING_SUGGESTION_CHOOSER:
                        final SpellingSuggestionChooser ssc = (SpellingSuggestionChooser) handler;
                        ssc.setMinScore(parseInt(rh.getAttribute("min-score"), -1));
                        ssc.setMaxSuggestions(parseInt(rh.getAttribute("max-suggestions"), -1));
                        ssc.setMaxDistance(parseInt(rh.getAttribute("max-distance"), -1));
                        ssc.setMuchBetter(parseInt(rh.getAttribute("much-better"), -1));
                        ssc.setLongQuery(parseInt(rh.getAttribute("long-query"), -1));
                        ssc.setVeryLongQuery(parseInt(rh.getAttribute("very-long-query"), -1));
                        ssc.setLongQueryMaxSuggestions(parseInt(rh.getAttribute("long-query-max-suggestions"), -1));
                        break;
                    case SUM:
                        final SumFastModifiers sfm = (SumFastModifiers) handler;
                        final String[] modifiers = rh.getAttribute("modifiers").split(",");
                        for(String modifier : modifiers){
                            sfm.addModifierName(modifier);
                        }
                        sfm.setNavigatorName(rh.getAttribute("navigation"));
                        sfm.setTargetModifier(rh.getAttribute("target"));
                        break;
                    case DATE_FORMAT:
                        final DateFormatHandler dh = (DateFormatHandler) handler;
                        if (rh.hasAttribute("prefix")) {
                            dh.setFieldPrefix(rh.getAttribute("prefix"));
                        }
                        dh.setSourceField(rh.getAttribute("source"));
                        break;
                    case WEATHER_CELCIUS:
                        final WeatherCelciusHandler ch = (WeatherCelciusHandler) handler;
                        ch.setTargetField(rh.getAttribute("target"));
                        ch.setSourceField(rh.getAttribute("source"));
                        break;
                    case MAP_COORD:
                        final MapCoordHandler mch = (MapCoordHandler) handler;
                        break;
                    case FORECAST_WIND:
                        final ForecastWindHandler wh = (ForecastWindHandler) handler;
                        break;
                    case DISCARD_DUPLICATES:    //subclasses must be checked first
                        final DiscardDuplicatesResultHandler ddh = (DiscardDuplicatesResultHandler) handler;
                        ddh.setSourceField(rh.getAttribute("key"));
                        ddh.setDiscardCase(new Boolean(rh.getAttribute("ignorecase")).booleanValue());
                        break;
                    case FORECAST_DATE:    //subclasses must be checked first
                        final ForecastDateHandler sdateh = (ForecastDateHandler) handler;
                        sdateh.setTargetField(rh.getAttribute("target"));
                        sdateh.setSourceField(rh.getAttribute("source"));
                        break;
                    case WEATHER_DATE:
                        final WeatherDateHandler dateh = (WeatherDateHandler) handler;
                        dateh.setTargetField(rh.getAttribute("target"));
                        dateh.setSourceField(rh.getAttribute("source"));
                        break;
                    case TVSEARCH_SORTING:
                        final TvSearchSortingHandler tssh = (TvSearchSortingHandler) handler;

                        tssh.setResultsPerBlock(Integer.parseInt(rh.getAttribute("results-per-block")));
                        tssh.setBlocksPerPage(Integer.parseInt(rh.getAttribute("blocks-per-page")));
                        break;
                    case FIELD_ESCAPE:
                        final FieldEscapeHandler feh = (FieldEscapeHandler) handler;
                        feh.setSourceField(rh.getAttribute("source-field"));
                        feh.setTargetField(rh.getAttribute("target-field"));
                        break;
                    case COMBINE_NAVIGATORS:
                        final CombineNavigatorsHandler cnh = (CombineNavigatorsHandler) handler;
                        cnh.setTarget(rh.getAttribute("target"));

                        final NodeList navs = rh.getElementsByTagName("navigator");

                        for (int i = 0; i < navs.getLength(); i++) {
                            final Element nav = (Element) navs.item(i);

                            final NodeList mods = nav.getElementsByTagName("modifier");
                            for (int j = 0; j < mods.getLength(); j++) {
                                final Element mod = (Element) mods.item(j);

                                cnh.addMapping(nav.getAttribute("name"), mod.getAttribute("name"));
                            }
                        }
                        break;
                    default:
                        break;
                }

                return handler;

            } catch (InstantiationException ex) {
                throw new InfrastructureException(ex);
            } catch (IllegalAccessException ex) {
                throw new InfrastructureException(ex);
            }
        }

    }
}
