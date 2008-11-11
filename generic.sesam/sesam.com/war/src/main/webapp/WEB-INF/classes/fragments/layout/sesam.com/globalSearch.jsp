<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:search="urn:jsptld:/WEB-INF/SearchPortal.tld"><!-- XXX a little awkward since this never exists in the skin -->
<!--
 * Copyright (2008) Schibsted Søk AS
 *   This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses />.
 *
    Document   : globalSearch
    Created on : 7/03/2008, 10:27:24
    Author     : mick
    Version    : $Id$
-->
    <c:set var="commandName" value="globalSearch" />
    <c:choose>
        <c:when test="${DataModel.searches[commandName].results.hitCount gt 0}">

            <div id="resultlist">
                <div id="resultHits">
                    <c:set var="msg_international_icon_alt"><search:text key="international_icon_alt" /></c:set>
                    <img src="/images/international_icon.png" width="16" height="16" alt="${msg_international_icon_alt}" />
                    <span>
                        <search:text key="international_search_results"
                            arg0="${DataModel.navigation.navigations.offset.fields.currentPageFromCount}"
                            arg1="${DataModel.navigation.navigations.offset.fields.currentPageToCount}"
                            arg2="${DataModel.searches[commandName].results.hitCount}"
                            arg3="${DataModel.query.xmlEscaped}" />
                    </span>
                </div>

                <c:forEach items="${DataModel.searches[commandName].results.results}" var="item" varStatus="loop">
                    <c:set var="i" value="${loop.count}" />
                    <c:set var="pos" value="${DataModel.navigation.navigations.offset.fields.currentPageFromCount + i}" />

                    <p id="p${i}">
                        <img class="search_big_url_icon" src="http://${item.fields.site}/favicon.ico" width="16" height="16" alt="" />
                        <jsp:text><![CDATA[&nbsp;]]></jsp:text>
                        <c:set var="item_href"><search:boomerang url="${item.fields.clickurl}" param="category:results;pos:${pos}" /></c:set>
                        <a href="${item_href}" class="search_big_url">${item.fields.title}</a>
                        <c:if test="${not empty item.fields.fileformat}">
                            <span class="timestamp" ><search:text key="fileformat_${item.fields.fileformat}" /></span>
                        </c:if>
                        <br />
                        <c:if test="${not empty item.fields.body}">
                            <span class="search_summary">${item.fields.body}</span>
                        </c:if>

                        <span class="search_small_url">
                            ${item.fields.url}

                            <!-- More hits from -->
                            <c:if test="${empty $DataModel.parameters.values.moreHits}">
                              -
                                <c:set var="moreHits_href"><search:boomerang url="/search/?c=${tab.key}&amp;q=${DataModel.searches[commandName].query.utf8UrlEncoded}+site%3A${item.fields.site}&amp;moreHits=true" param="category:results;pos:${pos}" /></c:set>
                                <jsp:element name="a">
                                  <jsp:attribute name="href"><c:out value="${moreHits_href}" escapeXml="true"/></jsp:attribute>
                                  <jsp:attribute name="class">more_hits_link</jsp:attribute>
                                  <jsp:body><search:text key="moreHitsFrom" /></jsp:body>
                                </jsp:element>

                            </c:if>

                            <!-- To website's front page -->
                            <c:set var="site" value="http://${item.fields.site}" />
                            <c:if test="${site ne item.fields.url}">
                              -
                                <c:set var="goTopDomain_href"><search:boomerang url="http://${item.fields.site}/" param="category:navigation;subcategory:site;pos:${pos}" /></c:set>
                                <a href="${goTopDomain_href}" class="search_big_url"><search:text key="goTopDomain" /></a>
                            </c:if>
                        </span>
                    </p>

                </c:forEach>
            </div>

        </c:when>
        <c:otherwise>
            <search:include include="no-hits"  />
        </c:otherwise>
    </c:choose>

</jsp:root>
