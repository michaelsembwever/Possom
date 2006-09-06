<%-- Copyright (2006) Schibsted Søk AS --%>
<%@ page
        language="java"
        errorPage="/internal-error.jsp"
        contentType="text/html; charset=utf-8"
        pageEncoding="UTF-8"
        %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>
<%@ page import="no.schibstedsok.searchportal.site.Site"%>
<% 
    final Site site = (Site)request.getAttribute(Site.NAME_KEY);
    String locale = site.getLocale().toString();
%>

<search:velocity template="/pages/index"/>

<c:if test="${!empty Missing_pagesindex_Template}">
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title>Sesam</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link href="css/front.css" rel="stylesheet" type="text/css" />
        <link href="css/ps.css" rel="stylesheet" type="text/css" />
        <link rel="icon" href="favicon.ico" type="image/x-icon" />
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
        <script type="text/javascript" language="JavaScript" src="javascript/common.js"></script>
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
        <td valign="bottom" align="left" width="170"><img src="images/index/menu_top.png" alt="Sesams&#248;k" /></td>
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
            <form name="sf" action="<search:linkPulse url="" param="category:front_search" index="true"/>" onsubmit='return check();'>
	    <input type="hidden" name="c" value="d" />
	    <table border="0" cellspacing="0" cellpadding="0" id="searchbox_top">
		<tr>
		    <td valign="top">
		    <input type="text" name="q" class="input_main" onfocus="this.form.q.select();" />
                    <script type="text/javascript"><!--
                        var focusControl = document.forms["sf"].elements["q"];
                        if (focusControl.type != "hidden" && !focusControl.disabled) {
                            focusControl.focus();
                        } // -->
                    </script>
		    </td>
                    <td>&nbsp;&nbsp;</td>
		    <td><input type="image" id="index_submit" src="images/searchbar/nett.png" /></td>
                    <td><img src="images/pix.gif" border="0" width="4" height="1" alt="" /></td>
		    <td><a href="javascript:toggleButton('front_button_layer');" onfocus="this.blur();"><img src="images/searchbar/dropdown.png" alt="" /></a></td>
		    <td><img src="images/pix.gif" border="0" width="15" alt="" /></td>
		    <td><a id="searchtip_front" href='<search:linkPulse url="?q=&amp;page=/pages/7/index" param="category:static;subcategory=header" index="true"/>'>S&#248;ketips</a></td>
		</tr>
		<tr>
		    <td valign="top" id="searchbox_tips">
                    	<input type="radio" name="ns" value="d" checked="checked" onclick='document.sf.c.value="d"' /> Norge &nbsp;&nbsp;
                	<input type="radio" name="ns" value="g" onclick='document.sf.c.value="g"' /> Verden
		    </td>
		    <td>&nbsp;</td>
		    <td colspan="3">
			<div id="front_button_layer">
			<table border="0" cellspacing="1" cellpadding="0" id="front_button_table">
			    <tr>
				<td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("d");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Nyhetss&#248;k</td>
			    </tr>
			    <tr>
				<td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("y");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Bedriftss&#248;k</td>
			    </tr>
			    <tr>
				<td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("w");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Persons&#248;k</td>
			    </tr>
			    <tr>
				<td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("p");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Bildes&#248;k</td>
			    </tr>
			    <tr>
				<td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("b");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Bloggs&#248;k</td>
			    </tr>
			    <tr>
				<td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("t");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>TV-s&#248;k</td>
			    </tr>
			    <tr>
				<td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("sw");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>V&#230;rs&#248;k</td>
			    </tr>
			    <tr>
				<td><a href="javascript:;" onclick='document.getElementById("front_button_layer").style.visibility="hidden";'><img src="../images/index/skjul_meny.png" alt="" align="left" /></a><div class="navbutend" onclick='document.getElementById("front_button_layer").style.visibility="hidden";'>&nbsp;&nbsp;Lukk</div></td>
			    </tr>
			</table>
			</div>
		    </td>
		    <td>&nbsp;</td>
		    <td>&nbsp;</td>
		</tr>
            </table>
            </form>
        </td>
    </tr>
    <tr>
        <td colspan="2"><img src="images/pix.gif" alt="" /></td>
    </tr>
    <tr>
        <td valign="top" align="left">
	<script type="text/javascript" language="JavaScript">
	    var menuCookie = getCookie("sesam_menu");
	    if(menuCookie == "closed"){
	      document.write('<div id="menutable" style="display:none;">'); 
	    }else{
	      document.write('<div id="menutable">');	
	    }
	</script>
            <table id="front_menu" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="imgpad"><img src="images/menu/icons/nyheter.png" alt="" align="left" /></td>
		    <td class="menupad"><a href='<search:linkPulse url="?c=m&amp;newscountry=Norge&amp;q=" param="category:front_service" index="true"/>' onclick="return strep(this);">Nyhetss&#248;k</a></td>
                </tr>
                <tr>
                    <td colspan="2"><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="imgpad"><img src="images/menu/icons/bedrift.png" alt="" align="left" /></td>
		    <td class="menupad" align="left"><a href='<search:linkPulse url="?c=y" param="category:front_service" index="true"/>' onclick="return strep(this);">Bedriftss&#248;k</a></td>
                </tr>
                <tr>
                    <td colspan="2"><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="imgpad"><img src="images/menu/icons/person.png" alt="" align="left" /></td>
		    <td class="menupad" align="left"><a href='<search:linkPulse url="?c=w" param="category:front_service" index="true"/>' onclick="return strep(this);">Persons&#248;k</a></td>
                </tr>
                <tr>
                    <td colspan="2"><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="imgpad"><img src="images/menu/icons/bilde.png" alt="" align="left" /></td>
		    <td class="menupad" align="left"><a href='<search:linkPulse url="?c=p" param="category:front_service" index="true"/>' onclick="return strep(this);">Bildes&#248;k</a></td>
                </tr>
                <tr>
                    <td colspan="2"><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="imgpad"><img src="images/menu/icons/blogg.png" alt="" align="left" /></td>
		    <td class="menupad" align="left"><a href='<search:linkPulse url="?c=b" param="category:front_service" index="true"/>' onclick="return strep(this);">Bloggs&#248;k</a>&#160;&#160;<span style="color: #AD248D;"><b>NY!</b></span></td>
                </tr>
                <tr>
                    <td colspan="2"><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="imgpad"><img src="images/menu/icons/tv.png" alt="" align="left" /></td>
		    <td class="menupad" align="left"><a href='<search:linkPulse url="?c=t" param="category:front_service" index="true"/>' onclick="return strep(this);">TV-s&#248;k</a>&#160;&#160;<span style="color: #AD248D;"><b>NY!</b></span></td>
                </tr>
		<tr>
                    <td colspan="2"><img src="images/index/dottedline.gif" alt="" /></td>
                </tr>
                <tr>
                    <td class="imgpad"><img src="images/menu/icons/ver.png" alt="" align="left" /></td>
		    <td class="menupad"><a href='<search:linkPulse url="?c=sw" param="category:front_service" index="true"/>' onclick="return strep(this);">V&#230;rs&#248;k</a>&#160;&#160;<span style="color: #AD248D;"><b>NY!</b></span></td>
                </tr>
                <tr>
                    <td class="lastpad" colspan="2" style="border-top: 1px solid #C5C5C5;"><a href="#" onclick='setCookie("sesam_menu", "closed", "", "/");document.getElementById("menutable").style.display="none";document.getElementById("menuopen").style.display="block";'><img src="images/index/skjul_meny.png" border="0" align="left" /><div style="padding-top: 3px;"> &nbsp;&nbsp;Skjul meny</div></a></td>
                </tr>
            </table>
            </div>
	    <script type="text/javascript" language="JavaScript">
	    	var menuCookie = getCookie("sesam_menu");
	    	if(menuCookie == "closed"){
	      	  document.write('<div id="menuopen">'); 
	    	}else{
	      	  document.write('<div id="menuopen" style="display: none;">');	
	    	}
	    </script>
            <table id="front_menu" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="lastpad"><a href="#" onclick='deleteCookie("sesam_menu", "/");document.getElementById("menutable").style.display="block";document.getElementById("menuopen").style.display="none";'><img src="images/index/vis_meny.png" border="0" align="left" /><div style="padding-top: 3px;"> &nbsp;&nbsp;Vis meny</div></a></td>
                </tr>
            </table>
            </div>
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






        <!-- start Gallup <%=locale  %>-->
        <% if ( "no".equals(locale) ) { %>        
        <script type='text/javascript' language='JavaScript' src='javascript/tmv11.js'></script>
        <script type="text/javascript" language="JavaScript"><!--
            var tmsec = new Array(2);
            tmsec[0]="tmsec=sesam";
            tmsec[1]="tmsec=sesamforside";
            getTMqs('','', 'sesam_no', 'no', 'iso-8859-15', tmsec);
            //-->
        </script>
        <noscript><img src="http://statistik-gallup.net/v11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=sesamforside" alt="" /></noscript>
        <% } %>
        <!-- end gallup -->

    </body>
</html>
</c:if>
