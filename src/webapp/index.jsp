<%@ page import="no.schibstedsok.front.searchportal.result.Linkpulse"%>
<%@ page import="no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator"%>
<%@ page import="no.schibstedsok.front.searchportal.site.Site"%>
<%@ page
        language="java"
        errorPage="/internal-error.jsp"
        contentType="text/html; charset=utf-8"
        pageEncoding="ISO-8859-1"
        %>
<%
    Linkpulse linkpulse = new Linkpulse(XMLSearchTabsCreator.valueOf(Site.DEFAULT).getProperties());
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
    <script type="text/javascript" language="JavaScript">
    <!--
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

<body onload="document.forms[0].q.focus();">

<div class="index_center">
    <img src="images/index/logo.jpg" id="logo_index" alt="logo" />
    <div id="index_searchlinks">
        Sesams&#248;k
        <a href="<%=linkpulse.getUrl("?nav_sources=contentsourcenavigator&amp;c=m&amp;contentsource=Norske Nyheter&amp;userSortBy=datetime&amp;q=", "category:topmenuFront_m", "sgo", "true") %>" onclick="return strep(this);">Nyhetss&#248;k</a>
        <a href="<%=linkpulse.getUrl("?c=y", "category:topmenuFront_y", "sgo", "true") %>" onclick="return strep(this);">Bedriftss&#248;k</a>
        <a href="<%=linkpulse.getUrl("?c=w", "category:topmenuFront_w", "sgo", "true") %>" onclick="return strep(this);">Persons&#248;k</a>
        <a href="<%=linkpulse.getUrl("?c=p", "category:topmenuFront_p", "sgo", "true") %>" onclick="return strep(this);">Bildes&#248;k</a>
    </div>
</div>

<jsp:include page="indBoxIncl.html" />

<div class="index_center">
    <div id="index_enrichment">
        <jsp:include page="indTextIncl.html" />

        <div id="footer_space">
            <div class="lightdots"><img src="images/pix.gif" width="100%" height="1" alt="" /></div>
            <div id="footer_help">
                <span class="copy">&copy;2005</span> &nbsp;&nbsp;
                <script type="text/javascript" language="JavaScript">
                    if(document.all){
                        document.write(link);
                    }
                </script>
                <a href='<%=linkpulse.getUrl("?c=d&amp;toolbar=true&amp;pg=1&amp;q=", "category:footer_toolbar", "sgo", "true") %>'>Verkt&#248;ylinje</a>
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href='<%=linkpulse.getUrl("?c=d&amp;box=true&amp;pg=1&amp;q=", "category:footer_searchbox", "sgo", "true") %>'>S&#248;keboks p&#229; din side</a>
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href='<%=linkpulse.getUrl("?c=d&amp;smart=true&amp;pg=1&amp;q=", "category:footer_smart", "sgo", "true") %>'>S&#248;ketips</a>
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href='<%=linkpulse.getUrl("?c=d&amp;ads_help=true&amp;pg=1&amp;q=", "category:footer_adshelp", "sgo", "true") %>'>Bli annons&#248;r</a>
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href='<%=linkpulse.getUrl("?c=d&amp;q=&amp;about=true&amp;pg=1", "category:footer_aboutUs", "sgo", "true") %>'>Om oss</a>
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href='<%=linkpulse.getUrl("?c=d&amp;q=&amp;help=true&amp;pg=1", "category:footer_help", "sgo", "true") %>'>Hjelp</a>
            </div>
        </div>
    </div>
</div>

<!-- start Gallup -->
<script type='text/javascript' language='JavaScript' src='javascript/tmv11.js'></script>
<script type="text/javascript" language="JavaScript">
<!--
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