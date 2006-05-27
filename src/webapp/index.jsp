<%-- Copyright (2006) Schibsted Søk AS --%>
<%@ page import="no.schibstedsok.front.searchportal.configuration.SiteConfiguration"%>
<%@ page import="no.schibstedsok.front.searchportal.view.output.VelocityResultHandler"%>
<%@ page import="no.schibstedsok.front.searchportal.result.Linkpulse"%>
<%@ page import="no.schibstedsok.front.searchportal.site.Site"%>
<%@ page import="org.apache.velocity.Template"%>
<%@ page import="org.apache.velocity.VelocityContext"%>
<%@ page import="org.apache.velocity.app.VelocityEngine"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page
        language="java"
        errorPage="/internal-error.jsp"
        contentType="text/html; charset=utf-8"
        pageEncoding="UTF-8"
        %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>

<search:import template="/pages/index"/>

<c:if test="${!empty Missing_pagesindex_Template}">
<%
final Site site = (Site) request.getAttribute(Site.NAME_KEY);
final java.util.Properties props = SiteConfiguration.valueOf(site).getProperties();
final Linkpulse linkpulse = new Linkpulse(props);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title>Sesam</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link href="css/default.css" rel="stylesheet" type="text/css" />
        <link href="css/front.css" rel="stylesheet" type="text/css" />
        <link href="css/ps.css" rel="stylesheet" type="text/css" />
        <link rel="icon" href="favicon.ico" type="image/x-icon" />
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
        <script type="text/javascript" language="JavaScript"><!--
            function strep(qtag) {
            if (window.RegExp && window.encodeURIComponent) {
            var qlink=qtag.href;
            var qenc=encodeURIComponent(document.forms[0].q.value);
            if(qlink.indexOf("q=")!=-1){
            qtag.href=qlink.replace(new RegExp("q=[^&$]*"),"q="+qenc);
            }else{
            qtag.href=qlink+"&q="+qenc;
            }
            }
            return 1;
            }

            function check() {
            if(document.forms[0].q.value == ""){
            document.forms[0].q.focus();
            return false;
            }
            }

            var link = '<a href="javascript:;" onclick="this.style.behavior=\'url(#default#homepage)\';this.setHomePage(\'http://www.sesam.no/\');">Sett som startside</a> &nbsp;&nbsp;|&nbsp;&nbsp;';
            var link2 = '<a href="javascript:;" onclick="this.style.behavior=\'url(#default#homepage)\';this.setHomePage(\'http://www.sesam.no/\');">Sett Sesam som din startside</a>';
            // -->
        </script>
    </head>

    <body>

