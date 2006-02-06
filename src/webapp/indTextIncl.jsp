<%@ page import="no.schibstedsok.front.searchportal.servlet.SearchServlet"%>
<%@ page import="no.schibstedsok.front.searchportal.site.Site"%>
<%@ page
        language="java"
        errorPage="/internal-error.jsp"
        contentType="text/html; charset=utf-8"
        pageEncoding="UTF-8"
        %>
<%
   final Site site = SearchServlet.getSite(request);
%>

<div class="index_center">

    <div id="news">
        <a href="search/?c=d&amp;toolbar=true&amp;pg=1&amp;q="><img src="<%= site.getImageDir() %>/index/sesamToolbar.gif" alt="" /></a>
        <img src="<%= site.getImageDir() %>/pix.gif" width="100%" height="1" class="lightdots" style="display: block;" alt="" />
        <div class="news_text">
            N&#229; kan du f&#229; tilgang til Sesam uansett hvor du er p&#229; nettet. Det tar bare noen sekunder &#229; installere - og den er helt gratis!<br />
            <a href="search/?c=d&amp;toolbar=true&amp;pg=1&amp;q=">Mer om Sesam verktøylinje</a>
        </div>
    </div>

    <img src="<%= site.getImageDir() %>/index/komtilsaken.jpg" alt="" /><br />
    <img src="<%= site.getImageDir() %>/pix.gif" width="100%" height="1" class="lightdots" alt="" />
    <div id="container">
        <div id="content-left">
            <dl>
                <dt><a href="search/?nav_sources=contentsourcenavigator&amp;c=m&amp;contentsource=Norske Nyheter&amp;userSortBy=datetime&amp;q="><img src="<%= site.getImageDir() %>/index/m.jpg" alt="Nyheter" /></a></dt>
                <dd>
                    <span class="bold_text">Nyheter:</span>&nbsp;S&#248;k p&#229; et aktuelt tema:<br />
                    <a href="search/?lang=en&amp;c=d&amp;q=Nyheter+OL&amp;s=d">Nyheter OL</a><br />&nbsp;
                </dd>
                <dt><a href="search/?c=w&amp;q="><img src="<%= site.getImageDir() %>/index/w.jpg" alt="Personer" /></a></dt>
                <dd>
                    <span class="bold_text">Persons&#248;k:</span>&nbsp;S&#248;k p&#229; et telefonnummer, person eller adresse:<br />
                    <a href="search/?c=d&amp;q=Harald Dronningensgate">Harald Dronningensgate</a>
                </dd>
            </dl>
        </div>
        <div id="content-right">
            <dl>
                <dt><span><a href="search/?c=y&amp;q="><img src="<%= site.getImageDir() %>/index/y.jpg" alt="Bedrifter" /></a></span></dt>
                <dd>
                    <span class="bold_text">Bedriftss&#248;k:</span>&nbsp;S&#248;k p&#229; firmanavn eller en bransje pluss et sted:<br />
                    <a href="search/?c=d&amp;q=23106600">23106600</a>
                    eller
                    <a href="search/?c=d&amp;q=Elektriker &#197;lesund">Elektriker &#197;lesund</a>
                </dd>
                <dt><span><a href="search/?c=p&amp;q="><img src="<%= site.getImageDir() %>/index/p.jpg" alt="Bilder" /></a></span></dt>
                <dd>
                    <span class="bold_text">Bildes&#248;k:</span>&nbsp;S&#248;k p&#229; en person, ting eller tema:<br />
                    <a href="search/?lang=en&amp;c=d&amp;q=Bilder+skispor&amp;s=d">Bilder skispor</a>
                </dd>
            </dl>
        </div>
    </div>

    <div id="market">
        <img src="<%= site.getImageDir() %>/index/moromedsesam.jpg" alt="" />
        <img src="<%= site.getImageDir() %>/pix.gif" width="100%" height="1" class="lightdots" style="display: block" alt="" />
        <div class="valign">
            <a href="http://www.sesam.no/spill/katapult/"><img src="<%= site.getImageDir() %>/index/katapult.jpg" alt="Katapult mann" /></a>&nbsp;
            <a href="http://www.sesam.no/spill/katapult/">Spill Sesam Katapult. Vinn PSP!</a>
        </div>
    </div>
</div>



