<%-- Copyright (2006) Schibsted Søk AS --%>
<%@ page
        language="java"
        errorPage="/internal-error.jsp"
        contentType="text/html; charset=utf-8"
        pageEncoding="UTF-8"
        %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>

<search:velocity template="/pages/index"/>

<c:if test="${!empty Missing_pagesindex_Template}">
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title>Sesam</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link href="css/default.css?x=2" rel="stylesheet" type="text/css" />
        <link href="css/front.css?x=2" rel="stylesheet" type="text/css" />
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

<div style="padding-left:28px; background-image: url(images/index/stripe_bg.gif); background-repeat: repeat-x; background-position: 0px 105px;" align="left">
<table border="0" width="850" cellspacing="0" cellpadding="0">
    <tr>
        <td valign="bottom" align="left" width="170"><img src="images/index/menu_top.gif" alt="Sesam s&#248;k" /></td>
        <td valign="middle" align="left"><img src="images/index/logo.png" id="logo_index" width="215" height="61" alt="logo" /></td>
    </tr>

    <tr>
        <td colspan="2"><img src="images/pix.gif" alt="" /></td>
    </tr>

    <tr>
        <td valign="top" align="left">
            <div id="menu_netsearch">
                <div style="padding: 20px 0px 0px 8px;"><img src="images/index/nettsok_30_30.gif" border="0" align="left" alt="" /></div>
                <div style="padding-top: 4px; font-size: 15px;">&nbsp; Netts&#248;k</div>
            </div>
        </td>
        <td valign="middle" align="left">
            <form name="sf" action="search/" onsubmit='return check();'>
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
                    <a id="searchtip_front" href='<search:linkPulse url="?q=&amp;page=/pages/7/index" param="category:static;subcategory=header" index="true"/>'>S&#248;ketips</a>
                </div>
                <div id="searchbox_tips">
                <input type="radio" name="c" value="d" checked="checked" /> Norge &nbsp;&nbsp;
                <input type="radio" name="c" value="g"  /> Verden
                </div>
            </form>
        </td>
    </tr>
    <tr>
        <td colspan="2"><img src="images/pix.gif" alt="" /></td>
    </tr>
    <tr>
        <td valign="top" align="left">
            <table id="front_menu" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="bg"><img src="images/index/nyheter_20_20.gif" alt="" align="left" /> &nbsp; <a href='<search:linkPulse url="?c=m&amp;newscountry=Norge&amp;q=" param="category:front_service" index="true"/>' onclick="return strep(this);">Nyhetss&#248;k</a></td>
                </tr>
                <tr>
                    <td><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="bg"><img src="images/index/bedrift_20_20.gif" alt="" align="left" /> &nbsp; <a href='<search:linkPulse url="?c=y" param="category:front_service" index="true"/>' onclick="return strep(this);">Bedriftss&#248;k</a></td>
                </tr>
                <tr>
                    <td><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="bg"><img src="images/index/person_20_20.gif" alt="" align="left" /> &nbsp; <a href='<search:linkPulse url="?c=w" param="category:front_service" index="true"/>' onclick="return strep(this);">Persons&#248;k</a></td>
                </tr>
                <tr>
                    <td><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="bg"><img src="images/index/bilder_20_20.gif" alt="" align="left" /> &nbsp; <a href='<search:linkPulse url="?c=p" param="category:front_service" index="true"/>' onclick="return strep(this);">Bildes&#248;k</a></td>
                </tr>
                <tr>
                    <td><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="bg"><img src="images/index/tv.gif" alt="" align="left" /> &nbsp; <a href='<search:linkPulse url="?c=t" param="category:front_service" index="true"/>' onclick="return strep(this);">TVs&#248;k</a>&#160;&#160;<span style="color: #AD248D;"><b>NY!</b></span></td>
                </tr>
                <tr>
                    <td><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="bg"><img src="images/index/ver.gif" alt="" align="left" /> &nbsp; <a href='<search:linkPulse url="?c=sw" param="category:front_service" index="true"/>' onclick="return strep(this);">V&#230;rs&#248;k</a>&#160;&#160;<span style="color: #AD248D;"><b>NY!</b></span></td>
                </tr>
                
            </table>
        </td>
        <td valign="top" align="left">
            <search:publish page="/pages/front"/>
        </td>
    </tr>

</table>

</div>

<img src="images/navLightLine.gif" style="padding-top:20px;" width="100%" height="1" alt="" />
<div id="footer_space_index">
    <search:publish page="/pages/footer"/>
    <br />
    <div style="padding: 8px 0px 20px 0px;"><img src="images/pix.gif" border="0" width="100%" height="2" alt="" /></div>
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
</c:if>
