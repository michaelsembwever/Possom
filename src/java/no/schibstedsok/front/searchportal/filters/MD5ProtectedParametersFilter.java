package no.schibstedsok.front.searchportal.filters;

import no.schibstedsok.front.searchportal.security.MD5Generator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MD5ProtectedParametersFilter implements Filter {

    private Map protectedParameters;
    private MD5Generator digestGenerator;

    private static Log log = LogFactory.getLog(MD5ProtectedParametersFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {

        protectedParameters = new HashMap();

        String secret = filterConfig.getInitParameter("secret");
        String parameters = filterConfig.getInitParameter("protectedParameters");

        Boolean t = Boolean.TRUE;

        String[] p = parameters.split(",");
        for (int i = 0; i < p.length; i++) {
            String parameter = p[i];
            log.info("Adding " + parameter + " as protected parameter");
            protectedParameters.put(parameter,  t);
        }

        digestGenerator = new MD5Generator(secret);
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Enumeration e = servletRequest.getParameterNames();

        while (e.hasMoreElements()) {
            String parameterName = (String) e.nextElement();

            if (log.isDebugEnabled()) {
                log.debug("Checking to see if " + parameterName + " is protected");
            }

            if (protectedParameters.containsKey(parameterName)) {

                log.debug(parameterName + " is protected");

                String md5_parameter = servletRequest.getParameter(parameterName + "_x");

                if (md5_parameter == null || !digestGenerator.validate(servletRequest.getParameter(parameterName), md5_parameter)) {
                    HttpServletResponse response = (HttpServletResponse) servletResponse;
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
        }

        servletRequest.setAttribute("hashGenerator", digestGenerator);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
    }

}
