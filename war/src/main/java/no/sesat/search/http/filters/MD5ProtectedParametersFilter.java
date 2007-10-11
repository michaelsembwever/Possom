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
package no.sesat.search.http.filters;

import no.sesat.search.security.MD5Generator;

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

    private Map<String, Boolean> protectedParameters;
    private MD5Generator digestGenerator;

    private static final Logger LOG = Logger.getLogger(MD5ProtectedParametersFilter.class);

    /** {@inheritDoc} **/
    public void init(final FilterConfig filterConfig) throws ServletException {

        protectedParameters = new HashMap<String, Boolean>();

        final String secret = filterConfig.getInitParameter("secret");
        final String parameters = filterConfig.getInitParameter("protectedParameters");

        final Boolean t = Boolean.TRUE;

        final String[] p = parameters.split(",");
        for (final String parameter : p) {
            LOG.info("Adding " + parameter + " as protected parameter");
            protectedParameters.put(parameter, t);
        }

        digestGenerator = new MD5Generator(secret);
    }

    /** {@inheritDoc} **/
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

        final Enumeration e = servletRequest.getParameterNames();

        while (e.hasMoreElements()) {
            final String parameterName = (String) e.nextElement();

            if (LOG.isTraceEnabled()) {
                LOG.trace("Checking to see if " + parameterName + " is protected");
            }

            if (protectedParameters.containsKey(parameterName)) {

                if (LOG.isTraceEnabled()) {
                    LOG.trace(parameterName + " is protected");
                }

                final String md5Parameter = servletRequest.getParameter(parameterName + "_x");

                if (md5Parameter == null
                        || !digestGenerator.validate(servletRequest.getParameter(parameterName), md5Parameter))
                {
                    final HttpServletResponse response = (HttpServletResponse) servletResponse;
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        }

        servletRequest.setAttribute("hashGenerator", digestGenerator);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /** {@inheritDoc} **/
    public void destroy() {
    }

}
