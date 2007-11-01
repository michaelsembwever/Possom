var dispName = location.host;

dispName = dispName.replace(/(\S+)\.(\S+)\.(\S+)$/, "$2");
if(dispName.length > 12){
  dispName = 'dette domenet';
}else{
  var first = dispName.substring(0,1);
  var rest = dispName.substring(1);
  first = first.toUpperCase();
  dispName = first + rest;
}

document.write('<div id="sesamsok" style="background:url(http://www.sesam.no/export/pss/tradebg_468x30.jpg) no-repeat; height:30px; width:468px; position:relative; text-align:left; font:11px Arial, Helvetica, sans-serif; color:#686663; line-height:1em;">');
document.write('  <form name="search" action="http://sesam.no/search/" method="get" >');
document.write('    <ul style="list-style:none; position:absolute; top:5px; left:5px; padding:0; margin:0; font-weight:bold; color:#87005B; width: 300px;">');
document.write('		<li style="display:inline; padding:0 5px 0 0; margin:0;"><label><input type="radio" name="c" value="pss" checked="checked" />');
document.write(dispName);
document.write('</label></li>');
document.write('		<li style="display:inline; padding:0; margin:0;"><label><input type="radio" name="c" value="d" /> Netts&oslash;k</label></li>');
document.write('	</ul>');
document.write('    <input type="text" name="q" style="width:155px; position:absolute; left:190px; top:3px; padding:1px 0 1px 3px; font-weight:bold;" />');
document.write('<input name="ss_lt" value="searchbox" type="hidden"/>');
document.write('<input name="ss_ss" value="' + dispName + '" type="hidden"/>');
document.write('<input name="ss_pid" value="sesamvenn" type="hidden"/>');
document.write('<input name="sitesearch" value="' + dispName + '" type="hidden"/>');
document.write('    <input type="image" style="position:absolute; left:360px; top:5px;" value="submit" src="http://www.sesam.no/export/pss/sokButtonsmall.gif" alt="S&oslash;k" />');
document.write('  </form>');
document.write('</div>');