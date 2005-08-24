<%@ page import="no.schibstedsok.front.searchportal.util.SearchConstants"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%

	// TODO: refactor to use Bean and SearchConstants.

    String collection = "d";	//default collection
    if(request.getParameter("c") != null)
	    collection = request.getParameter("c");

    String q = request.getParameter("q");
	    if(q!=null)
	        q = q.replaceAll("\"","&quot;");

    String search_button_text = "MAGISØK";
    String search_button_bgcolor = "#705CB3";
    String menu_line = "../images/menu/menu_line_magic.gif";
    if (collection.equals("g")) {
        search_button_text = "Verden";
        search_button_bgcolor = "#52A5DA";
        menu_line = "../images/menu/menu_line_sensis.gif";
    } else if (collection.equals("y")) {
        search_button_text = "BEDRIFTSØK";
        search_button_bgcolor = "#F6B331";
        menu_line = "../images/menu/menu_line_yp.gif";
    } else if (collection.equals("w")) {
        search_button_text = "PERSONSØK";
        search_button_bgcolor = "#ACCB49";
        menu_line = "../images/menu/menu_line_wp.gif";
    } else if (collection.equals("m")) {
        search_button_text = "NYHETSSØK";
        search_button_bgcolor = "#ED486B";
        menu_line = "../images/menu/menu_line_news.gif";
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title><%=q%></title>
        <link href="../css/decorator-style.css" rel="stylesheet" type="text/css" />
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    </head>

    <body onload="document.forms[0].q.focus();">

        <img src="../images/menu/logo.gif" id="logo" alt="logo" />

        <table cellpadding="0" cellspacing="0" border="0" id="table_menu">
            <tr><td class="first_cell"><a href="?c=d&amp;q=<%=q%>"><img src="../images/menu/magic.gif" class="menu_img" alt="Link til magisk søk" /></a><a href="?c=m&amp;q=<%=q%>"><img src="../images/menu/news.gif" class="menu_img" alt="link til nyheter" /></a><a href="?c=y&amp;q=<%=q%>"><img src="../images/menu/yp.gif" class="menu_img" alt="link til gule data" /></a><a href="?c=w&amp;q=<%=q%>"><img src="../images/menu/wp.gif" class="menu_img" alt="link til hvite sider" /></a><a href="?c=g&amp;q=<%=q%>"><img src="../images/menu/sensis.gif" class="menu_img" alt="link til verden søk" /></a><a href="#"><img src="../images/menu/picture.gif" class="menu_img_last" alt="Link til bilde søk" /></a></td></tr>

            <tr>
                <td style="background-image: url(<%=menu_line%>); background-repeat: repeat-x;">&nbsp;</td>
            </tr>

            <% if (collection.equals("y")) { %>
                <tr>
                    <td class="first_cell" id="header"><span id="form_header">Søk etter hva og hvor</span></td>
                </tr>

                <tr>
                    <td class="first_cell">Fx frisør Pettersen Bogstadveien Oslo</td>
                </tr>
            <%}%>

            <tr>
                <td class="first_cell">
                    <form name="sf" action="" id="search_form">
                        <input name="lang" value="en" type="hidden" />
                        <input name="c" value="<%=collection%>" type="hidden" />
                        <input name="q" type="text" value="<%= q %>" size="50" />
                        <input type="submit" class="search_button" value="<%=search_button_text%>" style="background-color:<%=search_button_bgcolor%>" />
                        <a href="#"><span class="link_style">Innstillinger</span></a>
                    </form>
                </td>
            </tr>

            <tr>
                <td>
                    <decorator:getProperty property="page.spelling-suggestions" />

                    <!-- Senis Beriket -->
                    <%if(collection.equals("d")){%>

                        <!-- TV Beriket -->
                        <decorator:getProperty property="page.tv-results" />

                        <decorator:getProperty property="page.sensis-enriched" />

                        <!-- Nyheter Beriket -->
                        <decorator:getProperty property="page.media-enriched" />

                        <!-- Wiki Beriket -->
                        <decorator:getProperty property="page.wiki-enriched" />

                        <!-- Webinnhold -->
                        <decorator:getProperty property="page.fast-results" />

                    <%}%>

                    <!-- Webinnhold Global index -->
                    <%if(collection.equals("g")){%>
                    <decorator:getProperty property="page.global-results" />
                    <%}%>

                    <!-- Persons -->
                    <%if(collection.equals("w")){%>
                    <decorator:getProperty property="page.persons-results" />
                    <%}%>

                    <!-- Companies -->
                    <%if(collection.equals("y")){%>

                    <%if(request.getParameter("companyId") != null) {%>
                    <decorator:getProperty property="page.infopage"/>
                    <%} else {%>
                    <decorator:getProperty property="page.companies-results"/>
                    <%}%>
                    <%}%>



                    <!-- Media index -->
                    <%if(collection.equals("m")){%>
	                    <decorator:getProperty property="page.media-collection-results" />
                    <%}%>

<%--               <td width="180" valign="top"><decorator:getProperty property="page.wiki-results" /></td>--%>
                </td>
            </tr>
        </table>

        <table id="table_footer">
            <%if(collection.equals("d")){%>
                <decorator:getProperty property="page.more-results-d"/>
            <%}else if(collection.equals("g")){%>
                <decorator:getProperty property="page.more-results-g"/>
            <%}else if(collection.equals("y")){%>
                <decorator:getProperty property="page.more-results-y"/>
            <%}else if(collection.equals("w")){%>
            <decorator:getProperty property="page.more-results-w"/>
            <%}else if(collection.equals("m")){%>
            <decorator:getProperty property="page.more-results-m"/>
            <%}%>

            <tr>
                <td id="footer" colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td class="text_align_center">
                    <a href="#">Om oss</a> |
                    <a href="#">Bli annonsør</a> |
                    <a href="#">Hjelp</a>
                </td>
            </tr>
        </table>


    </body>
</html>

