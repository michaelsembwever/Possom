// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.analyzer;

import com.thoughtworks.xstream.XStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.front.searchportal.configuration.loaders.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.ResourceContext;
import no.schibstedsok.front.searchportal.query.StopWordRemover;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.configuration.loaders.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.UrlResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.XStreamLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Responsible for loading and serving all the Regular Expression Token Evaluators.
 * These regular expression patterns come from the configuration file SearchConstants.REGEXP_EVALUATOR_XMLFILE.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Revision$</tt>
 */
public final class RegExpEvaluatorFactory {

    /**
     * The context the RegExpEvaluatorFactory must work against. *
     */
    public interface Context extends ResourceContext, SiteContext {
    }

    private static final Log LOG = LogFactory.getLog(RegExpEvaluatorFactory.class);

    private static final String ERR_MUST_USE_CONTEXT_CONSTRUCTOR = "Must use constructor that supplies a context!";
    private static final String ERR_DOC_BUILDER_CREATION = "Failed to DocumentBuilderFactory.newInstance().newDocumentBuilder()";
    private static final String ERR_COULD_NOT_FIND_TOKEN_PREDICATE = "Failed to find TokenPredicate.";

    /**
     * No need to synchronise this. Worse that can happen is multiple identical INSTANCES are created at the same
     * time. But only one will persist in the map.
     *  There might be a reason to synchronise to avoid the multiple calls to the search-front-config context to obtain
     * the resources to improve the performance. But I doubt this would gain much, if anything at all.
     */
    private static final Map/*<Site,RegExpEvaluatorFactory>*/ INSTANCES = new HashMap/*<Site,RegExpEvaluatorFactory>*/();

    private final Context context;
    private final DocumentLoader loader;
    private volatile boolean init = false;

    private Map/*<TokenPredicate>,<Collection>*/ regExpEvaluators = new HashMap/*<TokenPredicate>,<Collection>*/();

    /**
     * Illegal Constructor. Must use RegExpEvaluatorFactory(SiteContext).
     */
    private RegExpEvaluatorFactory() {
        throw new IllegalArgumentException(ERR_MUST_USE_CONTEXT_CONSTRUCTOR);
    }


    private RegExpEvaluatorFactory(final Context cxt)
            throws ParserConfigurationException {

        context = cxt;
        loader = context.newDocumentLoader(SearchConstants.REGEXP_EVALUATOR_XMLFILE,
                DocumentBuilderFactory.newInstance().newDocumentBuilder());

        INSTANCES.put(context.getSite(), this);
    }

    /** Loads the resource SearchConstants.REGEXP_EVALUATOR_XMLFILE containing all regular expression patterns
     *   for all the RegExpTokenEvaluators we will be using.
     *  Keeps thread-safe state so that this method can be called multiple times with the resource only loaded once.
     *  ( Truth is that it may run more than once, in parallel during the first call, but because regExpEvaluators uses
     *    the TokenPredicates as keys there will not be differing states. Just a small performance lost during this first
     *    call. )
     */
    private void init() {

        if (!init) {
            loader.abut();
            LOG.debug("Parsing " + SearchConstants.REGEXP_EVALUATOR_XMLFILE + " started");
            final Document doc = loader.getDocument();
            final Element root = doc.getDocumentElement();
            final NodeList evaluators = root.getElementsByTagName("evaluator");
            for (int i = 0; i < evaluators.getLength(); ++i) {

                final Element evaluator = (Element) evaluators.item(i);

                final String tokenName = evaluator.getAttribute("token");
                LOG.debug(" ->evaluator@token: " + tokenName);

                try  {
                    final TokenPredicate token = (TokenPredicate) TokenPredicate.class.getField(tokenName).get(null);

                    // final boolean queryDep = Boolean.parseBoolean(evaluator.getAttribute("query-dependant")); //Java5
                    final boolean queryDep = Boolean.valueOf(evaluator.getAttribute("query-dependant")).booleanValue();
                    LOG.debug(" ->evaluator@query-dependant: " + queryDep);

                    final Collection compiled = new ArrayList();

                    final NodeList patterns = ((Element) evaluator).getElementsByTagName("pattern");
                    for (int j = 0; j < patterns.getLength(); ++j) {
                        final Element pattern = (Element) patterns.item(j);
                        LOG.debug(" --->pattern: " + pattern);

                        final String expression = pattern.getFirstChild().getNodeValue();
                        final Pattern p = Pattern.compile("\\s*" + expression + "\\s*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                        compiled.add(p);
                    }

                    final RegExpTokenEvaluator regExpTokenEvaluator = new RegExpTokenEvaluator(compiled, queryDep);

                    regExpEvaluators.put(token, regExpTokenEvaluator);
                }  catch (NoSuchFieldException ex) {
                    LOG.error(ERR_COULD_NOT_FIND_TOKEN_PREDICATE, ex);
                }  catch (IllegalAccessException ex) {
                    LOG.error(ERR_COULD_NOT_FIND_TOKEN_PREDICATE, ex);
                }
            }
            LOG.debug("Parsing " + SearchConstants.REGEXP_EVALUATOR_XMLFILE + " finished");
        }
        init = true;
    }

    /** Main method to retrieve the correct RegExpEvaluatorFactory to further obtain
     * RegExpTokenEvaluators and StopWordRemover.
     * @param cxt the contextual needs this factory must use to operate.
     * @return RegExpEvaluatorFactory for this site.
     */
    public static RegExpEvaluatorFactory valueOf(final Context cxt) {
        final Site site = cxt.getSite();
        RegExpEvaluatorFactory instance = (RegExpEvaluatorFactory) INSTANCES.get(site);
        if (instance == null) {
            try {
                instance = new RegExpEvaluatorFactory(cxt);

            } catch (ParserConfigurationException ex) {
                LOG.error(ERR_DOC_BUILDER_CREATION, ex);
            }
        }
        return instance;
    }

    /**
     * Utility wrapper to the valueOf(Context).
     * <b>Makes the presumption we will be using the UrlResourceLoader to load all resources.</b>
     * @param site the site this RegExpEvaluatorFactory will work for.
     * @return RegExpEvaluatorFactory for this site.
     */
    public static RegExpEvaluatorFactory valueOf(final Site site) {

        // RegExpEvaluatorFactory.Context for this site & UrlResourceLoader.
        final RegExpEvaluatorFactory instance = RegExpEvaluatorFactory.valueOf(new RegExpEvaluatorFactory.Context() {
            public Site getSite() {
                return site;
            }

            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }

            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return UrlResourceLoader.newXStreamLoader(this, resource, xstream);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return UrlResourceLoader.newDocumentLoader(this, resource, builder);
            }

        });
        return instance;
    }

    /**
     *
     * @param token
     * @return
     */
    public RegExpTokenEvaluator getEvaluator(final TokenPredicate token) {
        init();
        return (RegExpTokenEvaluator) regExpEvaluators.get(token);
    }

    /**
     *
     * @param token
     * @return
     */
    public StopWordRemover getStopWordRemover(final TokenPredicate token) {
        init();
        return (StopWordRemover) getEvaluator(token);
    }
}
