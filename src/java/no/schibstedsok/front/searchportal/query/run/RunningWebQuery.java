// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.run;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/*
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 */
public final class RunningWebQuery extends RunningQueryImpl {

    private static final Logger LOG = Logger.getLogger(RunningWebQuery.class);
    
    private final HttpServletRequest request;
    private final HttpServletResponse response;


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

        super(cxt, query, new Hashtable<String,Object>());

        LOG.trace("RunningWebQuery(mode, " + query + ", request, response)");

        // Add all request parameters
        for (String parameterName : (Set<String>)request.getParameterMap().keySet()) {

            final String[] parameterValues = request.getParameterValues(parameterName);
            addParameter(parameterName, parameterValues.length>1 ? parameterValues : parameterValues[0]);

            if (LOG.isInfoEnabled()) {
                LOG.info("Added " + parameterName + ", values: " + StringUtils.join(parameterValues, ", "));
            }
        }
        
        // Add all request attributes (servlet may have added some things already)...
        for (Enumeration<String> e = (Enumeration<String>)request.getAttributeNames(); e.hasMoreElements();) {

            final String attrName = e.nextElement();
            // HACK backwards-compatibility since we never designed for unique names across parameters & attributes
            //  any attribute that overlaps a parameter's name won't be added!!
            if( !parameters.containsKey(attrName) ){
                addParameter(attrName, request.getAttribute(attrName));
                LOG.info("Added " + attrName + ", value: " + request.getAttribute(attrName));
            }
        }
        
        this.request = request;
        this.response = response;
        addParameter("request", request);
        addParameter("response", response);

        
    }
    
    public void run() throws InterruptedException{
        
        super.run();
        
        // push all parameters into request attributes
        for( Map.Entry<String,Object> entry : parameters.entrySet() ){
            // don't put back in String array that only contains one element
            if( entry.getValue() instanceof String[] && ((String[])entry.getValue()).length ==1 ){
                request.setAttribute(entry.getKey(), ((String[])entry.getValue())[0]);
            }else{
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        // ...and...
        request.setAttribute("queryHTMLEscaped", StringEscapeUtils.escapeHtml(getQueryString()));
        request.setAttribute("enrichments", getEnrichments());
        request.setAttribute("sources", getSources());
        request.setAttribute("hits",getHits());
    }
}
