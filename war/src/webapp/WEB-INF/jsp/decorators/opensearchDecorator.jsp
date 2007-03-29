<?xml version="1.0" encoding="UTF-8" ?>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
    <InputEncoding>utf-8</InputEncoding>
    <ShortName><%=((no.schibstedsok.searchportal.view.i18n.TextMessages) request.getAttribute("text")).getMessage("navbarMain_" + request.getParameter("c"))%></ShortName> 
    <Description>Search with Sesam Search</Description>
    <Image width="16" height="16">data:image/x-icon;base64,R0lGODlhEAAQAKIGAMx0ufrx+NaPxrxKo+rH460gjv///wAAACH5BAEAAAYALAAAAAAQABAAAAM6aLpGxBAKEasZw0JSytPK0A0BCHQdYBEiOn6N0LruIDgyTdswqw8wyAmlArUyoEYnqMEkF5OnwlFJAAA7</Image>
    <Url type="text/html" template="http://sesam.no/search/?q={searchTerms}&amp;c=<c:out value='${tab.key}'/>" /> 
</OpenSearchDescription> 



