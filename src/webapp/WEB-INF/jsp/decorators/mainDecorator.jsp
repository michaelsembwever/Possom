<%@ page
        language="java"
        pageEncoding="UTF-8"
        contentType="text/html;charset=utf-8"
        %>
<%@ page import="no.schibstedsok.front.searchportal.i18n.TextMessages"%>
<%@ page import="no.schibstedsok.front.searchportal.query.run.RunningQuery" %>
<%@ page import="no.schibstedsok.front.searchportal.result.Modifier"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="no.schibstedsok.front.searchportal.result.Enrichment"%>
<%@ page import="com.opensymphony.module.sitemesh.Page"%>
<%@ page import="com.opensymphony.module.sitemesh.RequestConstants"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%
    final TextMessages text = (TextMessages) request.getAttribute("text");

    String currentC = "d";    //default collection
    currentC = (String) request.getAttribute("c");
    String searchType = request.getParameter("s");
    String q = (String) request.getAttribute("q");
    String contentsource = (String) request.getParameter("contentsource");
    String qURLEncoded = URLEncoder.encode(q, "utf-8");
    q = StringEscapeUtils.escapeHtml(q);
    boolean publish = null != request.getParameter("page");
    String help = request.getParameter("help");
    String about = request.getParameter("about");
    String ads_help = request.getParameter("ads_help");
    String smart = request.getParameter("smart");
    String box = request.getParameter("box");
    String toolbar = request.getParameter("toolbar");
    String tradedoubler = request.getParameter("td");
    String ss = request.getParameter("ss");
    String ssr = request.getParameter("ssr");

    List enrichments = (List) request.getAttribute("enrichments");
    int enrichmentSize = enrichments.size();

    Page siteMeshPage = (Page) request.getAttribute(RequestConstants.PAGE);

    RunningQuery query = (RunningQuery) request.getAttribute("query");
    List sources = query.getSources();
    Integer hits = (Integer) query.getNumberOfHits("defaultSearch");
    Integer hits_int = (Integer) query.getNumberOfHits("globalSearch");
    Integer hits_w_int = (Integer) query.getNumberOfHits("whiteYelloweSourceNavigator");
    Integer hits_y_int = (Integer) query.getNumberOfHits("whiteYelloweSourceNavigator");

    int no_hits = 0;
    int int_hits = 0;
    int int_w_hits = 0;
    int int_y_hits = 0;

    if (hits != null) {
        no_hits = hits.intValue();
    }
    if (hits_int != null) {
        int_hits = hits_int.intValue();
    }
    if (hits_w_int != null) {
        int_w_hits = hits_w_int.intValue();
    }
    if (hits_y_int != null) {
        int_y_hits = hits_y_int.intValue();
    }

    String searchButton = "../tradedoubler/searchbox/button-sesam-long.png";
    if (currentC.equals("y")) searchButton = "../tradedoubler/searchbox/button-company.png";
    else if (currentC.equals("w")) searchButton = "../tradedoubler/searchbox/button-person.png";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title><% if((q != null) && (!q.equals(""))){ %><%=q%> - <%}%>Sesam</title>
    <link media="screen" href="../css/decorator-style.css" rel="stylesheet" type="text/css" />
    <link media="print" href="../css/print-style.css" rel="stylesheet" type="text/css" />
    <link rel="icon" href="../favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="../favicon.ico" type="image/x-icon" />
    <script type='text/javascript' language='JavaScript' src='../javascript/common.js'></script>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>


