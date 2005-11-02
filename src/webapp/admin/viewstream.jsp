<%@ page import="java.util.*,
                 com.opensymphony.clickstream.Clickstream,
                 com.opensymphony.clickstream.ClickstreamRequest" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
if (request.getParameter("sid") == null)
{
	response.sendRedirect("clickstreams.jsp");
	return;
}

Map clickstreams = (Map)application.getAttribute("clickstreams");

Clickstream stream = null;

if (clickstreams.get(request.getParameter("sid")) != null)
{
	stream = (Clickstream)clickstreams.get(request.getParameter("sid"));
}

if (stream == null)
{
	response.sendRedirect("clickstreams.jsp");
	return;
}
%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
        <title>Clickstream for <%= stream.getHostname() %></title>
    </head>

    <body>
        <p align="right"><a href="clickstreams.jsp">All streams</a></p>

        <h3>Clickstream for <%= stream.getHostname() %></h3>

        <b>Initial Referrer</b>: <a href="<%= stream.getInitialReferrer() %>"><%= stream.getInitialReferrer() %></a><br>
        <b>Hostname</b>: <%= stream.getHostname() %><br>
        <b>Session ID</b>: <%= request.getParameter("sid") %><br>
        <b>Bot</b>: <%= stream.isBot() ? "Yes" : "No" %><br>
        <b>Stream Start</b>: <%= stream.getStart() %><br>
        <b>Last Request</b>: <%= stream.getLastRequest() %><br>

        <% long streamLength = stream.getLastRequest().getTime() - stream.getStart().getTime(); %>
        <b>Session Length</b>:
        	<%= (streamLength > 3600000 ?
        		" " + (streamLength / 3600000) + " hours" : "") +
        	(streamLength > 60000 ?
        		" " + ((streamLength / 60000) % 60) + " minutes" : "") +
        	(streamLength > 1000 ?
        		" " + ((streamLength / 1000) % 60) + " seconds" : "") %><br>

        <b># of Requests</b>: <%= stream.getStream().size() %>

        <h3>Click stream:</h3>

        <ol>
        <%
        synchronized(stream) {
            Iterator clickstreamIt = stream.getStream().iterator();

            while (clickstreamIt.hasNext())
            {
                String click = ((ClickstreamRequest)clickstreamIt.next()).toString();
            %>
            <li><a href="http://<%= click %>"><%= click %></a></li>
            <%
            }
        }
        %>
        </table>
    </body>
</html>