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
public final class MD5ProtectedParametersFilter implements Filter {

    private Map protectedParameters;
    private MD5Generator digestGenerator;

    private static final Log LOG = LogFactory.getLog(MD5ProtectedParametersFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {

        protectedParameters = new HashMap();

        final String secret = filterConfig.getInitParameter("secret");
        final String parameters = filterConfig.getInitParameter("protectedParameters");

        final Boolean t = Boolean.TRUE;

        final String[] p = parameters.split(",");
        for (int i = 0; i < p.length; i++) {
            final String parameter = p[i];
            LOG.info("Adding " + parameter + " as protected parameter");
            protectedParameters.put(parameter,  t);
        }

        digestGenerator = new MD5Generator(secret);
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final Enumeration e = servletRequest.getParameterNames();

        while (e.hasMoreElements()) {
            final String parameterName = (String) e.nextElement();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking to see if " + parameterName + " is protected");
            }

            if (protectedParameters.containsKey(parameterName)) {

                LOG.debug(parameterName + " is protected");

                final String md5_parameter = servletRequest.getParameter(parameterName + "_x");

                if (md5_parameter == null || !digestGenerator.validate(servletRequest.getParameter(parameterName), md5_parameter)) {
                    HttpServletResponse response = (HttpServletResponse) servletResponse;
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
