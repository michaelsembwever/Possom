<%@ page
    language="java"
    errorPage="/internal-error.jsp"
    contentType="text/html; charset=iso-8859-1"
    pageEncoding="ISO-8859-1"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
  <TITLE>Search page Schibsted Søk BETA</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
  <LINK href="css/style.css" rel=stylesheet>
  <style type="text/css">
<!--

.search_button {
	font-family: Verdana, Arial, Helvetica, sans-serif;
	font-size: 12px;
	color: #FFFFFF;
	margin-left: 5px;
	font-weight: normal;
	height: 24px;
	border-color: #FFFFFF;
	background-image:    url("button_search.png");
	width: 100px;
}
a:link {
	color: #007AC8;
}
a:hover {
	color: #C73742;
}
a:visited {
	color: #99B5DA;
}
.search_box {
	height: 24px;
}
-->
  </style>
</HEAD>
<BODY onLoad="document.forms[0].q.focus();">


<table width="100%" height="100%" cellpadding="0" cellspacing="0">
	
	<tr>
	<td align="center" valign="middle" nowrap><img src="logo.png" alt="" vspace="4" border="0">	  <form action="search/">
	  <input name="lan" value="en" type="hidden">
	  <input name="c" value="d" type="hidden">
        <input maxlength="200" size="40" name="q" class="search_box" value="">
        <input name="submit" type="submit" class="search_button" value="Søk">
      </form>
	  <table width="100%" cellpadding="0" cellspacing="0" border="0">
	<tr>
		<td background="images/fanestrek_markert_under.png"><img src="images/fanestrek_markert_under.png" width="1" height="5"></td>
		
	</tr>
</table></td>
	<td align="center" valign="middle" nowrap>&nbsp;</td>
	</tr>
	<tr>
	  <td align="center" valign="bottom">&nbsp;</td>
	  <td valign="top" nowrap="nowrap">&nbsp;</td>
    </tr>
</table>


</BODY>
</HTML>