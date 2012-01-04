/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.http.filters;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.security.MD5Generator;
import no.sesat.search.site.config.SiteConfiguration;

import javax.servlet.http.HttpServletRequest;
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


/** Generalised way to protect parameter values through md5 signings.
 * A skin must define the properties (in configuration.properties):
 * md5.secret and md5.protectedParameters
 * Any secret can be chosen. Any parameter matches those listed in md5.protectedParameters (separated by commas)
 * are expected to have a paired parameter (called &lt;parameterName&gt;_x) that represents the signing of the
 * original parameter value. If this second parameter does not exist or it's not an accurate signing of the original
 * parameter then the request immediately returns with a 404 response error.
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class MD5ProtectedParametersFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(MD5ProtectedParametersFilter.class);

    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

        final Enumeration e = servletRequest.getParameterNames();

        if(servletRequest instanceof HttpServletRequest) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            final DataModel datamodel = (DataModel)httpServletRequest.getSession().getAttribute(DataModel.KEY);
            final SiteConfiguration siteConf = datamodel.getSite().getSiteConfiguration();

            if(null != siteConf.getProperty("md5.secret") && null != siteConf.getProperty("md5.protectedParameters")){

                final MD5Generator generator = new MD5Generator(siteConf.getProperty("md5.secret"));

                final Map<String, Boolean> protectedParameters = new HashMap<String, Boolean>();
                final String[] p = siteConf.getProperty("md5.protectedParameters").split(",");
                for (final String parameter : p) {
                    LOG.info("Adding " + parameter + " as protected parameter");
                    protectedParameters.put(parameter, Boolean.TRUE);
                }

                while (e.hasMoreElements()) {
                    final String paramName = (String) e.nextElement();

                    LOG.trace("Checking to see if " + paramName + " is protected");

                    if (protectedParameters.containsKey(paramName)) {

                        LOG.trace(paramName + " is protected");

                        final String md5Param = servletRequest.getParameter(paramName + "_x");

                        if (md5Param == null || !generator.validate(servletRequest.getParameter(paramName), md5Param)){
                            final HttpServletResponse response = (HttpServletResponse) servletResponse;
                            response.sendError(HttpServletResponse.SC_NOT_FOUND);
                            return;
                        }
                    }
                }

                servletRequest.setAttribute("hashGenerator", generator);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
    }

}
