// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper for parsing query string.
 * 
 * @author <a href="mailto:anders.johan.jamtli@sesam.no">Anders Johan Jamtli</a>
 */
public final class QueryStringHelper {
    /** A safer way to get parameters for the query string. 
     * Handles ISO-8859-1 and UTF-8 URL encodings. 
     * 
     * @param req The servlet request we are processing
     * @param parameter The parameter to retrieve
     * @return The correct decoded parameter
     */
    public static String safeGetParameter(HttpServletRequest req, String parameter){
        StringTokenizer st = new StringTokenizer(req.getQueryString(), "&");
        String reqValue = req.getParameter(parameter);
        String queryStringValue = null;
        
        parameter += "=";
        while(st.hasMoreTokens()) {
            String tmp = st.nextToken();
            if (tmp.startsWith(parameter)) {
                queryStringValue = tmp.substring(parameter.length());
                break;
            }
        }
       
        if (reqValue == null) {
            return null;
        }
        
        try {
	        String encodedReqValue = URLEncoder.encode(reqValue, "UTF-8");
            
            queryStringValue = queryStringValue.replaceAll("[+]", "%20");
            queryStringValue = queryStringValue.replaceAll("[*]", "%2A");
            
            encodedReqValue = encodedReqValue.replaceAll("[+]", "%20");
            encodedReqValue = encodedReqValue.replaceAll("[*]", "%2A");
            
	        if (!queryStringValue.equals(encodedReqValue)){
	            reqValue = URLDecoder.decode(queryStringValue, "ISO-8859-1");
	        }
        } catch (UnsupportedEncodingException e) {
            /* IGNORED */
        }

        return reqValue;
    }
}
