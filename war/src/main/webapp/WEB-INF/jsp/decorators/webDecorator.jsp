<%-- Copyright (2006-2007) Schibsted Søk AS
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
  -- Handles the logic on which "templating system" will be apropriate for this http request.
  --
  -- @author <a href="mailto:mick@semb.wever.org">Michael Semb Wever</a>
  -- @version $Id$
  --%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>

<%-- Option: allow the skin to define the toplevel template main.vm --%>
<search:velocity template="/pages/main"/>
<c:if test="${! empty Missing_pagesmain_Template}">
    <c:choose>
        <c:when test="${!empty param.layout}">
            <c:set var="layout" value="{tab.layouts[param.layout]}" scope="request"/>
        </c:when>
        <c:otherwise>
            <c:set var="layout" value="${tab.defaultLayout}" scope="request"/>
        </c:otherwise>
    </c:choose>
    <c:choose>
        <%-- Use any tab layout's custom front page if the query object does not exist --%>
        <c:when test="${DataModel.query.query.blank && !empty layout.front}">
            <search:velocity template="/pages/${layout.front}"/>
        </c:when>
        <%-- Use any tab layout's custom main page --%>
        <c:when test="${!empty layout.main}">
            <search:velocity template="/pages/${layout.main}"/>
        </c:when>
        <%-- Otherwise use the default templating layout --%>
        <c:otherwise>
            <%@ include file="httpDecorator.jsp" %>
        </c:otherwise>
    </c:choose>
</c:if>
