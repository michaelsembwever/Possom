/*
 * Copyright (2009) Schibsted ASA
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
 */
package no.sesat.search.run;


import no.sesat.commons.ioc.BaseContext;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.page.PageDataObject;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.view.SearchTabFactory;
import no.sesat.search.view.config.SearchTab;
import org.apache.log4j.Logger;


/**
 * Utility methods around a RunningQuery.
 *
 * @version <tt>$Id$</tt>
 */
public final class RunningQueryUtility {

   // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(RunningQueryUtility.class);
    private static final String ERR_MISSING_TAB = "No existing implementation for tab ";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    public static SearchTab findSearchTabByKey(
            final DataModel datamodel,
            final String cParameter,
            final DataModelFactory dmFactory,
            final BaseContext genericCxt){

        // determine the c parameter.
        //  default comes from SiteConfiguration unless there exists a page parameter when it becomes 'i'.
        final ParametersDataObject parametersDO = datamodel.getParameters();
        final StringDataObject page = parametersDO.getValue("page");

        final String searchTabKey = null != cParameter && 0 < cParameter.length()
                ? cParameter
                : null != page && null != page.getString() && 0 < page.getString().length() ? "i" : null;

        return findSearchTab(datamodel, searchTabKey, dmFactory, genericCxt, true);
    }

    public static SearchTab findSearchTabById(
            final DataModel datamodel,
            final String tabId,
            final DataModelFactory dmFactory,
            final BaseContext genericCxt){

        return findSearchTab(datamodel, tabId, dmFactory, genericCxt, false);
    }

    // Constructors --------------------------------------------------

    private RunningQueryUtility(){}

    // Public --------------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private static SearchTab findSearchTab(
            final DataModel datamodel,
            final String searchTab,
            final DataModelFactory dmFactory,
            final BaseContext genericCxt,
            final boolean useKey){

        LOG.info("searchTab: " + searchTab);

        SearchTab result = null;
        if(null != searchTab){
            try{
                final SearchTabFactory stFactory = SearchTabFactory.instanceOf(
                    ContextWrapper.wrap(
                        SearchTabFactory.Context.class,
                        genericCxt));

                result = useKey
                        ? stFactory.getTabByKey(searchTab)
                        : stFactory.getTabByName(searchTab);

                if(null == datamodel.getPage()){

                    final PageDataObject pageDO = dmFactory.instantiate(
                            PageDataObject.class,
                            datamodel,
                            new DataObject.Property("tabs", stFactory.getTabsByName()),
                            new DataObject.Property("currentTab", result));

                    datamodel.setPage(pageDO);
                }else{
                    datamodel.getPage().setCurrentTab(result);
                }

            }catch(AssertionError ae){
                // it's not normal to catch assert errors but we really want a 404 not 500 response error.
                LOG.error("Caught Assertion: " + ae);
            }
        }
        if(null == result){
            LOG.error(ERR_MISSING_TAB + searchTab);

            final String defaultSearchTab
                    = datamodel.getSite().getSiteConfiguration().getProperty(SiteConfiguration.DEFAULTTAB_KEY);

            // first going to fallback to defaultSearchTab in preference to the pending 404 response error.
            if(useKey || !defaultSearchTab.equals(searchTab)){
                result = findSearchTab(datamodel, defaultSearchTab, dmFactory, genericCxt, false);
            }
        }
        return result;
    }

    // Inner classes -------------------------------------------------
}
