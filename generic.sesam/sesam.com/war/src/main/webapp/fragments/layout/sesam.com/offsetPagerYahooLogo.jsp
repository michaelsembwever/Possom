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
<c:set var="img_yahooLogo"><search:findResource url="/images/yahoo.gif"/></c:set>
<c:set var="msg_logo_alt"><search:text key="poweredByYahoo"/></c:set>
<img src="${img_yahooLogo}" id="yahooSearch" alt="${msg_logo_alt}" />
</jsp:root>