<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%

	// TODO: refactor to use Bean and SearchConstants.

  String collection = "d";	//default collection
  if(request.getParameter("c") != null)
	collection = request.getParameter("c");

  String q = request.getParameter("q");
	if(q!=null)
	  q = q.replaceAll("\"","&quot;");
%>
<html>
<head>
<title><%=q%></title>
<style type="text/css">
<!--
<!--
#greenMain { color: #5A6623; }
#blueBoxTitle { color: #007AC8; }
#c3 { color: #FF0000; }
body,td,div{
	font-family: Verdana,arial,sans-serif;	
}

.all_type_standard_size{
	font-size: 0.9em;	
}
a:hover { color: #EA6A24; }


.beriketInnrykk{
	margin-left:3em;
}
.beriketInnrykkTidspunkt{
	font-size:      0.7em;
	margin-left:	3em;
	color: 			5A6623;
}
.beriketTittel{
  	font-size:      0.9em;
}
.beriketUrl{
  	font-size:      0.8em;
}
   
.boksTittel {
     font-size: 0.7em;
	 color: #007AC8;
     padding-bottom: 5px;
	 margin-bottom: 2em;
   	 border-bottom: solid 1px #007AC8;
   	 margin-bottom: 10px;
}

.boksInnhold{
  	font-size:      0.8em;
}
.boksSummary{
  	font-size:      0.8em;
	margin-top: 	1.2em;
}
.fanetxt {
	font-size: 0.9em;
	color: 5A6623;
	margin-left: 0.3em;
	margin-right: 0.3em;
}
.faneMarkert {
	font-size: 1.0em;
	color: #FFFFFF;
	margin-left: 0.3em;
	margin-right: 0.3em;
	font-weight: bold;
	
}
.Hjelp_stavelse{
	color: #FF6633;
}
.marger_resultat {
	margin-right: 0.9em;
}

.navigator_resultat {
	font-size: 0.6em;
	color: #5A6623;
	margin-right: 0.4em;
}

.search_button {
	background-color: #007AC8;
	height: 1.8em;
	width: 7em;
	color: #FFFFFF;
}
.search_textbox {
	height: 1.8em;
}
.litenUrl {
	font-size: 0.8em;
	color: #5A6623;
	margin-top: 0.7em;
}


-->

</style>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"></head>
<body onLoad="document.forms[0].q.focus();">

<table width="100%"><tr><td width="20"></td><td align="left" valign="bottom">
  <form name="sf" action="" style="margin-bottom: 0.6em; margin-right: 1.9em;">
      <input name="lang" value="en" type="hidden">
	<input name="c" value="<%=collection%>" type="hidden"/>
      <input name="q" type="text" value="<%= q %>" size="50"> 
      <input type="submit" class="search_button" value="Søk">
  </form>
</td></tr></table>

<table width="100%" cellpadding="0" cellspacing="0" border="0">
		<tr>
			<td width="20" valign="bottom">
				
				<table width="100%" cellpadding="0" cellspacing="0" border="0">
						<tr>
							<td width="4"><img src="../images/fanestrek_markerttop_V.png" width="4"></td>
							<td background="../images/fanestrek_markerttop.png"><img src="../images/fanestrek_markerttop.png"></td>
				
						</tr>
			</table>			</td>
			<td width="10" align="right" valign="bottom">
				<table cellpadding="0" cellspacing="0" border="0">
<%if(collection.equals("d")){%>
					<tr>
						<td width="6"><img src="../images/fanemarkert_toppV.png" width="6" height="4"></td>
						<td background="../images/fanemarkertTop_1px.png" ><img src="../images/fanemarkertTop_1px.png"></td>
						<td width="6"><img src="../images/fanemarkert_toppH.png" width="6" height="4"></td>
					</tr>
					<tr>
						<td width="6" background="../images/fanemarkert_Vmarg1px.png" ><img src="../images/fanemarkert_Vmarg1px.png" border="0"></td>
						<td bgcolor="#98ad3b"><span class="faneMarkert">Norge</span></td>
						<td width="6" background="../images/fanemarkert_Hmarg.png"><img src="../images/fanemarkert_Hmarg.png" width="6" height="1" border="0"></td>
					</tr>
					<tr>
						<td><img src="../images/fanemarkert_HbunnV.png" width="6" height="7"></td>
						<td bgcolor="#98ad3b"></td>
						<td width="6"><img src="../images/fanemarkert_HbunnH.png" width="6" height="7"></td>
					</tr>
<%} else {%>
					<tr>
						<td width="6"><img src="../images/fane_HToppV.png" width="6" height="4"></td>
						<td background="../images/fane_topp1px.png" ><img src="../images/fanemarkertTop_1px.png"></td>
						<td width="6"><img src="../images/fane_ToppH.png" width="6" height="4"></td>
					</tr>
					<tr>
						<td width="6" background="../images/fane_margV1px.png" ><img src="../images/fane_margV1px.png" border="0"></td>
						<td><nobr><a href="?c=d&q=<%=q%>" class="fanetxt">Norge</a><span class="navigator_resultat"><decorator:getProperty property="page.local-counter-web" /></span></nobr>
						<td width="6" background="../images/fane_margH1px.png"><img src="../images/fane_margH1px.png" width="6" height="1" border="0"></td>
					</tr>
					<tr>
						<td width="6" align="left"><img src="../images/fane_margbunnV.png"></td>
						<td background="../images/fane_bunn1px.png"><img src="../images/fane_bunn1px.png"></td>
						<td width="6"><img src="../images/fane_margbunnH.png"></td>
					</tr>
<%}%>
				</table>
		  </td>
			<td valign="bottom" width="10">
			
			
			
			<table cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td width="6" align="left"><img src="../images/fane_HToppV.png" width="6" height="4"></td>
						<td background="../images/fane_topp1px.png"><img src="../images/fane_topp1px.png" width="1" height="4"></td>
						<td width="6"><img src="../images/fane_ToppH.png" width="6" height="4"></td>
					</tr>
					<tr>
						<td width="6" align="left" background="../images/fane_margV1px.png"><img src="../images/fane_margV1px.png"></td>
<%if(collection.equals("bedrifter")){%>
	<td bgcolor="#98ad3b"><span class="faneMarkert">Bedrifter</span>
<%} else {%>
	<td><nobr><a href="?c=d&q=<%=q%>" class="fanetxt">Bedrifter</a><span class="navigator_resultat"><decorator:getProperty property="page.number-of-results-companies" /></span></nobr>
<%}%>
					</td>
					<td width="6" align="right" background="../images/fane_margH1px.png"><img src="../images/fane_margH1px.png" width="6" height="1"></td>
					</tr>
					<tr>
						<td width="6" align="left"><img src="../images/fane_margbunnV.png"></td>
						<td background="../images/fane_bunn1px.png"><img src="../images/fane_bunn1px.png"></td>
						<td width="6"><img src="../images/fane_margbunnH.png"></td>
					</tr>
		  </table>

		</td>
<td width="10" valign="bottom">
<table cellpadding="0" cellspacing="0" border="0">
					<tr>
						<td width="6" align="left"><img src="../images/fane_HToppV.png" width="6" height="4"></td>
						<td background="../images/fane_topp1px.png" ><img src="../images/fane_topp1px.png" width="1" height="4"></td>
						<td width="6"><img src="../images/fane_ToppH.png" width="6" height="4"></td>
					</tr>
					<tr>
						<td width="6" align="left" background="../images/fane_margV1px.png"><img src="../images/fane_margV1px.png"></td>
<%if(collection.equals("personser")){%>
	<td bgcolor="#98ad3b"><span class="faneMarkert">Personer</span>
<%} else {%>
	<td><nobr><a href="?c=d&q=<%=q%>" class="fanetxt">Personer</a><span class="navigator_resultat"><decorator:getProperty property="page.number-of-results-catalogue" /></span></nobr>
<%}%>

					</td>
						<td width="6" align="right" background="../images/fane_margH1px.png"><img src="../images/fane_margH1px.png" width="6" height="1"></td>
					</tr>
					<tr>
						<td width="6" align="left"><img src="../images/fane_margbunnV.png"></td>
						<td background="../images/fane_bunn1px.png"><img src="../images/fane_bunn1px.png"></td>
						<td width="6"><img src="../images/fane_margbunnH.png"></td>
					</tr>
	  </table>
</td>
<td valign="bottom" width="10">

<table cellpadding="0" cellspacing="0" border="0">
  <tr>
    <td width="6" align="left"><img src="../images/fane_HToppV.png" width="6" height="4"></td>
    <td background="../images/fane_topp1px.png" ><img src="../images/fane_topp1px.png" width="1" height="4"></td>
    <td width="6"><img src="../images/fane_ToppH.png" width="6" height="4"></td>
  </tr>
  <tr>
    <td width="6" align="left" background="../images/fane_margV1px.png"><img src="../images/fane_margV1px.png"></td>
<%if(collection.equals("maps")){%>
	<td bgcolor="#98ad3b"><span class="faneMarkert">Kart</span>
<%} else {%>
	<td><nobr><a href="?c=d&q=<%=q%>" class="fanetxt">Kart</a><span class="navigator_resultat"><decorator:getProperty property="page.number-of-results-maps" /></span></nobr>
<%}%>
    </td>
    <td width="6" align="right" background="../images/fane_margH1px.png"><img src="../images/fane_margH1px.png" width="6" height="1"></td>
  </tr>
  <tr>
    <td width="6" align="left"><img src="../images/fane_margbunnV.png"></td>
    <td background="../images/fane_bunn1px.png"><img src="../images/fane_bunn1px.png"></td>
    <td width="6"><img src="../images/fane_margbunnH.png"></td>
  </tr>
</table>

</td>
<td valign="bottom" width="10">
	<table cellpadding="0" cellspacing="0" border="0">
<%if(collection.equals("m")){%>
					<tr>
						<td width="6"><img src="../images/fanemarkert_toppV.png" width="6" height="4"></td>
						<td background="../images/fanemarkertTop_1px.png" ><img src="../images/fanemarkertTop_1px.png"></td>
						<td width="6"><img src="../images/fanemarkert_toppH.png" width="6" height="4"></td>
					</tr>
					<tr>
						<td width="6" background="../images/fanemarkert_Vmarg1px.png" ><img src="../images/fanemarkert_Vmarg1px.png" border="0"></td>
						<td bgcolor="#98ad3b"><span class="faneMarkert">Nyheter</span></td>
						<td width="6" background="../images/fanemarkert_Hmarg.png"><img src="../images/fanemarkert_Hmarg.png" width="6" height="1" border="0"></td>
					</tr>
					<tr>
						<td><img src="../images/fanemarkert_HbunnV.png" width="6" height="7"></td>
						<td bgcolor="#98ad3b"></td>
						<td width="6"><img src="../images/fanemarkert_HbunnH.png" width="6" height="7"></td>
					</tr>
<%} else {%>
					<tr>
						<td width="6"><img src="../images/fane_HToppV.png" width="6" height="4"></td>
						<td background="../images/fane_topp1px.png" ><img src="../images/fanemarkertTop_1px.png"></td>
						<td width="6"><img src="../images/fane_ToppH.png" width="6" height="4"></td>
					</tr>
					<tr>
						<td width="6" background="../images/fane_margV1px.png" ><img src="../images/fane_margV1px.png" border="0"></td>
						<td><nobr><a href="?c=m&q=<%=q%>" class="fanetxt">Nyheter</a><span class="navigator_resultat"><decorator:getProperty property="page.local-counter-media" /></span></nobr>
						<td width="6" background="../images/fane_margH1px.png"><img src="../images/fane_margH1px.png" width="6" height="1" border="0"></td>
					</tr>
					<tr>
						<td width="6" align="left"><img src="../images/fane_margbunnV.png"></td>
						<td background="../images/fane_bunn1px.png"><img src="../images/fane_bunn1px.png"></td>
						<td width="6"><img src="../images/fane_margbunnH.png"></td>
					</tr>
<%}%>
</table>

</td>
<td valign="bottom" width="10">
<table cellpadding="0" cellspacing="0" border="0">
<%if(collection.equals("g")){%>
					<tr>
						<td width="6"><img src="../images/fanemarkert_toppV.png" width="6" height="4"></td>
						<td background="../images/fanemarkertTop_1px.png" ><img src="../images/fanemarkertTop_1px.png"></td>
						<td width="6"><img src="../images/fanemarkert_toppH.png" width="6" height="4"></td>
					</tr>
					<tr>
						<td width="6" background="../images/fanemarkert_Vmarg1px.png" ><img src="../images/fanemarkert_Vmarg1px.png" border="0"></td>
						<td bgcolor="#98ad3b"><span class="faneMarkert">Verden</span></td>
						<td width="6" background="../images/fanemarkert_Hmarg.png"><img src="../images/fanemarkert_Hmarg.png" width="6" height="1" border="0"></td>
					</tr>
					<tr>
						<td><img src="../images/fanemarkert_HbunnV.png" width="6" height="7"></td>
						<td bgcolor="#98ad3b"></td>
						<td width="6"><img src="../images/fanemarkert_HbunnH.png" width="6" height="7"></td>
					</tr>
<%} else {%>
					<tr>
						<td width="6"><img src="../images/fane_HToppV.png" width="6" height="4"></td>
						<td background="../images/fane_topp1px.png" ><img src="../images/fanemarkertTop_1px.png"></td>
						<td width="6"><img src="../images/fane_ToppH.png" width="6" height="4"></td>
					</tr>
					<tr>
						<td width="6" background="../images/fane_margV1px.png" ><img src="../images/fane_margV1px.png" border="0"></td>
						<td><nobr><a href="?c=g&q=<%=q%>" class="fanetxt">Verden</a><span class="navigator_resultat"><decorator:getProperty property="page.global-counter" /></span></nobr>
						<td width="6" background="../images/fane_margH1px.png"><img src="../images/fane_margH1px.png" width="6" height="1" border="0"></td>
					</tr>
					<tr>
						<td width="6" align="left"><img src="../images/fane_margbunnV.png"></td>
						<td background="../images/fane_bunn1px.png"><img src="../images/fane_bunn1px.png"></td>
						<td width="6"><img src="../images/fane_margbunnH.png"></td>
					</tr>
<%}%>
</table></td>
<td align="right" valign="bottom"><span class="fanetxt"><a href="#" class="fanetxt">Avansert søk</a> | <a href="#" class="fanetxt">Innstillinger</a></span><table width="100%" cellpadding="0" cellspacing="0" border="0">
						<tr>
							
							<td valign="bottom" background="../images/fanestrek_markerttop.png"><img src="../images/fanestrek_markerttop.png"></td>
								<td valign="bottom" width="4"><img src="../images/fanestrek_markerttop_H.png" width="4"></td>
						</tr>
	  </table></td>
</tr>
<tr><td>
</td></tr>
</table>
<table width="100%" cellpadding="0" cellspacing="0" border="0">
	<tr>
		<td background="../images/fanestrek_markert_under.png"><img src="../images/fanestrek_markert_under.png" width="1" height="5"></td>
		
	</tr>
</table>



<table width="100%" class="all_type_standard_size">
<tr>
	<td valign="top"><div class="marger_resultat">

   <decorator:getProperty property="page.spelling-suggestions" />  

   <decorator:getProperty property="page.tv-results" />

   <!-- Senis Beriket -->
   <%if(collection.equals("d")){%>
	<decorator:getProperty property="page.sensis-enriched" />
   <%}%>

   <!-- Nyheter Beriket -->
   <%if(collection.equals("d")){%>
   <decorator:getProperty property="page.retriever-results" />
   <%}%>

   <!-- Webinnhold -->
   <decorator:getProperty property="page.fast-results" />

   <!-- Webinnhold Global index -->
   <%if(collection.equals("g")){%>
   <decorator:getProperty property="page.global-results" />
   <%}%>

   <!-- Media index -->
   <decorator:getProperty property="page.media-collection-results" />

   <td width="180" valign="top"><decorator:getProperty property="page.wiki-results" /></td>
</tr>
</table>
</body>
</html>
