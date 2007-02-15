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

        <%-- Option: use the tab's layout and datamodel --%>
        <c:when test="${tab.layout != null}">
            <%@ include file="httpDecorator.jsp" %>
        </c:when>
        <%-- Last Option: fallback to the original mainDecorator.jsp --%>
        <c:otherwise>
            <%@ include file="legacyDecorator.jsp" %>
        </c:otherwise>
    </c:choose>
</c:if>