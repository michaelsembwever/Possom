<%@ page
        language="java"
        pageEncoding="ISO-8859-1"
        contentType="text/html;charset=utf-8"
        %>

<%@ page import="org.apache.struts.util.MessageResources,
                 no.schibstedsok.front.searchportal.query.RunningQuery,
                 no.schibstedsok.front.searchportal.result.Modifier"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="no.schibstedsok.front.searchportal.result.Enrichment"%>
<%@ page import="com.opensymphony.module.sitemesh.Page"%>
<%@ page import="com.opensymphony.module.sitemesh.RequestConstants"%>
<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>

<%
    MessageResources text = (MessageResources) request.getAttribute("text");
    Locale locale = (Locale) request.getAttribute("locale");
    // TODO: refactor to use Bean and SearchConstants.
    String currentC = "d";    //default collection
    currentC = (String) request.getAttribute("c");
    String searchType = request.getParameter("s");
    String q = (String) request.getAttribute("q");
    String help = request.getParameter("help");

    String about = request.getParameter("about");

    String ads_help = request.getParameter("ads_help");

    String smart = request.getParameter("smart");

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
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<script type="text/javascript" language="JavaScript">
<!--
function strep(qtag) {
    if (window.RegExp && window.encodeURIComponent) {
        var qlink=qtag.href;
        var qenc=encodeURIComponent(document.sf.q.value);
        if(qlink.indexOf("q=")!=-1){
            qtag.href=qlink.replace(new RegExp("q=[^&$]*"),"q="+qenc);
        }else{
            qtag.href=qlink+"&amp;q="+qenc;
        }
    }
    return 1;
}

function ml(ar, na, s) {
  str = eval('String.fromCharCode(' + ar + ')');
  ss = '';
  if (na == '') na = str;
  if (s != '') ss = '?subject=' + s;
  document.write('<a href="mailto:' + str + ss + '">' + na + '<\/a>');
}

function setfocus() {
    if(document.forms[0].q.value == ""){
        document.forms[0].q.focus();
    }
}
// -->
</script>
</head>


<body onload="setfocus();<%if (currentC.equals("y") || currentC.equals("yip") || currentC.equals("w") || currentC.equals("wip")) {%>init();<%}%>">

<div id="globalmenu_table"><img src="../images/pix.gif" width="1" height="6" alt="" /></div>

