// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * QueryFactoryImpl is part of no.schibstedsok.front.searchportal.query.
 * Use this class to create an instance of a RunningQuery
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @vesrion $Revision$, $Author$, $Date$
 */
public final class QueryFactoryImpl extends QueryFactory {

    private static final Log LOG = LogFactory.getLog(QueryFactoryImpl.class);

    /**
     * Create a new instance of running query. The implementation can
     * be RunningWebQuery, RunningAdvWebQuery for example.
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
            final HttpServletResponse response) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: createQuery() Type=" + request.getParameter("t"));
        }

        RunningQuery query;

        if ("adv".equals(request.getParameter("t"))) {

            final AdvancedQueryBuilder builder = new AdvancedQueryBuilder(request);
            final Map param = new HashMap();
            param.put("type", "adv");
            query = new RunningQuery(cxt, builder.getQuery(), param);

            query.addParameter("request", request);
            query.addParameter("response", response);
            query.addParameter("filtertype", builder.getFilterType());
            query.addParameter("type", builder.getType());
            query.addParameter("language", builder.getFilterLanguage());

        } else if ("adv_urls".equals(request.getParameter("t"))) {
            // Search for similar urls
            final String q = "urls:" + request.getParameter("q_urls");
            if (LOG.isDebugEnabled()) {
                LOG.debug("createQuery: Query modified to " + q);
            }
            query = new RunningWebQuery(cxt, q, request, response);

        } else {
            final String q = request.getParameter("q");

            query = new RunningWebQuery(cxt, q, request, response);

            if ("m".equals(request.getParameter("c"))) {

                if (request.getParameter("userSortBy") == null
                        || "".equals(request.getParameter("q"))) {

                    query.addParameter("userSortBy", new String[]{"datetime", ""});
                }

                //
                if ("".equals(q) && request.getParameter("contentsource") == null) {
                    query.addParameter("contentsource", new String[]{"Norske Nyheter", ""});
                }
            }
        }
        return query;
    }
}
