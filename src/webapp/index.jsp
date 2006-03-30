<%-- Copyright (2006) Schibsted Søk AS --%>
<%@ page import="no.schibstedsok.front.searchportal.result.Linkpulse"%>
<%@ page import="no.schibstedsok.front.searchportal.site.Site"%>
<%@ page import="no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator"%>
<%@ page
        language="java"
        errorPage="/internal-error.jsp"
        contentType="text/html; charset=utf-8"
        pageEncoding="UTF-8"
        %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%  final Site site = (Site) request.getAttribute(Site.NAME_KEY);
    final java.util.Properties props = XMLSearchTabsCreator.valueOf(site).getProperties();
    final Linkpulse linkpulse = new Linkpulse(XMLSearchTabsCreator.valueOf(site).getProperties()); 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title>Sesam</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link href="css/decorator-style.css" rel="stylesheet" type="text/css" />
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

<div class="index_center">
    <img src="images/index/logo.png" id="logo_index" alt="logo" />
    <div id="index_searchlinks">
        Netts&#248;k
        <a href="<%=linkpulse.getUrl("?nav_sources=contentsourcenavigator&amp;c=m&amp;contentsource=Norske Nyheter&amp;userSortBy=datetime&amp;q=", "category:topmenuFront_m", "sgo", "true") %>" onclick="return strep(this);">Nyhetss&#248;k</a>
        <a href="<%=linkpulse.getUrl("?c=y", "category:topmenuFront_y", "sgo", "true") %>" onclick="return strep(this);">Bedriftss&#248;k</a>
        <a href="<%=linkpulse.getUrl("?c=w", "category:topmenuFront_w", "sgo", "true") %>" onclick="return strep(this);">Persons&#248;k</a>
        <a href="<%=linkpulse.getUrl("?c=p", "category:topmenuFront_p", "sgo", "true") %>" onclick="return strep(this);">Bildes&#248;k</a>
    </div>
</div>

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
