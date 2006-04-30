// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.analyser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**  Responsible for loading and serving all the AnalysisRule instances.
 * These rules consisting of score sets come from the configuration file SearchConstants.ANALYSIS_RULES_XMLFILE.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Revision$</tt>
 */
public final class AnalysisRuleFactory {

    /**
     * The context the AnalysisRuleFactory must work against. *
     */
    public interface Context extends BaseContext, ResourceContext, SiteContext {
    }

    private static final Logger LOG = Logger.getLogger(AnalysisRuleFactory.class);

    private static final String ERR_DOC_BUILDER_CREATION = "Failed to DocumentBuilderFactory.newInstance().newDocumentBuilder()";
    private static final String ERR_UNABLE_TO_FIND_PREDICATE = "Unable to find predicate with id ";
    private static final String ERR_UNABLE_TO_FIND_PREDICATE_UTILS_METHOD = "Unable to find method PredicateUtils.";
    private static final String ERR_UNABLE_TO_USE_PREDICATE_UTILS_METHOD = "Unable to use method PredicateUtils.";
    private static final String ERR_WHILE_READING_ELEMENT = "Error while reading element ";
    private static final String ERR_TOO_MANY_PREDICATES_IN_NOT
            = "Illegal to have more than one predicate inside a <not> element. Occurred under ";
    private static final String DEBUG_CREATED_PREDICATE = "Parsed predicate ";
    private static final String DEBUG_STARTING_RULE = "Parsing rule ";
    private static final String DEBUG_FINISHED_RULE = "Parsed rule ";

    /**
     * No need to synchronise this. Worse that can happen is multiple identical INSTANCES are created at the same
     * time. But only one will persist in the map.
     *  There might be a reason to synchronise to avoid the multiple calls to the search-front-config context to obtain
     * the resources to improve the performance. But I doubt this would gain much, if anything at all.
     */
    private static final Map<Site,AnalysisRuleFactory> INSTANCES = new HashMap<Site,AnalysisRuleFactory>();

    private final Map predicateIds = new HashMap();


    private final Map rules = new HashMap();

    private final Context context;
    private final DocumentLoader loader;
    private volatile boolean init = false;



    private AnalysisRuleFactory(final Context cxt)
            throws ParserConfigurationException {

        context = cxt;
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        loader = context.newDocumentLoader(SearchConstants.ANALYSIS_RULES_XMLFILE, builder);

        INSTANCES.put(context.getSite(), this);
    }

    private void init() {
        if (!init) {
            loader.abut();
            LOG.debug("Parsing " + SearchConstants.ANALYSIS_RULES_XMLFILE + " started");
            final Document doc = loader.getDocument();
            final Element root = doc.getDocumentElement();

            // global predicates
            final Map globalPredicates = new HashMap();
            readPredicates(root, globalPredicates);

            // ruleList
            final NodeList ruleList = root.getElementsByTagName("rule");
            for (int i = 0; i < ruleList.getLength(); ++i) {
                final Element rule = (Element) ruleList.item(i);
                final String id = rule.getAttribute("id");
                final AnalysisRule analysisRule = new AnalysisRule();
                LOG.debug(DEBUG_STARTING_RULE + id + " " + analysisRule);

                // private predicates
                final Map privatePredicates = new HashMap(globalPredicates);
                readPredicates(rule, privatePredicates);

                // scores
                final NodeList scores = rule.getElementsByTagName("score");
                for (int j = 0; j < scores.getLength(); ++j) {
                    final Element score = (Element) scores.item(j);
                    final String predicateName = score.getAttribute("predicate");
                    final Predicate predicate = findPredicate(predicateName, privatePredicates);
                    final int scoreValue = Integer.parseInt(score.getFirstChild().getNodeValue());

                    analysisRule.addPredicateScore(predicate, scoreValue);
                    analysisRule.setPredicateNameMap(Collections.unmodifiableMap(predicateIds));
                }
                rules.put(id, analysisRule);
                LOG.debug(DEBUG_FINISHED_RULE + id + " " + analysisRule);
            }
            LOG.debug("Parsing " + SearchConstants.ANALYSIS_RULES_XMLFILE + " finished");
        }
        init = true;
    }

    private Map readPredicates(final Element element, final Map predicateMap) {
        final NodeList predicates = element.getChildNodes(); //ElementsByTagName("predicate");

        for (int i = 0; i < predicates.getLength(); ++i) {
            final Node node = predicates.item(i);
            if (node instanceof Element) {
                final Element e = (Element) node;
                if ("predicate".equals(e.getTagName())) {
                    readPredicate(e, predicateMap);
                }
            }
        }
        return predicateMap;
    }

    private Predicate readPredicate(final Element element, final Map predicateMap) {
        Predicate result = null;

        final boolean hasId = element.hasAttribute("id");
        final boolean hasContent = element.hasChildNodes();

        if (hasId && !hasContent) {
            // it's an already defined predicate
            final String id = element.getAttribute("id");

            result = findPredicate(id, predicateMap);

        }  else  {
            // we must create it
            final NodeList operators = element.getChildNodes();
            for (int i = 0; i < operators.getLength(); ++i) {
                final Node operator = operators.item(i);
                if (operator != null && operator instanceof Element) {

                    result = createPredicate((Element) operator, predicateMap);
                    break;
                }
            }

            if (hasId) {
                // its got an ID so we must remember it.
                final String id = element.getAttribute("id");
                predicateMap.put(id, result);
                predicateIds.put(result, id);
                LOG.debug(DEBUG_CREATED_PREDICATE + id + " " + result);
            }
        }

        return result;
    }

