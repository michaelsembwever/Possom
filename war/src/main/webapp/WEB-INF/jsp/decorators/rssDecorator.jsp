<%-- Copyright (2008) Schibsted Søk AS
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
--%><%
{
    if (request.getParameter("encoding") != null && request.getParameter("encoding").equals("iso-8859-1")){
        response.setContentType("text/xml; charset=iso-8859-1");
    } else {
        response.setContentType("text/xml; charset=utf-8");
    }
}
%><%--
--%><%@ page import="no.sesat.search.view.config.SearchTab"%><%--
--%><%@ page import="no.sesat.search.view.output.SyndicationGenerator"%><%--
--%><%@ page import="no.sesat.search.site.Site"%><%--
--%><%@ page import="no.sesat.search.datamodel.DataModel" %><%--
--%><%@ page import="no.sesat.search.site.SiteContext" %><%--
--%><%@ page import="javax.xml.parsers.DocumentBuilder" %><%--
--%><%@ page import="java.util.Properties" %><%--
--%><%@ page import="no.sesat.search.site.config.*" %><%--
--%><%

    final HttpServletRequest req = request;

    final SyndicationGenerator.Context cxt = new SyndicationGenerator.Context() {

        public SearchTab getTab() {
            return (SearchTab) req.getAttribute("tab");
        }

        public String getURL() {
            return req.getRequestURL().append("?").append(req.getQueryString()).toString();        
        }

        public Site getSite() {
            return (Site) req.getAttribute(Site.NAME_KEY);
        }
        public DataModel getDataModel() {
            return (DataModel) req.getSession().getAttribute(DataModel.KEY);
        }
        public DocumentLoader newDocumentLoader(SiteContext siteCxt, String resource, DocumentBuilder builder) {
            return UrlResourceLoader.newDocumentLoader(siteCxt, resource, builder);
        }
        public no.sesat.search.site.config.PropertiesLoader newPropertiesLoader(SiteContext siteCxt, String resource, Properties properties) {
            return UrlResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
        }

        public BytecodeLoader newBytecodeLoader(SiteContext siteContext, String className, String jar) {
            return UrlResourceLoader.newBytecodeLoader(siteContext, className, jar);
        }
    };

    SyndicationGenerator generator = new SyndicationGenerator(cxt);
%><%--
--%><%= generator.generate() %>
