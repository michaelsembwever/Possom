/* Copyright (2007-2008) Schibsted ASA
 *   This file is part of SESAT.
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
 *
 * UrlRewriteFilter.java
 *
 * Created on 19 March 2007, 21:27
 *
 */

package no.sesat.search.http.filters;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.http.urlrewrite.UrlRewriterContainerFactory;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.UrlResourceLoader;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

/** Override of tuckey's UrlRewriteFilter that supplies an inputstream to the skins's urlrewrite.xml
 *   instead of the default /WEB-INF/urlrewrite.xml
 *
 * Make sure that any urlrewrite.xml has the attributes in the root element:
 * &lt;urlrewrite decode-using="null" use-query-string="true"&gt;
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class UrlRewriteFilter extends org.tuckey.web.filters.urlrewrite.UrlRewriteFilter{

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    @Override
    protected UrlRewriter getUrlRewriter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain){

        UrlRewriter result = null;
        if(request instanceof HttpServletRequest){
            final HttpServletRequest httpRequest = (HttpServletRequest)request;
            final Site site = (Site) httpRequest.getAttribute(Site.NAME_KEY);

            final UrlRewriterContainerFactory factory = UrlRewriterContainerFactory.instanceOf(
                    new UrlRewriterContainerFactory.Context(){
                        public DocumentLoader newDocumentLoader(SiteContext siteCxt,
                                                                String resource,
                                                                DocumentBuilder builder) {

                            return UrlResourceLoader.newDocumentLoader(siteCxt, resource, builder);
                        }
                        public Site getSite() {
                            return site;
                        }
                        public ServletContext getServletContext(){
                            return httpRequest.getSession().getServletContext();
                        }
            });

            result = factory.getUrlRewriter();

        }
        return result;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