    private Predicate findPredicate(final String name, final Map predicateMap) {
        Predicate result = null;
        // first check our predicateMap
        if (predicateMap.containsKey(name)) {
            result = (Predicate) predicateMap.get(name);

        }  else  {
            // second check TokenPredicate enumerations.
            try  {
                result = (Predicate) TokenPredicate.class.getField(name).get(null);

            }  catch (NoSuchFieldException ex) {
                LOG.error(ERR_UNABLE_TO_FIND_PREDICATE + name, ex);
            }  catch (IllegalAccessException ex) {
                LOG.error(ERR_UNABLE_TO_FIND_PREDICATE + name, ex);
            }

        }

        return result;
    }

    private Predicate createPredicate(final Element element, final Map predicateMap) {
        Predicate result = null;
        // The operator to use from PredicateUtils.
        //   The replaceAll's are so we end up with a method with one Predicate[] argument.
        final String methodName = element.getTagName()
            .replaceAll("and", "all")
            .replaceAll("or", "any")
            .replaceAll("either", "one")
            .replaceAll("neither", "none")
            + "Predicate";
        // because we can't use the above operator methods with only one child predicate
        //  the not operator must be a special case.
        final boolean notPredicate = "not".equals(element.getTagName());

        try {
            // Find PredicateUtils static method through reflection
            final Method method = notPredicate
                    ? null
                    : PredicateUtils.class.getMethod(methodName, new Class[]{Collection.class});

            // load all the predicates it will apply to
            final List childPredicates = new LinkedList();
            final NodeList predicates = element.getChildNodes();
            for (int i = 0; i < predicates.getLength(); ++i) {
                final Node node = predicates.item(i);
                if (node instanceof Element) {
                    final Element e = (Element) node;
                    if ("predicate".equals(e.getTagName())) {
                        childPredicates.add(readPredicate(e, predicateMap));
                    }
                }
            }
            if (notPredicate) {
                // there should only be one in the list
                if (childPredicates.size() > 1) {
                    throw new IllegalStateException(ERR_TOO_MANY_PREDICATES_IN_NOT + element.getParentNode());
                }
                result = PredicateUtils.notPredicate((Predicate) childPredicates.get(0));
            }  else  {
                // use the operator through reflection
                result = (Predicate) method.invoke(null, new Object[]{childPredicates});
            }

        } catch (SecurityException ex) {
            LOG.error(ERR_WHILE_READING_ELEMENT + element);
            LOG.error(ERR_UNABLE_TO_FIND_PREDICATE_UTILS_METHOD + methodName, ex);
        } catch (NoSuchMethodException ex) {
            LOG.error(ERR_WHILE_READING_ELEMENT + element);
            LOG.error(ERR_UNABLE_TO_FIND_PREDICATE_UTILS_METHOD + methodName, ex);
        }  catch (IllegalAccessException ex) {
            LOG.error(ERR_WHILE_READING_ELEMENT + element);
            LOG.error(ERR_UNABLE_TO_USE_PREDICATE_UTILS_METHOD + methodName, ex);
        }  catch (InvocationTargetException ex) {
            LOG.error(ERR_WHILE_READING_ELEMENT + element);
            LOG.error(ERR_UNABLE_TO_USE_PREDICATE_UTILS_METHOD + methodName, ex);
        }  catch (IllegalArgumentException ex) {
            LOG.error(ERR_WHILE_READING_ELEMENT + element);
            LOG.error(ERR_UNABLE_TO_USE_PREDICATE_UTILS_METHOD + methodName, ex);
        }

        return result;
    }

    /**
     *
     * Returns a map of all the rules. The key is the name of the rule
     *
     * @return all rules.
     */
    public Map getRules() {
        init();
        return rules;
    }


    /**
     *
     * Returns the rule with the name <code>ruleName</code>.
     *
     * @param   ruleName    the name of the rule
     * @return  the rule.
     */
    public AnalysisRule getRule(final String ruleName) {
        LOG.trace("getRule(" + ruleName + ")");

        init();
        final AnalysisRule rule = (AnalysisRule) rules.get(ruleName);

        return rule;
    }

    /**
     * Main method to retrieve the correct AnalysisRuleFactory to further obtain
     * AnalysisRule.
     *
     * @param cxt the contextual needs this factory must use to operate.
     * @return AnalysisRuleFactory for this site.
     */
    public static AnalysisRuleFactory valueOf(final Context cxt) {
        final Site site = cxt.getSite();
        AnalysisRuleFactory instance = INSTANCES.get(site);
        if (instance == null) {
            try {
                instance = new AnalysisRuleFactory(cxt);

            } catch (ParserConfigurationException ex) {
                LOG.error(ERR_DOC_BUILDER_CREATION, ex);
            }
        }
        return instance;
    }

    /**
     * Utility wrapper to the valueOf(Context).
     * <b>Makes the presumption we will be using the UrlResourceLoader to load all resources.</b>
     *
     * @param site the site this AnalysisRuleFactory will work for.
     * @return AnalysisRuleFactory for this site.
     */
    public static AnalysisRuleFactory valueOf(final Site site) {

        // RegExpEvaluatorFactory.Context for this site & UrlResourceLoader.
        final AnalysisRuleFactory instance = AnalysisRuleFactory.valueOf(new AnalysisRuleFactory.Context() {
            public Site getSite() {
                return site;
            }

            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return UrlResourceLoader.newDocumentLoader(this, resource, builder);
            }

        });
        return instance;
    }

}
