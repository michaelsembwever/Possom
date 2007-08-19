<%-- Copyright (2006-2007) Schibsted Søk AS --%>
<%@ page
        language="java"
        errorPage="/internal-error.jsp"
        contentType="text/html; charset=utf-8"
        pageEncoding="UTF-8"
        %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>
<%@ page import="no.sesat.search.site.Site"%>

<%
    final Site site = (Site)request.getAttribute(Site.NAME_KEY);
    String locale = site.getLocale().toString();
    String openSearchUrlStandard = "/search/?q=*&amp;c=d&amp;output=opensearch";
%>

<search:velocity template="/pages/index"/>

<c:if test="${!empty Missing_pagesindex_Template}">
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>Sesam</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link href="/css/front.css" rel="stylesheet" type="text/css" />
        <link rel="icon" href="/images/favicon.gif" type="image/x-icon" />
        <link rel="shortcut icon" href="/images/favicon.gif" type="image/x-icon" />
        <link rel="search" type="application/opensearchdescription+xml" title="Sesam.no" href="<%= openSearchUrlStandard %>%>" />
        <script type="text/javascript" language="JavaScript" src="/javascript/external/prototype.js"></script>
        <script type="text/javascript" language="JavaScript" src="/javascript/index.js"></script>
        <script type="text/javascript" language="JavaScript" src="/javascript/callAtIntervals.js"></script>
        <script type="text/javascript" language="JavaScript" src="/javascript/openSearch.js"></script>
    </head>
    <body>
        <img src="images/index/logo.png" id="logoIndex" width="215" height="61" alt="Sesamlogo" />
        <img src="images/index/menu_top.png" id="menuTop" width="151" height="23" alt="Sesams&#248;k" />
        <form name="sf" action="<search:boomerang url="/search/" param="category:front_search"/>" onsubmit='return check();'>
        <div id="searchbar">
            <div id="menuMiddle">
                <img src="images/index/nettsok_30_30.gif" width="30" height="30" alt="Netts&#248;k logo" />
                Netts&#248;k
            </div>
            <input type="text" name="q" id="searchInput" onfocus="this.form.q.select();" />
            <script type="text/javascript"><!--
                var focusControl = document.forms["sf"].elements["q"];
                if (focusControl.type != "hidden" && !focusControl.disabled) {
                    focusControl.focus();
                } // -->
            </script>                
            <input type="image" id="formSubmit" src="images/searchbar/nett.png" alt="" />
            <img src="images/searchbar/dropdown.png" id="dropdownImg" width="19" height="24" alt="Pil ned" />
            <a id="searchtip" href='<search:boomerang url="/search/?q=&amp;page=/pages/82/index" param="category:static;subcategory=header"/>'>S&#248;ketips</a>
            <div id="searchRadio">
                <input type="radio" name="c" value="d" checked="checked" /> Norge &nbsp;&nbsp;
                <input type="radio" name="c" value="g" /> Verden
            </div>
            <div id="front_button_layer">
                <div id="cn" class="nb">Nyhetss&#248;k</div>
                <div id="cy" class="nb">Bedriftss&#248;k</div>
                <div id="cw" class="nb">Persons&#248;k</div>
                <div id="cp" class="nb">Bildes&#248;k</div>
                <div id="cb" class="nb">Bloggs&#248;k</div>
                <div id="ct" class="nb">TV-guide</div>
                <div id="cwt" class="nb">NettTV-s&#248;k</div>
                <div id="csw" class="nb">V&#230;rs&#248;k</div>
                <div id="cmap" class="nb">Karts&#248;k</div>
                <div id="closeDD">
                    <img src="../images/index/skjul_meny.png" width="21" height="20" alt="Skjul meny" />
                    <span>Lukk</span>
                </div>
            </div>
        </div>
    </form>
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
                <div>
                    <img src="images/menu/icons/nyheter.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="/search/?c=m" param="category:front_service"/>' onclick="return strep(this);">Nyhetss&#248;k</a>
                </div>
                <div>
                    <img src="images/menu/icons/bedrift.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="/search/?c=y" param="category:front_service"/>' onclick="return strep(this);">Bedriftss&#248;k</a>
                </div>
                <div>
                    <img src="images/menu/icons/person.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="/search/?c=w" param="category:front_service"/>' onclick="return strep(this);">Persons&#248;k</a>
                </div>
                <div>
                    <img src="images/menu/icons/bilde.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="/search/?c=p" param="category:front_service"/>' onclick="return strep(this);">Bildes&#248;k</a>
                </div>
                <div>
                    <img src="images/menu/icons/blogg.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="/search/?c=b" param="category:front_service"/>' onclick="return strep(this);">Bloggs&#248;k</a>
                </div>
                <div>
                    <img src="images/menu/icons/tv.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="/search/?c=t" param="category:front_service"/>' onclick="return strep(this);">TV-guide</a>
                </div>
                <div>
                    <img src="images/menu/icons/webtv.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="/search/?c=wt" param="category:front_service"/>' onclick="return strep(this);">NettTV-s&#248;k</a>
                </div>
                <div>
                    <img src="images/menu/icons/ver.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="/search/?c=sw" param="category:front_service"/>' onclick="return strep(this);">V&#230;rs&#248;k</a>
                </div>
                <div>
                    <img src="images/menu/icons/kart.png" width="16" height="16" alt="" />
                    <a href='<search:boomerang url="http://kart.sesam.no/search/?c=map" param="category:front_service"/>' onclick="return strep(this);">Karts&#248;k</a><span class="betaNavbar">beta</span>
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
	<search:velocity template="/fragments/layout/publish/index"/>
    </div>
    </div>
    <div style="clear: both;"></div>
    <div id="footer_space_index">
        <search:velocity template="/fragments/layout/publish/footer"/>
    </div>

    <!-- start gallup -->
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
