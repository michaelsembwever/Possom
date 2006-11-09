<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="com.opensymphony.module.sitemesh.Page"%>
<%@ page import="com.opensymphony.module.sitemesh.RequestConstants"%>
<%@ page import="no.schibstedsok.searchportal.view.config.SearchTab"%>
<%@ page import="no.schibstedsok.searchportal.view.i18n.TextMessages"%>
<%@ page import="no.schibstedsok.searchportal.result.Enrichment"%>
<%@ page import="no.schibstedsok.searchportal.result.Modifier"%>
<%@ page import="no.schibstedsok.searchportal.site.Site"%>
<%@ page import="no.schibstedsok.searchportal.result.Linkpulse"%>
<%@ page import="no.schibstedsok.searchportal.site.config.SiteConfiguration"%>
<%@ page import="no.schibstedsok.searchportal.view.config.SearchTab" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>
<%
final TextMessages text = (TextMessages) request.getAttribute("text");
final Site site = (Site)request.getAttribute(Site.NAME_KEY);
String locale = site.getLocale().toString();
final SearchTab tab = (SearchTab)request.getAttribute("tab");

String currentC = "d";    //default collection
currentC = (String) request.getAttribute("c");
String q = (String) request.getAttribute("q");
final String contentsource = (String) request.getParameter("contentsource");
final String newscountry = (String) request.getParameter("newscountry");
final String qURLEncoded = URLEncoder.encode(q, "utf-8");
q = (String) request.getAttribute("queryHTMLEscaped");
final boolean publish = null != request.getParameter("page");
final String box = request.getParameter("box");
final String toolbar = request.getParameter("toolbar");
final String tradedoubler = request.getParameter("td");
final String ss = request.getParameter("ss");
final String ssr = request.getParameter("ssr");
final String vertikal = request.getParameter("vertikal") == null ? "" : request.getParameter("vertikal");

final List<Enrichment> enrichments = (List<Enrichment>) request.getAttribute("enrichments");
final int enrichmentSize = enrichments.size();
pageContext.setAttribute("enrichmentSize", enrichmentSize);

final Page siteMeshPage = (Page) request.getAttribute(RequestConstants.PAGE);
pageContext.setAttribute("siteMeshPage", siteMeshPage);

final List<Modifier> sources = (List<Modifier>)request.getAttribute("sources");
final Map<String,Integer> hits = (Map<String,Integer>)request.getAttribute("hits");
final Integer dHits = hits.get("defaultSearch");
final Integer gHits = hits.get("globalSearch");
final int no_hits = dHits!= null&&dHits > 0 ? dHits.intValue() : gHits!= null&&gHits > 0 ? gHits.intValue() : 0;

/* TODO: add tvSearch hits */
/*    final Integer tHits = (Integer) query.getNumberOfHits("tvSearch");
    final int no_hits = dHits != null && dHits > 0
            ? dHits.intValue()
            : gHits != null && gHits > 0 ? gHits.intValue() :
            tHits != null && tHits > 0 ? tHits.intValue() : 0;
*/

pageContext.setAttribute("no_hits", no_hits);

final Linkpulse linkpulse = new Linkpulse(site, SiteConfiguration.valueOf(site).getProperties());

String searchButton = "../tradedoubler/searchbox/button-sesam-long.png";
if (currentC.equals("y")) searchButton = "../tradedoubler/searchbox/button-company.png";
else if (currentC.equals("w")) searchButton = "../tradedoubler/searchbox/button-person.png";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <search:velocity template="/fragments/head"/>
</head>

