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
<%@ page import="no.schibstedsok.searchportal.mode.config.SiteConfiguration"%>
<%@ page import="no.schibstedsok.searchportal.view.config.SearchTab" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>
<%
final TextMessages text = (TextMessages) request.getAttribute("text");
final Site site = (Site)request.getAttribute(Site.NAME_KEY);
final SearchTab tab = (SearchTab)request.getAttribute("tab");

String currentC = "d";    //default collection
currentC = (String) request.getAttribute("c");
String q = (String) request.getAttribute("q");
final String contentsource = (String) request.getParameter("contentsource");
final String newscountry = (String) request.getParameter("newscountry");
String locale = site.getLocale().toString();
final String qURLEncoded = URLEncoder.encode(q, "utf-8");
q = (String) request.getAttribute("queryHTMLEscaped");
final boolean publish = null != request.getParameter("page");
final String help = request.getParameter("help");
final String about = request.getParameter("about");
final String ads_help = request.getParameter("ads_help");
final String smart = request.getParameter("smart");
final String box = request.getParameter("box");
final String toolbar = request.getParameter("toolbar");
final String tradedoubler = request.getParameter("td");
final String ss = request.getParameter("ss");
final String ssr = request.getParameter("ssr");

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
    <title><% if((q != null) && (!q.equals(""))){ %><%=q%> - <%}%>Sesam</title>
    <link media="screen" href="../css/<c:out value='${startTime}'/>/decorator-style.css" rel="stylesheet" type="text/css" />
    <link media="screen" href="../css/<c:out value='${startTime}'/>/sitesearch.css" rel="stylesheet" type="text/css" />
    <link media="screen" href="../css/<c:out value='${startTime}'/>/ps.css" rel="stylesheet" type="text/css" />
    <c:if test='${!(empty tab.rssResultName)}'>
    <link rel="alternate" type="application/rss+xml" title="RSS - Sesam" href="<c:out value='${request.requestURL}' escapeXml="false"/>?<c:out value='${request.queryString}' escapeXml="false"/>&output=rss" />
    </c:if>
    <c:forEach var="t" items="${tab.ancestry}">
        <link media="screen" href="../css/<c:out value='${startTime}'/>/tab/<c:out value='${t.id}'/>.css" rel="stylesheet" type="text/css" />
    </c:forEach>
    <c:forEach var="css" items="${tab.css}">
        <link media="screen" href="../css/<c:out value='${startTime}'/>/tab/<c:out value='${css}'/>.css" rel="stylesheet" type="text/css" />
    </c:forEach>

    <link media="print" href="../css/<c:out value='${startTime}'/>/print-style.css" rel="stylesheet" type="text/css" />
    <link rel="icon" href="../favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="../favicon.ico" type="image/x-icon" />
    <script type='text/javascript' language='JavaScript' src='../javascript/common.js?x=1'></script>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>


