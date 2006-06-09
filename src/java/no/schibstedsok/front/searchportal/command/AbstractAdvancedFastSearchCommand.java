/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractAdvancedFastSearchCommand.java
 *
 * Created on 14 March 2006, 19:51
 *
 */

package no.schibstedsok.front.searchportal.command;

import com.fastsearch.esp.search.ConfigurationException;
import com.fastsearch.esp.search.ISearchFactory;
import com.fastsearch.esp.search.SearchEngineException;
import com.fastsearch.esp.search.SearchFactory;
import com.fastsearch.esp.search.query.BaseParameter;
import com.fastsearch.esp.search.query.IQuery;
import com.fastsearch.esp.search.query.Query;
import com.fastsearch.esp.search.query.SearchParameter;
import com.fastsearch.esp.search.result.IDocumentSummary;
import com.fastsearch.esp.search.result.IDocumentSummaryField;
import com.fastsearch.esp.search.result.IModifier;
import com.fastsearch.esp.search.result.INavigator;
import com.fastsearch.esp.search.result.IQueryResult;
import com.fastsearch.esp.search.view.ISearchView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.AdvancedFastConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * Base class for commands queryinga FAST EPS Server.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractAdvancedFastSearchCommand extends AbstractSearchCommand {

    // Constants -----------------------------------------------------
    private final static String FACTORY_PROPERTY = 
            "com.fastsearch.esp.search.SearchFactory";
    private final static String HTTP_FACTORY =
            "com.fastsearch.esp.search.http.HttpSearchFactory";
    private final static String QR_SERVER_PROPERTY = 
            "com.fastsearch.esp.search.http.qrservers";
    private final static String ENCODER_PROPERTY = 
            "com.fastsearch.esp.search.http.encoderclass";
    private final static String ENCODER_CLASS = 
            "com.fastsearch.esp.search.http.DSURLUTF8Encoder";
    
    private static final Logger LOG = 
            Logger.getLogger(AbstractSimpleFastSearchCommand.class);


    // Attributes ----------------------------------------------------
    private final AdvancedFastConfiguration cfg;

    private Map<String,FastNavigator> navigatedTo = new HashMap<String,FastNavigator>();
    private Map<String,String[]> navigatedValues = new HashMap<String,String[]>();
    
    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of AbstractAdvancedFastSearchCommand */
    public AbstractAdvancedFastSearchCommand(
                    final Context cxt,
                    final Map parameters) {

        super(cxt, parameters);
          
        cfg = (AdvancedFastConfiguration) getSearchConfiguration();
    }

    // Public --------------------------------------------------------
    public SearchResult execute() {

        if (getNavigators() != null) {
            for (final Iterator iterator = getNavigators().keySet().iterator(); iterator.hasNext();) {

                final String navigatorKey = (String) iterator.next();

                if (getParameters().containsKey("nav_" + navigatorKey)) {
                    final String navigatedTo = getParameter("nav_" + navigatorKey);
                    addNavigatedTo(navigatorKey, navigatedTo);
                } else {
                    addNavigatedTo(navigatorKey, null);
                }
            }
        }

        final Properties props = new Properties();

        props.setProperty(FACTORY_PROPERTY, HTTP_FACTORY);
        props.setProperty(QR_SERVER_PROPERTY, cfg.getQueryServer());
        props.setProperty(ENCODER_PROPERTY, ENCODER_CLASS);

        try {

            final ISearchFactory factory = SearchFactory.newInstance(props);

            final String transformedQuery = getTransformedQuery();
            
            LOG.debug("Transformed query is " + transformedQuery);
            
            final IQuery query = new Query(transformedQuery);

            query.setParameter(new SearchParameter(
                    BaseParameter.OFFSET, getCurrentOffset(0)));
            query.setParameter(new SearchParameter(
                    BaseParameter.HITS, cfg.getResultsToReturn()));
            query.setParameter(new SearchParameter(
                    BaseParameter.SORT_BY, cfg.getSortBy()));

            final ISearchView view = factory.getSearchView(cfg.getView());

            final IQueryResult result = view.search(query);
            
            final FastSearchResult searchResult = new FastSearchResult(this);

            final int cnt = getCurrentOffset(0);
            final int maxIndex = getMaxDocIndex(result, cnt, cfg);
            
            searchResult.setHitCount(result.getDocCount());

            for (int i = cnt; i < maxIndex; i++) {
                try {
                    final IDocumentSummary document = result.getDocument(i + 1);
                    searchResult.addResult(createResultItem(document));
                } catch (NullPointerException e) { // THe doc count is not 100% accurate.
                    if (LOG.isDebugEnabled())
                        LOG.debug("Error finding document " + e);
                    return searchResult;
                }
            }

            if (getNavigators() != null) {
                collectModifiers(result, searchResult);
            }
            
            return searchResult;

        } catch (ConfigurationException ex) {
            LOG.error("exeute ", ex);
            throw new InfrastructureException(ex);
        } catch (SearchEngineException ex) {
            LOG.error("exeute ", ex);
            throw new InfrastructureException(ex);
        } catch (IOException ex) {
            LOG.error("exeute ", ex);
            throw new InfrastructureException(ex);
        }
    }

    public void addNavigatedTo(final String navigatorKey, final String navigatorName) {

        final FastNavigator navigator = (FastNavigator) getNavigators().get(navigatorKey);

        if (navigatorName == null) {
            navigatedTo.put(navigatorKey, navigator);
        } else {
            navigatedTo.put(navigatorKey, findChildNavigator(navigator, navigatorName));
        }
    }

    public FastNavigator getNavigatedTo(final String navigatorKey) {
        return (FastNavigator) navigatedTo.get(navigatorKey);
    }


    public FastNavigator getParentNavigator(final String navigatorKey) {
        if (getParameters().containsKey("nav_" + navigatorKey)) {
            final String navName =  getParameter("nav_" + navigatorKey);

            return findParentNavigator((FastNavigator) getNavigators().get(navigatorKey), navName);

        } else {
            return null;
        }
    }

    public FastNavigator getParentNavigator(final String navigatorKey, final String name) {
        if (getParameters().containsKey("nav_" + navigatorKey)) {

            return findParentNavigator((FastNavigator) getNavigators().get(navigatorKey), name);

        } else {
            return null;
        }
    }

    public FastNavigator findParentNavigator(final FastNavigator navigator, final String navigatorName) {
        if (navigator.getChildNavigator() == null) {
            return null;
        } else if (navigator.getChildNavigator().getName().equals(navigatorName)) {

            if (true) {
                return navigator;
            } else {
                return findParentNavigator(navigator.getChildNavigator(), navigatorName);

            }
        } else {
            return findParentNavigator(navigator.getChildNavigator(), navigatorName);
        }
    }

    public Map getNavigatedValues() {
        return navigatedValues;
    }

    public String getNavigatedValue(final String fieldName) {
        final String[] singleValue = (String[]) navigatedValues.get(fieldName);

        if (singleValue != null) {
            return (singleValue[0]);
        } else {
            return null;
        }
    }

    public boolean isTopLevelNavigator(final String navigatorKey) {
        return !getParameters().containsKey("nav_" + navigatorKey);
    }

    public Map getNavigatedTo() {
        return navigatedTo;
    }

    public String getNavigatorTitle(final String navigatorKey) {
        final FastNavigator nav = getNavigatedTo(navigatorKey);

        FastNavigator parent = findParentNavigator((FastNavigator) getNavigators().get(navigatorKey), nav.getName());

        String value = getNavigatedValue(nav.getField());

        if (value == null && parent != null) {

            value = getNavigatedValue(parent.getField());

            if (value == null) {

                parent = findParentNavigator((FastNavigator) getNavigators().get(navigatorKey), parent.getName());


                if (parent != null) {
                    value = getNavigatedValue(parent.getField());
                }
                return value;
            } else {
                return value;
            }
        }

        if (value == null) {
            return nav.getDisplayName();
        } else {
            return value;
        }

    }

    public String getNavigatorTitle(final FastNavigator navigator) {

        final String value = getNavigatedValue(navigator.getField());

        if (value == null) {
            return navigator.getDisplayName();
        } else {
            return value;
        }
    }

    public List getNavigatorBackLinks(final String navigatorKey) {

        final List backLinks = addNavigatorBackLinks(cfg.getNavigator(navigatorKey), new ArrayList(), navigatorKey);

        if (backLinks.size() > 0) {
            backLinks.remove(backLinks.size() - 1);
        }

        return backLinks;
    }

    public List addNavigatorBackLinks(final FastNavigator navigator, final List links, final String navigatorKey) {

        final String a = getParameter(navigator.getField());

        if (a != null) {

            LOG.debug(navigator.getName());
            LOG.debug(a);

            if (!(navigator.getName().equals("ywfylkesnavigator") && a.equals("Oslo"))) {
                if (!(navigator.getName().equals("ywkommunenavigator") && a.equals("Oslo"))) {
                    links.add(navigator);
                }
            }
        }

        if (navigator.getChildNavigator() != null) {
            final String n = getParameter("nav_" + navigatorKey);

            if (n != null && navigator.getName().equals(n)) {
                return links;
            }

            addNavigatorBackLinks(navigator.getChildNavigator(), links, navigatorKey);
        }

        return links;
    }
    public Map getOtherNavigators(final String navigatorKey) {

        final Map<String,String> otherNavigators = new HashMap<String,String>();

        for (String parameterName : (Set<String>)getParameters().keySet()) {

            if (parameterName.startsWith("nav_") && !parameterName.substring(parameterName.indexOf('_') + 1).equals(navigatorKey)) {
                final String paramValue = getParameter(parameterName);
                otherNavigators.put(parameterName.substring(parameterName.indexOf('_') + 1), paramValue);
            }
        }
        return otherNavigators;
    }

    private int getMaxDocIndex(
            final IQueryResult result, 
            final int cnt, 
            final AdvancedFastConfiguration cfg) 
    {
        return Math.min(cnt + cfg.getResultsToReturn(), result.getDocCount());
    }

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------
    // Generate query in FQL.
   
   /**
    *
    * @todo Work in progress.
    * quoting reserved words, operator precedence and more. 
    */ 
    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
            appendToQueryRepresentation(getTransformedTerm(clause));
        }
    }
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }
    protected void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" and ");
        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation(" (");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" or ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(") ");
    }
    protected void visitImpl(final DefaultOperatorClause clause) {

        
        
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" and ");
        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final NotClause clause) {
        appendToQueryRepresentation(" not ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");

    }
    protected void visitImpl(final AndNotClause clause) {
        appendToQueryRepresentation("andnot ");
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(")");
    }
    /**
     *
     * @param clause The clause to examine.
     */
    protected void visitImpl(final XorClause clause) {
        if (clause.getHint() == XorClause.PHRASE_ON_LEFT) {
            // Web searches should use phrases over separate words.
            clause.getFirstClause().accept(this);
        } else {
            // All other high level clauses are ignored.
            clause.getSecondClause().accept(this);
        }
    }

    protected Map<String,FastNavigator> getNavigators() {
        return cfg.getNavigators();
    }

    
    // Private -------------------------------------------------------

    private SearchResultItem createResultItem(final IDocumentSummary document) {

        final SearchResultItem item = new BasicSearchResultItem();

        for (final String field : cfg.getResultFields()) {

            final String split[] = field.split("AS");
            final String alias = split.length == 2 ? split[0].trim() : field;
            final IDocumentSummaryField summary = document.getSummaryField(field);

            if (summary != null && !summary.isEmpty()) 
                item.addField(alias, summary.getStringValue().trim());
        }

        return item;
    }

    private Collection createNavigationFilterStrings() {
        final Collection filterStrings = new ArrayList();

        for (final Iterator iterator = navigatedValues.keySet().iterator(); iterator.hasNext();) {
            final String field = (String) iterator.next();

            final String modifiers[] = (String[]) navigatedValues.get(field);


            for (int i = 0; i < modifiers.length; i++) {
                filterStrings.add("+" + field + ":\"" + modifiers[i] + "\"");
            }
        }

        return filterStrings;
    }

    private String getNavigatorsString() {

        if (getNavigators() != null) {


            Collection allFlattened = new ArrayList();


            for (FastNavigator navigator : getNavigators().values()) {

                allFlattened.addAll(flattenNavigators(new ArrayList(), navigator));
            }

            return StringUtils.join(allFlattened.iterator(), ',');
        } else {
            return "";
        }
    }

    private Collection flattenNavigators(Collection soFar, FastNavigator nav) {

        soFar.add(nav);

        if (nav.getChildNavigator() != null) {
            flattenNavigators(soFar, nav.getChildNavigator());
        }

        return soFar;
    }

    private void collectModifiers(IQueryResult result, FastSearchResult searchResult) {

        for (String navigatorKey : navigatedTo.keySet()) {

            collectModifier(navigatorKey, result, searchResult);
        }

    }

    private void collectModifier(String navigatorKey, IQueryResult result, FastSearchResult searchResult) {

        final FastNavigator nav = (FastNavigator) navigatedTo.get(navigatorKey);

        INavigator navigator = result.getNavigator(nav.getName());

        if (navigator != null) {

            Iterator modifers = navigator.modifiers();

            while (modifers.hasNext()) {
                IModifier modifier = (IModifier) modifers.next();
                Modifier mod = new Modifier(modifier.getName(), modifier.getCount(), nav);
                searchResult.addModifier(navigatorKey, mod);
            }

            if (searchResult.getModifiers(navigatorKey) != null) {
                Collections.sort(searchResult.getModifiers(navigatorKey));
            }

        } else if (nav.getChildNavigator() != null) {
            navigatedTo.put(navigatorKey, nav.getChildNavigator());
            collectModifier(navigatorKey, result, searchResult);
        }
    }

    private FastNavigator findChildNavigator(FastNavigator nav, String nameToFind) {

        if (getParameters().containsKey(nav.getField())) {
            
            navigatedValues.put(nav.getField(), getParameters().get(nav.getField()) instanceof String[]
                    ? (String[])getParameters().get(nav.getField())
                    : new String[]{getParameter(nav.getField())});
        }

        if (nav.getName().equals(nameToFind)) {
            if (nav.getChildNavigator() != null) {
                return nav.getChildNavigator();
            } else {
                return nav;
            }
        }

        if (nav.getChildNavigator() == null) {
            throw new RuntimeException("Navigator " + nameToFind + " not found.");
        }

        return findChildNavigator(nav.getChildNavigator(), nameToFind);
    }

    // Inner classes -------------------------------------------------
}

