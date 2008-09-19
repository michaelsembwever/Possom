<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>

<%-- The skinnable error message --%>
<c:catch var="error">
    <search:velocity template="/pages/500"/>
</c:catch>

<%-- If this site does not exist redirect to super domain (for now hardcoded to sesam.com) --%>
<c:if test="${!empty error}"><c:redirect url="http://sesam.com"/></c:if>

<%-- At the bare minimum display the following basic 500 error page --%>
<c:if test="${!empty Missing_pages500_Template}">
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title>500 - Internal error</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <link rel="icon" href="http://sesam.com/images/favicon.gif" type="image/x-icon" />
        <link rel="shortcut icon" href="http://sesam.com/images/favicon.gif" type="image/x-icon" />
    </head>
    <body style="margin:0; padding:0;">
        <div style="width:100%; height:6px; background:#A6408E;"><img src="http://sesam.com/images/pix.gif" width="1" height="6" alt="" /></div>
        <img src="http://sesam.com/images/logo.png" width="146" height="44" style="padding:20px 0 10px 30px;" alt="logo" />
        <div style="padding-left:30px; font-size:12px; font-family: arial, sans-serif;">
            <p>500 Internal Server Error
                The server encountered an unexpected condition which prevented it from fulfilling the request.</p>
        </div>
        <hr/>
        <c:out value="${error}"/>
    </body>
</html>    
</c:if>