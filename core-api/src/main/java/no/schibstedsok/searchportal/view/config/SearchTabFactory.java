// Copyright (2006-2007) Schibsted Søk AS
/*
 * ViewFactory.java
 *
 * Created on 19. april 2006, 20:48
 */

package no.schibstedsok.searchportal.view.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.site.config.AbstractConfigFactory;
import no.schibstedsok.searchportal.mode.NavigationConfig;
import no.schibstedsok.searchportal.site.config.ResourceContext;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.ResourceContext;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.Spi;
import no.schibstedsok.searchportal.view.config.SearchTab.Layout;
import no.schibstedsok.searchportal.view.i18n.TextMessages;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class SearchTabFactory extends AbstractDocumentFactory implements SiteKeyedFactory{

    /**
     * The context any SearchTabFactory must work against.
     */
    public interface Context extends ResourceContext, AbstractConfigFactory.Context {}

   // Constants -----------------------------------------------------

    private static final Map<Site, SearchTabFactory> INSTANCES = new HashMap<Site,SearchTabFactory>();
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();
    
    private static final NavFactory NAV_FACTORY = new NavFactory();    

    private static final String MSG_NAV_PREFIX = "navigation_";

    /**
     * Name of the configuration file.
     */
    public static final String VIEWS_XMLFILE = "views.xml";

    private static final Logger LOG = Logger.getLogger(SearchTabFactory.class);
    private static final String ERR_DOC_BUILDER_CREATION
            = "Failed to DocumentBuilderFactory.newInstance().newDocumentBuilder()";
    private static final String INFO_PARSING_TAB = "Parsing tab ";
    private static final String INFO_PARSING_ENRICHMENT = " Parsing enrichment ";
    private static final String INFO_PARSING_NAVIGATION_ID = " Parsing navigation id ";
    private static final String INFO_PARSING_NAVIGATION_NAME = " Parsing navigation name ";

    private static final String MSG_DISPLAY_NAV_PREFIX = "navigation_display_";
    private static final String MISSING_NAV = "Mo message prop. for ";

    private static final String RESET_NAV_ELEMENT = "reset";
    private static final String NAV_CONFIG_ELEMENT = "config";
    
    // Attributes ----------------------------------------------------

    private final Map<String,SearchTab> tabsByName = new HashMap<String,SearchTab>();
    private final Map<String,SearchTab> tabsByKey = new HashMap<String,SearchTab>();
    // redundant w/ init() in constructor TODO remove
    private final ReentrantReadWriteLock tabsLock = new ReentrantReadWriteLock(); 

    private final DocumentLoader loader;
    private final Context context;
    

    // Static --------------------------------------------------------

    /** Return the factory in use for the skin defined within the context. *
     * @param cxt
     * @return
     */
    public static SearchTabFactory valueOf(final Context cxt) {

        final Site site = cxt.getSite();
        assert null != site;

        SearchTabFactory instance;
        try{
            INSTANCES_LOCK.readLock().lock();
            instance = INSTANCES.get(site);
        }finally{
            INSTANCES_LOCK.readLock().unlock();
        }

        if (instance == null) {
            try {
                instance = new SearchTabFactory(cxt);
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
            return null != INSTANCES.remove(site);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of ViewFactory */
    private SearchTabFactory(final Context cxt) throws ParserConfigurationException {

        LOG.trace("SearchTabFactory(cxt)");
        try{
            INSTANCES_LOCK.writeLock().lock();

            context = cxt;

            // configuration files
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            loader = context.newDocumentLoader(cxt, VIEWS_XMLFILE, builder);

            // start initialisation
            init();

            // update the store of factories
            INSTANCES.put(context.getSite(), this);
            LOG.debug("site: "+ context.getSite() + "; tabsByName:" + tabsByName);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }

    }

    // Public --------------------------------------------------------

    /** Find the tab with the given id.
     * Search recursively up through the skin's parents.
     * <b>Allow to return null.</b>
     * @param id
     * @return
     */
    public SearchTab getTabByName(final String id){

        LOG.trace("getTabByName(" + id + ')');
        LOG.trace(tabsByName);

        SearchTab tab = getTabImpl(id);
        if(null == tab && null != context.getSite().getParent()){
            // not found in this site's views.xml. look in parent's site.
            final SearchTabFactory factory = valueOf(ContextWrapper.wrap(
                    Context.class,
                    new SiteContext(){
                        public Site getSite(){
                            return context.getSite().getParent();
                        }
                    },
                    context
                ));
            tab = factory.getTabByName(id);

        }else{
            LOG.trace("found tab for " + id + " against SearchTabFactory for " + context.getSite());
        }

        return tab;
    }

    /** Find the tab with the given key.
     * Search recursively up through the skin's parents.
     * <b>Allow to return null.</b>
     * @param key
     * @return
     */
    public SearchTab getTabByKey(final String key){

        LOG.trace("getTabByKey(" + key + ')');

        SearchTab tab = getTabByKeyImpl(key);
        if(null == tab && null != context.getSite().getParent()){
            // not found in this site's views.xml. look in parent's site.
            final SearchTabFactory factory = valueOf(ContextWrapper.wrap(
                    Context.class,
                    new SiteContext(){
                        public Site getSite(){
                            return context.getSite().getParent();
                        }
                    },
                    context
                ));
            tab = factory.getTabByKeyImpl(key);

        }else{
            LOG.trace("found tab for " + key + " against SearchTabFactory for " + context.getSite());
        }

        return tab;
    }

    public Map<String,SearchTab> getTabsByName(){
        
        LOG.trace("getTabsByName()");

        try{
            tabsLock.readLock().lock();
            return Collections.unmodifiableMap(tabsByName);

        }finally{
            tabsLock.readLock().unlock();
        }        

    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------


    private void init() {

        loader.abut();
        LOG.info("Parsing " + VIEWS_XMLFILE + " started. " + "Site: " + context.getSite());
        final Document doc = loader.getDocument();
        final Element root = doc.getDocumentElement();
        if( null != root ){
            final TextMessages msgs = TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class, context));

            final NodeList tabList = root.getElementsByTagName("tab");

            for(int i = 0 ; i < tabList.getLength(); ++i){
                final Element tabE = (Element) tabList.item(i);
                final String id = tabE.getAttribute("id");
                LOG.info(INFO_PARSING_TAB + id);
                final SearchTab inherit = getTabByName(tabE.getAttribute("inherit"));
                final String mode = parseString(tabE.getAttribute("mode"), inherit != null ? inherit.getMode() : "");
                final String key = parseString(tabE.getAttribute("key"), "");
                final String parentKey = parseString(tabE.getAttribute("parent-key"),
                        inherit != null ? inherit.getParentKey() : "");
                final String adCommand = parseString(tabE.getAttribute("ad-command"),
                        inherit != null ? inherit.getAdCommand() : "");
                final String allCss = parseString(tabE.getAttribute("css"), null);
                final String[] css = allCss != null ? allCss.split(",") : new String[]{};
                final String allJavascript = parseString(tabE.getAttribute("javascript"), null);
                final String[] javascript = allJavascript != null ? allJavascript.split(",") : new String[]{};

                
                // enrichment hints
                final NodeList enrichmentNodeList = tabE.getElementsByTagName("enrichment");
                final Collection<SearchTab.EnrichmentHint> enrichments = new ArrayList<SearchTab.EnrichmentHint>();
                for(int j = 0 ; j < enrichmentNodeList.getLength(); ++j){
                    final Element e = (Element) enrichmentNodeList.item(j);
                    final String rule = e.getAttribute("rule");
                    LOG.info(INFO_PARSING_ENRICHMENT + rule);
                    final int threshold = parseInt(e.getAttribute("threshold"), -1);
                    final float weight = parseFloat(e.getAttribute("weight"), -1);
                    final String command = e.getAttribute("command");
                    final SearchTab.EnrichmentHint enrichment
                            = new SearchTab.EnrichmentHint(rule, threshold, weight, command);
                    enrichments.add(enrichment);
                }

                // navigation hints
                final NodeList navigationNodeList = tabE.getElementsByTagName("navigation");
                Element navE = null;
                for(int j = 0 ; null == navE && j < navigationNodeList.getLength(); ++j){
                    final Element n = (Element) navigationNodeList.item(j);
                    // only interested in the direct children
                    if(tabE == n.getParentNode()){

                        navE = n;
                    }
                }
                
                final NavigationConfig navConf = parseNavigation(
                        mode,
                        null != navE ? navE.getElementsByTagName("navigation") : new NodeList() {
                            public Node item(final int arg0) {
                                throw new IllegalArgumentException("empty nodelist");
                            }
                            public int getLength() {
                                return 0;
                            }
                        }, 
                        null != inherit ? inherit.getNavigationConfiguration() : null);
                               
                // the tab's layout
                final Layout layout = new Layout(null != inherit ? inherit.getLayout() : null)
                        .readLayout((Element)tabE.getElementsByTagName("layout").item(0));

                final SearchTab tab = new SearchTab(
                        inherit,
                        id,
                        mode,
                        key,
                        parentKey,
                        tabE.getAttribute("rss-result-name"),
                        parseBoolean(tabE.getAttribute("rss-hidden"), false),
                        parseInt(tabE.getAttribute("page-size"), inherit != null ? inherit.getPageSize() : -1),
                        navConf,
                        parseInt(tabE.getAttribute("enrichment-limit"), inherit != null
                            ? inherit.getEnrichmentLimit()
                            : -1),
                        parseInt(tabE.getAttribute("enrichment-on-top"), inherit != null
                            ? inherit.getEnrichmentOnTop()
                            : -1),
                        parseInt(tabE.getAttribute("enrichment-on-top-score"), inherit != null
                            ? inherit.getEnrichmentOnTopScore()
                            : -1),
                        enrichments,
                        adCommand,
                        parseInt(tabE.getAttribute("ad-limit"), inherit != null ? inherit.getAdLimit() : -1),
                        parseInt(tabE.getAttribute("ad-on-top"), inherit != null ? inherit.getAdOnTop() : -1),
                        Arrays.asList(css),
                        Arrays.asList(javascript),
                        parseBoolean(tabE.getAttribute("absolute-ordering"), inherit != null
                            ? inherit.isAbsoluteOrdering()
                            : false),
                        parseBoolean(tabE.getAttribute("display-css"), true),
                        parseBoolean(tabE.getAttribute("execute-on-blank"), inherit != null
                        ? inherit.isExecuteOnBlank()
                        : true),
                        layout);

                try{
                    tabsLock.writeLock().lock();
                    tabsByName.put(id, tab);
                    tabsByKey.put(key, tab);
                }finally{
                    tabsLock.writeLock().unlock();
                }
            }
        }

        // finished
        LOG.info("Parsing " + VIEWS_XMLFILE + " finished");

    }

    private SearchTab getTabImpl(final String id){

        LOG.trace("getTabImpl(" + id + ')');

        try{
            tabsLock.readLock().lock();
            return tabsByName.get(id);

        }finally{
            tabsLock.readLock().unlock();
        }
    }

    private SearchTab getTabByKeyImpl(final String key){

        LOG.trace("getTabByKeyImpl(" + key + ')');

        try{
            tabsLock.readLock().lock();
            return tabsByKey.get(key);

        }finally{
            tabsLock.readLock().unlock();
        }
    }


    private NavigationConfig parseNavigation(
            final String modeId, 
            final NodeList navigationElements,
            final NavigationConfig inherit) {
        
        final NavigationConfig cfg = new NavigationConfig(inherit);
        
        for (int i = 0; i < navigationElements.getLength(); i++) {
            final Element navigationElement = (Element) navigationElements.item(i);
            final NavigationConfig.Navigation navigation = new NavigationConfig.Navigation(navigationElement);

            final NodeList navs = navigationElement.getChildNodes();

            for (int l = 0; l < navs.getLength(); l++) {
                final Node navNode = navs.item(l);

                if (navNode instanceof Element 
                        && ! (RESET_NAV_ELEMENT.equals(navNode.getNodeName()) 
                        || NAV_CONFIG_ELEMENT.equals(navNode.getNodeName()))) {
                    
                    final NavigationConfig.Nav nav = NAV_FACTORY.parseNav((Element) navNode,navigation,  context, null);
                    navigation.addNav(nav, cfg);
                }
            }

            for (int j = 0; j < navs.getLength(); j++) {
                final Node navElement = navs.item(j);

                if (RESET_NAV_ELEMENT.equals(navElement.getNodeName())) {
                    final String resetNavId = ((Element)navElement).getAttribute("modeId");
                    if (modeId != null) {
                        final NavigationConfig.Nav nav = navigation.getNavMap().get(resetNavId);
                        if (nav != null) {
                            navigation.addReset(nav);
                        } else {
                            LOG.error("Error in config, <reset modeId=\"" + modeId + "\" />, in mode " + modeId + " not found");
                        }
                    }
                }
            }

            cfg.addNavigation(navigation);
        }
        return cfg;
    }
    
    // Inner classes -------------------------------------------------
    
    private static final class NavFactory extends AbstractConfigFactory<NavigationConfig.Nav> {

        NavigationConfig.Nav parseNav(
                final Element element,
                final NavigationConfig.Navigation navigation,
                final Context context,
                final NavigationConfig.Nav parent) {
            
            try {

                Class<NavigationConfig.Nav> clazz = null;

                // TODO: Temporary to keep old-style modes.xml working.
                if ("reset".equals(element.getNodeName()) || "static-parameter".equals(element.getNodeName())) {
                    clazz = findClass("nav", context);
                } else {
                    clazz = findClass(element.getNodeName(), context);
                }

                final Constructor<NavigationConfig.Nav> c
                        = clazz.getConstructor(NavigationConfig.Nav.class, NavigationConfig.Navigation.class, Element.class);

                final NavigationConfig.Nav nav = c.newInstance(parent, navigation, element);

                final NodeList children = element.getChildNodes();

                for (int i = 0; i < children.getLength(); ++i) {
                    final Node navNode = children.item(i);

                    if (navNode instanceof Element && !NAV_CONFIG_ELEMENT.equals(navNode.getNodeName())) {
                        nav.addChild(parseNav((Element) navNode, navigation, context, nav));
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

        protected Class<NavigationConfig.Nav> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {
            
            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass " + className);

            // Special case for "nav".
            final String classNameFQ = xmlName.equals("nav")
                    ? NavigationConfig.Nav.class.getName()
                    : "no.schibstedsok.searchportal.mode.navigation."+ className+ "NavigationConfig";

            final Class<NavigationConfig.Nav> clazz = loadClass(context, classNameFQ, Spi.SEARCH_COMMAND_CONFIG);

            LOG.debug("Found class " + clazz.getName());
            return clazz;
        }
    }

}