<%--
--%><%@ page import="no.schibstedsok.searchportal.view.config.SearchTab"%><%--
--%><%@ page import="no.schibstedsok.searchportal.view.output.SyndicationGenerator"%><%--
--%><%@ page import="no.schibstedsok.searchportal.site.Site"%><%--
--%><%@ page import="no.schibstedsok.searchportal.datamodel.DataModel" %><%--
--%><%@ page import="no.schibstedsok.searchportal.site.SiteContext" %><%--
--%><%@ page import="javax.xml.parsers.DocumentBuilder" %><%--
--%><%@ page import="java.util.Properties" %><%--
--%><%@ page import="no.schibstedsok.searchportal.site.config.*" %><%--
--%><%@page contentType="text/xml"%><%@page pageEncoding="UTF-8"%><%--
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
        public no.schibstedsok.searchportal.site.config.PropertiesLoader newPropertiesLoader(SiteContext siteCxt, String resource, Properties properties) {
            return UrlResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
        }

        public BytecodeLoader newBytecodeLoader(SiteContext siteContext, String className) {
            return UrlResourceLoader.newBytecodeLoader(siteContext, className);
        }
    };

    SyndicationGenerator generator = new SyndicationGenerator(cxt);
%><%--
--%><%= generator.generate() %>