<table border="0" cellspacing="0" cellpadding="0" id="body_table">
    <tr>
        <td width="180"><a href="../"><img src="../images/logo.jpg" width="146" height="44" alt="" /></a></td>
        <td width="24"><img src="../images/pix.gif" width="24" height="60" alt="" /></td>
        <td valign="bottom">
            <div id="linkback">
                <a href="?c=d&amp;q=<%=q%>" <%if (currentC.equals("d")) {%>class="highlite"<%}%> onclick="return strep(this);">Sesams&#248;k</a>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <a href="?nav_sources=contentsourcenavigator&amp;c=m&amp;q=<%=q%>" <%if (currentC.equals("m")) {%>class="highlite"<%}%> onclick="return strep(this);">Nyhetss&#248;k</a>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <a href="?c=y&amp;q=<%=q%>" <%if (currentC.equals("y") || currentC.equals("yip")) {%>class="highlite"<%}%> onclick="return strep(this);">Bedriftss&#248;k</a>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <a href="?c=w&amp;q=<%=q%>" <%if (currentC.equals("w") || currentC.equals("wip")) {%>class="highlite"<%}%> onclick="return strep(this);">Persons&#248;k</a>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <a href="?c=p&amp;q=<%=q%>" <%if (currentC.equals("p")) {%>class="highlite"<%}%> onclick="return strep(this);">Bildes&#248;k</a>
            </div>
        </td>
        <td>&nbsp;</td>
    </tr>

    <tr>
        <td class="cell_one" valign="top">

            <%if (currentC.equals("y") || currentC.equals("yip")) {%>
                <table border="0" cellspacing="0" cellpadding="0" class="navbar_table">
                    <tr>
                        <td colspan="2" class="navbar_menu" id="navbar_menu_y">Bedriftss&#248;k</td>
                    </tr>
                    <tr>
                        <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" /></td>
                    </tr>
                    <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#FAFAFA';" onclick='location.href="?c=d&amp;q=<%=q%>";'>
                        <td class="nav_pad_icon">
                            <img src="../images/nav_d.gif" class="nav_icon" align="left" alt="sesam_icon" />
                            <a href="?c=d&amp;q=<%=q%>">Sesams&#248;k</a>
                        </td>
                        <td class="nav_pad" align="right">&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" class="dots" alt="" /></td>
                    </tr>
                    <% for (Iterator iterator = sources.iterator(); iterator.hasNext();) {
                    Modifier e = (Modifier) iterator.next();
                    %>
                        <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#FAFAFA';" onclick='location.href="?q=<%=q%>&amp;<%=query.getSourceParameters(e.getName())%>";'>
                            <td class="nav_pad_icon">
                                <img <% if (e.getName().equals("Bedriftssøk")) { %> src="../images/nav_y.gif" <% } else if (e.getName().equals("Personsøk")) { %> src="../images/nav_w.gif" <% } %> class="nav_icon" align="left" alt="" />
                                <a href="?q=<%=q%>&amp;<%=query.getSourceParameters(e.getName())%>" onclick="return strep(this);"><%= e.getName() %></a>
                            </td>
                            <td class="nav_pad" align="right"><%=text.getMessage(locale, "numberFormat", new Integer(e.getCount())) %></td>
                        </tr>
                        <tr>
                            <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" class="dots" alt="" /></td>
                        </tr>
                    <%}%>
                    <% if(int_w_hits > 0) { %>
                    <% }else{ %>
                    <tr>
                        <td colspan="2" style="padding: 0px; background: #FFFFFF;"><img src="../images/pix.gif" width="100%" height="25" alt="" /></td>
                    </tr>
                    <%}%>
                </table>

                <% if (currentC.equals("yip")) {%>
                    <decorator:getProperty property="page.infopage-nav"/>
                <% } else { %>
                    <decorator:getProperty property="page.companies-nav"/>
                <% } %>

            <%}else if (currentC.equals("w") || currentC.equals("wip")) {%>
                <table border="0" cellspacing="0" cellpadding="0" class="navbar_table">
                    <tr>
                        <td colspan="2" class="navbar_menu" id="navbar_menu_w">Persons&#248;k</td>
                    </tr>
                    <tr>
                        <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td>
                    </tr>
                    <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#FAFAFA';" onclick='location.href="?c=d&amp;q=<%=q%>";'>
                        <td class="nav_pad_icon">
                            <img src="../images/nav_d.gif" class="nav_icon" align="left" alt="sesam_icon" />
                            <a href="?c=d&amp;q=<%=q%>">Sesams&#248;k</a>
                        </td>
                        <td class="nav_pad" align="right">&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" class="dots" alt="" /></td>
                    </tr>
                   <% for (Iterator iterator = sources.iterator(); iterator.hasNext();) {
                    Modifier e = (Modifier) iterator.next();
                    %>
                    <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#FAFAFA';" onclick='location.href="?q=<%=q%>&amp;<%=query.getSourceParameters(e.getName())%>";'>
                        <td class="nav_pad_icon">
                            <img <% if (e.getName().equals("Bedriftssøk")) { %> src="../images/nav_y.gif" <% } else if (e.getName().equals("Personsøk")) { %> src="../images/nav_w.gif" <% } %> class="nav_icon" align="left" alt="" />
                            <a href="?q=<%=q%>&amp;<%=query.getSourceParameters(e.getName())%>" onclick="return strep(this);"><%= e.getName() %></a>
                        </td>
                        <td class="nav_pad" align="right"><%=text.getMessage(locale, "numberFormat", new Integer(e.getCount())) %></td>
                    </tr>
                    <tr>
                        <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" class="dots" alt="" /></td>
                    </tr>
                    <%}%>
                    <% if(int_y_hits > 0) { %>
                    <% }else{ %>
                    <tr>
                        <td colspan="2" style="padding: 0px; background: #FFFFFF;"><img src="../images/pix.gif" width="100%" height="25" alt="" /></td>
                    </tr>
                    <%}%>
                </table>
                <% if (currentC.equals("wip")) {%>
                    <decorator:getProperty property="page.infopage-nav"/>
                <% } else { %>
                    <decorator:getProperty property="page.persons-nav"/>
                <% } %>

            <%}else if (currentC.equals("p")) {%>
                <decorator:getProperty property="page.picture-nav"/>

            <%}else if (currentC.equals("m")) {%>
                <decorator:getProperty property="page.newsSearchNavigator" />
                <decorator:getProperty property="page.media-collection-nav"/>
            <% }else{ %>

                <table border="0" cellspacing="0" cellpadding="0" class="navbar_table">
                        <tr>
                            <td colspan="2" class="navbar_menu" id="navbar_menu_d">Sesams&#248;k</td>
                        </tr>
                        <tr>
                            <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" alt="" /></td>
                        </tr>
                        <% for (Iterator iterator = sources.iterator(); iterator.hasNext();) {
                        Modifier e = (Modifier) iterator.next();

                        %>
                        <tr onmouseover="this.style.background='#E3DEE4';" onmouseout="this.style.background='#FAFAFA';" onclick='location.href="?q=<%=q%>&amp;<%=query.getSourceParameters(e.getName())%>";'>
                            <td class="nav_pad_icon">
                                <img <% if (e.getName().equals("Bedriftssøk")) { %> src="../images/nav_y.gif" <% } else if (e.getName().equals("Personsøk")) { %> src="../images/nav_w.gif" <% } else if (e.getName().equals("Bildesøk")) { %> src="../images/nav_p.gif" <% } else if (e.getName().startsWith("Nyhetss")) { %> src="../images/nav_m.gif" <% } %> class="nav_icon" align="left" alt="" />
                                <a href="?q=<%=q%>&amp;<%=query.getSourceParameters(e.getName())%>" onclick="return strep(this);"><%= e.getName() %></a>
                            </td>
                            <td class="nav_pad" align="right"><%=text.getMessage(locale, "numberFormat", new Integer(e.getCount())) %></td>
                        </tr>
                        <tr>
                            <td colspan="2" class="nopad"><img src="../images/pix.gif" width="100%" height="1" class="dots" alt="" /></td>
                        </tr>
                        <%}%>
                </table>
                <% if ("true".equals(smart)) { %>
                    <decorator:getProperty property="page.smart-nav"/>
                <% }else if ("true".equals(help)){ %>
                    <decorator:getProperty property="page.help-nav"/>
                <% }else if ("true".equals(about)){ %>
                    <decorator:getProperty property="page.about-nav"/>
                <%}%>

            <%}%>
        </td>

            <td class="cell_two" valign="top"><img src="../images/pix.gif" width="24" height="1" alt="" class="dash_<%=currentC%>" /></td>
            <%if (!currentC.equals("y") && !currentC.equals("yip") && !currentC.equals("w") && !currentC.equals("wip")) {%>
            <td class="cell_three" valign="top">
            <% }else{ %>
            <td class="cell_three" valign="top" colspan="2">
            <%}%>
                <decorator:getProperty property="page.search-bar"/>
                <!-- Magic -->
                <%if (currentC.equals("d")) {%>


                        <%--  Header  --%>
                        <% if ("true".equals(smart)) { %>
                            <decorator:getProperty property="page.greybar_smart"/>
                        <% } else if ("true".equals(help)) { %>
                            <decorator:getProperty property="page.greybar_help"/>
                        <%}else{%>
                            <% if ("g".equals(searchType)) { %>
                                <decorator:getProperty property="page.global_greybar"/>
                             <% } else { %>
                                 <% if (no_hits > 0) { %>
                                    <%--  shows the result as usual if 1 or less enrichments  --%>
                                    <decorator:getProperty property="page.greybar_magic"/>
                                 <%  } else { %>
                                    <% if (!q.trim().equals(""))  { %>
                                        <decorator:getProperty property="page.greybar_magic_enrichment"/>
                                    <% } %>
                                 <% } %>
                             <% } %>
                         <% } %>  <%--  Help header  --%>


                         <%--  Sok smart  --%>
                         <% if ("true".equals(smart)) { %>
                            <decorator:getProperty property="page.smart"/>
                         <% } else if ("true".equals(help)) { %>
                             <decorator:getProperty property="page.help"/>
                         <% } else if ("true".equals(about)) { %>
                            <decorator:getProperty property="page.about"/>
                         <% } else if ("true".equals(ads_help)) { %>
                            <decorator:getProperty property="page.ads_help"/>
                         <% } else { %>

                         <div id="result_container">
                             <% if ("n".equals(searchType)) { %>
                                    <% if (no_hits > 0) { %>
                                        <decorator:getProperty property="page.fast-results"/>
                                    <%  } else if(!q.trim().equals("")){ %>
                                        <decorator:getProperty property="page.noHits"/>
                                    <% } %>
                             <% } else if ("g".equals(searchType)) { %>
                                    <% if (int_hits > 0) { %>
                                        <decorator:getProperty property="page.global-results"/>
                                    <%  } else if(!q.trim().equals("")){ %>
                                        <decorator:getProperty property="page.noHits"/>
                                    <% } %>
                             <%  } else { %>

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
                        </div>
                        <% } %>  <%-- Sok smart --%>
                <%}%>



                <!-- Media -->
                <%if (currentC.equals("m")) {%>
                    <!-- Moreover -->
                    <%-- <decorator:getProperty property="page.globalNewsEnrichment"/> --%>
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

                <%--  footer  --%>
                <%if (q==null||!q.trim().equals("")||"m".equals(currentC)) {%>
                    <decorator:getProperty property="page.verbosePager"/>
                <%}%>

            </td>

            <%if ( currentC.equals("d") || currentC.equals("m") || currentC.equals("g")) {%>
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

<decorator:getProperty property="page.map-script"/>

</body>
</html>