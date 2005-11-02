package no.schibstedsok.front.searchportal.query;

import no.schibstedsok.front.searchportal.configuration.SearchMode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * QueryFactoryImpl is part of no.schibstedsok.front.searchportal.query
 * Use this class to create an instance of a RunningQuery
 *
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
     *
     * @param mode      with SearchConfiguration passed to RunningQuery
     * @param request   with parameters populated with search params
     * @return instance of RunningQuery
     */
    public RunningQuery createQuery(SearchMode mode,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: createQuery() Type=" + request.getParameter("t"));
        }

        if("adv".equals(request.getParameter("t"))){

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
            String q="urls:" + request.getParameter("q_urls");
            if (log.isDebugEnabled()) {
                log.debug("createQuery: Query modified to " + q);
            }
            RunningQuery query = new RunningWebQuery(mode,
                                      q,
                                      request,
                                       response);
            return query;

        } else {
            /*
            Map params = new HashMap();
            String sortBy = (String)request.getParameter("userSortBy");

            if(sortBy != null){
                if(log.isDebugEnabled()){
                    log.debug("createQuery: Adding user sortby");
                }
                params.put("userSortBy", "docdatetime");
            }
            System.out.println("ADDING PARAMS " + sortBy);
              */
            RunningQuery query = new RunningWebQuery(mode,
                                      request.getParameter("q"),
                                      request,
                                      response);
            
            return query;
        }
    }
}
