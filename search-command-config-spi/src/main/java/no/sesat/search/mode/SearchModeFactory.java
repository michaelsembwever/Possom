/* Copyright (2006-2009) Schibsted ASA
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

 */
package no.sesat.search.mode;


import no.sesat.Interpreter;
import no.sesat.search.run.transform.RunTransformerConfig;
import no.sesat.search.site.config.AbstractConfigFactory;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.mode.config.CommandConfig;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.query.transform.QueryTransformerConfig;
import no.sesat.search.result.handler.ResultHandlerConfig;
import no.sesat.search.site.Site;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.sesat.search.mode.config.BaseSearchConfiguration;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.run.handler.RunHandlerConfig;

/**<p>
 * Controlling class around the deserialisation of modes.xml into all the SearchModes.
 *  This in turn deserialises all SearchConfigurations, QueryTransformers, QueryBuilders, and ResultHandlers.</p><p>
 *
 * Deserialisation of xml elements to configuration classes works through each implementation of AbstractDocumentFactory
 * These being: <br/>
 *  mode --> SearchModeFactory<br/>
 *  *-commands --> SearchModeFactory.SearchCommandFactory</br>
 *  query-builder --> QueryBuilderFactory<br/>
 *  query-transformers --> QueryTransformerFactory<br/>
 *  result-handlers --> ResultHandlerFactory<br/>
 *  run-handlers --> RunHandlerFactory<br/></p><p>
 *
 * The lookup of configuration classname from the xml element name generally follows the pattern provided by the
 *  @see #xmlToBeanName(java.lang.String) method.
 * But each AbstractDocumentFactory.findClass(..) provides the final implementations with details of which package to
 *  look in and any static Prefix and Suffixes to the classname.</p><p>
 * For search configuration classes the implementation is such:<br/>
 *  1) to be found in the no.sesat.search.mode.config package<br/>
 *  2) the first character always becomes uppercase (to suit java standards for naming classes)<br/>
 *  3) the suffix "Config" is added.<br/>
 * For example &lt;solr-command id="abc" .../&gt; becomes "SolrCommandConfig".
 * </p><p>
 *  The lookup from configuration class to control class happens via the value of the Controller annotation.
 *  (There's discussion in moving this annotation to a normal string attribute in the configuration bean).
 * </p>
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
    private static final RunHandlerConfigFactory RUN_HANDLER_FACTORY = new RunHandlerConfigFactory();
    private static final RunTransformerConfigFactory RUN_TRANSFORMER_FACTORY = new RunTransformerConfigFactory();
    private static final QueryBuilderFactory QUERY_BUILDER_FACTORY = new QueryBuilderFactory();


    /**
     * The name of the modes configuration file.
     */
    public static final String MODES_XMLFILE = "modes.xml";
    private static final String RUN_HANDLERS = "run-handlers";
    private static final String RUN_TRANSFORMERS = "run-transformers";

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


    static {
        Interpreter.addFunction("commands", new Interpreter.Function() {
            public String execute(Interpreter.Context ctx) {
                String res = "";
                COMMANDS_LOCK.readLock().lock();
                try {
                    for (SearchMode c : COMMANDS.keySet()) {
                        res += "Mode: " + c + "\n";
                        for (String s : COMMANDS.get(c).keySet()) {
                            res += "   " + COMMANDS.get(c).get(s).toString() + "\n";
                        }
                        res += "\n";
                    }
                }
                finally {
                    COMMANDS_LOCK.readLock().unlock();
                }
                return res;

            }
            @Override
            public String describe() {
                return "Print out the SearchModes in COMMANDS.";
            }
        });
    }

    /**
     * Returns the factory for the site in the context.
     *
     * @param cxt The context.
     * @return the factory for the context.
     */
    public static SearchModeFactory instanceOf(final Context cxt) {

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
            final SearchModeFactory factory = instanceOf(ContextWrapper.wrap(
                    Context.class,
                    context.getSite().getParent().getSiteContext(),
                    context
            ));
            mode = factory.getMode(id);
        }
        return mode;
    }

    // Package protected ---------------------------------------------

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

                fillBeanProperty(mode, inherit, "evaluation", ParseType.Boolean, modeE, "true");
                fillBeanProperty(mode, inherit, "analysis", ParseType.Boolean, modeE, "false");
                fillBeanProperty(mode, inherit, "autoBroadening", ParseType.Boolean, modeE, "true");

                mode.setRunTransformers(getRunTransformers(modeE, inherit));
                mode.setRunHandlers(getRunHandlers(modeE, inherit));

                // setup new commands list for this mode
                final Map<String, SearchConfiguration> modesCommands = new HashMap<String, SearchConfiguration>();
                try {
                    COMMANDS_LOCK.writeLock().lock();
                    COMMANDS.put(mode, modesCommands);
                } finally {
                    COMMANDS_LOCK.writeLock().unlock();
                }


                final NodeList childNodes = modeE.getChildNodes();
                final Collection<SearchConfiguration> searchConfigurations = new ArrayList<SearchConfiguration>();

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
                        modesCommands.put(sc.getId(), sc);
                        searchConfigurations.add(sc);

