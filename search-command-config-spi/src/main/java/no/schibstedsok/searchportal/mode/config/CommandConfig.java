// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.query.transform.QueryTransformerConfig;
import no.schibstedsok.searchportal.result.handler.ResultHandlerConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import no.schibstedsok.searchportal.site.config.SiteClassLoaderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A common base class for search configurations.
 * TODO rename to BaseSearchConfiguration since it is directly used by default commands in modes.xml
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class CommandConfig implements SearchConfiguration {
    

    /**
     * 
     */
    protected static final int DEFAULT_DOCUMENTS_TO_RETURN = 10;    

    private static final Logger LOG = Logger.getLogger(CommandConfig.class);

    private static final String ERR_ONLY_ONE_CHILD_NAVIGATOR_ALLOWED
            = "Each FastNavigator is only allowed to have one child. Parent was ";
    
    private static final String ERR_FAILED_QUERYTRANSFORMERS_COPY = "Failed to defensively clone QueryTransformers";
    private static final String INFO_PARSING_NAVIGATOR = "  Parsing navigator ";

    private String name;
    private final List<QueryTransformerConfig> queryTransformers = new ArrayList<QueryTransformerConfig>();
    private final List<ResultHandlerConfig> resultHandlers = new ArrayList<ResultHandlerConfig>();
//    private int pageSize = DEFAULT_DOCUMENTS_TO_RETURN;
    private final Map<String,String> resultFields = new HashMap<String,String>();
    private int resultsToReturn;
    private boolean paging = false;
//    private boolean child = false;
    private String rule;
    private int ruleThreshold = -1;
    private String queryParameter;
    private boolean alwaysRun = false;

    private String statisticalName;

    /** TODO comment me. *
     * @param sc 
     */
    public CommandConfig(){}

    /**
     * Sets the paging enabled status of this configuration. The default is
     * false.
     *
     * @param pagingEnabled
     */
    public final void setPaging(final boolean pagingEnabled) {

        LOG.trace("setPagingEnabled() " + pagingEnabled);
        this.paging = pagingEnabled;
    }

    /**
     * Returns a (defensive copy) list of {@link QueryTransformer} that should be applied
     * to the query before it is sent to the search command.
     * The list is also unmodifiable.
     *
     * @return queryTransfomer
     */
    public final List<QueryTransformerConfig> getQueryTransformers() {

        return Collections.unmodifiableList(queryTransformers);
    }

    /** {@inheritDoc} **/
    public final void addQueryTransformer(final QueryTransformerConfig queryTransformer) {
        if(queryTransformer != null){
            queryTransformers.add(queryTransformer);
        }
    }

    /** {@inheritDoc} **/
    public final List<ResultHandlerConfig> getResultHandlers() {
        return resultHandlers;
    }

    /** {@inheritDoc} **/
    public final void addResultHandler(final ResultHandlerConfig handler) {
        resultHandlers.add(handler);
    }

    /** {@inheritDoc} **/
    public final String getName() {
        return name;
    }

    /** TODO comment me. *
     * @param name 
     */
    public final void setName(final String name) {
        this.name = name;
    }

//    /** TODO comment me. *
//     * @return 
//     */
//    public final int getPageSize() {
//        return pageSize;
//    }

//    /** TODO comment me. *
//     * @param pageSize 
//     */
//    public final void setPageSize(final int pageSize) {
//        this.pageSize = pageSize;
//    }

    /** {@inheritDoc} **/
    public final boolean isPaging() {
        return paging;
    }

    /** {@inheritDoc} **/
    public final void addResultField(final String... fieldName) {
        resultFields.put(fieldName[0].trim(), (fieldName.length >1 ? fieldName[1] : fieldName[0]).trim());
    }

    /** {@inheritDoc} **/
    public final Map<String,String> getResultFields() {
        return Collections.unmodifiableMap(resultFields);
    }

    /** {@inheritDoc} **/
    public final int getResultsToReturn() {
        return resultsToReturn;
    }

    /** {@inheritDoc} **/
    public final void setResultsToReturn(final int no) {
        this.resultsToReturn = no;
    }

//    /** {@inheritDoc} **/
//    public boolean isChild() {
//        return child;
//    }

    /** {@inheritDoc} **/
    public String getQueryParameter() {
        return queryParameter;
    }

    /** {@inheritDoc} *
     * @return 
     */
    public boolean isAlwaysRun() {
        return alwaysRun;
    }

    /** TODO comment me. *
     * @param enable 
     */
    public void setAlwaysRun(final boolean enable){
        alwaysRun = enable;
    }

    /** TODO comment me. *
     * @param useParameterAsQuery 
     */
    public void setQueryParameter(final String useParameterAsQuery) {
        this.queryParameter = useParameterAsQuery;
    }

    /** {@inheritDoc} **/
    public String getStatisticalName() {
        return statisticalName;
    }

    /** TODO comment me. *
     * @param name 
     */
    public void setStatisticalName(final String name){
        statisticalName = name;
    }

    /** {@inheritDoc} **/
    public String toString(){
        return getClass().getSimpleName() + " [" + name + "]";
    }

    /**
     * Holds value of property fieldFilters.
     */
    private final Map<String,String> fieldFilters = new HashMap<String,String>();

    /** TODO comment me. *
     * @param field 
     */
    public void addFieldFilter(final String field, final String filter){
        fieldFilters.put(field, filter);
    }

    /**
     * Getter for property fieldFilters.
     * @return Value of property fieldFilters.
     */
    public Map<String,String> getFieldFilters() {
        return Collections.unmodifiableMap(fieldFilters);
    }

    /** {@inheritDoc} **/
    public void clearQueryTransformers() {
        queryTransformers.clear();
    }

    /** {@inheritDoc} **/
    public void clearResultHandlers() {
        resultHandlers.clear();
    }
    
    /** {@inheritDoc} **/
    public void clearFieldFilters() {
        fieldFilters.clear();
    }
    
    /** {@inherit}
     */
    public CommandConfig readSearchConfiguration(
            final Element element, 
            final SearchConfiguration inherit){
        
        setName(element.getAttribute("id"));
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "alwaysRun", ParseType.Boolean, element, "false");

        // field-filters
        if(null!=inherit){
            fieldFilters.putAll(inherit.getFieldFilters());
        }
        if (element.hasAttribute("field-filters")) {
            if (element.getAttribute("field-filters").length() > 0) {
                final String[] fieldFilters = element.getAttribute("field-filters").split(",");
                for (String fieldFilter : fieldFilters) {
                    if (fieldFilter.contains(" AS ")) {
                        final String[] ff = fieldFilter.split(" AS ");
                        addFieldFilter(ff[0].trim(), ff[1].trim());
                    } else {
                        addFieldFilter(fieldFilter, fieldFilter);
                    }
                }
            } else {
                // If attribute is present and empty, clear the field filters. This creates an option
                // for child commands to not inherit field filters.
                clearFieldFilters();
            }
        }

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "paging", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "queryParameter", ParseType.String, element, "");

        // result-fields
        if(null!=inherit){
            resultFields.putAll(inherit.getResultFields());
        }
        if (element.getAttribute("result-fields").length() > 0) {
            final String[] resultFields = element.getAttribute("result-fields").split(",");
            for (String resultField : resultFields) {
                addResultField(resultField.trim().split(" AS "));
            }
        }
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "resultsToReturn", ParseType.Int, element, "-1");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "statisticalName", ParseType.String, element, "");

        return this;
    }
    
    /** Currently only used by the fast subclasses but hopefully open to all one day. **/
    protected final Collection<Navigator> parseNavigators(final Element navsE) {

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

    /**
     *
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Inherited
    public @interface Controller {
        /**
         *
         * @return
         */
        public String value();
    }     
}
