<%@ page import="no.schibstedsok.front.searchportal.result.BasicSearchResult"%><%--
--%><%@ page import="no.schibstedsok.front.searchportal.view.config.SearchTab"%><%--
--%><%@ page import="java.util.Map"%><%--
--%><%@ page import="no.schibstedsok.front.searchportal.result.SearchResult"%><%--
--%><%@ page import="no.schibstedsok.front.searchportal.view.output.SyndicationGenerator"%><%--
--%><%@ page import="no.schibstedsok.front.searchportal.site.Site"%><%--
--%><%@page contentType="text/xml"%><%@page pageEncoding="UTF-8"%><%--

--%><%
        final Site site = (Site) request.getAttribute(Site.NAME_KEY);
        final SearchTab searchTab = (SearchTab) request.getAttribute("tab");
        final Map<String, SearchResult> results = (Map<String, SearchResult>) request.getAttribute("results");
   
        final SyndicationGenerator generator = new SyndicationGenerator(results.get(searchTab.getRssResultName()), site, request, searchTab.getId());
%><%--
--%><%= generator.generate() %>