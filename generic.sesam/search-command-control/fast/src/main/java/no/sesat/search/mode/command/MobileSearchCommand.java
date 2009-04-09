/* Copyright (2006-2008) Schibsted ASA
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
 *
 * MobileSearchCommand.java
 *
 * Created on March 10, 2006, 2:22 PM
 *
 */
package no.sesat.search.mode.command;

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
import no.sesat.search.mode.config.MobileCommandConfig;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

/**
 * A search command that uses FAST's msearch API.
 *
 *
 * @version $Id$
 */
public final class MobileSearchCommand extends AbstractSearchCommand {

    private static final Logger LOG = Logger.getLogger(MobileSearchCommand.class);

    private static final String PERSONALIZATION_GROUP = "aspiro-sesam1";
    private static final String USER_AGENT_PARAMETER="ua";
    private static final String MSEARCH_CLIENT_PROPS = "msearch-client.properties";
    private static final String ORIGINATION_PARAMETER = "originator";

    private final MobileCommandConfig cfg;

    public MobileSearchCommand(final Context cxt) {

        super(cxt);
        cfg = (MobileCommandConfig) cxt.getSearchConfiguration();
    }


    public ResultList<ResultItem> execute() {
        try {
            final IMSearchFactory factory = MSearchFactory.newInstance();
            final IMSearchEngine engine = factory.createSearchEngine();
            final ISearchParameters params = new SearchParameters();
            final IMSearchInfo searchInfo = MSearchInfoFactory.getMSearchInfo();

            if (!cfg.getSortBy().equals("")) {
                params.setParameter(new SearchParameter(BaseParameter.SORT_BY,
                        cfg.getSortBy()));
            }

            params.setParameter(new SearchParameter(
                    BaseParameter.QUERY, getTransformedQuery()));
            params.setParameter(new SearchParameter(
                    "offset", getOffset())); // BaseParameter.OFFSET not in Fast4

            if (getParameter("msite") != null)  {
                String filter = "+(";
                String [] arr = getParameter("msite").split(";");
                for (int i=0;i<arr.length;i++){
                    filter = filter + " domain:" + arr[i];
                }
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

            if ("telenor".equals(getParameter(ORIGINATION_PARAMETER))
              && !cfg.getTelenorPersonalizationGroup().equals("")){

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

            final ResultList<ResultItem> searchResult = new BasicResultList<ResultItem>();

            if( null != result ){

                final int cnt = getOffset();
                final int maxIndex = Math.min(cnt + cfg.getResultsToReturn(), result.getDocCount());

                searchResult.setHitCount(result.getDocCount());

                for (int i = cnt; i < maxIndex; i++) {
                    //catch nullpointerException because of unaccurate doccount
                    try {
                        final IDocumentSummary document = result.getDocument(i + 1);
                        final ResultItem item = createResultItem(document);
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
            LOG.error(ex.getMessage());
            return new BasicResultList<ResultItem>();
        } catch (IOException ex) {
            throw new SearchCommandException(ex);
        } catch (FastException ex) {
            throw new SearchCommandException(ex);
        }
    }

    private IDeviceCapabilities getDeviceCapabilities() {

        IDeviceCapabilities cap = null;

        if (null != getParameter(USER_AGENT_PARAMETER)) {
            final String userAgent = (getParameter(USER_AGENT_PARAMETER));
            cap = DeviceCapabilitiesFactory.getDeviceCapabilities();
            cap.setUserAgent(userAgent);
        }
        return cap;
    }

    private ResultItem createResultItem(final IDocumentSummary document) {

        ResultItem item = new BasicResultItem();

        for (final Map.Entry<String,String> entry : cfg.getResultFieldMap().entrySet()) {

                final IDocumentSummaryField summary = document.getSummaryField(entry.getKey());

                if (summary != null) {
                    item = item.addField(entry.getValue(), summary.getSummary());
                }
        }

        return item;
    }

    private StringBuilder filterBuilder = null;

    @Override
    protected String getFilter() {

        synchronized (this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuilder(super.getFilter());
            }
        }
        return filterBuilder.toString();
    }


}
