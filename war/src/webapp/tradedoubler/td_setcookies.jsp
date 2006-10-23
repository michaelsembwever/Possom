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