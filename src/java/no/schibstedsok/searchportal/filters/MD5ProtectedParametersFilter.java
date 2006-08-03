// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.filters;

import no.schibstedsok.searchportal.security.MD5Generator;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class MD5ProtectedParametersFilter implements Filter {

    private Map protectedParameters;
    private MD5Generator digestGenerator;

    private static final Logger LOG = Logger.getLogger(MD5ProtectedParametersFilter.class);

    /** @inherit **/
    public void init(final FilterConfig filterConfig) throws ServletException {

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

    /** @inherit **/
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

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
                    final HttpServletResponse response = (HttpServletResponse) servletResponse;
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        }

        servletRequest.setAttribute("hashGenerator", digestGenerator);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /** @inherit **/
    public void destroy() {
    }

}
