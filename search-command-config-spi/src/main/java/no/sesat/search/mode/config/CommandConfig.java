/* Copyright (2006-2008) Schibsted Søk AS
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
package no.sesat.search.mode.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import no.sesat.search.query.transform.QueryTransformerConfig;
import no.sesat.search.result.handler.ResultHandlerConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.querybuilder.InfixQueryBuilderConfig;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.transform.DefaultInitialisationQueryTransformerConfig;
import no.sesat.search.result.Navigator;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A common base class for search configurations.
 * TODO rename to BaseSearchConfiguration since it is directly used by default commands in modes.xml
 *
 *
 * @version <tt>$Id$</tt>
 */
public class CommandConfig implements BaseSearchConfiguration, SearchConfiguration.ModesW3cDomDeserialiser {

    // Constants -----------------------------------------------------

    /**
     *
     */
    protected static final int DEFAULT_DOCUMENTS_TO_RETURN = 10;

    private static final Logger LOG = Logger.getLogger(CommandConfig.class);

    private static final String ERR_ONLY_ONE_CHILD_NAVIGATOR_ALLOWED
            = "Each FastNavigator is only allowed to have one child. Parent was ";

    private static final String ERR_FAILED_QUERYTRANSFORMERS_COPY = "Failed to defensively clone QueryTransformers";
    private static final String INFO_PARSING_NAVIGATOR = "  Parsing navigator ";

    // Attributes ----------------------------------------------------

    private String name;
    private QueryBuilderConfig queryBuilder = new InfixQueryBuilderConfig();
    private QueryTransformerConfig initialQueryTransformer = new DefaultInitialisationQueryTransformerConfig();
    private final List<QueryTransformerConfig> queryTransformers = new ArrayList<QueryTransformerConfig>();
    private final List<ResultHandlerConfig> resultHandlers = new ArrayList<ResultHandlerConfig>();

    private final Map<String,String> resultFields = new TreeMap<String,String>();
    private int resultsToReturn = -1;

    private String queryParameter = "";
    private boolean alwaysRun = true;
    private boolean runBlank = false;
    private boolean asynchronous = false;
    private String statisticalName = "";

    /**
     * Holds value of property fieldFilters.
     */
    private final Map<String,String> fieldFilters = new HashMap<String,String>();

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     */
    public CommandConfig(){}

    // Public --------------------------------------------------------

    /**
     * Returns a (defensive copy) list of {@link QueryTransformerConfig} that should be applied
     * to the query before it is sent to the search command.
     * The list is also unmodifiable.
     *
     * @return queryTransfomer
     */
    public final List<QueryTransformerConfig> getQueryTransformers() {

        return Collections.unmodifiableList(queryTransformers);
    }

    public final void addQueryTransformer(final QueryTransformerConfig queryTransformer) {
        if(queryTransformer != null){
            queryTransformers.add(queryTransformer);
        }
    }

    public final List<ResultHandlerConfig> getResultHandlers() {
        return resultHandlers;
    }

    public final void addResultHandler(final ResultHandlerConfig handler) {
        resultHandlers.add(handler);
    }

    public final String getName() {
        return name;
    }

    public final String getId() {
        return name;
    }

    /**
     *
     * @param id Name for this configuration.
     */
    public final void setId(final String id) {
        this.name = id;
    }

    public final void addResultField(final String... fieldName) {
        resultFields.put(fieldName[0].trim(), (fieldName.length >1 ? fieldName[1] : fieldName[0]).trim());
    }

    /**
     * @param resultFields
     *            Result fields to add.
     */
    public final void addResultFields(final String[] resultFieldArray) {
        for (String resultField : resultFieldArray) {
            if (resultField != null && !resultField.equals("")) {
                String [] split = resultField.trim().split(" AS ");
                resultFields.put(split[0].trim(), (split.length >1 ? split[1] : split[0]).trim());
            }
        }
    }

    public final String[] getResultFields() {
        String [] res = new String[resultFields.size()];
        int index = 0;
        for (String key : resultFields.keySet()) {
            String value = resultFields.get(key);
            res[index] = (key.equals(value)) ? key :  key + " AS " + value;
            index ++;
        }
        return res;
    }

