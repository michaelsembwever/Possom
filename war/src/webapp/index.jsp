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

<%!
    String format(String verticalName)  {

        String s = "<tr><td>";
        return s;



    }
%>
<%
    final Site site = (Site)request.getAttribute(Site.NAME_KEY);
    String locale = site.getLocale().toString();
    String openSearchUrlStandard = "/search/?q=*&amp;c=d&amp;output=opensearch&amp;IGNORE=NOCOUNT";
    String openSearchUrlJavascript = "http://sesam.no/search/?q=*&c=d&output=opensearch&IGNORE=NOCOUNT";    
%>

<search:velocity template="/pages/index"/>

<c:if test="${!empty Missing_pagesindex_Template}">
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title>Sesam</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link href="/css/front.css" rel="stylesheet" type="text/css" />
        <link href="/css/ps.css" rel="stylesheet" type="text/css" />
        <link rel="icon" href="/images/favicon.gif" type="image/x-icon" />
        <link rel="shortcut icon" href="/images/favicon.gif" type="image/x-icon" />
        <link rel="search" type="application/opensearchdescription+xml" title="Sesam.no" href="<%= openSearchUrlStandard %>%>" />
        <script type="text/javascript" language="JavaScript" src="/javascript/external/prototype.js"></script>
        <script type="text/javascript" language="JavaScript" src="/javascript/common.js"></script>
        <script type="text/javascript" language="JavaScript" src="/javascript/callAtIntervals.js"></script>
        <script type="text/javascript" language="JavaScript" src="/javascript/openSearch.js"></script>

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
            // -->
        </script>
    </head>

    <body>

      <script type="text/javascript" language="javascript">
            var openSearch = new OpenSearch(10, 10, 100, 30, displayOpenSearchInfo,
                    '<%= openSearchUrlJavascript %>');

            if (openSearch.browserSupports()) {
                openSearch.run();
            }
            function displayOpenSearchInfo() {

                var browser = navigator.appName;
                var browserVersion1 = parseFloat(navigator.appVersion);
                var browserVersion = navigator.appVersion;
                var browserAgent = navigator.userAgent;

               if (browserAgent.match("Firefox/2") == "Firefox/2") {
                    document.write("<a href=\"#\" title=\"Sesam.se\" onclick=\"openSearch.logSelection('frontpage', ''); openSearch.add(); this.style.display='none'\" style=\"position:absolute; right:20px; padding-top:0px; text-decoration:none; border:none;\"><img src=\"/images/opensearch/opensearchbanner.png\" style=\"border:none;\" /></a>")
               } else if (browserAgent.match("MSIE 7") == "MSIE 7") {
                   document.write("<a href=\"#\" title=\"Sesam.se\" onclick=\"openSearch.logSelection('frontpage', ''); openSearch.add(); this.style.display='none'\" style=\"position:absolute; right:20px; top:0px; text-decoration:none; border:none;\"><img src=\"/images/opensearch/opensearchbanner.png\" style=\"border:none;\" /></a>")
              }
            }
        </script>
      

        <img src="images/index/logo.png" id="logoIndex" width="215" height="61" alt="Sesamlogo" />
        <img src="images/index/menu_top.png" id="menuTop" width="151" height="23" alt="Sesams&#248;k" />
        <table id="searchbar" cellspacing="0" cellpadding="0">
            <tr>
                <td id="menuCol">
                    <div id="menuMiddle">
                        <img src="images/index/nettsok_30_30.gif" width="30" height="30" alt="Netts&#248;k logo" />
                        Netts&#248;k
                    </div>
                </td>
                <td id="searchForm">
                <form name="sf" action="<search:linkPulse url="/search/" param="category:front_search" index="true"/>" onsubmit='return check();'>
                <table cellspacing="0" cellpadding="0">
                    <tr>
                        <td>
                            <input type="text" name="q" id="searchInput" onfocus="this.form.q.select();" />
                            <script type="text/javascript"><!--
                                var focusControl = document.forms["sf"].elements["q"];
                                if (focusControl.type != "hidden" && !focusControl.disabled) {
                                    focusControl.focus();
                                } // -->
                            </script>
                        </td>
                        <td><input type="image" id="formSubmit" src="images/searchbar/nett.png" alt="" /></td>
                        <td><a href="javascript:toggleButton('front_button_layer');" onfocus="this.blur();"><img src="images/searchbar/dropdown.png" width="19" height="24" alt="Pil ned" /></a></td>
                        <td><a id="searchtip" href='<search:linkPulse url="/search/?q=&amp;page=/pages/82/index" param="category:static;subcategory=header" index="true"/>'>S&#248;ketips</a></td>
                    </tr>
                    <tr>
                        <td id="searchRadio">
                            <input type="radio" name="c" value="d" checked="checked" /> Norge &nbsp;&nbsp;
                            <input type="radio" name="c" value="g" /> Verden
                        </td>

                        <td colspan="3">
                            <div id="front_button_layer">
                            <table cellspacing="1" cellpadding="0" id="front_button_table">
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("m","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Nyhetss&#248;k</td>
                                </tr>
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("y","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Bedriftss&#248;k</td>
                                </tr>
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("w","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Persons&#248;k</td>
                                </tr>
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("p","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Bildes&#248;k</td>
                                </tr>
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("b","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Bloggs&#248;k</td>
                                </tr>
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("t","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>TV-guide</td>
                                </tr>
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("wt","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>NettTV-s&#248;k</td>
                                </tr>
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("sw","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>V&#230;rs&#248;k</td>
                                </tr>
                                <tr>
                                    <td class="navbut" onclick='deleteCookie("sesam_menu", "/");document.getElementById("front_button_layer").style.visibility="hidden";menu_submit("map","");' onmouseover='this.style.backgroundColor="#DADBDD"' onmouseout='this.style.backgroundColor="#FFF"'>Karts&#248;k</td>
                                </tr>
                                <tr>
                                    <td onclick='document.getElementById("front_button_layer").style.visibility="hidden";'><img src="../images/index/skjul_meny.png" width="21" height="20" alt="" align="left" /><div class="navbutend">&nbsp;&nbsp;Lukk</div></td>
                                </tr>
                            </table>
                            </div>
                        </td>
                    </tr>
                </table>
                </form>
            </td>
        </tr>
    </table>
    <div id="nav">
        <% String hide = ""; String open = ""; %>
        <script type="text/javascript" language="JavaScript">
            var menuCookie = getCookie("sesam_menu");
            if (menuCookie == "closed") {
                <% hide = "style='display:none;"; %>
                <% open = ""; %>
            } else {
                <% hide = ""; %>
                <% open = "style='display:none;'"; %>                
            }
        </script>         
        <div id="menutable" <%=hide %>>
            <div class="navMain">
                <div class="navRow">
                    <img src="images/menu/icons/nyheter.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=m" param="category:front_service" index="true"/>' onclick="return strep(this);">Nyhetss&#248;k</a>
                </div>
                <div class="navRow">
                    <img src="images/menu/icons/bedrift.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=y" param="category:front_service" index="true"/>' onclick="return strep(this);">Bedriftss&#248;k</a>
                </div>
                <div class="navRow">
                    <img src="images/menu/icons/person.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=w" param="category:front_service" index="true"/>' onclick="return strep(this);">Persons&#248;k</a>
                </div>
                <div class="navRow">
                    <img src="images/menu/icons/bilde.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=p" param="category:front_service" index="true"/>' onclick="return strep(this);">Bildes&#248;k</a>
                </div>
                <div class="navRow">
                    <img src="images/menu/icons/blogg.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=b" param="category:front_service" index="true"/>' onclick="return strep(this);">Bloggs&#248;k</a>
                </div>
                <div class="navRow">
                    <img src="images/menu/icons/tv.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=t" param="category:front_service" index="true"/>' onclick="return strep(this);">TV-guide</a>
                </div>
                <div class="navRow">
                    <img src="images/menu/icons/webtv.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=wt" param="category:front_service" index="true"/>' onclick="return strep(this);">NettTV-s&#248;k</a>
                </div>
                <div class="navRow">
                    <img src="images/menu/icons/ver.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=sw" param="category:front_service" index="true"/>' onclick="return strep(this);">V&#230;rs&#248;k</a>
                </div>
                <div class="navRow">
                    <img src="images/menu/icons/kart.png" width="16" height="16" alt="" />
                    <a href='<search:linkPulse url="/search/?c=map" param="category:front_service" index="true"/>' onclick="return strep(this);">Karts&#248;k</a><span class="betaNavbar">beta</span>
                </div>
            </div>
            <div id="navToggleHide">    
                <img src="images/index/skjul_meny.png" width="21" height="20" alt="" />
                <span>Skjul meny</span>
            </div>
        </div>
        <div id="menuopen" <%=open %> >
            <div id="navToggleOpen">
                <img src="images/index/vis_meny.png" width="21" height="20" alt="" />
                <span>Vis meny</span>
            </div>
        </div>    
    </div>
    <div id="articles">
        <search:publish page="/pages/front"/>
    </div>
    </div>
    <div style="clear: both;"></div>
    <div id="footer_space_index">
        <search:publish page="/pages/footer"/>
    </div>

        <!-- start gallup (<%=locale  %>) -->
        <% if ( "no".equals(locale) ) { %>        
        <script type='text/javascript' language='JavaScript' src='/javascript/tmv11.js'></script>
        <script type="text/javascript" language="JavaScript"><!--
            var tmsec = new Array(3);
            tmsec[0]="tmsec=sesam";
            tmsec[1]="tmsec=sesamforside";
            tmsec[2]="tmsec=sesamforside";
            getTMqs('','', 'sesam_no', 'no', 'iso-8859-15', tmsec);
            //-->
        </script>
        <noscript><img src="http://statistik-gallup.net/V11***sesam_no/no/iso-8859-15/tmsec=sesam&amp;tmsec=sesamforside&amp;tmsec=sesamforside" alt="" /></noscript>
        <% } %>
        <!-- end gallup -->

    </body>
</html>
</c:if>