<body onload="<%if (currentC.equals("y") || currentC.equals("yip") || currentC.equals("w") || currentC.equals("wip")) {%>init();<%}%>">

    <%-- sitesearch --%>
    <% if (currentC.equals("d") && "ds".equals(ss) ||
            currentC.equals("d") && "di".equals(ss) ||
            currentC.equals("d") && "pr".equals(ss) ||
            currentC.equals("d") && "im".equals(ss) ||
            currentC.equals("d") && "nrk".equals(ss) ||
            currentC.equals("d") && "af".equals(ss) ||
            currentC.equals("d") && "fv".equals(ss) ||
            currentC.equals("d") && "aa".equals(ss) ||
            currentC.equals("d") && "bt".equals(ss) ||
            currentC.equals("d") && "it".equals(ss)) { %>


        <div id="frame">
            <div id="header">
                <% if ("ds".equals(ss)) { %>
                    <a href="http://www.dinside.no"><img id="sitelogo" src="../images/sitesearch/aller/dinside/logo.gif" alt="Dinside logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" style="padding-bottom: 11px;" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.dinside.no/data/">Data</a><span>|</span></li>
                        <li><a href="http://www.dinside.no/reise/">Reise</a><span>|</span></li>
                        <li><a href="http://www.dinside.no/motor/">Motor</a><span>|</span></li>
                        <li><a href="http://www.dinside.no/okonomi/">økonomi</a><span>|</span></li>
                        <li><a href="http://www.dinside.no/jobb/">Jobb</a><span>|</span></li>
                        <li><a href="http://www.dinside.no/bolig/">Bolig</a></li>
                    </ul>
                <% } else if ("di".equals(ss)) { %>
                    <a href="http://www.digi.no"><img id="sitelogo" src="../images/sitesearch/aller/digi/logo.gif" alt="Digi logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" style="padding-bottom: 7px;" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.digi.no/bed_tek/">Bedriftsteknologi</a><span>|</span></li>
                        <li><a href="http://www.digi.no/pers_tek/">Personlig teknologi</a><span>|</span></li>
                        <li><a href="http://www.digi.no/resultater/">Resultater/finans</a></li>
                    </ul>
                <% } else if ("pr".equals(ss)) { %>
                    <a href="http://www.propaganda-as.no/"><img id="sitelogo" src="../images/sitesearch/aller/propaganda/logo.gif" alt="Propaganda logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.propaganda-as.no/emneomrader/">Sist uke</a><span>|</span></li>
                        <li><a href="http://www.propaganda-as.no/emneomrader/reklame/">Reklame</a><span>|</span></li>
                        <li><a href="http://www.propaganda-as.no/emneomrader/media/">Media</a><span>|</span></li>
                        <li><a href="http://www.propaganda-as.no/emneomrader/prinfo/">PR/info</a><span>|</span></li>
                        <li><a href="http://www.propaganda-as.no/jobb/">Jobb</a><span>|</span></li>
                        <li><a href="http://www.propaganda-as.no/emneomrader/design/">Design</a></li>
                    </ul>
                <% } else if ("im".equals(ss)) { %>
                    <a href="http://www.imarkedet.no/"><img id="sitelogo" src="../images/sitesearch/aller/imarkedet/logo.gif" alt="iMarkedet logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" style="padding-bottom: 13px;" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.imarkedet.no/anyhet/">Nyheter</a><span>|</span></li>
                        <li><a href="http://www.imarkedet.no/analyse/">Analyser</a><span>|</span></li>
                        <li><a href="http://www.imarkedet.no/unoterte/">Unoterte</a><span>|</span></li>
                        <li><a href="http://www.imarkedet.no/itinternett/">IT-markedet</a><span>|</span></li>
                        <li><a href="http://www.imarkedet.no/shippingoffshore/">Shipping/offshore</a></li>
                    </ul>
                <% } else if ("it".equals(ss)) { %>
                    <a href="http://www.itavisen.no/"><img id="sitelogo" src="../images/sitesearch/aller/itavisen/logo.gif" alt="Itavisen logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.itavisen.no/nyheter/">Nyheter</a><span>|</span></li>
                        <li><a href="http://www.itavisen.no/tester/">Tester</a><span>|</span></li>
                        <li><a href="http://www.itavisen.no/spill/">Spill</a><span>|</span></li>
                        <li><a href="http://www.itavisen.no/bransjen/">IT-bransjen</a></li>
                    </ul>
                <% } else if ("nrk".equals(ss)) { %>
                    <a href="http://www.nrk.no/"><img id="sitelogo" src="../images/sitesearch/nrk/logo.gif" alt="Nrk logo" title="Til forsida på NRK.no" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.nrk.no/nyheter/">Nyheter</a><span>|</span></li>
                        <li><a href="http://www.nrk.no/sport/">Sport</a><span>|</span></li>
                        <li><a href=" http://www.nrk.no/underholdning/">Undeholdning</a><span>|</span></li>
                        <li><a href="http://www.nrk.no/musikk/">Musikk</a><span>|</span></li>
                        <li><a href="http://www.nrk.no/p3/">P3</a><span>|</span></li>
                        <li><a href="http://www.nrk.no/barn/">Barn</a><span>|</span></li>
                        <li><a href="http://www.nrk.no/tv/">TV</a><span>|</span></li>
                        <li><a href="http://www.nrk.no/radio/">Radio</a><span>|</span></li>
                        <li><a href="http://www7.nrk.no/nrkplayer/default.aspx?Hovedkategori_id=2">Nett-TV</a><span>|</span></li>
                        <li><a href="http://www.nrk.no/tjenester/nrk_nettradio/3220264.html?kanal=p1">Nettradio</a></li>
                    </ul>
                <% } else if ("af".equals(ss)) { %>
                    <a href="http://www.aftenposten.no/"><img id="sitelogo" src="../images/sitesearch/aftenposten/logo.gif" alt="Aftenposten logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.aftenposten.no/nyheter/iriks/">Innenriks</a><span>|</span></li>
                        <li><a href="http://www.aftenposten.no/nyheter/uriks/">Utenriks</a><span>|</span></li>
                        <li><a href="http://www.aftenposten.no/nyheter/okonomi/">&#216;konomi</a><span>|</span></li>
                        <li><a href="http://debatt.aftenposten.no/Group.asp">Meninger & Debatt</a><span>|</span></li>
                        <li><a href="http://www.aftenposten.no/nyheter/sport/">Sport</a><span>|</span></li>
                        <li><a href="http://forbruker.no/">Forbruker.no</a></li>
                    </ul>
                <% } else if ("fv".equals(ss)) { %>
                    <a href="http://www.fedrelandsvennen.no/"><img id="sitelogo" src="../images/sitesearch/fevennen/logo.gif" alt="Aftenposten logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.fedrelandsvennen.no/">Hovedsiden</a><span>|</span></li>
                        <li><a href="http://www.fedrelandsvennen.no/nyheter/">Nyheter</a><span>|</span></li>
                        <li><a href="http://www.fedrelandsvennen.no/sport/">Sport</a><span>|</span></li>
                        <li><a href="http://fotball.fvn.no/">Fotball</a><span>|</span></li>
                        <li><a href="http://www.fedrelandsvennen.no/kulturpuls/">Kulturpuls</a><span>|</span></li>
                        <li><a href="http://www.fedrelandsvennen.no/vi_og_vart/">Vi & v&#229;rt</a><span>|</span></li>
                        <li><a href="http://www.fedrelandsvennen.no/meninger/">Meninger</a><span>|</span></li>
                        <li><a href="http://www.fedrelandsvennen.no/nyttig/varet/">V&#230;ret</a></li>
                    </ul>
                <% } else if ("aa".equals(ss)) { %>
                    <a href="http://www.adressa.no/"><img id="sitelogo" src="../images/sitesearch/adresseavisen/logo.gif" alt="Adresseavisen logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.adressa.no/nyheter/">Nyheter</a><span>|</span></li>
                        <li><a href="http://www.adressa.no/nyheter/trondheim/">Trondheim</a><span>|</span></li>
                        <li><a href="http://www.adressa.no/kultur/">Kultur</a><span>|</span></li>
                        <li><a href="http://www.adressa.no/sport/">Sport</a><span>|</span></li>
                        <li><a href="http://fotball.adressa.no/">Fotball</a><span>|</span></li>
                        <li><a href="http://www.adressa.no/forbruker/">Forbruker</a><span>|</span></li>
                        <li><a href="http://www.adressa.no/forbruker/sexogsamliv/">Sex og samliv</a><span>|</span></li>
                        <li><a href="http://www.adressa.no/forbruker/reiseliv/">Reiseliv</a><span>|</span></li>
                        <li><a href="ttp://www.adressa.no/forbruker/bil/">Bil</a><span>|</span></li>
                        <li><a href="http://www.adressa.no/forbruker/digital/">Teknologi</a></li>
                    </ul>
                <% } else if ("bt".equals(ss)) { %>
                    <a href="http://www.bt.no/"><img id="sitelogo" src="../images/sitesearch/bt/logo.png" alt="Bergens Tidende logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://www.bt.no/lokalt">Lokalt</a><span>|</span></li>
                        <li><a href="http://www.bt.no/innenriks">Innenriks</a><span>|</span></li>
                        <li><a href="http://www.bt.no/utenriks">Utenriks</a><span>|</span></li>
                        <li><a href="http://www.bt.no/sport">Sport</a><span>|</span></li>
                        <li><a href="http://www.bt.no/kultur">Kultur</a><span>|</span></li>
                        <li><a href="http://www.bt.no/okonomi">&#216;konomi</a><span>|</span></li>
                        <li><a href="http://www.bt.no/bergenpuls">Bergenpuls</a><span>|</span></li>
                        <li><a href="http://www.bt.no/forbruker">Forbruker</a><span>|</span></li>
                        <li><a href="http://www.bt.no/kamera">Webkamera</a></li>
                    </ul>
                <% } else if ("sa".equals(ss)) { %>
                    <a href="http://aftenbladet.no/"><img id="sitelogo" src="../images/sitesearch/sa/logo.gif" alt="Stavanger Aftenblad logo" /></a>
                    <a href="../"><img src="../images/sitesearch/aller/sesam.gif" alt="Sesam logo" /></a>
                    <ul id="sitelinks">
                        <li><a href="http://aftenbladet.no/">Hovedsiden</a><span>|</span></li>
                        <li><a href="http://aftenbladet.no/lokalt/">Lokalt</a><span>|</span></li>
                        <li><a href="http://aftenbladet.no/innenriks/">Innenriks</a><span>|</span></li>
                        <li><a href="http://aftenbladet.no/utenriks/">Utenriks</a><span>|</span></li>
                        <li><a href="http://aftenbladet.no/debatt/">Debatt</a><span>|</span></li>
                        <li><a href="http://aftenbladet.no/vaeret/">Været</a><span>|</span></li>
                        <li><a href="http://fotball.aftenbladet.no/">Fotball</a><span>|</span></li>
                        <li><a href="http://aftenbladet.no/sport/">Sport</a><span>|</span></li>
                        <li><a href="http://aftenbladet.no/kultur/">Kultur</a><span>|</span></li>
                        <li><a href="http://kundesenter.aftenbladet.no/">Kundesenter</a></li>
                    </ul>
                <% } %>
                <decorator:getProperty property="page.search-bar"/>

            </div>

            <div id="content_ss">
                <div id="globalmenu_table"><img src="../images/pix.gif" width="1" height="6" alt="" /></div>
                <div id ="content_top">
                    <dl>
                        <dt>
                            <span id="sitename">
                                <% if ("ds".equals(ssr)) { %> Dinside:
                                <% } else if ("di".equals(ssr)) { %> Digi:
                                <% } else if ("pr".equals(ssr)) { %> Propaganda:
                                <% } else if ("it".equals(ssr)) { %> Itavisen:
                                <% } else if ("im".equals(ssr)) { %> iMarkedet:
                                <% } else if ("nrk".equals(ssr)) { %> NRK:
                                <% } else if ("af".equals(ssr)) { %> Aftenposten:
                                <% } else if ("fv".equals(ssr)) { %> F&#230;drelandsvennen:
                                <% } else if ("aa".equals(ssr)) { %> Adresseavisen:
                                <% } else if ("bt".equals(ssr)) { %> Bergens Tidende:
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
                    <% if (!"d".equals(ssr)) {%>
                        <decorator:getProperty property="page.fast-results"/>
                    <% } else { %>
                        <%-- Display enrichments in order --%>
                        <% if ( enrichmentSize > 1 ) { %>
                           <%
                               for (Iterator i = enrichments.iterator(); i.hasNext();) {
                                   Enrichment ee = (Enrichment) i.next();
                                   if (ee.getAnalysisResult() >= 85) {
                                       String el = siteMeshPage.getProperty("page." + ee.getName());
                           %>
                           <%= el == null ? "" : el %>
                           <% } } %>
                            <%--  Shows the 3 first hits if more than 1 enrichment  --%>
                              <decorator:getProperty property="page.fast-results-norwegian_part1"/>
                               <%
                                   for (Iterator i = enrichments.iterator(); i.hasNext();) {
                                       Enrichment ee = (Enrichment) i.next();

                                       if (ee.getAnalysisResult() < 85) {
                                           String el = siteMeshPage.getProperty("page." + ee.getName());
                               %>
                               <%= el == null ? "" : el %>
                               <% } } %>
                                <%--  Shows the 7 next hits after the second/third enrichments  --%>
                                  <decorator:getProperty property="page.fast-results-norwegian_part2"/>
                        <% } else { %>  <%-- one or zero enrichment --%>
                            <% if (enrichmentSize == 1) { %>
                                <% if (no_hits > 0) { %>
                                    <%
                                          Enrichment enrichment = (Enrichment) enrichments.get(0);
                                          String enrichment1 = siteMeshPage.getProperty("page." + enrichment.getName());{
                                      } %>
                                    <% if (enrichment.getAnalysisResult() >= 85) { %>
                                            <%= enrichment1 == null ? "" : enrichment1 %>
                                              <decorator:getProperty property="page.fast-results"/>
                                    <% } else { %>
                                             <decorator:getProperty property="page.fast-results-norwegian_part1"/>
                                           <%= enrichment1 == null ? "" : enrichment1 %>
                                             <decorator:getProperty property="page.fast-results-norwegian_part2"/>
                                    <%  } %>
                                <% } else { %>
                                    <%
                                          Enrichment enrichment = (Enrichment) enrichments.get(0);
                                          String enrichment1 = siteMeshPage.getProperty("page." + enrichment.getName());{
                                      } %>
                                    <%= enrichment1 == null ? "" : enrichment1 %>
                                <% } %>
                            <% } else { %>  <%-- Display enrichments in order --%>
                                <% if (no_hits > 0) { %>
                                    <%--  shows the result as usual if 1 or less enrichments  --%>
                                      <decorator:getProperty property="page.fast-results"/>
                                <% } else if(!q.trim().equals("")){%>
                                      <decorator:getProperty property="page.noHits"/>
                                <% } %>
                            <% } %>
                        <% } %>
                    <% } %>
                </div>

                <div id="content_right_ss">
                    <decorator:getProperty property="page.ads"/>
                </div>
            </div>

            <div id="footer_ss">
                <decorator:getProperty property="page.verbosePager"/>
            </div>
        </div>


    <% } else { %>

<decorator:getProperty property="page.search-bar"/>

<table border="0" cellspacing="0" cellpadding="0" id="body_table">
    <%if (q.trim().equals("")) {%>
    <tr>
        <td class="cell_one">&nbsp;</td>
        <td class="cell_three">&nbsp;</td>
        <td class="cell_four">&nbsp;</td>
    </tr>
    <% }else{ %>
    <tr>
        <td class="cell_one"><span class="pad_5l">Sorter trefene dine</span></td>
        <td class="cell_three"><decorator:getProperty property="page.middle-bar"/></td>
        <td class="cell_four"><decorator:getProperty property="page.greybar_ad"/></td>
    </tr>
    <tr>
	<td colspan="3"><img src="../images/pix.gif" width="100%" height="1" class="lightdots" alt="" /></td>
    </tr>
    <tr>
	<td><img src="../images/pix.gif" border="0" width="204" height="12" /></td>
	<td><img src="../images/pix.gif" border="0" width="100%" height="12" /></td>
	<td><img src="../images/pix.gif" border="0" width="204" height="12" /></td>
    </tr>
    <% } %>

    <%if (q.trim().equals("") && currentC.equals("m")) {%>
	<tr>
	    <td colspan="3">
		<decorator:getProperty property="page.media-Intropage"/>	
	    </td>
	</tr>
    <% } %>

    <tr>
	<%if (q.trim().equals("") && !currentC.equals("m")) {%>

	<%}else if (currentC.equals("m") || currentC.equals("d") || !q.trim().equals("")) {%>
        <td class="cell_one" valign="top">

            <%if (currentC.equals("y") || currentC.equals("yip")) {%>
                <table border="0" cellspacing="0" cellpadding="0" class="leftbar_table">
        		    <% int i=0; %>
                    <% for (Iterator iterator = sources.iterator(); iterator.hasNext();) {
                        Modifier e = (Modifier) iterator.next();
                        ++i;
                    %>
                    <% if(i!=1) {%>
                            <tr>
                                <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td>
                            </tr>
                    <% } %>
                        <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#F3F3F3';" onclick='strepRollover("?q=<%=qURLEncoded%>&amp;<%=query.getSourceParameters(e.getName())%>");'>
                            <td class="nav_pad_icon">
                                <img <% if (e.getName().startsWith("Persons")) { %> src="../images/menu/person.gif" <% } else if (e.getName().startsWith("Netts")) { %> src="../images/menu/nettsok.gif" <% } %> class="nav_icon" align="left" alt="" />
                                <a href="?q=<%=qURLEncoded%>&amp;<%=query.getSourceParameters(e.getName())%>" onclick="return strep(this);"><%= e.getName() %></a>
                            </td>
                            <td class="nav_pad" align="right"><%=text.getMessage("numberFormat", new Integer(e.getCount())) %></td>
                        </tr>
                    <%}%>
                </table>
                <decorator:getProperty property="page.companies-nav"/>
            <%}else if (currentC.equals("w") || currentC.equals("wip")) {%>
                <table border="0" cellspacing="0" cellpadding="0" class="leftbar_table">
		            <% int i=0; %>
                    <% for (Iterator iterator = sources.iterator(); iterator.hasNext();) {
                        Modifier e = (Modifier) iterator.next();
		                ++i;
                    %>
                    <% if(i!=1) {%>
                            <tr>
                                <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td>
                            </tr>
                    <% } %>
                    <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#F3F3F3';" onclick='strepRollover("?q=<%=qURLEncoded%>&amp;<%=query.getSourceParameters(e.getName())%>");'>
                        <td class="nav_pad_icon">
                            <img <% if (e.getName().startsWith("Bedriftss")) { %> src="../images/menu/bedrift.gif" <% } else if (e.getName().startsWith("Netts")) { %> src="../images/menu/nettsok.gif" <% } %> class="nav_icon" align="left" alt="" />
                            <a href="?q=<%=qURLEncoded%>&amp;<%=query.getSourceParameters(e.getName())%>" onclick="return strep(this);"><%= e.getName() %></a>
                        </td>
                        <td class="nav_pad" align="right"><%=text.getMessage("numberFormat", new Integer(e.getCount())) %></td>
                    </tr>
                    <%}%>
                </table>
                <% if (currentC.equals("wip")) {%>
                    <decorator:getProperty property="page.infopage-nav"/>
                    <decorator:getProperty property="page.persons-nav"/>
                <% } else { %>
                    <decorator:getProperty property="page.persons-nav"/>
                <% } %>
            <%}else if (currentC.equals("m")) {%>
                <table border="0" cellspacing="0" cellpadding="0" class="leftbar_table">
		            <% int i=0; %>
                    <% for (Iterator iterator = sources.iterator(); iterator.hasNext();) {
                        Modifier e = (Modifier) iterator.next();
                        ++i;
                                %>
                        <% if(i!=1) {%>
                                <tr>
                                    <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td>
                                </tr>
                        <% } %>
                        <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#F3F3F3';" onclick='strepRollover("?q=<%=qURLEncoded%>&amp;<%=query.getSourceParameters(e.getName())%>");'>
                            <td class="nav_pad_icon">
                                <img src="../images/menu/nettsok.gif" class="nav_icon" align="left" alt="" />
                                <a href="?q=<%=qURLEncoded%>&amp;<%=query.getSourceParameters(e.getName())%>" onclick="return strep(this);"><%= e.getName() %></a>
                            </td>
                            <td class="nav_pad" align="right"><%=text.getMessage("numberFormat", new Integer(e.getCount())) %></td>
                        </tr>
                    <%}%>
                </table>
                <decorator:getProperty property="page.newsSearchNavigator" />
                <decorator:getProperty property="page.media-collection-nav"/>
            <% }else{ %>

                <table border="0" cellspacing="0" cellpadding="0" class="leftbar_table">
		            <% int i = 0; %>
                    <% for (Iterator iterator = sources.iterator(); iterator.hasNext();) {
                        Modifier e = (Modifier) iterator.next();
                        if ( (currentC.equals("d") && !e.getName().equals("Nettsøk")) || (currentC.equals("d") && searchType != null && searchType.equals("g")) || currentC.equals("p")) {
                            ++i;
                    %>

                            <% if(i!=1) {%>
                                <tr>
                                    <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td>
                                </tr>
                            <% } %>
                            <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#F3F3F3';" onclick='strepRollover("?q=<%=qURLEncoded%>&amp;<%=query.getSourceParameters(e.getName())%>");'>
                                <td class="nav_pad_icon">
                                    <img <% if (e.getName().startsWith("Bedrifts")) { %> src="../images/menu/bedrift.gif" <% } else if (e.getName().startsWith("Persons")) { %> src="../images/menu/person.gif" <% } else if (e.getName().startsWith("Bildes")) { %> src="../images/menu/bilde.gif" <% } else if (e.getName().equals("Verden")) { %> src="../images/menu/nettsok.gif"  <% } else if (e.getName().startsWith("Nyhetss")) { %> src="../images/menu/nyheter.gif" <% } else if (e.getName().startsWith("Netts")) { %> src="../images/menu/nettsok.gif" <% } %> class="nav_icon" align="left" alt="" />
                                    <a href="?q=<%=qURLEncoded%>&amp;<%=query.getSourceParameters(e.getName())%>" onclick="return strep(this);"><%= e.getName() %></a>
                                </td>
                                <td class="nav_pad" align="right"><%=text.getMessage("numberFormat", new Integer(e.getCount())) %></td>
                            </tr>
                        <%}%>
                    <%}%>
                </table>

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
        </td>
	<% } %>

        <%if (q.trim().equals("")) {%>
            <td valign="top" colspan="3">
        <% }else if (!currentC.equals("y") && !currentC.equals("yip") && !currentC.equals("w") && !currentC.equals("wip")) {%>
            <td class="cell_three" valign="top">
        <% }else{ %>
            <td class="cell_three" valign="top" colspan="2">
        <%}%>
                <%--<decorator:getProperty property="page.search-bar"/>--%>
                <!-- Magic -->
                <%if (currentC.equals("d")) {%>


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

			<table>
			<tr>
                        <decorator:getProperty property="page.spellcheck"/>
                         <td id="result_container">
                             <% if ("n".equals(searchType)) { %>
                                    <% if (no_hits > 0) { %>
                                        <decorator:getProperty property="page.fast-results"/>
                                    <%  } else if(!q.trim().equals("")){ %>
                                        <decorator:getProperty property="page.noHits"/>
                                    <% } %>
                             <% } else if ("g".equals(searchType)) { %>
                                    <decorator:getProperty property="page.global-results"/>
                             <%  } else { %>

                                <decorator:getProperty property="page.main_ads"/>
                             <!--
                                  Enrichments debugging:

                                   <%
                                     for (Iterator i = enrichments.iterator(); i.hasNext();) {
                                         Enrichment ee = (Enrichment) i.next();

                                         %>
                                    <%= ee.getName() %>
                                    <%= ee.getAnalysisResult() %>
                                    <%
                                     }
                                   %>
                             -->

                              <decorator:getProperty property="page.globalSearchTips" />

                                <%-- Display enrichments in order --%>
                                <% if ( enrichmentSize > 1 ) { %>


                                   <%
                                     for (Iterator i = enrichments.iterator(); i.hasNext();) {
                                         Enrichment ee = (Enrichment) i.next();

                                         if (ee.getAnalysisResult() >= 85) {
                                             String el = siteMeshPage.getProperty("page." + ee.getName());
                                   %>
                                   <%= el == null ? "" : el %>
                                   <% } } %>

                                    <%--  Shows the 3 first hits if more than 1 enrichment  --%>
                                    <decorator:getProperty property="page.fast-results-norwegian_part1"/>

                                       <%
                                         for (Iterator i = enrichments.iterator(); i.hasNext();) {
                                             Enrichment ee = (Enrichment) i.next();

                                             if (ee.getAnalysisResult() < 85) {
                                                 String el = siteMeshPage.getProperty("page." + ee.getName());
                                       %>
                                       <%= el == null ? "" : el %>
                                       <% } } %>

                                        <%--  Shows the 7 next hits after the second/third enrichments  --%>
                                        <decorator:getProperty property="page.fast-results-norwegian_part2"/>


                                <% } else { %>  <%-- one or zero enrichment --%>

                                        <% if (enrichmentSize == 1) { %>

                                            <% if (no_hits > 0) { %>
                                                    <%
                                                        Enrichment enrichment = (Enrichment) enrichments.get(0);
                                                        String enrichment1 = siteMeshPage.getProperty("page." + enrichment.getName());{
                                                    } %>
                                                    <% if (enrichment.getAnalysisResult() >= 85) { %>
                                                            <%= enrichment1 == null ? "" : enrichment1 %>
                                                            <decorator:getProperty property="page.fast-results"/>
                                                    <% } else { %>
                                                           <decorator:getProperty property="page.fast-results-norwegian_part1"/>
                                                           <%= enrichment1 == null ? "" : enrichment1 %>
                                                           <decorator:getProperty property="page.fast-results-norwegian_part2"/>
                                                    <%  } %>
                                            <% } else { %>
                                                    <%
                                                        Enrichment enrichment = (Enrichment) enrichments.get(0);
                                                        String enrichment1 = siteMeshPage.getProperty("page." + enrichment.getName());{
                                                    } %>
                                                    <%= enrichment1 == null ? "" : enrichment1 %>
                                            <% } %>


                                        <% } else { %>  <%-- Display enrichments in order --%>

                                                <% if (no_hits > 0) { %>
                                                    <%--  shows the result as usual if 1 or less enrichments  --%>
                                                    <decorator:getProperty property="page.fast-results"/>
                                                <% } else if(!q.trim().equals("")){%>
                                                    <decorator:getProperty property="page.noHits"/>
                                                <% } %>
                                        <% } %>
                                    <% } %>

                            <% } %>
                        </td>
			</tr>
			</table>
                        <% } %>  <%-- Sok smart --%>
                <%}%>



                <%if (currentC.equals("m")) {%>
                    <decorator:getProperty property="page.main_ads"/>
                    <decorator:getProperty property="page.media-collection-results"/>
                <%}%>


                <%if (currentC.equals("yip")) {%>
                <decorator:getProperty property="page.infopage"/>
                <%}%>
                <%if (currentC.equals("wip")) {%>
                <decorator:getProperty property="page.infopage"/>
                <%}%>

                <!-- Companies -->
                <%if (currentC.equals("y")) {%>
                <%if (request.getParameter("companyId") != null) {%>
                <%} else {%>
                <decorator:getProperty property="page.pseudo-local"/>
                <decorator:getProperty property="page.companies-results"/>
                <%}%>
                <%}%>

                <!-- Persons -->
                <%if (currentC.equals("w")) {%>
                <decorator:getProperty property="page.persons-results"/>
                <%}%>

                <%if (currentC.equals("p")) {%>
                <decorator:getProperty property="page.picture-results"/>
                <%}%>

            </td>

	    <%if (q.trim().equals("")) {%>

            <%}else if ( currentC.equals("d") || currentC.equals("m") || currentC.equals("g")) {%>
                <td class="cell_four" valign="top" width="225">
                    <decorator:getProperty property="page.ads"/>
                </td>
            <%}else if (currentC.equals("p")) {%>
                <td class="cell_four" valign="bottom" width="225">
                    <decorator:getProperty property="page.ads"/>
                </td>
            <%}%>

    </tr>
</table>

<%--  footer  --%>
<%if (q==null||!q.trim().equals("")||"m".equals(currentC)) {%>
<decorator:getProperty property="page.verbosePager"/>
<%}%>

<decorator:getProperty property="page.footer"/>

<%}%>

<decorator:getProperty property="page.map-script"/>

<!-- start Gallup -->
<script type='text/javascript' language='JavaScript' src='../javascript/tmv11.js'></script>
<script type="text/javascript" language="JavaScript">
<!--
var tmsec = new Array(2);
tmsec[0]="tmsec=sesam";
<% if (currentC.equals("d") && "g".equals(searchType)) { %> tmsec[1]="tmsec=sesamsok_verden";
<% } else if (currentC.equals("d")) { %> tmsec[1]="tmsec=sesamsok";
<% } else if (currentC.equals("m") && "Norske nyheter".equals(contentsource)) { %> tmsec[1]="tmsec=nyhetssok_norske";
<% } else if (currentC.equals("m") && "Internasjonale nyheter".equals(contentsource)) { %> tmsec[1]="tmsec=nyhetssok_internasjonale";
<% } else if (currentC.equals("m") && "Nordiske nyheter".equals(contentsource)) { %> tmsec[1]="tmsec=nyhetssok_nordiske";
<% } else if (currentC.equals("m") && "Mediearkivet".equals(contentsource)) { %> tmsec[1]="tmsec=nyhetssok_papir";
<% } else if (currentC.equals("m")) { %> tmsec[1]="tmsec=nyhetssok";
<% } else if (currentC.equals("y")) { %> tmsec[1]="tmsec=bedriftssok";
<% } else if (currentC.equals("yip")) { %> tmsec[1]="tmsec=bedriftssok_info";
<% } else if (currentC.equals("w")) { %> tmsec[1]="tmsec=personsok";
<% } else if (currentC.equals("wip")) { %> tmsec[1]="tmsec=personsok_info";
<% } else if (currentC.equals("p")) { %> tmsec[1]="tmsec=bildesok";
<% } %>
getTMqs('','', 'sesam_no', 'no', 'iso-8859-15', tmsec);
//-->
</script>
<% if (currentC.equals("d") && "g".equals(searchType)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=sesamsok_verden" alt="" /></noscript>
<% } else if (currentC.equals("d")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=sesamsok" alt="" /></noscript>
<% } else if (currentC.equals("m") && "Norske nyheter".equals(contentsource)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_norske" alt="" /></noscript>
<% } else if (currentC.equals("m") && "Internasjonale nyheter".equals(contentsource)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_internasjonale" alt="" /></noscript>
<% } else if (currentC.equals("m") && "Nordiske nyheter".equals(contentsource)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_nordiske" alt="" /></noscript>
<% } else if (currentC.equals("m") && "Mediearkivet".equals(contentsource)) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok_papir" alt="" /></noscript>
<% } else if (currentC.equals("m")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=nyhetssok" alt="" /></noscript>
<% } else if (currentC.equals("y")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=bedriftssok" alt="" /></noscript>
<% } else if (currentC.equals("yip")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=bedriftssok_info" alt="" /></noscript>
<% } else if (currentC.equals("w")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=personsok" alt="" /></noscript>
<% } else if (currentC.equals("wip")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=personsok_info" alt="" /></noscript>
<% } else if (currentC.equals("p")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=bildesok" alt="" /></noscript>
<% } %>

<!-- end gallup -->

</body>
</html>
