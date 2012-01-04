<%-- Copyright (2012) Schibsted ASA
 *   This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ page 
import="no.sesat.search.util.TradeDoubler,java.net.URLEncoder" %>
<%@ page import="no.sesat.search.datamodel.DataModel" %>


<%
    final DataModel datamodel = (DataModel) session.getAttribute(DataModel.KEY);

    //	session.setAttribute("TDUID", request.getParameter("tduid"));
    Cookie cookie = new Cookie("TRADEDOUBLER", request.getParameter("tduid"));

    int duration = 60 * 60 * 24 * 365;

    cookie.setMaxAge(duration);
    cookie.setPath("/");
    response.addCookie(cookie);

    String sitesearch = request.getParameter("sitesearch");
    if (sitesearch == null) {
        sitesearch = "";
    }
   
    if (request.getParameter("url") != null && request.getParameter("url").length() > 0) {
        response.sendRedirect(request.getParameter("url"));
    } else if (request.getParameter("URL") != null && request.getParameter("URL").length() > 0) {
        response.sendRedirect(request.getParameter("URL"));
    } else if (request.getParameter("alt") != null
            && request.getParameter("alt").length() > 0
            && Integer.parseInt(request.getParameter("alt")) > 0
            && Integer.parseInt(request.getParameter("alt")) < 100) {

        TradeDoubler td = new TradeDoubler(null);

        String organization = TradeDoubler.getOrganization();
        String event = "46755"; // Search lead
        String orderNumber = td.getUUID();
        String currency = "NOK";
        String value = "1";
        String checksum = td.getChecksum(orderNumber, value);
        String tduid = request.getParameter("tduid");

        String lang = request.getParameter("lang");
        if (lang == null || lang.length() == 0) {
            lang = "en";
        }

        String q = request.getParameter("q");

        if (q != null) {
            q = datamodel.getParameters().getValue("q").getUtf8UrlEncoded();
        } else {
            q = "";
        }

        String c = request.getParameter("c");
        String s = request.getParameter("s");
        if (s == null || s.length() == 0) {
            s = "";
        }
        String affId = request.getParameter("affId");
        if (affId == null || affId.length() == 0) {
            affId = "";
        }
        String alt = request.getParameter("alt");
        if (alt == null || alt.length() == 0) {
            alt = "";
        }

        if (sitesearch.length() > 0) {
            sitesearch = "&sitesearch=" + sitesearch;
        }
%>
<html>
<head><title>Sesam</title></head>
<body onLoad="javascript:goSesam()">
<%
   if (!c.equalsIgnoreCase("pss")) {
%>
<img src="http://tbs.tradedoubler.com/report?organization=<%=organization%>&event=<%=event%>&leadNumber=<%=orderNumber%>&orderValue=<%=value%>&currency=<%=currency%>&checksum=v04<%=checksum%>&tduid=<%=tduid%>" alt="">
<%
   }
%>
<script type="text/javascript" language="JavaScript">
	function goSesam() {
		document.location="http://www.sesam.no/search/?c=<%= c %>&lang=<%= lang %>&s=<%= s %>&q=<%= q %>&ss_ss=<%= affId %>&ss_lt=tradedoubler&ss_pid=ad_td&ss_sec=<%= alt %><%= sitesearch %>";
	}
</script>
</body>
</html>
<%
	} else {
		response.sendRedirect("http://www.sesam.no/");
	}
%>
