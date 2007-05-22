<%-- Copyright (2006-2007) Schibsted Søk AS
  -- 
  -- Handles the logic on which "templating system" will be apropriate for this http request.
  -- @author <a href="mailto:mick@semb.wever.org">Michael Semb Wever</a>
  -- @version $Id$
  --%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>

<%-- Option: allow the skin to define the toplevel template main.vm --%>
<search:velocity template="/pages/main"/>
<c:if test="${! empty Missing_pagesmain_Template}">
    <c:choose>
        <%-- Use any tab layout's custom front page if the query object does not exist --%>
        <c:when test="${DataModel.query.query.blank && !empty tab.layout.front}">
            <search:velocity template="/fragments/layout/frontpages/${tab.layout.front}"/>
        </c:when>
        <%-- Use any tab layout's custom main page --%>
        <c:when test="${!empty tab.layout.main}">
            <search:velocity template="/pages/${tab.layout.main}"/>
        </c:when>
        <%-- Otherwise use the default templating layout --%>
        <c:otherwise>
            <%@ include file="httpDecorator.jsp" %>
        </c:otherwise>
    </c:choose>
</c:if>