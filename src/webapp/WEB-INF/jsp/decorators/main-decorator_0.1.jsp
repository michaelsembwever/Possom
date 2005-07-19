<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%

  String collection = "d";	//default collection
  if(request.getParameter("c") != null)
	collection = request.getParameter("c");

  String q = request.getParameter("q");
	if(q!=null)
	  q = q.replaceAll("\"","&quot;");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
 <head>
  <title><%= q %> - <decorator:title default="Schibsted Søk" /></title>
  <link rel="stylesheet" href="../css/mockup.css" />
 </head>
 <body onLoad="document.forms[0].q.focus();">
  <div class="hoyreSpalte">
     <a href="">Avansert søk</a> &middot; <a href="">Innstillinger</a>
     <p/>
     <!-- Wiki -->
     <decorator:getProperty property="page.wiki-results" />
   </div>

<div id="hovedinnhold" style="width: 710px;">

    <img src="../images/mockup2logo.png" alt="" width="103" height="25" style="float: left;" />	  
    <form name="sf" action="" style="padding-left: 1px; margin-bottom: 0px; padding-bottom: 0px;">
      <input name="lang" value="en" type="hidden">
	<input name="c" value="<%=collection%>" type="hidden"/>
      <input name="q" type="text" class="inputFelt" value="<%= q %>" /> 
      <input type="submit" class="submitKnapp" value="Søk" /> 
    </span>
     </form>
     <br/>
  <div class="faneNorge<%if(collection.equals("d")){%>Valgt<%}%>">
	<a href="?q=<%=q%>&c=d">Norge</a> <decorator:getProperty property="page.number-of-results-local" /> 		  
  </div>

  <div class="faneBedrifter<%if(collection.equals("b")){%>Valgt<%}%>">
    <a href="" class="faneLenke">Bedrifter</a>
  </div>
  <div class="fanePersoner<%if(collection.equals("c")){%>Valgt<%}%>">
    <a href="" class="faneLenke">Personer</a>
  </div>
  <div class="faneKart<%if(collection.equals("map")){%>Valgt<%}%>">
    <a href="" class="faneLenke">Kart</a>
  </div>
  <div class="faneNyheter<%if(collection.equals("m")){%>Valgt<%}%>">
  	<a href="?c=m&q=<%=q%>" class="faneLenke">Nyheter<span class="noDec">   	<!-- Norsk -->
		<decorator:getProperty property="page.number-of-results-news" />
    </span></a>
  </div>
  <div class="faneVerden<%if(collection.equals("w")){%>Valgt<%}%>">
    <a href="?c=w&q=<%=q%>" class="faneLenke">Verden<span class="noDec"> 			  	
     <%if(!collection.equals("w")){%>
	<!-- Sensis -->
     <decorator:getProperty property="page.global-counter" /> 
	<%}%>
     </span></a>
  </div>
  <br />

<%
String indicator = "faneNorgeValgtIndikator";
if(collection.equals("m")){
	indicator = "faneNyheterValgtIndikator";
} else if(collection.equals("w")){
	indicator = "faneVerdenValgtIndikator";

}
%>

  <div class="<%=indicator%>"  style="clear: left;"><img src="http://www.vg.no/" width="1" height="1" alt="" /></div>
  <div class="faneNorgeValgtNavigator">
   <decorator:getProperty property="page.spelling-suggestions" />  	
  </div> 
  <br />

   <!-- Nyheter Beriket -->
   <%if(!collection.equals("m")){%>
   <decorator:getProperty property="page.retriever-results" />
   <%}%>

   <!-- Senis Beriket -->
   <%if(!collection.equals("w")){%>
	<decorator:getProperty property="page.sensis-enriched" />
   <%}%>

   <!-- Webinnhold -->
   <decorator:getProperty property="page.fast-results" />

   <!-- Webinnhold Global index -->
   <decorator:getProperty property="page.global-results" />

   <!-- Media index -->
   <decorator:getProperty property="page.media-collection-results" />



   <!-- Navigasjon/browse -->


    </div>
   </div>
 
  </div> 


</div>
 </body>
</html>
