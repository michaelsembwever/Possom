/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.run;


import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.request.ParametersDataObject;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * QueryFactoryImpl is part of no.sesat.search.query.
 * Use this class to create an instance of a RunningQuery.
 * <p/>
 * TODO Replace the code in createQuery with a RunningQueryTransformer sub-module that is
 * configured per mode and permits manipulation of the datamodel before the RunningQuery is constructed.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
public final class QueryFactoryImpl extends QueryFactory {

    private static final Logger LOG = Logger.getLogger(QueryFactoryImpl.class);

    /**
     * Create a new instance of running query. The implementation can
     * be RunningWebQuery for example.
     * <p/>
     * <b>NewsSearch business rules:</b>
     * <p/>
     * Set default parameter userSortBy to "datetime" if query is empty
     * Set contentsource to Norske Nyheter if query is empty and  contentsource is null.
     * (Kindof WEIRD business rules!!) It the query is not empty, then default is
     * to search in all contentsource
     *
     * @param mode    with SearchConfiguration passed to RunningQuery
     * @param request with parameters populated with search params
     * @return instance of RunningQuery
     */
    public RunningQuery createQuery(
            final RunningQuery.Context cxt,
            final HttpServletRequest request,
            final HttpServletResponse response) throws SiteKeyedFactoryInstantiationException {

        final DataModel datamodel = (DataModel) request.getSession().getAttribute(DataModel.KEY);
        final ParametersDataObject parametersDO = datamodel.getParameters();

        final String tParam = null != parametersDO.getValue("t") ? parametersDO.getValue("t").getString() : "";

        LOG.debug("createQuery() Type=" + tParam);

        final RunningQueryImpl query;

        if ("adv_urls".equals(tParam)) {

            // Search for similar urls
            final String qUrlsParam = null != parametersDO.getValue("q_urls")
                    ? parametersDO.getValue("q_urls").getString()
                    : "";

            final String q = "urls:" + qUrlsParam;
            LOG.debug("Query modified to " + q);
            query = new RunningWebQuery(cxt, q, request, response);

        } else {

            final String qParam = null != parametersDO.getValue("q") ? parametersDO.getValue("q").getString() : "";

            query = new RunningWebQuery(cxt, qParam, request, response);

            final String cParam = null != parametersDO.getValue("c") ? parametersDO.getValue("c").getString() : "";

            if ("nm".equals(cParam)) {
                final Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("myNews".equals(cookie.getName().trim())) {
                            LOG.debug("Adding cookie: " + cookie.getName() + "=" + cookie.getValue());
                            datamodel.getJunkYard().getValues().put("myNews", cookie.getValue());
                        }
                    }
                }
            }
        }
        return query;
    }
}
