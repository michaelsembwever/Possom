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

<body onload="sesamInit('<%= currentC %>', '<%= vertikal %>', '<%= q.trim() %>', <%= publish %>);">
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
        <%if (q.trim().equals("") && ((currentC.equals("m") && vertikal.equals("m")) || currentC.equals("y") || currentC.equals("yg") || currentC.equals("w") || currentC.equals("p") || currentC.equals("pp") || currentC.equals("sw") || currentC.equals("b") )) {%>
            <decorator:getProperty property="page.newsearch-bar"/>
	<% }else if(q.trim().equals("") && currentC.equals("d") && publish) {%>
	    <decorator:getProperty property="page.omsesam-bar"/>
	<% }else{ %>
	    <decorator:getProperty property="page.search-bar"/>
	<% } %>

        <table border="0" cellspacing="0" cellpadding="0" id="body_table">
        <%if(q.trim().equals("") && currentC.equals("d") && publish) {%>
            <tr>
        <%} else if (q.trim().equals("") && !currentC.equals("m") && !currentC.equals("l") && !currentC.equals("t") && !currentC.equals("wt") && !currentC.equals("tvmc")) {%>
            <tr>
                <td class="cell_one">&nbsp;</td>
                <td class="cell_three">&nbsp;</td>
                <td class="cell_four">&nbsp;</td>
            </tr>
        <% }else{ %>
            <tr>
                <%if (q.trim().equals("") && currentC.equals("m") && vertikal.equals("m")) {%>
                <%}else if ((currentC.equals("b") || currentC.equals("m") || currentC.equals("l") || currentC.equals("d") || currentC.equals("g") || currentC.equals("pss")) || !q.trim().equals("") || currentC.equals("t") || currentC.equals("tvmc") || currentC.equals("wt")) {%>
                    <td class="cell_one" valign="top">
                        <%if (q.trim().equals("") && (currentC.equals("t") || currentC.equals("tvmc") || currentC.equals("m") || currentC.equals("l"))) { %>
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

                        <c:if test='${tab.showRss}'>
                            <decorator:getProperty property="page.rss-nav"/>
                        </c:if>
                    </td>
                <% } %>
        <% } %>
        <%if (q.trim().equals("") && !currentC.equals("t") && !currentC.equals("tvmc") && !currentC.equals("l") && !currentC.equals("m") && !currentC.equals("wt")) {%>
            <td valign="top" colspan="3">
        <%}else if (!currentC.equals("y") && !currentC.equals("yip") && !currentC.equals("w") && !currentC.equals("wip")&& !currentC.equals("swip") && !currentC.equals("wipgift")) {%>
            <td class="cell_three" valign="top">
        <% }else{ %>
            <td class="cell_three" valign="top" colspan="2">
 	<%}%>

        <%if (q.trim().equals("") && !currentC.equals("m") && !currentC.equals("l") && !currentC.equals("t") && !currentC.equals("wt") && !currentC.equals("tvmc")) {%>
	<%}else {%>
            <%if (currentC.equals("d") || currentC.equals("g") || currentC.equals("p") || currentC.equals("pp") || currentC.equals("pip") || currentC.equals("pipn") || currentC.equals("t") || currentC.equals("tvmc") || currentC.equals("wt")) {%>
                <search:velocity template="fragments/middlebar" />
            <% }else if(q.trim().equals("") && currentC.equals("m") && vertikal.equals("m")){ %>	
            <% }else { %>
                <decorator:getProperty property="page.middle-bar"/>
            <% } %>
       	<% } %>

        <decorator:getProperty property="page.publishing_page"/>
        <decorator:getProperty property="page.spellcheck"/>
        <decorator:getProperty property="page.main_ads"/>
        <search:velocity template="fragments/top3AdsTop" command="top3Ads"/>
        <%if (currentC.equals("d") || "g".equals(currentC) ) {%>    
            <search:velocity template="/enrichments/enrichment-handler"/>
        <% } else if (q.trim().equals("") && currentC.equals("m") && vertikal.equals("m")) {%>
        <% } else if (currentC.equals("p") || currentC.equals("pp") || currentC.equals("pip")) {%>
            <div>            
                <% if (currentC.equals("p")) {%>
                    <decorator:getProperty property="page.picsearch-results"/>
                <% } else { %>
                    <search:velocity template="results/scanpix" command="scanpix"/>
                    <decorator:getProperty property="page.picsearch-results"/>
                <%}%>
                <div class="clearFloat">&nbsp;</div>
            </div>
        <% } else if (currentC.equals("t") || currentC.equals("tvmc")) { %>
            <search:velocity template="results/tvSearch" command="tvSearch"/>
        <% } else { %>
            <decorator:getProperty property="page.search-results"/>
            <search:velocity template="results/giftProviders" command="giftProviders"/>            
        <%}%>      

        </td>
        <td class="cell_four">
            <% if (currentC.equals("p") || currentC.equals("pp") || currentC.equals("pip") ) {%>
                <decorator:getProperty property="page.ads"/>
                <decorator:getProperty property="page.ads-logo"/>
            <%}else if (currentC.equals("b") ) {%>
                <decorator:getProperty property="page.feedback"/>
            <%} else if (currentC.equals("t") || currentC.equals("tvmc")) {%>
                <decorator:getProperty property="page.ads"/>
            <%} else {%>
                <decorator:getProperty property="page.ads"/>
            <%}%>
        </td>
    </tr>
    <% if ( currentC.equals("sw") || currentC.equals("swip") ) {%>
        <decorator:getProperty property="page.ads_floating"/>
    <% } %>
    <tr>
        <td>&nbsp;</td>
        <td colspan="2">
          <%--  offset  --%>
          <%if (currentC.equals("pp")) {%>
            <search:velocity template="fragments/offsetPager" command="scanpix"/>
          <% } else { %>
              <decorator:getProperty property="page.offsetPager"/>
          <% } %>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
          <search:velocity template="fragments/top3AdsBottom" command="top3Ads"/>
        </td>
        <td>&nbsp;</td>
    </tr>
     
</table>
    <decorator:getProperty property="page.verbosePager"/>
    <decorator:getProperty property="page.footer"/>
<%}%>

<decorator:getProperty property="page.map-script"/>
    </c:if>

<search:velocity template="fragments/gallup" />
</body>
</html>
