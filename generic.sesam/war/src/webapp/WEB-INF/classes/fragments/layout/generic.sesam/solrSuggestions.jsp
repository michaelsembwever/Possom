<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">
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
     *   Version    : $Id$
     *
     * See generic.sesam/war/src/main/javascript/external/mwsuggest.js
     * Simple way of manually writing out the json array. Alternative would be to use json-taglib.
    -->
    <jsp:text><![CDATA[[]]>"</jsp:text><c:out value="${DataModel.query.utf8UrlEncoded}"/><jsp:text>",</jsp:text>
    <jsp:text><![CDATA[[]]></jsp:text><c:forEach var="item" items="${DataModel.searches['solrSuggestions'].results.results}">
        <jsp:text>"</jsp:text><c:out value="${item.fields.list_entry}"/><jsp:text>",</jsp:text>
    </c:forEach><jsp:text>""<![CDATA[]]]]></jsp:text>
</jsp:root>