//                    }else if("navigation".equals(childElement.getTagName())){
//                        // navigation
//                        assert null == mode.getNavigationConfiguration() : "NavigationConfiguration already set!";
//
//                        final NodeList navigationElements = childElement.getElementsByTagName("navigation");
//
//                        mode.setNavigationConfiguration(parseNavigation(id, navigationElements));
                    }
                }
                mode.setSearchConfigurations(searchConfigurations);

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

    private final List<RunHandlerConfig> getRunHandlers(final Element e, final SearchMode inherit) {
        final List<RunHandlerConfig> runHandlers = new ArrayList<RunHandlerConfig>();

        final NodeList rhNodes = e.getElementsByTagName(RUN_HANDLERS);
        if (rhNodes.getLength() > 0) {
            final NodeList nodes = rhNodes.item(0).getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i) instanceof Element) {
                    final Element node = (Element) nodes.item(i);
                    if (RUN_HANDLER_FACTORY.supported(node.getTagName(), context)) {
                        RunHandlerConfig rhc = RUN_HANDLER_FACTORY.parseRunHandlerConfiguration(context, node);
                        runHandlers.add(rhc);
                    }
                }
            }
        } else if (inherit != null && inherit.getRunHandlers().size() > 0)  {
            runHandlers.addAll(inherit.getRunHandlers());
        }
        return runHandlers;
    }

    private final List<RunTransformerConfig> getRunTransformers(final Element e, final SearchMode inherit) {
        final List<RunTransformerConfig> runTransformers = new ArrayList<RunTransformerConfig>();

        final NodeList rtNodes = e.getElementsByTagName(RUN_TRANSFORMERS);
        if (rtNodes.getLength() > 0) {
            final NodeList nodes = rtNodes.item(0).getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i) instanceof Element) {
                    final Element node = (Element) nodes.item(i);
                    if (RUN_TRANSFORMER_FACTORY.supported(node.getTagName(), context)) {
                        RunTransformerConfig rtc = RUN_TRANSFORMER_FACTORY.parseRunTransformerConfiguration(context, node);
                        runTransformers.add(rtc);
                    }
                }
            }
        } else if (inherit != null && inherit.getRunTransformers().size() > 0)  {
            runTransformers.addAll(inherit.getRunTransformers());
        }
        return runTransformers;
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
    private static final class RunHandlerConfigFactory extends AbstractConfigFactory<RunHandlerConfig> {
        RunHandlerConfigFactory() {}

        @Override
        protected Class<RunHandlerConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            final String classNameFQ = "no.sesat.search.run.handler."+ className+ "RunHandlerConfig";
            final Class<RunHandlerConfig> clazz = loadClass(context, classNameFQ, Spi.RUN_HANDLER_CONFIG);

            LOG.debug("run-handler: Found class " + clazz.getName());
            return clazz;
        }

        protected final RunHandlerConfig parseRunHandlerConfiguration(final Context context, final Element e) {
            return construct(e, context).readRunHandler(e);
        };
    }

    private static final class RunTransformerConfigFactory extends AbstractConfigFactory<RunTransformerConfig> {
        RunTransformerConfigFactory() {}

        @Override
        protected Class<RunTransformerConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            final String classNameFQ = "no.sesat.search.run.transform."+ className+ "RunTransformerConfig";
            final Class<RunTransformerConfig> clazz = loadClass(context, classNameFQ, Spi.RUN_TRANSFORM_CONFIG);

            LOG.debug("run-transformer: Found class " + clazz.getName());
            return clazz;
        }

        protected final RunTransformerConfig parseRunTransformerConfiguration(final Context context, final Element e) {
            return construct(e, context).readRunTransformer(e);
        };
    }

    private static final class SearchCommandFactory extends AbstractConfigFactory<CommandConfig> {

        SearchCommandFactory() {}

        SearchConfiguration parseSearchConfiguration(
                final SearchModeFactory.Context cxt,
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

                if(sc instanceof BaseSearchConfiguration){

                    // TODO move this into CommandConfig
                    final BaseSearchConfiguration bsc = (BaseSearchConfiguration)sc;

                    // initial query transformer (fills out the transformedTerms map the first time)
                    NodeList iqtNodeList = commandE.getElementsByTagName("initial-query-transformer");
                    final Element iqtRootElement = (Element) iqtNodeList.item(0);
                    if (iqtRootElement != null) {
                        iqtNodeList = iqtRootElement.getChildNodes();
                        for (int i = 0; i < iqtNodeList.getLength(); i++) {
                            final Node node = iqtNodeList.item(i);
                            if (!(node instanceof Element)) {
                                continue;
                            }

                            final Element iqtE = (Element) node;
                            if (QUERY_TRANSFORMER_FACTORY.supported(iqtE.getTagName(), cxt)) {

                                bsc.setInitialQueryTransformer(
                                        QUERY_TRANSFORMER_FACTORY.parseQueryTransformer(iqtE, cxt));

                                // only one can be defined so we're finished
                                break;
                            }
                        }
                    } else if (null != inherit && inherit instanceof BaseSearchConfiguration) {
                        // inherit it
                        bsc.setInitialQueryTransformer(((BaseSearchConfiguration)inherit).getInitialQueryTransformer());
                    }

                    // query transformers
                    NodeList qtNodeList = commandE.getElementsByTagName("query-transformers");
                    final Element qtRootElement = (Element) qtNodeList.item(0);
                    if (qtRootElement != null) {
                        qtNodeList = qtRootElement.getChildNodes();

                        // clear all inherited query-transformers
                        bsc.clearQueryTransformers();

                        for (int i = 0; i < qtNodeList.getLength(); i++) {
                            final Node node = qtNodeList.item(i);
                            if (!(node instanceof Element)) {
                                continue;
                            }
                            final Element qt = (Element) node;
                            if (QUERY_TRANSFORMER_FACTORY.supported(qt.getTagName(), cxt)) {
                                bsc.addQueryTransformer(QUERY_TRANSFORMER_FACTORY.parseQueryTransformer(qt, cxt));
                            }
                        }
                    }else if (null != inherit && inherit instanceof BaseSearchConfiguration) {
                        // inherit all
                        for (QueryTransformerConfig qtc : ((BaseSearchConfiguration)inherit).getQueryTransformers()) {
                            bsc.addQueryTransformer(qtc);
                        }
                    }

                    // query builder
                    NodeList qbNodeList = commandE.getElementsByTagName("query-builder");
                    final Element qbRootElement = (Element) qbNodeList.item(0);
                    if (qbRootElement != null) {
                        qbNodeList = qbRootElement.getChildNodes();
                        for (int i = 0; i < qbNodeList.getLength(); i++) {
                            final Node node = qbNodeList.item(i);
                            if (!(node instanceof Element)) {
                                continue;
                            }
                            final Element qb = (Element) node;
                            if (QUERY_BUILDER_FACTORY.supported(qb.getTagName(), cxt)) {
                                bsc.setQueryBuilder(QUERY_BUILDER_FACTORY.parseQueryBuilder(qb, cxt));
                                // only one can be defined so we're finished
                                break;
                            }
                        }
                    }else if (null != inherit && inherit instanceof BaseSearchConfiguration) {
                        // inherit it
                        bsc.setQueryBuilder(((BaseSearchConfiguration)inherit).getQueryBuilder());
                    }

                    // result handlers
                    NodeList rhNodeList = commandE.getElementsByTagName("result-handlers");
                    final Element rhRootElement = (Element) rhNodeList.item(0);
                    if (rhRootElement != null) {
                        rhNodeList = rhRootElement.getChildNodes();

                        // clear all inherited result handlers
                        bsc.clearResultHandlers();

                        for (int i = 0; i < rhNodeList.getLength(); i++) {
                            final Node node = rhNodeList.item(i);
                            if (!(node instanceof Element)) {
                                continue;
                            }
                            final Element rh = (Element) node;
                            if (RESULT_HANDLER_FACTORY.supported(rh.getTagName(), cxt)) {
                                bsc.addResultHandler(RESULT_HANDLER_FACTORY.parseResultHandler(rh, cxt));
                            }
                        }
                    } else if (null != inherit && inherit instanceof BaseSearchConfiguration) {
                        // inherit all
                        for (ResultHandlerConfig rhc : ((BaseSearchConfiguration)inherit).getResultHandlers()) {
                            bsc.addResultHandler(rhc);
                        }
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
                final SearchModeFactory.Context context) {

            CommandConfig sc = construct(element, context);

            assert null == inherit || inherit.getClass().isAssignableFrom(sc.getClass())
                    : "Can only inherit from same or superclass configuration. "
                    + element.getAttribute("id") + '(' + sc.getClass().getSimpleName() + ')'
                    + " trying to inherit from " + inherit.getId() + '(' + inherit.getClass().getSimpleName() + ')';

            sc.readSearchConfiguration(element, inherit, context);

            LOG.info("ParsedSearchConfiguration: " + sc);
            return sc;
        }

        protected Class<CommandConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());
            LOG.debug("findClass (SearchConfiguration) " + className);

            final String classNameFQ = "no.sesat.search.mode.config."+ className+ "Config";
            final Class<CommandConfig> clazz = loadClass(context, classNameFQ, Spi.SEARCH_COMMAND_CONFIG);

            LOG.debug("Found class " + clazz.getName());
            return clazz;
        }
    }

    private static final class QueryTransformerFactory extends AbstractConfigFactory<QueryTransformerConfig> {

        QueryTransformerFactory() {
        }

        QueryTransformerConfig parseQueryTransformer(final Element qt, final SearchModeFactory.Context context) {
            return construct(qt, context).readQueryTransformer(qt);
        }

        protected Class<QueryTransformerConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass (QueryTransformerConfig) " + className);

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

        ResultHandlerConfig parseResultHandler(final Element rh, final SearchModeFactory.Context context) {
            return construct(rh, context).readResultHandler(rh);
        }

        protected Class<ResultHandlerConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass (ResultHandlerConfig) " + className);

            final String classNameFQ = "no.sesat.search.result.handler."
                    + className
                    + "ResultHandlerConfig";

            final Class<ResultHandlerConfig> clazz = loadClass(context, classNameFQ, Spi.RESULT_HANDLER_CONFIG);
            LOG.info("Found class " + clazz.getName());
            return clazz;
        }
    }

    private static final class QueryBuilderFactory extends AbstractConfigFactory<QueryBuilderConfig> {

        QueryBuilderFactory() {}

        QueryBuilderConfig parseQueryBuilder(final Element qt, final SearchModeFactory.Context context) {

            final QueryBuilderConfig config = construct(qt, context);

            if( config instanceof QueryBuilderConfig.ModesW3cDomDeserialiser ){
                return ((QueryBuilderConfig.ModesW3cDomDeserialiser)config).readQueryBuilder(qt);
            }

            throw new UnsupportedOperationException("Unknown deserialisation method");
        }

        protected Class<QueryBuilderConfig> findClass(final String xmlName, final Context context)
                throws ClassNotFoundException {

            final String bName = xmlToBeanName(xmlName);
            final String className = Character.toUpperCase(bName.charAt(0)) + bName.substring(1, bName.length());

            LOG.debug("findClass (QueryBuilderConfig) " + className);

            final String classNameFQ = "no.sesat.search.mode.config.querybuilder."
                    + className
                    + "QueryBuilderConfig";

            final Class<QueryBuilderConfig> clazz = loadClass(context, classNameFQ, Spi.SEARCH_COMMAND_CONFIG);

            LOG.debug("Found class " + clazz.getName());

            return clazz;
        }

    }

}
