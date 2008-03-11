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
    Document   : main
    Author     : mick
    Version    : $Id$
--%>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:search="urn:jsptld:/WEB-INF/SearchPortal.tld"><%-- XXX a little awkward since this never exists in the skin --%>
<jsp:output 
    doctype-root-element="html" 
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" 
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" />
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <search:include include="head-element"/>
</head>
<body>
    <div id="frame">
        <div id="header">
            <search:include include="top-col-one" />
            <search:include include="top-col-two" />
            <search:include include="top-col-three" />
        </div>
        <table cellspacing="0" cellpadding="0" style="width: 100%;">
            <tr>
                <td style="vertical-align: top; border-right: 1px solid #DADADA;">
                    <div id="main">
                        <div id="dummy">&nbsp;</div>
                        <search:include include="main-col-one" />
                        <search:include include="main-col-two" />
                        <search:include include="main-col-three" />
                        <search:include include="main-col-four" />
                    </div>
                </td>
                <td style="vertical-align: top;">
                    <div id="sidebar">
                        <search:include include="sidebar-col-one" />
                        <search:include include="sidebar-col-two" />
                        <search:include include="sidebar-col-three" />                   
                    </div>
                </td>
            </tr>
        </table>
        <div id="footer">
            <search:include include="bottom-col-one" />
            <search:include include="bottom-col-two" />
            <search:include include="bottom-col-three" />
            <search:include include="bottom-col-four" />
        </div>      
    </div>

</body>
</html>
</jsp:root>