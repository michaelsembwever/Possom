/* Copyright (2006-2007) Schibsted Søk AS
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

 */
package no.sesat.search.run;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version <tt>$Id$</tt>
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
                           final HttpServletResponse response) throws SiteKeyedFactoryInstantiationException {

        super(cxt, query);

        this.request = request;
        this.response = response;        

        if (LOG.isTraceEnabled()) {
            LOG.trace("RunningWebQuery(mode, " + query + ", request, response)");
        }

        // XXX The rest is redundant code!! stop using junkyard!
        final Map<String,Object> parameters = datamodel.getJunkYard().getValues();        
        
        // Add all request parameters
        /* SEE "Add all request attributes" below */
        
        for (String key : datamodel.getParameters().getValues().keySet()) {

            final String value = datamodel.getParameters().getValue(key).getString();
            parameters.put(key,  value);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Added " + key + ", value: " + value + ", ");
            }
        }

        // Hack to keep vg site search working. Dependent on old query
        // parameters. Remove when vg has been reimplented a proper site search.
        if (parameters.containsKey("nav_newspaperNames")) {
            parameters.put("nav_newspaperNames", "newssourcenavigator");
        }

        if (parameters.containsKey("nav_videosourceNames")) {
            parameters.put("nav_videosourceNames", "videosourcenavigator");
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
            parameters.put(attrName, request.getAttribute(attrName));
            if (LOG.isTraceEnabled()) {
                LOG.trace("Added " + attrName + ", value: " + request.getAttribute(attrName));
            }
        }

        for (final String attrName : ATTRS_TO_COPY) {
            if (request.getAttribute(attrName) != null) {
                parameters.put(attrName, request.getAttribute(attrName));

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Added(Manually) " + attrName + ", value: " + request.getAttribute(attrName));
                }
            }
        }

        for (final String header : HEADERS_TO_COPY) {
            if (request.getHeader(header) != null) {
                parameters.put(header, request.getHeader(header));

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Added HTTP header " + header + ", value: " + request.getHeader(header));
                }
            }
        }

        parameters.put("request", request);
        parameters.put("response", response);


    }

    @Override
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
            for( Map.Entry<String,Object> entry : datamodel.getJunkYard().getValues().entrySet() ){
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
            request.setAttribute("enrichments", getEnrichments());
            request.setAttribute("hits",getHits());
        }
    }
}
