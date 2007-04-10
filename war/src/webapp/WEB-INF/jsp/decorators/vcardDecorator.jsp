<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %><% String currentC = (String) request.getAttribute("c"); %><%if (currentC.equals("yvc") || currentC.equals("yip")) { %><search:velocity template="/results/vcardYellow"/><% } else { %><search:velocity template="/results/vcardWhite"/><%}%>      


