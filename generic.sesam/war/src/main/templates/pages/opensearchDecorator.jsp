<%-- Copyright (2008-2009) Schibsted ASA
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
--%><%@ page contentType="text/xml; charset=utf-8" %><%--
--%><?xml version="1.0" encoding="UTF-8" ?>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>
<search:include include="not-include-anything-but-initialise-layout-variable"/>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
    <InputEncoding>utf-8</InputEncoding>
    <ShortName><c:out value="${layout.properties['opensearchShortName']}"/></ShortName>
    <Description><c:out value="${layout.properties['opensearchDescription']}"/></Description>
    <Image width="16" height="16">data:image/x-icon;base64,<c:out value="${layout.properties['opensearchImage']}"/></Image>
    <Url type="text/html" template="http://<c:out value='${DataModel.site.site.name}'/>search/?q={searchTerms}&amp;c=<c:out value='${$DataModel.page.currentTab.key}'/>"/>
</OpenSearchDescription>