<div style="background-image: url(images/index/stripe_bg.gif); background-repeat: repeat-x; background-position: 0px 105px;" align="center">
<table border="0" width="850" cellspacing="0" cellpadding="0">
    <tr>
        <td valign="bottom" align="left" width="170"><img src="images/index/menu_top.gif" border="0" /></td>
        <td valign="middle" align="left"><img src="images/index/logo.png" id="logo_index" width="215" height="61" alt="logo" /></td>
    </tr>

    <tr>
        <td colspan="2"><img src="images/pix.gif" border="0" /></td>
    </tr>

    <tr>
        <td valign="top" align="left">
            <div style="width: 151px; height: 72px; background-image: url(images/index/menu_main_bg.gif);">
                <div style="border-left: 1px solid #C5C5C5; border-right: 1px solid #C5C5C5; height: 72px;">
                    <div style="padding: 20px 0px 0px 8px;"><img src="images/index/nettsok_30_30.gif" border="0" align="left" /></div>
                    <div style="padding-top: 4px; font-size: 15px;">&nbsp; Netts&#248;k</div>
                </div>
            </div>
        </td>
        <td valign="middle" align="left">
            <form name="sf" action="search/" onsubmit='return check();'>
            <input name="lan" value="en" type="hidden" />
            <div style="margin-top: 6px">
            <input type="text" name="q" class="input_main" onfocus="this.form.q.select();" /> &nbsp;
            <script type="text/javascript"><!--
                var focusControl = document.forms["sf"].elements["q"];
                if (focusControl.type != "hidden" && !focusControl.disabled) {
                    focusControl.focus();
                } // -->
            </script>

            <input type="image" id="index_submit" src="images/searchbar/nettsok_knapp.gif" />
	    &nbsp;
            <a href="<%=linkpulse.getUrl("?q=&page=/pages/7/index", "category:static;subcategory=header", "sgo", "true") %>">S&#248;ketips</a>
            </div>
            <div id="searchbox_tips">
            <input type="radio" name="c" value="d" checked="checked" /> Norge &nbsp;&nbsp;
            <input type="radio" name="c" value="g"  /> Verden
            </div>
            </form>
        </td>
    </tr>

    <tr>
        <td colspan="2"><img src="images/pix.gif" border="0" /></td>
    </tr>

    <tr>
        <td valign="top" align="left">
            <table border="0" width="151" cellspacing="0" cellpadding="0" style="border-left: 1px solid #C5C5C5; border-right: 1px solid #C5C5C5; border-bottom: 1px solid #C5C5C5; font-size: 12px;">
                <tr>
                    <td style="background-image: url(images/index/menu_bg.gif); height: 34px; padding-left: 8px;" valign="middle"><img src="images/index/nyheter_20_20.gif" border="0" align="left" /> &nbsp; <a href="<%=linkpulse.getUrl("?nav_sources=contentsourcenavigator&amp;c=m&amp;contentsource=Norske nyheter&amp;userSortBy=datetime&amp;q=", "category:front_service", "sgo", "true") %>" onclick="return strep(this);">Nyhetss&#248;k</a></td>
                </tr>
                <tr>
                    <td><img src="images/index/dottedline.gif" border="0" /></td>
                </tr>
                <tr>
                    <td style="background-image: url(images/index/menu_bg.gif); height: 34px; padding-left: 8px;" valign="middle"><img src="images/index/bedrift_20_20.gif" border="0" align="left" /> &nbsp; <a href="<%=linkpulse.getUrl("?c=y", "category:front_service", "sgo", "true") %>" onclick="return strep(this);">Bedriftss&#248;k</a></td>
                </tr>
                <tr>
                    <td><img src="images/index/dottedline.gif" border="0" /></td>
                </tr>
                <tr>
                    <td style="background-image: url(images/index/menu_bg.gif); height: 34px; padding-left: 8px;" valign="middle"><img src="images/index/person_20_20.gif" border="0" align="left" /> &nbsp; <a href="<%=linkpulse.getUrl("?c=w", "category:front_service", "sgo", "true") %>" onclick="return strep(this);">Persons&#248;k</a></td>
                </tr>
                <tr>
                    <td><img src="images/index/dottedline.gif" border="0" /></td>
                </tr>
                <tr>
                    <td style="background-image: url(images/index/menu_bg.gif); height: 34px; padding-left: 8px;" valign="middle"><img src="images/index/bilder_20_20.gif" border="0" align="left" /> &nbsp; <a href="<%=linkpulse.getUrl("?c=p", "category:front_service", "sgo", "true") %>" onclick="return strep(this);">Bildes&#248;k</a></td>
                </tr>
            </table>
        </td>
        </td>
        <td valign="top" align="left">
        <%
            try{
                final java.net.URLConnection urlConn = new java.net.URL(props.getProperty("publishing.system.baseURL")+"/pages/front.html").openConnection();
                urlConn.addRequestProperty("host", props.getProperty("publishing.system.host-header"));
                final java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(urlConn.getInputStream()));
                for(String line = reader.readLine();line!=null;line=reader.readLine()){
                    out.println(line);
                }
            }catch(Exception e){
                org.apache.log4j.Logger.getLogger("index.jsp").error("Failed to import pub/pages/front.html");
            }
        %>
        </td>
    </tr>

</table>

<div id="footer_space_index">
<div class="lightdots"><img src="images/pix.gif" width="100%" height="1" alt="" /></div>
        <%
            try{
                final java.net.URLConnection urlConn = new java.net.URL(props.getProperty("publishing.system.baseURL")+"/pages/footer.html").openConnection();
                urlConn.addRequestProperty("host", props.getProperty("publishing.system.host-header"));
                final java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(urlConn.getInputStream()));
                for(String line = reader.readLine();line!=null;line=reader.readLine()){
                    out.println(line);
                }
            }catch(Exception e){
                org.apache.log4j.Logger.getLogger("index.jsp").error("Failed to import pub/pages/footer.html");
            }
        %>
<br />
<div style="padding: 8px 0px 20px 0px;"><img src="images/pix.gif" border="0" width="100%" height="2" alt="" /></div>
</div>

</div>




        <!-- start Gallup -->
        <script type='text/javascript' language='JavaScript' src='javascript/tmv11.js'></script>
        <script type="text/javascript" language="JavaScript"><!--
            var tmsec = new Array(2);
            tmsec[0]="tmsec=sesam";
            tmsec[1]="tmsec=sesamforside";
            getTMqs('','', 'sesam_no', 'no', 'iso-8859-15', tmsec);
            //-->
        </script>
        <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=sesamforside" alt="" /></noscript>
        <!-- end gallup -->

    </body>
</html>
<%--   } --%>
</c:if>
