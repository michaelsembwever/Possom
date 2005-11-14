package no.schibstedsok.front.searchportal.query;

import no.schibstedsok.front.searchportal.configuration.SearchMode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * QueryFactoryImpl is part of no.schibstedsok.front.searchportal.query
 * Use this class to create an instance of a RunningQuery
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class QueryFactoryImpl extends QueryFactory {

    Log log = LogFactory.getLog(QueryFactoryImpl.class);

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
    public RunningQuery createQuery(SearchMode mode,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: createQuery() Type=" + request.getParameter("t"));
        }

        if ("adv".equals(request.getParameter("t"))) {

            AdvancedQueryBuilder builder = new AdvancedQueryBuilder(request);
            HashMap param = new HashMap();
            param.put("type", "adv");
            RunningQuery query =
                    new RunningQuery(mode, builder.getQuery(), param);

            query.addParameter("request", request);
            query.addParameter("response", response);
            query.addParameter("filtertype", builder.getFilterType());
            query.addParameter("type", builder.getType());
            query.addParameter("language", builder.getFilterLanguage());

            return query;

        } else if ("adv_urls".equals(request.getParameter("t"))) {
            // Search for similar urls
            String q = "urls:" + request.getParameter("q_urls");
            if (log.isDebugEnabled()) {
                log.debug("createQuery: Query modified to " + q);
            }
            RunningQuery query = new RunningWebQuery(mode,
                    q,
                    request,
                    response);
            return query;

        } else {
            String q = request.getParameter("q");

            RunningQuery query = new RunningWebQuery(mode,
                    q,
                    request,
                    response);

            if ("m".equals(request.getParameter("c"))) {

                if (request.getParameter("userSortBy") == null
                        ||"".equals(request.getParameter("q"))) {
                    query.addParameter("userSortBy", new String[]{"datetime", ""});
                }

                //
                if ("".equals(q) && request.getParameter("contentsource") == null) {
                    query.addParameter("contentsource", new String[]{"Norske Nyheter", ""});
                }
            }
            return query;
        }
    }
}
