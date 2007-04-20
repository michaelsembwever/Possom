// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.query.transform.QueryTransformerConfig;
import no.schibstedsok.searchportal.result.handler.ResultHandlerConfig;
import no.schibstedsok.searchportal.util.SearchConstants;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * A common base class for search configurations.
 * TODO rename to BaseSearchConfiguration since it is directly used by default commands in modes.xml
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class AbstractSearchConfiguration implements SearchConfiguration {

    private static final Logger LOG = Logger.getLogger(AbstractSearchConfiguration.class);

    private static final String ERR_FAILED_QUERYTRANSFORMERS_COPY = "Failed to defensively clone QueryTransformers";

    private String name;
    private final List<QueryTransformerConfig> queryTransformers = new ArrayList<QueryTransformerConfig>();
    private final List<ResultHandlerConfig> resultHandlers = new ArrayList<ResultHandlerConfig>();
    private int pageSize = SearchConstants.DEFAULT_DOCUMENTS_TO_RETURN;
    private final Map<String,String> resultFields = new HashMap<String,String>();
    private int resultsToReturn;
    private boolean paging = false;
    private boolean child = false;
    private String rule;
    private int ruleThreshold = -1;
    private String queryParameter;
    private boolean alwaysRun = false;

    private String statisticalName;

    /** TODO comment me. *
     * @param sc 
     */
    public AbstractSearchConfiguration(final SearchConfiguration sc){
        if(sc != null && sc instanceof AbstractSearchConfiguration){
            final AbstractSearchConfiguration asc = (AbstractSearchConfiguration) sc;
            name = asc.name;
            queryTransformers.addAll(asc.queryTransformers);
            resultHandlers.addAll(asc.resultHandlers);
            pageSize = asc.pageSize;
            resultFields.putAll(asc.resultFields);
            fieldFilters.putAll(asc.fieldFilters);
            resultsToReturn = asc.resultsToReturn;
            paging = asc.paging;
            child = asc.child;
            queryParameter = asc.queryParameter;
            alwaysRun = asc.alwaysRun;
        }
    }

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

    /** TODO comment me. **/
    public final void setName(final String name) {
        this.name = name;
    }

    /** TODO comment me. **/
    public final int getPageSize() {
        return pageSize;
    }

    /** TODO comment me. **/
    public final void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

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

    /** {@inheritDoc} **/
    public boolean isChild() {
        return child;
    }

    /** {@inheritDoc} **/
    public String getQueryParameter() {
        return queryParameter;
    }

    /** {@inheritDoc} **/
    public boolean isAlwaysRun() {
        return alwaysRun;
    }

    /** TODO comment me. **/
    public void setAlwaysRun(final boolean enable){
        alwaysRun = enable;
    }

    /** TODO comment me. **/
    public void setQueryParameter(final String useParameterAsQuery) {
        this.queryParameter = useParameterAsQuery;
    }

    /** {@inheritDoc} **/
    public String getStatisticalName() {
        return statisticalName;
    }

    /** TODO comment me. **/
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

    /** TODO comment me. **/
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
}
