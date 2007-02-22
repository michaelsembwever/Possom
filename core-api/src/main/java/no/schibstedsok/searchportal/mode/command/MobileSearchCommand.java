// Copyright (2006) Schibsted Søk AS
/*
 * MobileSearchCommand.java
 *
 * Created on March 10, 2006, 2:22 PM
 *
 */
package no.schibstedsok.searchportal.mode.command;

import no.fast.ds.common.FastException;
import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.IDocumentSummary;
import no.fast.ds.search.IDocumentSummaryField;
import no.fast.ds.search.IQuery;
import no.fast.ds.search.IQueryResult;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.Query;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchParameters;

import no.fast.msearch.Exception.ConfigurationException;
import no.fast.msearch.search.DeviceCapabilitiesFactory;
import no.fast.msearch.search.IDeviceCapabilities;
import no.fast.msearch.search.IMSearchEngine;
import no.fast.msearch.search.IMSearchFactory;
import no.fast.msearch.search.IMSearchInfo;
import no.fast.msearch.search.IMSearchResult;
import no.fast.msearch.search.MSearchFactory;
import no.fast.msearch.search.MSearchInfoFactory;
import no.fast.personalization.api.ExplicitUserGroupPersonalizationFactory;
import no.fast.personalization.api.IPersonalizationSpecification;

import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.MobileSearchConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * A search command that uses FAST's msearch API.
 *
 * @author magnuse
 */
public final class MobileSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(MobileSearchCommand.class);

    private static final String PERSONALIZATION_GROUP = "aspiro-sesam1";
    private static final String USER_AGENT_PARAMETER="ua";
    private static final String MSEARCH_CLIENT_PROPS = "msearch-client.properties";
    private static final String ORIGINATION_PARAMETER = "originator";

    private final MobileSearchConfiguration cfg;

    public MobileSearchCommand(final SearchCommand.Context cxt, final Map parameters) {
        super(cxt, parameters);
        cfg = (MobileSearchConfiguration) cxt.getSearchConfiguration();
    }


    public SearchResult execute() {
        try {
            final IMSearchFactory factory = MSearchFactory.newInstance();
            final IMSearchEngine engine = factory.createSearchEngine();
            final ISearchParameters params = new SearchParameters();
            final IMSearchInfo searchInfo = MSearchInfoFactory.getMSearchInfo();
            final List sources = new ArrayList();

            if (!cfg.getSortBy().equals("")) {
                params.setParameter(new SearchParameter(BaseParameter.SORT_BY,
                        cfg.getSortBy()));
            }

            params.setParameter(new SearchParameter(
                    BaseParameter.QUERY, getTransformedQuery()));
            params.setParameter(new SearchParameter(
                    "offset", getCurrentOffset(0)));

            if (getParameter("msite") != null)  {
                String filter = "+(";
                String [] arr = getParameter("msite").split(";");
                for (int i=0;i<arr.length;i++) 
                    filter = filter + " domain:" + arr[i];
                
                filter = filter + ")";
                params.setParameter(new SearchParameter(
                        BaseParameter.FILTER, filter ));
            } else {            
                if (!cfg.getFilter().equals("")) {
                    params.setParameter(new SearchParameter(
                            BaseParameter.FILTER, cfg.getFilter()));
                }
            }

            final IDeviceCapabilities cap = getDeviceCapabilities();

            String personalizationGroup = cfg.getPersonalizationGroup();

            if (getParameter(ORIGINATION_PARAMETER).equals("telenor")
              && !cfg.getTelenorPersonalizationGroup().equals(""))
            {
                personalizationGroup = cfg.getTelenorPersonalizationGroup();
            }

            final IPersonalizationSpecification ps =
                    ExplicitUserGroupPersonalizationFactory.getUserGroupSpecification(personalizationGroup);


            final IQuery query = new Query(params);

            if (LOG.isDebugEnabled()) {
                LOG.debug("mSearch query is " + query);
            }

            final List<IMSearchResult> results = cap != null ? engine.search(query, ps, cap) : engine.search(query, ps);

            IMSearchResult mResult = null;

            for (final IMSearchResult r : results) {
                if (r.getSource().equals(cfg.getSource())) {
                    mResult = r;
                    break;
                }
            }

            final IQueryResult result = mResult.getResult();

            final SearchResult searchResult = new BasicSearchResult(this);
            
            if( null != result ){
                
                final int cnt = getCurrentOffset(0);
                final int maxIndex = Math.min(cnt + cfg.getResultsToReturn(), result.getDocCount());

                searchResult.setHitCount(result.getDocCount());

                for (int i = cnt; i < maxIndex; i++) {
                    //catch nullpointerException because of unaccurate doccount
                    try {
                        final IDocumentSummary document = result.getDocument(i + 1);
                        final SearchResultItem item = createResultItem(document);
                        searchResult.addResult(item);
                    } catch (NullPointerException e) {
                        return searchResult;
                    }
                }
            }else{
                LOG.error("IMSearchResult.getResult() returned null");
            }

            return searchResult;
            
        } catch (ConfigurationException ex) {
            throw new InfrastructureException(ex);
        } catch (IOException ex) {
            throw new InfrastructureException(ex);
        } catch (FastException ex) {
            throw new InfrastructureException(ex);
        }
    }

    private IDeviceCapabilities getDeviceCapabilities() {

        IDeviceCapabilities cap = null;

        if (getParameters().containsKey(USER_AGENT_PARAMETER)) {
            final String userAgent = (getParameter(USER_AGENT_PARAMETER));
            cap = DeviceCapabilitiesFactory.getDeviceCapabilities();
            cap.setUserAgent(userAgent);
        }
        return cap;
    }

    private SearchResultItem createResultItem(final IDocumentSummary document) {
        final SearchResultItem item = new BasicSearchResultItem();

        for (final Map.Entry<String,String> entry : cfg.getResultFields().entrySet()) {

                final IDocumentSummaryField summary = document.getSummaryField(entry.getKey());

                if (summary != null) {
                    item.addField(entry.getValue(), summary.getSummary());
                }
        }

        return item;
    }

    private StringBuilder filterBuilder = null;
    protected String getAdditionalFilter() {
        System.out.println("FILTER");
        synchronized (this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuilder(super.getAdditionalFilter());
            }
        }
        return filterBuilder.toString();
    }

    
}