    public final Map<String,String> getResultFieldMap() {
        return Collections.unmodifiableMap(resultFields);
    }

    public final int getResultsToReturn() {
        return resultsToReturn;
    }

    public final void setResultsToReturn(final int no) {
        this.resultsToReturn = no;
    }

    public String getQueryParameter() {
        return queryParameter;
    }


    public boolean isAlwaysRun() {
        return alwaysRun;
    }

    /**
     * @param enable
     */
    public void setAlwaysRun(final boolean enable){
        alwaysRun = enable;
    }


    public boolean isRunBlank() {
        return runBlank;
    }

    /**
     * @param enable
     */
    public void setRunBlank(final boolean enable){
        runBlank = enable;
    }

    /**
     * @param useParameterAsQuery
     */
    public void setQueryParameter(final String useParameterAsQuery) {
        this.queryParameter = useParameterAsQuery;
    }

    public String getStatisticalName() {
        return statisticalName;
    }

    /**
     * @param name
     */
    public void setStatisticalName(final String name){
        statisticalName = name;
    }


    public boolean isAsynchronous() {
        return asynchronous;
    }

    /**
     * @param asynchronous
     */
    public void setAsynchronous(final boolean asynchronous){
        this.asynchronous = asynchronous;
    }

    /**
     * @param fieldAndFilter
     *            String containing name of field and filter seperated with ' AS '.
     */
    private void setFieldFilter(final String fieldAndFilter) {
        String parsed[] = fieldAndFilter.trim().split(" AS ");
        String field = parsed[0].trim();
        fieldFilters.put(field, (parsed.length > 1) ? parsed[1].trim() : field);
    }

    /**
     * Syntax: field-filters="size, nyhetskilde AS newssource"
     *
     * Just "size" will be the same as writing "size AS size"
     *
     * @param fieldFilters Array of field filters.
     */
    public void setFieldFilters(final String[] fieldFilters) {
        for (String string : fieldFilters) {
            setFieldFilter(string);
        }
    }

    /**
     * Getter for property fieldFilters.
     * @return Value of property fieldFilters.
     */
    public Map<String,String> getFieldFilterMap() {
        return Collections.unmodifiableMap(fieldFilters);
    }

    public void clearQueryTransformers() {
        queryTransformers.clear();
    }

    public void clearResultHandlers() {
        resultHandlers.clear();
    }

    public void clearFieldFilters() {
        fieldFilters.clear();
    }

    public QueryBuilderConfig getQueryBuilder() {
        return queryBuilder;
    }

    public void setQueryBuilder(final QueryBuilderConfig queryBuilderConfig) {
        this.queryBuilder = queryBuilderConfig;
    }

    public QueryTransformerConfig getInitialQueryTransformer() {
        return initialQueryTransformer;
    }

    public void setInitialQueryTransformer(final QueryTransformerConfig initialQueryTransformer) {
        this.initialQueryTransformer = initialQueryTransformer;
    }

    // Protected --------------------------------------------------------


    public SearchConfiguration readSearchConfiguration(final Element element, final SearchConfiguration inherit, Context context) {

        if(null!=inherit){
            fieldFilters.putAll(inherit.getFieldFilterMap());
        }
        if(null!=inherit){
            resultFields.putAll(inherit.getResultFieldMap());
        }

        ModesSearchConfigurationDeserializer.readSearchConfiguration(this, element, inherit);

        if (element.hasAttribute("field-filters")) {
            if (element.getAttribute("field-filters").length() == 0) {
               clearFieldFilters();
            }
        }

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

                    final boolean boundaryMatch = navE.getAttribute("boundary-match").equals("true");

                    final Navigator nav = new Navigator(
                            name,
                            navE.getAttribute("field"),
                            navE.getAttribute("display-name"),
                            sort,
                            boundaryMatch);
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

    @Override
    public String toString() {
        try {
            return this.getClass().getSimpleName() + " " + BeanUtils.describe(this).toString();
        } catch (Exception e) {
            LOG.warn("Failed to do BeanUtils.describe", e);
        }
        return this.getClass().getSimpleName();
    }

    // Inner classes -------------------------------------------------


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
