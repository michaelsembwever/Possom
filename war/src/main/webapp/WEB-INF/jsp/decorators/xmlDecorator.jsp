<%-- Copyright (2008) Schibsted Søk AS
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
--%><%@ page pageEncoding="UTF-8" contentType="text/xml" %><%--
--%><%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %><%--
--%><%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %><%--
--%><?xml version="1.0" encoding="UTF-8"?>
<search:velocity template="xml/${tab.id}"/>
<c:if test="${!empty Missing_xml$tab.id_Template}">
 <search:main/>
</c:if>