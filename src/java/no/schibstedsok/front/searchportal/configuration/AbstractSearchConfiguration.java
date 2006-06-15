// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.front.searchportal.query.transform.QueryTransformer;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;
import no.schibstedsok.front.searchportal.util.SearchConstants;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * A common base class for search configurations.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class AbstractSearchConfiguration implements SearchConfiguration {

    private static final Logger LOG = Logger.getLogger(AbstractSearchConfiguration.class);

    private static final String ERR_FAILED_QUERYTRANSFORMERS_COPY = "Failed to defensively clone QueryTransformers";

    private String name;
    private final List<QueryTransformer> queryTransformers = new ArrayList<QueryTransformer>();
    private final List<ResultHandler> resultHandlers = new ArrayList<ResultHandler>();
    private int pageSize = SearchConstants.DEFAULT_DOCUMENTS_TO_RETURN;
    private final Map<String,String> resultFields = new HashMap<String,String>();
    private int resultsToReturn;
    private boolean isPagingEnabled = false;
    private boolean child = false;
    private String rule;
    private int ruleThreshold = -1;
    private String useParameterAsQuery;
    private boolean isAlwaysRunEnabled = false;

    private String statisticsName;

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
            isPagingEnabled = asc.isPagingEnabled;
            child = asc.child;
            useParameterAsQuery = asc.useParameterAsQuery;
            isAlwaysRunEnabled = asc.isAlwaysRunEnabled;
        }
    }

    /**
     * Sets the paging enabled status of this configuration. The default is
     * false.
     *
     * @param pagingEnabled
     */
    public final void setPagingEnabled(final boolean pagingEnabled) {

        LOG.trace("setPagingEnabled() " + pagingEnabled);
        this.isPagingEnabled = pagingEnabled;
    }

    /**
     * Returns a (defensive copy) list of {@link QueryTransformer} that should be applied
     * to the query before it is sent to the search command.
     * The list is also unmodifiable.
     *
     * @return queryTransfomer
     */
    public final List<QueryTransformer> getQueryTransformers() {

        final List<QueryTransformer> copy = new ArrayList<QueryTransformer>();
        if(queryTransformers != null){
            try {
                for(QueryTransformer qt : queryTransformers){
                        copy.add((QueryTransformer)qt.clone());
                }
            } catch (CloneNotSupportedException ex) {
                LOG.error(ERR_FAILED_QUERYTRANSFORMERS_COPY, ex);
            }
        }
        return Collections.unmodifiableList(copy);
    }

    public final void addQueryTransformer(final QueryTransformer queryTransformer) {
        if(queryTransformer != null){
            queryTransformers.add(queryTransformer);
        }
    }

    public final List<ResultHandler> getResultHandlers() {
        return resultHandlers;
    }

    public final void addResultHandler(final ResultHandler handler) {
        resultHandlers.add(handler);
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final int getPageSize() {
        return pageSize;
    }

    public final void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public final boolean isPagingEnabled() {
        return isPagingEnabled;
    }

    public final void addResultField(final String... fieldName) {
        resultFields.put(fieldName[0].trim(), (fieldName.length >1 ? fieldName[1] : fieldName[0]).trim());
    }

    public final Map<String,String> getResultFields() {
        return Collections.unmodifiableMap(resultFields);
    }

    public final int getResultsToReturn() {
        return resultsToReturn;
    }

    public final void setResultsToReturn(final int no) {
        this.resultsToReturn = no;
    }

    public boolean isChild() {
        return child;
    }

    public String getUseParameterAsQuery() {
        return useParameterAsQuery;
    }

    public boolean isAlwaysRunEnabled() {
        return isAlwaysRunEnabled;
    }

    public void setAlwaysRunEnabled(final boolean enable){
        isAlwaysRunEnabled = enable;
    }

    public void setUseParameterAsQuery(final String useParameterAsQuery) {
        this.useParameterAsQuery = useParameterAsQuery;
    }

    public String getStatisticsName() {
        return statisticsName;
    }

    public void setStatisticsName(final String name){
        statisticsName = name;
    }

    public String toString(){
        return getClass().getSimpleName() + " [" + name + "]";
    }

    /**
     * Holds value of property fieldFilters.
     */
    private final Map<String,String> fieldFilters = new HashMap<String,String>();

    void addFieldFilter(final String field, final String filter){
        fieldFilters.put(field, field);
    }

    /**
     * Getter for property fieldFilters.
     * @return Value of property fieldFilters.
     */
    public Map<String,String> getFieldFilters() {
        return Collections.unmodifiableMap(fieldFilters);
    }
}
