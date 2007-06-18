<%-- Copyright (2006-2007) Schibsted Søk AS
  -- 
  -- The Homegrown Templating System.
  -- @author <a href="mailto:mick@semb.wever.org">Michael Semb Wever</a>
  -- @version $Id$
--%>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <search:include include="head-element"/>
</head>

<body>

<%-- FIXME please remove the following scriptlet code (on the right) from this jsp --%>
                                                                                                                        <% // If velocity debug is turned on, display developer bar .
                                                                                                                            // TODO Move to wanted place or add show/hide function to the developerbar
                                                                                                                            if ("true".equals(System.getProperty("VELOCITY_DEBUG")) && !"true".equals(System.getProperty("VELOCITY_DEVELOPERBAR_HIDDEN"))) {
                                                                                                                                String loadPath = System.getProperty("VELOCITY_DEBUG_TEMPLATE_DIR");
                                                                                                                                if (loadPath == null) {
                                                                                                                                    loadPath = "(URLResourceLoader)";
                                                                                                                                }
                                                                                                                        %>
                                                                                                                        <%
                                                                                                                            boolean VELOCITY_DEBUG_ON = "true".equals(System.getProperty("VELOCITY_DEBUG_ON"));
                                                                                                                            String background = "#DDDDDD";
                                                                                                                            String color = "black";
                                                                                                                            String linkText = VELOCITY_DEBUG_ON ? "Off" : "On";
                                                                                                                        %>

                                                                                                                        <div width="100%" style="font-size:12px;background-color:<%=background%>;border:1px solid #C0C0C0; color:<%=color%>">
                                                                                                                            DeveloperBar:
                                                                                                                            VelocityDebug: <a style="color: blue" href="/servlet/VelocityDebug?<%= request.getQueryString()%>"><%= linkText %>
                                                                                                                            </a>
                                                                                                                            |
                                                                                                                            TemplatePath:  <%= loadPath %>
                                                                                                                        </div>


                                                                                                                        <%
                                                                                                                            }
                                                                                                                        %>

<search:include include="topBar"/>
<search:include include="header"/>

<table cellspacing="0" cellpadding="0" id="body_table">
    <tr>
        <c:choose>
        <c:when test="${tab.layout.properties['left-column-hide'] == 'true'}">
        <td valign="top" colspan="3">
            </c:when>
            <c:otherwise>
        <td class="cell_one" valign="top">
            <search:include include="left-col-one"/>

            <search:include include="left-col-two"/>

            <search:include include="left-col-three"/>

            <search:include include="left-col-four"/>

            <search:include include="left-col-five"/>
        </td>
        <td class="cell_three" valign="top" colspan="2">
            </c:otherwise>
            </c:choose>

            <search:include include="middle-col-one"/>

            <search:include include="spellcheck"/>
            <search:include include="middle-col-two"/>
            <search:include include="middle-col-three"/>
            <search:include include="middle-col-four"/>
            <search:include include="middle-col-five"/>

        </td>
        <td class="cell_four">
            <search:include include="right-col-one"/>
            <search:include include="right-col-two"/>
        </td>
    </tr>
    <search:include include="bottom-ads"/>
    <c:if test="${tab.layout.properties['offset-pager-hide'] != 'true'}">
        <tr>
            <td>&nbsp;</td>
            <td colspan="2">
                <search:include include="offsetPager"/>
            </td>
        </tr>
    </c:if>
    <c:if test="${tab.layout.properties['top3-ads-bottom-hide'] != 'true'}">
        <tr>
            <td>&nbsp;</td>
            <td>
                <search:include include="top3-ads-bottom"/>
            </td>
            <td>&nbsp;</td>
        </tr>
    </c:if>
</table>
<search:include include="verbosePager"/>
<search:include include="footer"/>
<search:include include="bottom-one"/>

<search:include include="map-script"/>

<search:include include="gallup"/>
</body>
</html>
