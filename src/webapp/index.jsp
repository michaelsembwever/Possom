<%@ page
        language="java"
        errorPage="/internal-error.jsp"
        contentType="text/html; charset=utf-8"
        pageEncoding="ISO-8859-1"
        %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title>Sesam</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link href="css/decorator-style.css" rel="stylesheet" type="text/css" />
    <link rel="icon" href="favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
    <script type="text/javascript" language="JavaScript">
    <!--
    function strep(qtag) {
        if (window.RegExp && window.encodeURIComponent) {
            var qlink=qtag.href;
                var qenc=encodeURIComponent(document.forms[0].q.value);
                if(qlink.indexOf("q=")!=-1){
                    qtag.href=qlink.replace(new RegExp("q=[^&$]*"),"q="+qenc);
                }else{
                    qtag.href=qlink+"&q="+qenc;
                }
        }
        return 1;
    }

    function check() {
        if(document.forms[0].q.value == ""){
            document.forms[0].q.focus();
            return false;
        }
    }

    var link = '<a href="javascript:;" onclick="this.style.behavior=\'url(#default#homepage)\';this.setHomePage(\'http://www.sesam.no/\');">Sett som startside</a> &nbsp;&nbsp;|&nbsp;&nbsp;';
    // -->
    </script>
</head>

<body onload="document.forms[0].q.focus();">

<div class="index_center">
    <div style="width: 680px; text-align: left;"><img src="images/index/logo.jpg" id="logo_index" alt="logo" /></div><br/>
    <div id="index_searchlinks">
        Sesams&#248;k
        <a href="search/?nav_sources=contentsourcenavigator&amp;c=m&amp;contentsource=Norske nyheter&amp;userSortBy=datetime&amp;q=" onclick="return strep(this);">Nyhetss&#248;k</a>
        <a href="search/?c=y" onclick="return strep(this);">Bedriftss&#248;k</a>
        <a href="search/?c=w" onclick="return strep(this);">Persons&#248;k</a>
        <a href="search/?c=p" onclick="return strep(this);">Bildes&#248;k</a>
    </div>
</div>

<div><img src="images/pix.gif" width="100%" height="1" class="dots" alt="" /></div>
<div id="index_element">
    <div class="index_center">
    <form action="search/" onsubmit='return check();'>
        <input name="lan" value="en" type="hidden" />
        <input name="c" value="d" type="hidden" />
        <table width="680" border="0" cellspacing="0" cellpadding="0" style="padding-top:6px;">
            <tr>
                <td width="400"><input type="text" name="q" class="input_main" /></td>
                <td width="15">&nbsp;</td>
                <td width="265" align="left"><input type="submit" value="Sesams&#248;k" id="submit_button" class="submit_d" /> &nbsp; <a href="search/?c=d&amp;smart=true&amp;pg=1&amp;q=">S&#248;k smart</a></td>
            </tr>
            <tr>
                <td colspan="3" class="index_example" align="left">Eksempel: <a href="search/?lan=en&amp;c=d&amp;q=trollveggen">Trollveggen</a></td>
            </tr>
        </table>
    </form>
    </div>
</div>
<div><img src="images/pix.gif" width="100%" height="1" class="dots" alt="" /></div>

<div class="index_center">
    <div id="index_enrichment">
        <table id="t1">
            <tr>
                <td colspan="4"><img src="images/index/komtilsaken.jpg" alt="" /></td>
            </tr>
            <tr><td colspan="4" class="nopad"><img src="images/pix.gif" width="100%" height="1" class="lightdots" alt="" /></td></tr>
            <tr>
                <td class="col_1" valign="top"><img src="images/index/m.jpg" alt="Nyheter" /></td>
                <td valign="top">
                    <span class="bold_text">Nyheter:</span>&nbsp;S&#248;k p&#229; et aktuelt tema:<br />
                    <a href="search/?c=d&amp;q=Nyheter Sesam">Nyheter Sesam</a>
                </td>
                <td class="col_3" valign="top"><img src="images/index/y.jpg" alt="Bedrifter" /></td>
                <td valign="top">
                    <span class="bold_text">Bedriftss&#248;k:</span>&nbsp;S&#248;k p&#229; firmanavn eller en bransje pluss et sted:<br />
                    <a href="search/?c=d&amp;q=Elektriker &#197;lesund">Elektriker &#197;lesund</a>
                </td>
            </tr>
            <tr>
                <td class="col_1" valign="top"><img src="images/index/w.jpg" alt="Personer" /></td>
                <td valign="top">
                    <span class="bold_text">Persons&#248;k:</span>&nbsp;S&#248;k p&#229; et telefonnummer, person eller adresse:<br />
                    <a href="search/?c=d&amp;q=anne oslo">Anne Oslo</a>
                </td>
                <td class="col_3" valign="top"><img src="images/index/p.jpg" alt="Bilder" /></td>
                <td valign="top">
                    <span class="bold_text">Bildes&#248;k:</span>&nbsp;S&#248;k p&#229; en person, ting eller tema:<br />
                    <a href="search/?c=d&amp;q=bilder hamster">Bilder hamster</a>
                </td>
            </tr>
        </table>

        <table id="t2">
            <tr>
                <td colspan="3"><img src="images/index/magiskeord.jpg" alt="" /></td>
            </tr>
            <tr><td colspan="3" class="nopad"><img src="images/pix.gif" width="680" height="1" class="lightdots" alt="" /></td></tr>
            <tr>
                <td width="183">
                    Skriv: <span class="bold_magic_text">TV</span> &lt;program&gt;<br />
                    Eksempel: <a href="search/?c=d&amp;q=tv kveldsnytt">TV kveldsnytt</a>
                </td>
                <td width="183">
                    Skriv: <span class="bold_magic_text">V&#230;r</span> &lt;sted&gt;<br />
                    Eksempel: <a href="search/?c=d&amp;q=v&#230;r troms&#248;">V&#230;r Troms&#248;</a>
                </td>
                <td width="314" align="left">
                    Skriv: <span class="bold_magic_text">Fakta</span> &lt;tema&gt;<br />
                    Eksempel: <a href="search/?c=d&amp;q=Fakta fiolin">Fakta fiolin</a>
                </td>
            </tr>
            <tr>
                <td colspan="3"><a href="search/?c=d&amp;smart=true&amp;pg=2&amp;q=">Flere magiske ord fra Sesam her!</a></td>
            </tr>
        </table>

        <div id="footer_space">
            <div class="lightdots"><img src="images/pix.gif" width="100%" height="1" alt="" /></div>
            <div id="footer_help">
                <span class="copy">&copy;2005</span> &nbsp;&nbsp;
                <script type="text/javascript" language="JavaScript">
                    if(document.all){
                        document.write(link);
                    }
                </script>
                <a href="search/?c=d&amp;smart=true&amp;pg=1&amp;q=">S&#248;k smart</a>
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href="search/?c=d&amp;ads_help=true&amp;q=">Bli annons&#248;r</a>
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href="search/?c=d&amp;about=true&amp;q=&amp;pg=1">Om oss</a>
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href="search/?c=d&amp;q=&amp;help=true&amp;pg=1">Hjelp</a>
            </div>
        </div>
    </div>
</div>

</body>
</html>