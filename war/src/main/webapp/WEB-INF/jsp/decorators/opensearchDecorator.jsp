<?xml version="1.0" encoding="UTF-8" ?>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="/WEB-INF/SearchPortal.tld" prefix="search" %>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
    <InputEncoding>utf-8</InputEncoding>
    <ShortName>Sesam <search:text key="navbarMain_${DataModel.parameters.values['c'].utf8UrlEncoded}"/></ShortName> 
    <Description>Search with Sesam Search</Description>
    <Image width="16" height="16">data:image/x-icon;base64,R0lGODlhEAAQAKIGAMx0ufrx+NaPxrxKo+rH460gjv///wAAACH5BAEAAAYALAAAAAAQABAAAAM6aLpGxBAKEasZw0JSytPK0A0BCHQdYBEiOn6N0LruIDgyTdswqw8wyAmlArUyoEYnqMEkF5OnwlFJAAA7</Image>
    <Url type="text/html" template="http://<c:out value='${DataModel.site.site.name}'/>search/?q={searchTerms}&amp;c=<c:out value='${DataModel.parameters.values["c"].utf8UrlEncoded}'/>"/> 
</OpenSearchDescription> 
