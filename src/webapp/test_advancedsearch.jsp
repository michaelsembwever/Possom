<html>
    <head>

    <style type="text/css">
    
    body { 
        font-family: verdana, arial;
        font-size: 11px; 
    }

        
    font {
        font-family: verdana, arial;
        font-size: 11px;
    }
    
    .header {
        font-family: verdana, arial;
        font-size: 13px;
        //font-weight: bold;
    }
    
    label {
        font-family: verdana, arial;
        font-size: 11px;
    }

    input {
        font-family: verdana, arial;
        font-size: 11px;
    }
º

    </style>

    </head>
    <title> Type Your Advanced Sqearch</title>
    
    
<body>


<font class="header">
    Proximity<br>
</font>

    Fast also supports proximity, wich means you can find documents matching <br>
    similiar words using the operator NEAR, SIMILAR_TO and so forth..
    <p>
<font class="header">
    Query operators:
</font>
    <ul>
        <li> Boolean query operators(AND | OR | ANDNOT | RANK | ANY )</li>
        <li> Proximity query operators (NEAR |ÊONEAR)</li>
        <li> Arithmetic query operators</li>
    </ul>
       
<font class="header">
    Query Transformations   <br>
</font>
    FAST is able to perform a number of automatic suggested transformations of<br>
    the users query. Read <code> Chapter 04 ;-)</code>

    <p><p><p>
<hr>
<font class="header">
    Advanced Search<br>
</font>

<table>
    <tr>
	<form name="sf" action="./search/">
	   <input type="hidden" name="c" value="g"/>
	    <input type="hidden" name="t" value="adv" />
	    <td width=250>
          <label>Som inneholder alle ordene:</label>
        </td>
	    <td>
          <input name="q_all" type="text" size="25" />
        </td>
	 </tr>
     <tr>
	    <td>
          <label>Eksakte frasen:</label></td>
	    <td>
           <input name="q_phrase" type="text" size="25" />
        </td>
	 </tr><tr>
	    <td>
          <label>Som inneholder noen av ordene:</label>
        </td>
	    <td>
          <input name="q_or" type="text" size="25" />
        </td>
	  </tr><tr>
	    <td>
          <label>Som ikke inneholder ordene:</label>
       </td>
	    <td>
          <input name="q_not" type="text" size="25" />
        </td>
	 </tr><tr>
	    <td><label>Language</label></td>
	    <td>
		<select name="q_lang">
			<option> ---  </option>
			<option>no
			<option>en
			<option>se
		<select>
	    </td>
	  </tr>
      <tr>
       <td></td>
	     <td align="right"><input type="submit" id="search_button"  />
        </td>
      </form>
    </tr>
</table>
<hr>
<table>
    <tr>
      <form name="sf" action="./search/">
        <input type="hidden" name="c" value="g" />      
        <input type="hidden" name="t" value="adv_urls" />
        
        <td width=100>
            <label>Som ligner</label>
        </td>
        <td>
            <input name="q_urls" type="text" size="38" value="http://"/>
        </td>
        <td>
            <input type="submit" />
        </td>
     </tr>
     </form>
   <tr>
</table>
<hr>

<table>
    <tr>
      <form name="sf" action="./search/">
        <input type="hidden" name="c" value="g" />      
        <input type="hidden" name="t" value="adv_link" />
        <td width=100>
            <label>Som har link til</label>
        </td>
        <td>
            <input name="q_link" type="text" size="38" value="http://"/>
        </td>
        <td>
            <label>
                <input type="submit" />
            </label>
        </td>
      </form>
    <tr>
</table>
</body>
</html>