<body onload="<%if (currentC.equals("y") || currentC.equals("yg") || currentC.equals("yipticker") || currentC.equals("w") || currentC.equals("sw") || currentC.equals("swip")) {%>init();<%} else if (currentC.equals("yip") || currentC.equals("wip")) {%>init(); checkTab();<% } %>">

    <search:velocity template="/pages/main"/>

    <c:if test="${! empty Missing_pagesmain_Template}">

    <%-- old-school sitesearch --%>
    <% if (currentC.equals("d") && (
             "ds".equals(ss) ||
             "di".equals(ss) ||
             "pr".equals(ss) ||
             "im".equals(ss) ||
             "nrk".equals(ss) ||
             "af".equals(ss) ||
             "fv".equals(ss) ||
             "aa".equals(ss) ||
             "bt".equals(ss) ||
             "sa".equals(ss) ||
             "it".equals(ss))) { %>


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

        <%if (q.trim().equals("") && (currentC.equals("y") || currentC.equals("yg") || currentC.equals("w") || currentC.equals("p") || currentC.equals("sw") || currentC.equals("b"))) {%>
	<decorator:getProperty property="page.newsearch-bar"/>
	<% }else{ %>
	<decorator:getProperty property="page.search-bar"/>
	<% } %>

<table border="0" cellspacing="0" cellpadding="0" id="body_table">
    <%if (q.trim().equals("") && !currentC.equals("l") && !currentC.equals("m") && !currentC.equals("t") && !currentC.equals("wt")) {%>
    <tr>
        <td class="cell_one">&nbsp;</td>
        <td class="cell_three">&nbsp;</td>
        <td class="cell_four">&nbsp;</td>
    </tr>
    <% }else{ %>
    <tr>
        <td class="cell_one"><span class="pad_5l"><%if (!currentC.equals("l")) { %><%=text.getMessage("naviger") %><% } %></span></td>
        <td class="cell_three">
                <%if (currentC.equals("pp") || currentC.equals("pip") || currentC.equals("pipn")) {%>
                        <search:velocity template="results/middlebar" command="scanpix"/>
                <%} else { %>        
                    <decorator:getProperty property="page.middle-bar"/>
                <% } %>
        </td>
        <td class="cell_four"><decorator:getProperty property="page.greybar_ad"/></td>
    </tr>
    <tr>
	<td colspan="3"><img src="../images/pix.gif" width="100%" height="1" class="lightdots" alt="" /></td>
    </tr>
    <tr>
	<td><img src="../images/pix.gif" border="0" width="204" height="12" alt="" /></td>
	<td><img src="../images/pix.gif" border="0" width="100%" height="12" alt="" /></td>
	<td><img src="../images/pix.gif" border="0" width="204" height="12" alt="" /></td>
    </tr>
    <% } %>

    <tr>
	<%if (q.trim().equals("") && (!currentC.equals("l") && !currentC.equals("m") && !currentC.equals("t") && !currentC.equals("wt")) ) {%>

	<%}else if ((currentC.equals("b") || currentC.equals("m") || currentC.equals("l") || currentC.equals("d")|| currentC.equals("g") || currentC.equals("pss")) || !q.trim().equals("") || currentC.equals("t") || currentC.equals("wt")) {%>
        <td class="cell_one" valign="top">
            <table border="0" cellspacing="0" cellpadding="0" class="menu">
                <c:set var="rows" value="${0}"/>
                <c:forEach var="e" items="${sources}">
                    <c:set var="hint" value="${e.navigationHint}"/>
                    <c:if test="${!empty hint && !empty hint.tab}">
                        <c:set var="rows" value="${rows +1}"/>
                        <c:set var="navUrl" value="?q=${q}&c=${hint.tab.key}${hint.urlSuffix}"/>
                        <c:if test="${rows > 1}">
                            <tr><td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td></tr>
                        </c:if>
                        <tr onclick='strepRollover("<c:out value='${navUrl}'/>");'>
                            <td class="nav_pad_icon">
                                <img src='<c:out value="../images/menu/${hint.image}"/>' class="nav_icon" align="left" alt="" />
                                <a href='<search:linkPulse url="${navUrl}" param="category:navigation;subcategory:service_left" index=""/>'
                                    onclick="return strep(this);"><c:out value="${hint.displayName}"/></a>
                            </td>
                            <td class="nav_pad"><search:text key="numberFormat" args="${e.count}"/></td>
                        </tr>
                    </c:if>
                </c:forEach>
            </table>

            <%if (currentC.equals("y") || currentC.equals("yg") || currentC.equals("yip") || currentC.equals("yipticker")) {%>

                <decorator:getProperty property="page.companies-nav"/>
            <%}else if (currentC.equals("w") || currentC.equals("wip") || currentC.equals("wipgift")) {%>
                <% if (!currentC.equals("wipgift")) {%>
                    <decorator:getProperty property="page.persons-nav"/>
                <% } %>
            <%}else if (currentC.equals("m")) {%>
                <decorator:getProperty property="page.newsSearchNavigator" />
                <decorator:getProperty property="page.media-collection-nav"/>
            <%}else if (currentC.equals("sw") || currentC.equals("swip")) {%>
                <decorator:getProperty property="page.weather-nav"/>
            <%}else if (currentC.equals("t")) {%>
                <decorator:getProperty property="page.tvSearchNavigator" />
            <%}else if (currentC.equals("wt")) {%>
                <decorator:getProperty property="page.webtvNavigator"/>
            <%}else if (currentC.equals("b")) {%>

            <% }else{ %>
                <decorator:getProperty property="page.relevantQueries" />
                <% if ("true".equals(smart)) { %>
                    <decorator:getProperty property="page.smart-nav"/>
                <% }else if ("true".equals(help)){ %>
                    <decorator:getProperty property="page.help-nav"/>
                <% }else if ("true".equals(about)){ %>
                    <decorator:getProperty property="page.about-nav"/>
                <% }else if ("true".equals(ads_help)){ %>
                    <decorator:getProperty property="page.ads_help-nav"/>
                <%}%>

            <%}%>

            <search:velocity template="navigators/scanpix" command="scanpix"/>
            <decorator:getProperty property="page.blogDateNavigation"/>

            <c:if test='${!(empty tab.rssResultName)}'>
                <decorator:getProperty property="page.rss-nav"/>
            </c:if>
        </td>
	<% } %>

        <%if (q.trim().equals("") && !currentC.equals("t") && !currentC.equals("wt")) {%>
            <td valign="top" colspan="3">
        <% }else if (!currentC.equals("y") && !currentC.equals("yip") && !currentC.equals("w") && !currentC.equals("wip")&& !currentC.equals("swip") && !currentC.equals("wipgift")) {%>
            <td class="cell_three" valign="top">
        <% }else{ %>
            <td class="cell_three" valign="top" colspan="2">
        <%}%>
                <%--<decorator:getProperty property="page.search-bar"/>--%>
                <!-- Magic -->
                <%if (currentC.equals("d") || "g".equals(currentC) || "pss".equals(currentC)) {%>


                    <%--  Header  --%>
                    <% if ("true".equals(smart)) { %>
                        <decorator:getProperty property="page.greybar_smart"/>
                    <% } else if ("true".equals(help)) { %>
                        <decorator:getProperty property="page.greybar_help"/>
                    <% } %>  <%--  Help header  --%>


                     <%--  Sok smart  --%>
                     <% if ( publish ) { %>
                        <decorator:getProperty property="page.publishing_page"/>
                     <% } else if ("true".equals(smart)) { %>
                        <decorator:getProperty property="page.smart"/>
                     <% } else if ("true".equals(help)) { %>
                         <decorator:getProperty property="page.help"/>
                     <% } else if ("true".equals(about)) { %>
                        <decorator:getProperty property="page.about"/>
                     <% } else if ("true".equals(ads_help)) { %>
                        <decorator:getProperty property="page.ads_help"/>
                     <% } else if ("true".equals(box)) { %>
                        <decorator:getProperty property="page.searchbox"/>
                     <% } else if ("true".equals(toolbar)) { %>
                        <decorator:getProperty property="page.toolbar"/>
                     <% } else if ("true".equals(tradedoubler)) { %>
                        <decorator:getProperty property="page.tradedoubler"/>
                     <% } else { %>

			<table border="0" width="100%">
			<tr>
                <decorator:getProperty property="page.spellcheck"/>
                 <td id="result_container">

                        <decorator:getProperty property="page.main_ads"/>

                     <c:choose>
                         <c:when test="${no_hits >0 || enrichmentSize >0}">

                             <!--  Enrichments on top: <c:out value="${tab.enrichmentOnTop}"/>
                              Enrichments in total: <c:out value="${tab.enrichmentLimit}"/>
                               <c:forEach var="ee" items="${enrichments}">
                                   <c:out value="${ee.name}"/>: <c:out value="${ee.analysisResult}"/>
                               </c:forEach> -->

                              <decorator:getProperty property="page.globalSearchTips" />

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
                </td>
			</tr>
            </table>
                        <% } %>  <%-- Sok smart --%>
                <%}%>



                <%if (currentC.equals("m")) {%>
                    <decorator:getProperty property="page.main_ads"/>
                    <decorator:getProperty property="page.media-collection-results"/>
                <%}%>

                <%if (currentC.equals("l")) {%>
                    <decorator:getProperty property="page.main_ads"/>
                    <decorator:getProperty property="page.media-collection-results"/>
                <%}%>


                <%if (currentC.equals("yip") || currentC.equals("yipticker")) {%>
                <decorator:getProperty property="page.infopage"/>
                <%}%>
                <%if (currentC.equals("wip") || currentC.equals("swip")) {%>
                <decorator:getProperty property="page.infopage"/>
                <%}%>

                <%if (currentC.equals("wipgift")) {%>
                <decorator:getProperty property="page.infopage"/>
                <%}%>

                <!-- Companies -->
                <%if (currentC.equals("y") || currentC.equals("yg")) {%>
                <%if (request.getParameter("companyId") != null) {%>
                <%} else {%>
                <decorator:getProperty property="page.pseudo-local"/>
                <decorator:getProperty property="page.companies-results"/>
                <%}%>
                <%}%>

                <!-- Weather -->
                <%if (currentC.equals("sw") && !q.trim().equals("")) {%>
                <decorator:getProperty property="page.weather"/>
                <%}%>

                <!-- Persons -->
                <%if (currentC.equals("w")) {%>
                <decorator:getProperty property="page.persons-results"/>
                <%}%>

                <%if (currentC.equals("p") || currentC.equals("pp") || currentC.equals("pip") || currentC.equals("pipn")) {%>
                    <div>
                        <decorator:getProperty property="page.picsearch-results"/>
                        <search:velocity template="results/scanpix" command="scanpix"/>
                        <!--search:import template="picSearch"/-->

                        <div class="clearFloat">&nbsp;</div>
                    </div>
                <%}%>

                <%if (currentC.equals("t")) {%>
                    <decorator:getProperty property="page.tv-results"/>
                <%}%>

                <%if (currentC.equals("wt")) {%>
                    <decorator:getProperty property="page.webtv-results"/>
                <%}%>

                <%if (currentC.equals("b")) {%>
                <decorator:getProperty property="page.blog-search"/>
                <%}%>

                    <%--  offset  --%>
                    <%if (q==null || !q.trim().equals("") || "m".equals(currentC) || "l".equals(currentC)) {%>
                        <%if (currentC.equals("pp")) {%>
                                <search:velocity template="results/offsetPager" command="scanpix"/>
                        <% } else { %>
                            <decorator:getProperty property="page.offsetPager"/>
                         <% } %>
                    <%}%>
               </td>

            <%if (q.trim().equals("") && !currentC.equals("t")) {%>

            <%}else if ( currentC.equals("d") || currentC.equals("m") || currentC.equals("g") || currentC.equals("pss")) {%>
                <td class="cell_four" valign="top" width="225">
                    <decorator:getProperty property="page.ads"/>
                </td>
            <%}else if ( currentC.equals("pp") || currentC.equals("pip") || currentC.equals("pipn")) {%>
                <td class="cell_four" valign="top" width="225">
                    <decorator:getProperty property="page.ads"/>

                </td>
            <%}else if (currentC.equals("p") ) {%>
                    <decorator:getProperty property="page.ads"/>
                    <decorator:getProperty property="page.ads-picsearch-logo"/>
                </td>
            <%}else if (currentC.equals("b") ) {%>
                <td class="cell_four" valign="top" width="225">
                    <decorator:getProperty property="page.feedback"/>
                </td>
            <%} else if (currentC.equals("t")) {%>
                <td class="cell_four" valign="top" width="225">
                    <decorator:getProperty property="page.tvSearchWebTv"/>
                    <decorator:getProperty property="page.ads"/>
                </td>
            <%} else if (currentC.equals("wt")) {%>
                <td class="cell_four" valign="top" width="225">
                    <decorator:getProperty property="page.ads"/>
                </td>
            <%}%>

        </tr>
    </table>

    <%--  footer  --%>
    <%if (q==null || !q.trim().equals("") || "m".equals(currentC) ) {%>
    <decorator:getProperty property="page.verbosePager"/>
    <%}%>

    <%if ("l".equals(currentC) ) {%>
    <decorator:getProperty property="page.verbosePager"/>
    <%}%>


    <decorator:getProperty property="page.footer"/>

<%}%>

    </c:if>

