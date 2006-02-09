<%@ page
        language="java"
        pageEncoding="UTF-8"
        contentType="text/html;charset=utf-8"%>

<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<head>
    <title>Sesam</title>
    <link href="../css/decorator-style.css" rel="stylesheet" type="text/css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
</head>
<body>
    <decorator:getProperty property="page.infopage" />
     <div id="info_submenu">
    [ <a href="#" onclick="window.close()"> Lukk Vindu </a> ]
     </div>

</body>
</html>

