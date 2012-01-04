<%-- Copyright (2012) Schibsted ASA
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
--%>
<%
	if (request.getParameter("onr") != null && request.getParameter("onr").length() > 0){
		Cookie c = new Cookie("TRADEDOUBLER-onr", request.getParameter("onr"));
		int duration = 60 * 60; // Valid one hour
		c.setMaxAge(duration);
		c.setPath("/");
		response.addCookie(c);
	}

	if (request.getParameter("checksum") != null && request.getParameter("checksum").length() > 0){
		Cookie c = new Cookie("TRADEDOUBLER-cs", request.getParameter("checksum"));
		int duration = 60 * 60;
		c.setMaxAge(duration);
		c.setPath("/");
		response.addCookie(c);
	}
	
	if (request.getParameter("reportInfo") != null && request.getParameter("reportInfo").length() > 0){
		Cookie c = new Cookie("TRADEDOUBLER-ri", request.getParameter("reportInfo"));
		int duration = 60 * 60;
		c.setMaxAge(duration);
		c.setPath("/");
		response.addCookie(c);
	}
	if (request.getParameter("url") != null && request.getParameter("url").length() > 0){
		response.sendRedirect(request.getParameter("url"));
	} else {
		response.sendRedirect("http://www.sesam.no/");
	}
%>	
