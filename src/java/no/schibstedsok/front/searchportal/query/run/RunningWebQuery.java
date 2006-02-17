// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.run;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import no.schibstedsok.front.searchportal.query.*;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/*
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * TODO move common code out of RunningQueryImpl and into AbstractRunningQuery, '
 *  and change extension to AbstractRunningQuery.
 */
public class RunningWebQuery extends RunningQueryImpl {

    private static final Log LOG = LogFactory.getLog(RunningWebQuery.class);


    /**
     *
     * @param mode
     * @param query
     * @param request
     * @param response
     */
    public RunningWebQuery(final Context cxt,
                           final String query,
                           final HttpServletRequest request,
                           final HttpServletResponse response) {

        super(cxt, query, new HashMap());

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: RunningWebQuery(mode, query, request,response)");
        }
        for (Iterator iterator = request.getParameterMap().keySet().iterator(); iterator.hasNext();) {
            String parameterName = (String) iterator.next();
            String[] parameterValues = request.getParameterValues(parameterName);
            addParameter(parameterName, parameterValues);

            if (LOG.isDebugEnabled()) {
                StringBuffer buff = new StringBuffer();

                for (int i = 0; i < parameterValues.length; i++) {
                    buff.append(parameterValues[i] + ", ");
                }
                LOG.debug("RunningWebQuery: Added " + parameterName + ", values: " + buff);
            }

        }
        addParameter("request", request);
        addParameter("response", response);

        request.setAttribute("enrichments", getEnrichments());
    }
}
