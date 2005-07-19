<%--
  Created by IntelliJ IDEA.
  User: itthkjer
  Date: 07.jun.2005
  Time: 12:10:40
  To change this template use File | Settings | File Templates.
--%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix='html' %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html:form action="/login.do?method=loginCheck" styleId="loginForm" >
    <fieldset>
        <legend>Login</legend>
        <label for="username" class="first">Brukernavn
            <html:text property="username" size="20" />
        </label>
        <label for="password">Passord
            <html:password property="password" size="20" />
        </label>
        <label for="retailer" class="last">Retailer
            <html:password property="retailer" size="20" />
        </label>
        <html:submit styleId="bSubmit" >Login</html:submit>
    </fieldset>
</html:form>




