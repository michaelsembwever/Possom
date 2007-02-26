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
        <search:velocity template="/fragments/head"/>
    </head>

    <body onload="sesamInit('<c:out value="${c}"/>', '<c:out value="${vertikal}"/>', '<c:out value="${queryHTMLEscaped}"/>', <c:out value="${null != page}"/>);">


        <c:choose>
            <%-- old-school sitesearch [REMOVE ME-start] --%>
            <c:when test="${c == 'd' && (param.ss == 'di' || param.ss == 'pr' || param.ss == 'im' || param.ss == 'af' || param.ss == 'fv' || param.ss == 'aa' || param.ss == 'bt' || param.ss == 'sa')}">

                <div id="frame">
                    <div id="header">
                        <search:velocity template="legacy/skin/headers/${param.ss}"/>
                        <search:include include="search-bar"/>
                    </div>
                    <div id="content_ss">
                        <div id="globalmenu_table"><img src="../images/pix.gif" width="1" height="6" alt="" /></div>
                        <div id ="content_top">
                            <dl>
                                <dt>
                                    <span class="sitename">
                                        <c:choose>
                                            <c:when test="${param.ssr == 'ds'}"> Dinside:</c:when>
                                            <c:when test="${param.ssr == 'di'}"> Digi:</c:when>
                                            <c:when test="${param.ssr == 'pr'}"> Propaganda:</c:when>
                                            <c:when test="${param.ssr == 'it'}"> Itavisen:</c:when>
                                            <c:when test="${param.ssr == 'im'}"> iMarkedet:</c:when>
                                            <c:when test="${param.ssr == 'nrk'}"> NRK:</c:when>
                                            <c:when test="${param.ssr == 'af'}"> Aftenposten:</c:when>
                                            <c:when test="${param.ssr == 'fv'}"> F&#230;drelandsvennen:</c:when>
                                            <c:when test="${param.ssr == 'aa'}"> no:</c:when>
                                            <c:when test="${param.ssr == 'bt'}"> bt.no:</c:when>
                                            <c:when test="${param.ssr == 'sa'}"> Stavanger Aftenblad::</c:when>
                                            <c:when test="${param.ssr == 'd'}"> Nettet:</c:when>
                                        </c:choose>
                                    </span>
                                    <search:include include="greybar-sitesearch"/>
                                </dt>
                                <dd><search:include include="greybar-ad"/></dd>
                            </dl>
                        </div>
                        <div class="greybar_line"><img src="../images/pix.gif" width="1" height="1" alt="" /></div>
                        <%--sesam search in sitesearch modus--%>
                        <div id="content_left_ss">
                            <search:include include="fast-results"/>
                        </div>
                        <div id="content_right_ss">
                            <search:include include="ads"/>
                        </div>
                    </div>
                    <div id="footer_ss">
                        <search:include include="offsetPager"/>
                    </div>
                </div><%-- old-school sitesearch [REMOVE ME-end] --%>







        </c:when>
        <c:otherwise>
            <%-- This is the real thing. The new templating system all should use. --%>

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

                                <c:if test="${tab.showRss}">
                                    <search:include include="left-col-four"/>
                                </c:if>
                            </td>
                            <td class="cell_three" valign="top" colspan="2">
                        </c:otherwise>
                    </c:choose>

                    <search:include include="middle-col-one"/>

                    <search:include include="spellcheck"/>
                    <search:include include="middle-col-two"/>

                    <search:include include="middle-col-three"/>

                    <search:include include="middle-col-four"/>

                    </td>
                    <td class="cell_four">
                        <search:include include="right-col-one"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td colspan="2">
                        <search:include include="offsetPager"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <search:include include="top3-ads-bottom"/>
                    </td>
                    <td>&nbsp;</td>
                </tr>

            </table>
            <search:include include="verbosePager"/>
            <search:include include="footer"/>

            </c:otherwise>
        </c:choose>

        <search:include include="map-script"/>

        <search:include include="gallup" />
    </body>
</html>
