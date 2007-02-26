// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.run;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.schibstedsok.searchportal.util.QueryStringHelper;
import org.apache.log4j.Logger;


/**
 * QueryFactoryImpl is part of no.schibstedsok.searchportal.query.
 * Use this class to create an instance of a RunningQuery
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("createQuery() Type=" + request.getParameter("t"));
        }

        final RunningQueryImpl query;

        if ("adv_urls".equals(request.getParameter("t"))) {
            // Search for similar urls
            final String q = "urls:" + request.getParameter("q_urls");
            LOG.debug("Query modified to " + q);
            query = new RunningWebQuery(cxt, q, request, response);

        } else {
            final String q = QueryStringHelper.safeGetParameter(request, "q");

            query = new RunningWebQuery(cxt, q, request, response);

            if ("m".equals(request.getParameter("c"))) {
                if (request.getParameter("userSortBy") == null || "".equals(request.getParameter("q"))) {

                    query.addParameter("userSortBy", "datetime");
                }

                if ("".equals(q) && request.getParameter("contentsource") == null && (request.getParameter("newscountry") == null || request.getParameter("newscountry").equals(""))) {
                    query.addParameter("newscountry", "Norge");
                }
                
            }else if ("t".equals(request.getParameter("c"))) {
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
