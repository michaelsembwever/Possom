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
 --%><%
 {
	 final String userAgent = request.getHeader("User-Agent");
	 String showid = request.getParameter("showId");
	 String charset = "utf-8";
	
	 if (userAgent.indexOf("Windows") != -1) {
	     charset = "iso-8859-1";
	 }
	 if (showid == null) {
	     showid = "";
	 }
	 
	 response.setCharacterEncoding(charset);
	 response.setContentType("text/x-vcard; charset=" + charset);
	 response.setHeader("Content-Disposition","attachment;filename=vcard-" + showid + ".vcf");
 }
 %>
 <%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %><%--
 
 --%><% String currentC = (String) request.getAttribute("c"); %><%--
 --%><%if (currentC.equals("yvc") || currentC.equals("yip")) { %><search:velocity template="/results/vcardYellow"/><% } else { %><search:velocity template="/results/vcardWhite"/><%}%>      
