<%-- Copyright (2006-2008) Schibsted Søk AS
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
  -- Handles the logic on which "templating system" will be apropriate for this http request.
  --
  -- @author <a href="mailto:mick@semb.wever.org">Michael Semb Wever</a>
  -- @version $Id$
  --%><%
 {
      if (request.getParameter("encoding") != null && request.getParameter("encoding").equals("iso-8859-1")){
          response.setContentType("text/html; charset=iso-8859-1"); // for external javascript document.write(), where server uses iso encoding
      }
      else {
          response.setContentType("text/html; charset=UTF-8");
      }
 }
  %><%--
  --%><%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %><%--
  --%>
 <search:main/> 