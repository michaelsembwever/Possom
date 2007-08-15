
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
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <search:include include="head-element"/>
</head>
<body>
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
                <search:include include="error-msg"/>
                <search:include include="middle-col-one"/>
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
    </table>
    <search:include include="bottom-one"/>
    <search:include include="bottom-two"/>
    <search:include include="bottom-three"/>
    <search:include include="bottom-four"/>
    <search:include include="bottom-five"/>
    <search:include include="bottom-six"/>
    <search:include include="bottom-seven"/>
    <search:include include="bottom-eight"/>
</body>
</html>
