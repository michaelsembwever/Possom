// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.query.transform.QueryTransformer;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;
import no.schibstedsok.front.searchportal.util.SearchConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A common base class for search configurations.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public abstract class AbstractSearchConfiguration implements SearchConfiguration {

    Log log = LogFactory.getLog(AbstractSearchConfiguration.class);

    private String name;
    private List queryTransformers = new ArrayList();
    private List resultHandlers = new ArrayList();
    private int pageSize = SearchConstants.DEFAULT_DOCUMENTS_TO_RETURN;
    private Collection resultFields = new ArrayList();
    private int resultsToReturn;
    private boolean isPagingEnabled = false;
    private boolean child = false;
    private String rule;
    private int ruleThreshold = -1;
    private String useParameterAsQuery;
    private boolean isAlwaysRunEnabled = false;

    private String statisticsName;


    /**
     * Sets the paging enabled status of this configuration. The default is
     * false.
     *
     * @param pagingEnabled
     */
    public final void setPagingEnabled(final boolean pagingEnabled) {
        if (log.isDebugEnabled()) {
            log.debug("setPagingEnabled() " + pagingEnabled);
        }
        this.isPagingEnabled = pagingEnabled;
    }

    /**
     * Returns a list of {@link QueryTransformer} that should be applied
     * to the query before it is sent to the search command.
     *
     * @return queryTransfomer
     */
    public final List getQueryTransformers() {
        return queryTransformers;
    }

    public final void addQueryTransformer(final QueryTransformer queryTransformer) {
        queryTransformers.add(queryTransformer);
    }

    public final List getResultHandlers() {
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

    public final void addResultField(final String fieldName) {
        resultFields.add(fieldName);
    }

    public final Collection getResultFields() {
        return resultFields;
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

    public String getRule() {
        return rule;
    }

    public int getRuleThreshold() {
        return ruleThreshold;
    }

    public String getUseParameterAsQuery() {
        return useParameterAsQuery;
    }

    public boolean isAlwaysRunEnabled() {
        return isAlwaysRunEnabled;
    }

    public void setUseParameterAsQuery(final String useParameterAsQuery) {
        this.useParameterAsQuery = useParameterAsQuery;
    }

    public String getStatisticsName() {
        return statisticsName;
    }
}
