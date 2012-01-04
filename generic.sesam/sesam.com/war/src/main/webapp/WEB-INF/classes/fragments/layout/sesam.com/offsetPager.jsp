<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
        xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:c="http://java.sun.com/jsp/jstl/core"
        xmlns:search="urn:jsptld:/WEB-INF/SearchPortal.tld"><!-- XXX a little awkward since SearchPortal.tld never exists in the skin -->
<!-- 
 * Copyright (2012) Schibsted ASA
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
 *
    Document   : main
    Author     : mick
    Version    : $Id$
-->
<c:if test="${DataModel.navigation.navigations['offset'].resultsSize gt 1}">
    <c:set var="pages" value="${DataModel.navigation.navigations['offset'].results}"/>
    <div id="offset">
        <span class="active"><search:text key="result_pages"/><jsp:text><![CDATA[&nbsp;]]></jsp:text></span>
        <c:forEach var="page" items="${pages}">
            <c:choose>
                <c:when test="${page.selected}">
                    <span class="active">${page.title}</span>
                </c:when><c:otherwise>
                    <c:set var="link_page"><search:boomerang url="${page.url}" param="category:navigation;subcategory:paging"/></c:set>                    
                    <a href="${link_page}"><span>${page.title}</span></a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </div>
</c:if>
</jsp:root>