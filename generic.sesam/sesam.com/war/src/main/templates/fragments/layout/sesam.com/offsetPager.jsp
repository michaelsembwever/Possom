<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
        xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:c="http://java.sun.com/jsp/jstl/core"
        xmlns:search="urn:jsptld:/WEB-INF/SearchPortal.tld"><!-- XXX a little awkward since SearchPortal.tld never exists in the skin -->
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
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
    Document   : main
    Author     : mick
    Version    : $Id$
-->
<c:set var="pages" value="${DataModel.navigation.navigations['offset'].results}"/>
<c:if test="${pages.size gt 1}">
<!-- #if ($pages.size() > 1) -->
    <div id="offset">
        <search:text key="result_pages"/><jsp:text><![CDATA[&nbsp;]]></jsp:text>
        <ul id="offsetList">
        <c:forEach var="page" items="${pages}">
            <c:choose>
                <c:when test="${page.selected}">
                    <li class="active">${page.title}</li>
                </c:when><c:otherwise>
                    <c:set var="link_page"><search:boomerang url="${page.url}" param="category:navigation;subcategory:paging"/></c:set>                    
                    <li><a href="${link_page}">${item.title}</a></li>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        </ul>
        <search:include template="offsetPagerLogo"/>
    </div>
</c:if>
</jsp:root>