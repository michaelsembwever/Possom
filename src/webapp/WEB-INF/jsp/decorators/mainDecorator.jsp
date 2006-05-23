<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.StringWriter"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.opensymphony.module.sitemesh.Page"%>
<%@ page import="com.opensymphony.module.sitemesh.RequestConstants"%>
<%@ page import="com.opensymphony.module.sitemesh.util.OutputConverter"%>
<%@ page import="no.schibstedsok.front.searchportal.view.i18n.TextMessages"%>
<%@ page import="no.schibstedsok.front.searchportal.view.output.VelocityResultHandler"%>
<%@ page import="no.schibstedsok.front.searchportal.query.run.RunningQuery" %>
<%@ page import="no.schibstedsok.front.searchportal.result.Enrichment"%>
<%@ page import="no.schibstedsok.front.searchportal.result.Modifier"%>
<%@ page import="no.schibstedsok.front.searchportal.site.Site"%>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.apache.velocity.Template"%>
<%@ page import="org.apache.velocity.VelocityContext"%>
<%@ page import="org.apache.velocity.app.VelocityEngine"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ page import="no.schibstedsok.front.searchportal.result.Linkpulse"%>
<%@ page import="no.schibstedsok.front.searchportal.configuration.SiteConfiguration"%>
<%@ page import="no.schibstedsok.front.searchportal.view.config.SearchTab" %>
<%
    final TextMessages text = (TextMessages) request.getAttribute("text");
    final Site site = (Site)request.getAttribute(Site.NAME_KEY);

    String currentC = "d";    //default collection
    currentC = (String) request.getAttribute("c");
    String q = (String) request.getAttribute("q");
    final String contentsource = (String) request.getParameter("contentsource");
    final String qURLEncoded = URLEncoder.encode(q, "utf-8");
    q = StringEscapeUtils.escapeHtml(q);
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

    final RunningQuery query = (RunningQuery) request.getAttribute("query");
    final List<Modifier> sources = query.getSources();
    final Integer dHits = (Integer) query.getNumberOfHits("defaultSearch");
    final Integer gHits = (Integer) query.getNumberOfHits("globalSearch");
    final int no_hits = dHits != null && dHits > 0 
            ? dHits.intValue() 
            : gHits != null && gHits > 0 ? gHits.intValue() : 0;

    pageContext.setAttribute("no_hits", no_hits);
    
    final Linkpulse linkpulse = new Linkpulse(SiteConfiguration.valueOf(site).getProperties());

    String searchButton = "../tradedoubler/searchbox/button-sesam-long.png";
    if (currentC.equals("y")) searchButton = "../tradedoubler/searchbox/button-company.png";
    else if (currentC.equals("w")) searchButton = "../tradedoubler/searchbox/button-person.png";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title><% if((q != null) && (!q.equals(""))){ %><%=q%> - <%}%>Sesam</title>
    <link media="screen" href="../css/default.css?x=1" rel="stylesheet" type="text/css" />
    <link media="screen" href="../css/decorator-style.css?y=1" rel="stylesheet" type="text/css" />
    <link media="screen" href="../css/sitesearch.css?x=1" rel="stylesheet" type="text/css" />
    <link media="screen" href="../css/front.css?x=1" rel="stylesheet" type="text/css" />
    <link media="screen" href="../css/ps.css?x=1" rel="stylesheet" type="text/css" />
    <link media="print" href="../css/print-style.css?x=1" rel="stylesheet" type="text/css" />
    <link rel="icon" href="../favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="../favicon.ico" type="image/x-icon" />
    <script type='text/javascript' language='JavaScript' src='../javascript/common.js'></script>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>


