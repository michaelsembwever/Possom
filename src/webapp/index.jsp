<%@ page
    language="java"
    errorPage="/internal-error.jsp"
    contentType="text/html; charset=iso-8859-1"
    pageEncoding="ISO-8859-1"
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <title>Search page Schibsted Søk BETA</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
        <link href="css/front.css" rel="stylesheet" type="text/css" />
    </head>

    <body onload="document.forms[0].q.focus();">

        <img src="logo_front.gif" id="logo" alt="logo" />
        <table cellpadding="0" cellspacing="0" border="0" id="front">
            <tr><td id="flip_hover" style="background-color: #705CB3"><div>Magisøk</div></td><td class="flip"><div class="space">Nyhetssøk</div></td><td><img class="dot" src="dot_14.gif" alt="dot" /></td><td class="flip"><div>Bedriftsøk</div></td><td><img class="dot" src="dot_14.gif" alt="dot" /></td><td class="flip"><div>Personsøk</div></td><td><img class="dot" src="dot_14.gif" alt="dot" /></td><td class="flip"><div>Nettsøk verden</div></td><td><img class="dot" src="dot_14.gif" alt="dot" /></td><td class="flip"><div>Bilder</div></td></tr>
        </table>
        <div style="width: 100%; background-image: url(menu_line_magic.gif); background-repeat: repeat-x;">&nbsp;</div>

        <table width="100%" cellpadding="0" cellspacing="0" id="form">
            <tr>
                <td>
                    <form action="search/">
                        <input name="lan" value="en" type="hidden">
                        <input name="c" value="d" type="hidden">
                        <input maxlength="200" size="40" name="q" class="search_box" value="">
                        <input name="submit" type="submit" class="search_button" value="Søk">
                    </form>
                </td>
            </tr>
        </table>

        <table cellspacing="0" cellpadding="0" width="100%" id="footer">
            <tr>
                 <td colspan="3"><div style="background-image: url(menu_line_magic.gif); background-repeat: repeat-x;">&nbsp;</div></td>
            </tr>
            <tr>
                <td>
                    <div id="footer_text">
                        <a href="#">Annonser hos oss</a> <img src="dot_10.gif" class="dot_10" alt="logo" />
                        <a href="#">FAQ</a>  <img src="dot_10.gif" class="dot_10" alt="logo" />
                        <a href="#">Nettsidekart</a> <img src="dot_10.gif" class="dot_10" alt="logo" />
                        <a href="#">Om oss</a>
                    </div>
                </td>
            </tr>
        </table>



<%--	--%>
<%--	<tr>--%>
<%--	<td align="center" valign="middle" nowrap><img src="logo.png" alt="" vspace="4" border="0">	  <form action="search/">--%>
<%--	  <input name="lan" value="en" type="hidden">--%>
<%--	  <input name="c" value="d" type="hidden">--%>
<%--        <input maxlength="200" size="40" name="q" class="search_box" value="">--%>
<%--        <input name="submit" type="submit" class="search_button" value="Søk">--%>
<%--      </form>--%>
<%--	  <table width="100%" cellpadding="0" cellspacing="0" border="0">--%>
<%--	<tr>--%>
<%--		<td background="images/fanestrek_markert_under.png"><img src="images/fanestrek_markert_under.png" width="1" height="5"></td>--%>
<%--		--%>
<%--	</tr>--%>
<%--</table></td>--%>
<%--	<td align="center" valign="middle" nowrap>&nbsp;</td>--%>
<%--	</tr>--%>
<%--	<tr>--%>
<%--	  <td align="center" valign="bottom">&nbsp;</td>--%>
<%--	  <td valign="top" nowrap="nowrap">&nbsp;</td>--%>
<%--    </tr>--%>
<%--</table>--%>


    </body>
</html>