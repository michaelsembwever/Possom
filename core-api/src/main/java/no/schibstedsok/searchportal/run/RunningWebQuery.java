// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.run;


import java.io.IOException;
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
    private static final String ERR_SEND_ERROR = "!!! Unable to sendError !!!";
    
    private final HttpServletRequest request;
    private final HttpServletResponse response;


    /**
     * Request attributes that should be added to parameter map. This is needed because request.getAttributeNames()
     * does not include all attributes passed by mod_jk. http://issues.apache.org/bugzilla/show_bug.cgi?id=25363.
     */
    private static final String[] ATTRS_TO_COPY = new String[] {
            "REMOTE_ADDR"
    };

    /**
     * Request headers to be copied to parameter map.
     */
    private static final String[] HEADERS_TO_COPY = new String[] {
            "user-agent",
            "x-forwarded-for"
    };

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

        if (LOG.isTraceEnabled()) {
            LOG.trace("RunningWebQuery(mode, " + query + ", request, response)");
        }

        // Add all request parameters 
        /* SEE "Add all request attributes" below */
        for (String parameterName : (Set<String>)request.getParameterMap().keySet()) {

            final String[] parameterValues = request.getParameterValues(parameterName);
            addParameter(parameterName, parameterValues.length>1 ? parameterValues : parameterValues[0]);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Added " + parameterName + ", values: " + StringUtils.join(parameterValues, ", "));
            }
        }
        
        // Hack to keep vg site search working. Dependent on old query
        // parameters. Remove when vg has been reimplented a proper site search.
        if (parameters.containsKey("nav_newspaperNames")) {
            parameters.put("nav_newspaperNames", "newssourcenavigator");
        }

        if (parameters.containsKey("ywpopnavn")) {
            parameters.put("newssource", parameters.get("ywpopnavn"));
            parameters.remove("ywpopnavn");
        }
        
        // Add all request attributes (servlet may have added some things already)...
        for (Enumeration<String> e = (Enumeration<String>)request.getAttributeNames(); e.hasMoreElements();) {

            final String attrName = e.nextElement();
            /*
                // HACK backwards-compatibility since we never designed for unique names across parameters & attributes
                //  any attribute that overlaps a parameter's name won't be added!!
                
             * this has now been changed. request parameters are first put into the parameters map and 
             * are overwritten with request attributes. this is a basic attempt to prevent parameter injection.
             */
            addParameter(attrName, request.getAttribute(attrName));
            if (LOG.isTraceEnabled()) {
                LOG.trace("Added " + attrName + ", value: " + request.getAttribute(attrName));
            }
        }

        for (final String attrName : ATTRS_TO_COPY) {
            if (request.getAttribute(attrName) != null) {
                addParameter(attrName, request.getAttribute(attrName));

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Added(Manually) " + attrName + ", value: " + request.getAttribute(attrName));
                }
            }
        }

        for (final String header : HEADERS_TO_COPY) {
            if (request.getHeader(header) != null) {
                addParameter(header, request.getHeader(header));

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Added HTTP header " + header + ", value: " + request.getHeader(header));
                }
            }
        }

        this.request = request;
        this.response = response;
        addParameter("request", request);
        addParameter("response", response);

        
    }
    
    public void run() throws InterruptedException{
        
        super.run();
        
        if( allCancelled ){
            
            try {
                
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (IOException ex) {
                LOG.error(ERR_SEND_ERROR, ex);
            }
            
        }else{
            
            // push all parameters into request attributes, they are needed by jsp and taglib
            for( Map.Entry<String,Object> entry : parameters.entrySet() ){
                // don't put back in String array that only contains one element
                if( entry.getValue() instanceof String[] && ((String[])entry.getValue()).length ==1 ){
                    request.setAttribute(entry.getKey(), ((String[])entry.getValue())[0]);
                }else{
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Added " + entry.getKey() + ", value: " + request.getAttribute(entry.getKey()));
                }
            }
            // ...and...
            request.setAttribute("queryHTMLEscaped", StringEscapeUtils.escapeHtml(getQueryString()));
            request.setAttribute("enrichments", getEnrichments());
            request.setAttribute("sources", getSources());
            request.setAttribute("hits",getHits());
        }
    }
}