<body onload="<%if (currentC.equals("y") || currentC.equals("yipticker") || currentC.equals("w") ) {%>init();<%} else if (currentC.equals("yip") || currentC.equals("wip")) {%>init(); checkTab();<% } %>">

    <% // sitesearch
    final VelocityEngine engine = VelocityResultHandler.getEngine(site);
    final Template template = VelocityResultHandler.getTemplate(engine, site, "/pages/main");
    if (template != null){
        // it *is* new-school n we doda-dance
        final VelocityContext context = VelocityResultHandler.newContextInstance(engine);

        for (Modifier mod : sources) {
            if ( mod.getName().equals("sesam_hits")) {
                context.put("sesam_hits", text.getMessage("numberFormat", mod.getCount()));
            }
        }
        // populate context with sitemesh stuff
        context.put("request", request);
        context.put("response", response);
        context.put("page", siteMeshPage);
        context.put("base", request.getContextPath());
        context.put("title", OutputConverter.convert(siteMeshPage.getTitle()));
        context.put("text", text);
        {
            final StringWriter buffer = new StringWriter();
            siteMeshPage.writeBody(OutputConverter.getWriter(buffer));
            context.put("body", buffer.toString());
        }
        //{ // missing frm our version of sitemesh
        //    final StringWriter buffer = new StringWriter();
        //    siteMeshPage.writeHead(OutputConverter.getWriter(buffer));
        //    context.put("head", buffer.toString());
        //}
        // merge it into the JspWriter
        template.merge(context, out);%>
    <%-- old-school sitesearch --%>
    <% } else if (currentC.equals("d") && (
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
                        <li><a href="http://www.fedrelandsvennen.no/varet/">V&#230;ret</a></li>
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
                <decorator:getProperty property="page.verbosePager"/>
            </div>
        </div>


    <% } else { %>

        <%if (q.trim().equals("") && (currentC.equals("y") || currentC.equals("w") || currentC.equals("p"))) {%>
	<decorator:getProperty property="page.newsearch-bar"/>
	<% }else{ %>
	<decorator:getProperty property="page.search-bar"/>
	<% } %>

<table border="0" cellspacing="0" cellpadding="0" id="body_table">
    <%if (q.trim().equals("") && !currentC.equals("m")) {%>
    <tr>
        <td class="cell_one">&nbsp;</td>
        <td class="cell_three">&nbsp;</td>
        <td class="cell_four">&nbsp;</td>
    </tr>
    <% }else{ %>
    <tr>
        <td class="cell_one"><span class="pad_5l">Naviger</span></td>
        <td class="cell_three"><decorator:getProperty property="page.middle-bar"/></td>
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
	<%if (q.trim().equals("") && !currentC.equals("m")) {%>

	<%}else if (currentC.equals("m") || currentC.equals("d")|| currentC.equals("g") || currentC.equals("pss") || !q.trim().equals("")) {%>
        <td class="cell_one" valign="top">
            <%if (currentC.equals("y") || currentC.equals("yip") || currentC.equals("yipticker")) {%>
                <table border="0" cellspacing="0" cellpadding="0" class="menu">
        		    <% int i=0; %>
                    <% for (Modifier e : sources) {
                        SearchTab.NavigatorHint hint = query.getSearchTab().getNavigationHint(e.getName());
                        if (hint != null) {
                            ++i;
                        if(i!=1) {%>
                                <tr><td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td></tr>
                        <% } %>
                            <tr onclick='strepRollover("?q=<%=qURLEncoded%>&amp;<%="c=" + hint.getTab().getKey() +hint.getUrlSuffix()%>");'>
                                <td class="nav_pad_icon">
                                    <img <% if (e.getName().startsWith("Persons")) { %> src="../images/menu/person.gif" <% } else if (e.getName().startsWith("Netts")) { %> src="../images/menu/nettsok.gif" <% } %> class="nav_icon" align="left" alt="" />
                                    <% String x = "?q=" + qURLEncoded + "&amp;" + "c=" + hint.getTab().getKey() +hint.getUrlSuffix(); %>
                                    <a href="<%=linkpulse.getUrl(x, "category:navigation;subcategory:service_left", "sgo", "") %>" onclick="return strep(this);"><%= e.getName() %></a>
                                </td>
                                <td class="nav_pad"><%=text.getMessage("numberFormat", e.getCount()) %></td>
                            </tr>
                        <%}%>
                    <%}%>
                </table>
                <decorator:getProperty property="page.companies-nav"/>
            <%}else if (currentC.equals("w") || currentC.equals("wip") || currentC.equals("wipgift")) {%>
                <table border="0" cellspacing="0" cellpadding="0" class="menu">
		            <% int i=0; %>
                    <% for (Modifier e : sources) {
                        SearchTab.NavigatorHint hint = query.getSearchTab().getNavigationHint(e.getName());
                        if (hint != null) {
                                    ++i;
                        if(i!=1) {%>
                                <tr><td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td></tr>
                        <% } %>
                        <tr onclick='strepRollover("?q=<%=qURLEncoded%>&amp;<%="c=" + hint.getTab().getKey() +hint.getUrlSuffix()%>");'>
                            <td class="nav_pad_icon">
                                <img <% if (e.getName().startsWith("Bedriftss")) { %> src="../images/menu/bedrift.gif" <% } else if (e.getName().startsWith("Netts")) { %> src="../images/menu/nettsok.gif" <% } %> class="nav_icon" align="left" alt="" />
                                <% String x = "?q=" + qURLEncoded + "&amp;" + "c=" + hint.getTab().getKey() +hint.getUrlSuffix(); %>
                                <a href="<%=linkpulse.getUrl(x, "category:navigation;subcategory:service_left", "sgo", "") %>" onclick="return strep(this);"><%= e.getName() %></a>
                            </td>
                            <td class="nav_pad"><%=text.getMessage("numberFormat", e.getCount()) %></td>
                        </tr>
                        <%}%>
                    <%}%>
                </table>
                <% if (!currentC.equals("wipgift")) {%>
                    <decorator:getProperty property="page.persons-nav"/>
                <% } %>
            <%}else if (currentC.equals("m")) {%>
                <table border="0" cellspacing="0" cellpadding="0" class="menu">
		            <% int i=0; %>
                    <% for (Modifier e : sources) {
                        SearchTab.NavigatorHint hint = query.getSearchTab().getNavigationHint(e.getName());
                        if (hint != null) {
                            ++i;
                            if(i!=1) {%>
                                    <tr><td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td></tr>
                            <% } %>
                            <tr onclick='strepRollover("?q=<%=qURLEncoded%>&amp;<%="c=" + hint.getTab().getKey() +hint.getUrlSuffix()%>");'>
                                <td class="nav_pad_icon">
                                    <img src="../images/menu/nettsok.gif" class="nav_icon" align="left" alt="" />
                                    <% String x = "?q=" + qURLEncoded + "&amp;" + "c=" + hint.getTab().getKey() +hint.getUrlSuffix(); %>
                                    <a href="<%=linkpulse.getUrl(x, "category:navigation;subcategory:service_left", "sgo", "") %>" onclick="return strep(this);"><%= e.getName() %></a>
                                </td>
                                <td class="nav_pad"><%=text.getMessage("numberFormat", e.getCount()) %></td>
                            </tr>
                        <%}%>
                    <%}%>
                </table>
                <decorator:getProperty property="page.newsSearchNavigator" />
                <decorator:getProperty property="page.media-collection-nav"/>
            <% }else{ %>

                <table border="0" cellspacing="0" cellpadding="0" class="menu">
		            <% int i = 0; %>
                    <% for (Modifier e : sources) {
                        SearchTab.NavigatorHint hint = query.getSearchTab().getNavigationHint(e.getName());
                        if (hint != null) {
                            if ( (currentC.equals("d") && !e.getName().equals("Norske nettsider"))
                                    || currentC.equals("g") || currentC.equals("pss") || currentC.equals("p") || currentC.equals("b")) {
                                ++i;
                                if(i!=1) {%>
                                    <tr><td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td></tr>
                                <% } %>
                                <tr onclick='strepRollover("?q=<%=qURLEncoded%>&amp;<%="c=" + hint.getTab().getKey() +hint.getUrlSuffix()%>");'>
                                    <td class="nav_pad_icon">
                                        <img <% if (e.getName().startsWith("Bedrifts")) { %> src="../images/menu/bedrift.gif" <% } else if (e.getName().startsWith("Persons")) { %> src="../images/menu/person.gif" <% } else if (e.getName().startsWith("Bildes")) { %> src="../images/menu/bilde.gif" <% } else if (e.getName().equals("Verden")) { %> src="../images/menu/nettsok.gif"  <% } else if (e.getName().startsWith("Nyhetss")) { %> src="../images/menu/nyheter.gif" <% } else if (e.getName().startsWith("Netts")) { %> src="../images/menu/nettsok.gif" <% } %> class="nav_icon" align="left" alt="" />
                                        <% String x = "?q=" + qURLEncoded + "&amp;" + "c=" + hint.getTab().getKey() +hint.getUrlSuffix(); %>
                                        <a href="<%=linkpulse.getUrl(x, "category:navigation;subcategory:service_left", "sgo", "") %>" onclick="return strep(this);"><%= e.getName() %></a>
                                    </td>
                                    <td class="nav_pad"><%=text.getMessage("numberFormat", e.getCount()) %></td>
                                </tr>
                            <%}%>
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
        <% }else if (!currentC.equals("y") && !currentC.equals("yip") && !currentC.equals("w") && !currentC.equals("wip") && !currentC.equals("wipgift")) {%>
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
                                               <c:out value="${siteMeshPage.properties[pageName]}" escapeXml="false"/>
                                           </c:if>
                                       </c:forEach>

                                        <% if ("d".equals(currentC) || "pss".equals(currentC)) { %>
                                           <%--  Shows the 3 first hits if more than 1 enrichment  --%>
                                           <decorator:getProperty property="page.fast-results-norwegian_part1"/>


                                       <%-- Show tab's proceeding enrichments --%>
                                       <c:forEach var="ee" items="${enrichments}" varStatus="i">
                                           <c:if test="${(i.index >= tab.enrichmentOnTop || ee.analysisResult <= tab.enrichmentOnTopScore) && i.index < tab.enrichmentLimit}">
                                               <c:set var="pageName" value="page.${ee.name}"/>
                                               <c:out value="${siteMeshPage.properties[pageName]}" escapeXml="false"/>
                                           </c:if>
                                       </c:forEach>

                                           <%--  Shows the 7 next hits after the second/third enrichments  --%>
                                           <decorator:getProperty property="page.fast-results-norwegian_part2"/>

                                       <% } else if ("g".equals(currentC)) { %>

                                            <decorator:getProperty property="page.global-results"/>
                                       <% } %>
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


                <%if (currentC.equals("yip") || currentC.equals("yipticker")) {%>
                <decorator:getProperty property="page.infopage"/>
                <%}%>
                <%if (currentC.equals("wip")) {%>
                <decorator:getProperty property="page.infopage"/>
                <%}%>

                <%if (currentC.equals("wipgift")) {%>
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

                <%if (currentC.equals("t")) {%>
                <decorator:getProperty property="page.tv-results"/>
                <%}%>

                <%if (currentC.equals("b")) {%>
                <decorator:getProperty property="page.blog-search"/>
                <%}%>
               </td>

	    <%if (q.trim().equals("")) {%>

            <%}else if ( currentC.equals("d") || currentC.equals("m") || currentC.equals("g") || currentC.equals("pss")) {%>
                <td class="cell_four" valign="top" width="225">
                    <decorator:getProperty property="page.ads"/>
                </td>
            <%}else if (currentC.equals("p")) {%>
                    <decorator:getProperty property="page.ads"/>
                    <decorator:getProperty property="page.ads-picsearch-logo"/>
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
<% if (currentC.equals("g")) { %> tmsec[1]="tmsec=sesamsok_verden";
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
<% } else if (currentC.equals("b")) { %> tmsec[1]="tmsec=bloggsok";
<% } else if (currentC.equals("n")) { %> tmsec[1]="tmsec=siteN24";
<% } %>
getTMqs('','', 'sesam_no', 'no', 'iso-8859-15', tmsec);
//-->
</script>
<% if (currentC.equals("g") ) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=sesamsok_verden" alt="" /></noscript>
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
<% } else if (currentC.equals("b")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=bloggesok" alt="" /></noscript>
<% } else if (currentC.equals("n")) { %> <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=siteN24" alt="" /></noscript>
<% } %>

<!-- end gallup -->

</body>
</html>
