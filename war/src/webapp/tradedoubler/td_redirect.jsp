<%@ page import="no.schibstedsok.searchportal.util.TradeDoubler,java.net.URLEncoder" %>
<%@ page import="no.schibstedsok.searchportal.datamodel.DataModel" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.StringTokenizer" %>
<%!

    // TODO This is cutnpaste from a private method in DataModelFilter.
    // That function needs to be public and put in a requst helper class or similar.
    // AndersJ  ?

     /** A safer way to get parameters for the query string.
     * Handles ISO-8859-1 and UTF-8 URL encodings.
     *
     * @param request The servlet request we are processing
     * @param parameter The parameter to retrieve
     * @return The correct decoded parameter
     *
     * @author <a href="mailto:anders.johan.jamtli@sesam.no">Anders Johan Jamtli</a>
     */
  static String getParameterSafely(final HttpServletRequest request, final String parameter){

        final StringTokenizer st = new StringTokenizer(request.getQueryString(), "&");
        String value = request.getParameter(parameter);
        String queryStringValue = null;

        final String parameterEquals = parameter + '=';
        while(st.hasMoreTokens()) {
            final String tmp = st.nextToken();
            if (tmp.startsWith(parameterEquals)) {
                queryStringValue = tmp.substring(parameterEquals.length());
                break;
            }
        }

        if (null != value && null != queryStringValue) {

            try {
                final String encodedReqValue = URLEncoder.encode(value, "UTF-8")
                        .replaceAll("[+]", "%20")
                        .replaceAll("[*]", "%2A");

                queryStringValue = queryStringValue
                        .replaceAll("[+]", "%20")
                        .replaceAll("[*]", "%2A");

                if (!queryStringValue.equalsIgnoreCase(encodedReqValue)){
                    value = URLDecoder.decode(queryStringValue, "ISO-8859-1");
                }

            } catch (UnsupportedEncodingException e) {
                 throw new RuntimeException(e.getMessage());
            }
        }

        return value;
    }
%>
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
            q = getParameterSafely(request, "q");
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
