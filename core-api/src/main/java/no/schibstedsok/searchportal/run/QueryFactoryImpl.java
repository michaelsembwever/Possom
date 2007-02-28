// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.run;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;


/**
 * QueryFactoryImpl is part of no.schibstedsok.searchportal.query.
 * Use this class to create an instance of a RunningQuery.
 * 
 * TODO Replace the code in createQuery with a RunningQueryTransformer sub-module that is
 *  configured per mode and permits manipulation of the datamodel before the RunningQuery is constructed.
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

            if ("m".equals(cParam)) {

                final String userSortByParam = null != parametersDO.getValue("userSortBy")
                        ? parametersDO.getValue("userSortBy").getString()
                        : null;

                if (null == userSortByParam || "".equals(qParam)) {

                    query.addParameter("userSortBy", "datetime");
                }

                final String contentsourceParam = null != parametersDO.getValue("contentsource")
                        ? parametersDO.getValue("contentsource").getString()
                        : null;

                final String newscountryParam = null != parametersDO.getValue("newscountry")
                        ? parametersDO.getValue("newscountry").getString()
                        : "";

                if ("".equals(qParam) && null == contentsourceParam && "".equals(newscountryParam)) {
                    query.addParameter("newscountry", "Norge");
                }

            }else if ("t".equals(cParam)) {
                final Cookie cookies[] = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("myChannels".equals(cookie.getName())){
                            query.addParameter("myChannels", cookie.getValue());
                        }
                    }
                }
            }
        }
        return query;
    }
}