<decorator:getProperty property="page.map-script"/>

<!-- start Gallup (no TNS for sweden -->

<% if ( "no".equals(locale) ) { %>
<script type='text/javascript' language='JavaScript' src='../javascript/tmv11.js'></script>
<script type="text/javascript" language="JavaScript">
<!--
var tmsec = new Array(2);
tmsec[0]="tmsec=sesam";
<% if (currentC.equals("g")) { %> tmsec[1]="tmsec=sesamsok_verden";
<% } else if (currentC.equals("d")) { %> tmsec[1]="tmsec=sesamsok";
<% } else if (currentC.equals("m") && "Norge".equals(newscountry)) { %> tmsec[1]="tmsec=nyhetssok_norske";
<% } else if (currentC.equals("m") && "Internasjonale nyheter".equals(contentsource)) { %> tmsec[1]="tmsec=nyhetssok_internasjonale";
<% } else if (currentC.equals("m") && "Norden-no".equals(newscountry)) { %> tmsec[1]="tmsec=nyhetssok_nordiske";
<% } else if (currentC.equals("m") && "Mediearkivet".equals(contentsource)) { %> tmsec[1]="tmsec=nyhetssok_papir";
<% } else if (currentC.equals("m")) { %> tmsec[1]="tmsec=nyhetssok";
<% } else if (currentC.equals("l")) { %> tmsec[1]="tmsec=nyhetssok_siste";
<% } else if (currentC.equals("y")) { %> tmsec[1]="tmsec=bedriftssok";
<% } else if (currentC.equals("yip")) { %> tmsec[1]="tmsec=bedriftssok_info";
<% } else if (currentC.equals("w")) { %> tmsec[1]="tmsec=personsok";
<% } else if (currentC.equals("wip")) { %> tmsec[1]="tmsec=personsok_info";
<% } else if (currentC.equals("sw")) { %> tmsec[1]="tmsec=weathersok";
<% } else if (currentC.equals("swip")) { %> tmsec[1]="tmsec=weathersok_info";
<% } else if (currentC.equals("p")) { %> tmsec[1]="tmsec=bildesok";
<% } else if (currentC.equals("b")) { %> tmsec[1]="tmsec=bloggsok";
<% } else if (currentC.equals("n")) { %> tmsec[1]="tmsec=<search:velocity template="/pages/tns"/>";
<% } else { %> tmsec[1]="tmsec=xxx";
<% } %>
getTMqs('','', 'sesam_no', 'no', 'iso-8859-15', tmsec);
//-->
</script>
<% if (currentC.equals("g") ) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=sesamsok_verden" alt="" /></noscript>
<% } else if (currentC.equals("d")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=sesamsok" alt="" /></noscript>
<% } else if (currentC.equals("m") && "Norge".equals(newscountry)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_norske" alt="" /></noscript>
<% } else if (currentC.equals("m") && "Internasjonale nyheter".equals(contentsource)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_internasjonale" alt="" /></noscript>
<% } else if (currentC.equals("m") && "Norden-no".equals(newscountry)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_nordiske" alt="" /></noscript>
<% } else if (currentC.equals("m") && "Mediearkivet".equals(contentsource)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_papir" alt="" /></noscript>
<% } else if (currentC.equals("m")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok" alt="" /></noscript>
<% } else if (currentC.equals("l")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_siste" alt="" /></noscript>
<% } else if (currentC.equals("y")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=bedriftssok" alt="" /></noscript>
<% } else if (currentC.equals("yip")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=bedriftssok_info" alt="" /></noscript>
<% } else if (currentC.equals("w")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=personsok" alt="" /></noscript>
<% } else if (currentC.equals("wip")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=personsok_info" alt="" /></noscript>
<% } else if (currentC.equals("p")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=bildesok" alt="" /></noscript>
<% } else if (currentC.equals("b")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=bloggesok" alt="" /></noscript>
<% } else if (currentC.equals("n")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=<search:velocity template="/pages/tns"/>" alt="" /></noscript>
<% } %>

<% } %>
<!-- end gallup -->

</body>
</html>
