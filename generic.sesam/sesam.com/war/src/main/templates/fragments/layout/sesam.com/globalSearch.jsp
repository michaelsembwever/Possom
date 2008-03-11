<?xml version="1.0" encoding="UTF-8"?><%-- 
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
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
    Document   : globalSearch
    Created on : 7/03/2008, 10:27:24
    Author     : mick
    Version    : $Id$
--%>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core_rt"
    xmlns:search="urn:jsptld:/WEB-INF/SearchPortal.tld"><%-- XXX a little awkward since this never exists in the skin --%>

    <c:test>
        <c:when test="${datamodel.searches[commandName].results.hitCount gt 0}">

            <div id="resultlist">
                <div id="resultHits">
                    <c:set var="msg_international_icon_alt"><search:text key="international_icon_alt"/></c:set>
                    <img src="/images/menu/icons/nettsok.png" width="16" height="16" alt="${msg_international_icon_alt}"/>
                    <span>                        
                        <c:set var="hitcount"><search:hitcount hitcount="${$datamodel.searches[commandName].results.hitCount}"/></c:set>
                        <search:text key="international_search_results" 
                            arg0="${datamodel.navigation.navigations.offset.fields.currentPageFromCount}" 
                            arg1="${datamodel.navigation.navigations.offset.fields.currentPageToCount}" 
                            arg2="${hitcount}" 
                            arg3="${datamodel.query.utf8UrlEncoded}"/>
                    </span>
                    
                    <c:set var="msg_rss_link"><search:text key="rss_link"/></c:set>
                    <a href=""><search:text key="atom_link"/></a>
                    <a href="">${msg_rss_link}</a>
                    <img src="/images/rssTest.jpg" id="rss" alt="${msg_rss_link}"/>
                    <div class="clear"></div>
                </div>

                <c:foreach items="${datamodel.searches[commandName].results.results}" var="item" index="i">
                    <c:set var="pos" value="${datamodel.navigation.navigations.offset.fields.currentPageFromCount + i}"/>               

                    <p id="p${i}">
                        <jsp:element name="a">
                            <jsp:attribute name="href"><search:boomerang url="${item.fields.url}" param="category:results;pos:${pos}"/></jsp:attribute>
                            <jsp:attribute name="class">search_big_url</jsp:attribute>
                            <jsp:body>${item.fields.title}</jsp:body> <%-- FIXME needs triming at like 70 characters --%>
                        </jsp:element>                        
                        <br/>

                        <c:if test="${not empty item.fields.fileformat}">
                            <span class="timestamp"><search:text key="fileformat_${item.fields.fileformat}"/></span>
                        </c:if>
                        <c:if test="${not empty item.fields.body}">
                            <span class="search_summary">${item.fields.body}</span>
                        </c:if>

                        <span class="search_small_url">
                            ${item.fields.body}

                            <%-- More hits from --%>
                            <c:if test="${not empty $datamodel.parameters.values.domain and $datamodel.parameters.values.domain.xmlEscaped eq $item.fields.site}">
                              -
                                <jsp:element name="a">
                                    <jsp:attribute name="href"><search:boomerang url="/search/?c=${tab.key}&amp;q=${datamodel.searches[$commandName].query.utf8UrlEncoded}&domain=${item.fields.site}" param="category:results;pos:${pos}"/></jsp:attribute>
                                    <jsp:attribute name="class">more_hits_link</jsp:attribute>
                                    <jsp:body><search:text key="moreHitsFrom"/></jsp:body>
                                </jsp:element> 
                            </c:if>

                            <%-- To website's front page --%>
                            <c:set var="site" value="http://${item.fields.site}"/>
                            <c:if test="${site ne item.fields.url}">
                              -
                                <jsp:element name="a">
                                    <jsp:attribute name="href"><search:boomerang url="http://${item.fields.site}/" param="category:navigation;subcategory:site;pos:${pos}"/></jsp:attribute>
                                    <jsp:attribute name="class">search_big_url</jsp:attribute>
                                    <jsp:body><search:text key="goTopDomain"/></jsp:body>
                                </jsp:element> 
                            </c:if>
                        </span>
                    </p>

                </c:foreach>
            </div>

        </c:when>
        <c:otherwise>
            <search:include include="no-hits" />
        </c:otherwise>
    </c:test>

</jsp:root>