<%if (currentC.equals("y") || currentC.equals("yg") || currentC.equals("w") || currentC.equals("sw") || currentC.equals("swip")) {%>
<body onLoad="init();">
<%} else if (currentC.equals("yip") || currentC.equals("wip") || currentC.equals("yipticker")) {%>
<body onLoad="init(); checkTab();">
<%} else if (q.trim().equals("") && currentC.equals("m") && vertikal.equals("m") && !publish) {%>
<body onLoad="sndReq('/search/?c=l&newscountry=Norge&q=&output=rss');scrollbox();">
<%} else {%>
<body>
<% } %>
    <search:velocity template="/pages/main"/>

    <c:if test="${! empty Missing_pagesmain_Template}">

    <%-- old-school sitesearch --%>
    <% if (currentC.equals("d") && (
             "di".equals(ss) ||
             "pr".equals(ss) ||
             "im".equals(ss) ||
             "af".equals(ss) ||
             "fv".equals(ss) ||
             "aa".equals(ss) ||
             "bt".equals(ss) ||
             "sa".equals(ss))) { %>

        <div id="frame">
            <div id="header">
                <search:velocity template="legacy/skin/headers/${param.ss}"/>
                <decorator:getProperty property="page.search-bar"/>

            </div>
            <div id="content_ss">
                <div id="globalmenu_table"><img src="../images/pix.gif" width="1" height="6" alt="" /></div>
                <div id ="content_top">
                    <dl>
                        <dt>
                            <span class="sitename">
                                <% if ("ds".equals(ssr)) { %> Dinside:
                                <% } else if ("di".equals(ssr)) { %> Digi:
                                <% } else if ("pr".equals(ssr)) { %> Propaganda:
                                <% } else if ("it".equals(ssr)) { %> Itavisen:
                                <% } else if ("im".equals(ssr)) { %> iMarkedet:
                                <% } else if ("nrk".equals(ssr)) { %> NRK:
                                <% } else if ("af".equals(ssr)) { %> Aftenposten:
                                <% } else if ("fv".equals(ssr)) { %> F&#230;drelandsvennen:
                                <% } else if ("aa".equals(ssr)) { %> Adresseavisen:
                                <% } else if ("bt".equals(ssr)) { %> bt.no:
                                <% } else if ("sa".equals(ssr)) { %> Stavanger Aftenblad:
                                <% } else if ("d".equals(ssr)) { %> Nettet:
                                <% } %>
                            </span>
                            <decorator:getProperty property="page.greybar_sitesearch"/>
                        </dt>
                        <dd><decorator:getProperty property="page.greybar_ad"/></dd>
                    </dl>
                </div>
                <div class="greybar_line"><img src="../images/pix.gif" width="1" height="1" alt="" /></div>
                <%--sesam search in sitesearch modus--%>
                <div id="content_left_ss">
                    <decorator:getProperty property="page.fast-results"/>
                </div>
                <div id="content_right_ss">
                    <decorator:getProperty property="page.ads"/>
                </div>
            </div>
            <div id="footer_ss">
                <decorator:getProperty property="page.offsetPager"/>
            </div>
        </div>
        
    <% } else { %>

        <%if (q.trim().equals("") && ((currentC.equals("m") && vertikal.equals("m")) || currentC.equals("y") || currentC.equals("yg") || currentC.equals("w") || currentC.equals("p") || currentC.equals("sw") || currentC.equals("b") )) {%>
            <decorator:getProperty property="page.newsearch-bar"/>
	<% }else if(q.trim().equals("") && currentC.equals("d") && publish) {%>
	    <decorator:getProperty property="page.omsesam-bar"/>
	<% }else{ %>
	    <decorator:getProperty property="page.search-bar"/>
	<% } %>

<%-- temporary sesam.se header fix for pub system --%>        
<% if ( !"sv".equals(locale) ) { %>      
<table border="0" cellspacing="0" cellpadding="0" id="body_table">
<% } else { %>
    <%if (currentC.equals("d") && publish) {%>
        <table width="100%" cellspacing="0" cellpadding="0" id="body_table">    
    <% } else { %>
        <table border="0" cellspacing="0" cellpadding="0" id="body_table">    
    <% } %>
<% } %>
    <%if(q.trim().equals("") && currentC.equals("d") && publish) {%>
	<tr>
    <%} else if (q.trim().equals("") && !currentC.equals("m") && !currentC.equals("l") && !currentC.equals("t") && !currentC.equals("wt")) {%>
    	<tr>
            <td class="cell_one">&nbsp;</td>
            <td class="cell_three">&nbsp;</td>
            <td class="cell_four">&nbsp;</td>
    	</tr>
    <% }else{ %>
        <tr>
            <%if (q.trim().equals("") && currentC.equals("m") && vertikal.equals("m")) {%>
            <%}else if ((currentC.equals("b") || currentC.equals("m") || currentC.equals("l") || currentC.equals("d") || currentC.equals("g") || currentC.equals("pss")) || !q.trim().equals("") || currentC.equals("t") || currentC.equals("wt")) {%>
                <td class="cell_one" valign="top">
                    <%if (q.trim().equals("") && (currentC.equals("t") || currentC.equals("m") || currentC.equals("l"))) { %>
                        <decorator:getProperty property="page.frontMenu"/>
                    <% } else { %>                

                        <search:velocity template="/navigators/navbarMain"/>

                    <% } %>
                    <% if (currentC.equals("m")) { %>
                        <decorator:getProperty property="page.newsSearchNavigator" />
                        <decorator:getProperty property="page.media-collection-nav"/>
                    <%}else {%>
                        <decorator:getProperty property="page.sub-navigator"/>
                    <%}%>            

                    <decorator:getProperty property="page.relevantQueries" />

                    <search:velocity template="navigators/scanpix" command="scanpix"/>
                    <decorator:getProperty property="page.blogDateNavigation"/>

                    <c:if test='${!(empty tab.rssResultName)}'>
                        <decorator:getProperty property="page.rss-nav"/>
                    </c:if>
                </td>
            <% } %>
    <% } %>
        

        <%if (q.trim().equals("") && !currentC.equals("t") && !currentC.equals("l") && !currentC.equals("m") && !currentC.equals("wt")) {%>
            <td valign="top" colspan="3">
        <%}else if (!currentC.equals("y") && !currentC.equals("yip") && !currentC.equals("w") && !currentC.equals("wip")&& !currentC.equals("swip") && !currentC.equals("wipgift")) {%>
            	<td class="cell_three" valign="top">
        <% }else{ %>
            	<td class="cell_three" valign="top" colspan="2">
 	<%}%>

        <%if (q.trim().equals("") && !currentC.equals("m") && !currentC.equals("l") && !currentC.equals("t") && !currentC.equals("wt")) {%>
	<%}else {%>
            <% if ( !"sv".equals(locale) ) { %>        
                <%if (currentC.equals("p") || currentC.equals("pp") || currentC.equals("pip") || currentC.equals("pipn")) {%>
                    <search:velocity template="fragments/middlebar" command="scanpix"/>
                <% }else if(q.trim().equals("") && currentC.equals("m") && vertikal.equals("m")){ %>	
                <% }else { %>
                    <decorator:getProperty property="page.middle-bar"/>
                <% } %>
            <% }else { %>
                <decorator:getProperty property="page.middle-bar"/>
            <% } %>
       	<% } %>

        <%if (currentC.equals("d") || "g".equals(currentC) || "pss".equals(currentC)) {%>

             <%--  Sok smart  --%>
             <% if ( publish ) { %>
                <decorator:getProperty property="page.publishing_page"/>
             <% } else if ("true".equals(box)) { %>
                <decorator:getProperty property="page.searchbox"/>
             <% } else if ("true".equals(toolbar)) { %>
                <decorator:getProperty property="page.toolbar"/>
             <% } else if ("true".equals(tradedoubler)) { %>
                <decorator:getProperty property="page.tradedoubler"/>
             <% } else { %>

                <decorator:getProperty property="page.spellcheck"/>
                <decorator:getProperty property="page.main_ads"/>

                 <c:choose>
                     <c:when test="${no_hits >0 || enrichmentSize >0}">

                         <!--  Enrichments on top: <c:out value="${tab.enrichmentOnTop}"/>
                          Enrichments in total: <c:out value="${tab.enrichmentLimit}"/>
                           <c:forEach var="ee" items="${enrichments}">
                               <c:out value="${ee.name}"/>: <c:out value="${ee.analysisResult}"/>
                           </c:forEach> -->

                           <%-- Show tab's leading enrichments --%>
                           <c:forEach var="ee" items="${enrichments}" varStatus="i">
                               <c:if test="${i.index < tab.enrichmentOnTop && ee.analysisResult > tab.enrichmentOnTopScore}">
                                   <c:set var="pageName" value="page.${ee.name}"/>
                                   <search:velocity template="enrichments/${ee.name}" command="${ee.name}"/>
                                    <c:out value="${siteMeshPage.properties[pageName]}" escapeXml="false"/>
                               </c:if>
                           </c:forEach>

                            <c:choose>
                                <c:when test="${tab.id == 'local-internet' || tab.id == 'pss'}">
                                   <%--  Shows the 3 first hits if more than 1 enrichment  --%>
                                   <decorator:getProperty property="page.fast-results-norwegian_part1"/>


                                   <%-- Show tab's proceeding enrichments --%>
                                   <c:forEach var="ee" items="${enrichments}" varStatus="i">
                                       <c:if test="${(i.index >= tab.enrichmentOnTop || ee.analysisResult <= tab.enrichmentOnTopScore) && i.index < tab.enrichmentLimit}">
                                           <c:set var="pageName" value="page.${ee.name}"/>
                                           <search:velocity template="enrichments/${ee.name}" command="${ee.name}"/>
                                           <c:out value="${siteMeshPage.properties[pageName]}" escapeXml="false"/>
                                       </c:if>
                                   </c:forEach>

                                   <%--  Shows the 7 next hits after the second/third enrichments  --%>
                                   <decorator:getProperty property="page.fast-results-norwegian_part2"/>

                                </c:when>
                                <c:when test="${tab.id == 'global-internet'}">
                                    <decorator:getProperty property="page.global-results"/>
                               </c:when>
                           </c:choose>
                     </c:when>
                     <c:otherwise>
                         <decorator:getProperty property="page.noHits" />
                     </c:otherwise>
                 </c:choose>

            <% } %>  <%-- Sok smart --%>
            
        <% } else if (currentC.equals("m") || currentC.equals("l")) {%>
            <%if (q.trim().equals("") && currentC.equals("m") && vertikal.equals("m")) {%>
            <%}else{%>
                <decorator:getProperty property="page.main_ads"/>
                <decorator:getProperty property="page.search-results"/>
            <%}%>	            
        <% } else if (currentC.equals("p") || currentC.equals("pp") || currentC.equals("pip")) {%>
            <div>            
                <% if ( !"sv".equals(locale) ) { %>
                    <% if (currentC.equals("p")) {%>
                    <decorator:getProperty property="page.picsearch-results"/>
                    <% } else { %>
                    <search:velocity template="results/scanpix" command="scanpix"/>
                    <decorator:getProperty property="page.picsearch-results"/>
                    <%}%>
                <% } else { %>
                    <decorator:getProperty property="page.picsearch-results"/>
                <% } %>
                <!--search:import template="picSearch"/-->

                <div class="clearFloat">&nbsp;</div>
            </div>
        <% } else { %>
            <decorator:getProperty property="page.search-results"/>
            <search:velocity template="results/giftProviders" command="giftProviders"/>            
        <%}%>

      
        <%--  offset  --%>
        <%if (q==null || !q.trim().equals("") || "m".equals(currentC) || "l".equals(currentC)) {%>
            <%if (currentC.equals("pp")) {%>
                    <search:velocity template="fragments/offsetPager" command="scanpix"/>
            <% } else { %>
                <% if ( !"sv".equals(locale) ) { %>
                    <decorator:getProperty property="page.offsetPager"/>
                <% } %>            
             <% } %>
        <%}%>
        </td>

    	<%if(q.trim().equals("") && currentC.equals("d") && publish) {%>
	<%} else {%>
        <td class="cell_four" valign="top" width="225">
	    <% if(q.trim().equals("") && currentC.equals("m") && vertikal.equals("m")){ %>	
	    <% } else { %>
            	<div id="midbar_right">
                    <decorator:getProperty property="page.greybar_ad"/>
            	</div>

            	<% if (currentC.equals("p") || currentC.equals("pp") || currentC.equals("pip") ) {%>
                    <decorator:getProperty property="page.ads"/>
                    <decorator:getProperty property="page.ads-logo"/>
            	<%}else if (currentC.equals("b") ) {%>
                    <decorator:getProperty property="page.feedback"/>
            	<%} else if (currentC.equals("t")) {%>
                    <decorator:getProperty property="page.tvSearchWebTv"/>
                    <decorator:getProperty property="page.ads"/>
            	<%} else {%>
                    <decorator:getProperty property="page.ads"/>
            	<%}%>
            <%}%>
        </td>
	<%}%>
    </tr>
    <% if ( currentC.equals("sw") || currentC.equals("swip") ) {%>
        <decorator:getProperty property="page.ads_floating"/>
    <% } %>
    
     <% if ( "sv".equals(locale) ) { %>
        <tr>
            <td>&nbsp;</td>
            <td colspan="2"><decorator:getProperty property="page.offsetPager"/></td>
        </tr>     
     <% } %>    
</table>

    <decorator:getProperty property="page.verbosePager"/>

    <decorator:getProperty property="page.footer"/>

<%}%>

    </c:if>

<decorator:getProperty property="page.map-script"/>

<search:velocity template="fragments/gallup" />
</body>
</html>
