<%@page import="no.schibstedsok.searchportal.util.TradeDoubler,no.schibstedsok.searchportal.util.QueryStringHelper,java.net.URLEncoder" %>
<%@ page import="no.schibstedsok.searchportal.datamodel.DataModel" %>
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
        String q = URLEncoder.encode(datamodel.getQuery().getUtf8UrlEncoded());
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
