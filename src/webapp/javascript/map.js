var isNS = false;
var isIE = false;
var isOpera = false;
if (navigator.appName.indexOf("Opera") >= 0) 
	isOpera = true;
if (navigator.appName.indexOf("Netscape") >= 0) 
	isNS = true;
if (navigator.appName.indexOf("Microsoft") >= 0) 
	isIE = true;

var url = "";
var zoomBoxColor = "#FF0000";	// color of zoombox
var ovBoxSize = 1; 		// Zoombox line width;
var xMapOffset = 0;
var yMapOffset = 0; 

// Global variables to save mouse position
var mouseX=0;
var mouseY=0;
var x1=0;
var y1=0;
var x2=0;
var y2=0;
var zleft=0;
var zright=0;
var ztop=0;
var zbottom=0;
var navigating=false;//navigeringsmodus av/på

//setter kartets posisjon i dokumentet
function setImgMapOffset(obj){
    xMapOffset = findPosX(obj);
    yMapOffset = findPosY(obj);
}

function findPosX(obj){
    var curleft = 0;
    if (obj.offsetParent)	{
            while (obj.offsetParent)		{
                    curleft += obj.offsetLeft
                    obj = obj.offsetParent;
            }
    }
    else if (obj.x)
            curleft += obj.x;
    return curleft;
}

function findPosY(obj){
    var curtop = 0;
    if (obj.offsetParent)	{
        while (obj.offsetParent)		{
                curtop += obj.offsetTop
                obj = obj.offsetParent;
        }
    }
    else if (obj.y)
        curtop += obj.y;
    return curtop;
}

//Skrur på hendelser i documentet til gdMapFrame
function initializeEvents() {
    if (isNS) {
            document.captureEvents(Event.MOUSEMOVE | Event.MOUSEDOWN | Event.MOUSEUP);
    }
    document.onmousemove = getMouse;
    document.onmousedown = mapTool;
    document.onmouseup = chkMouseUp;
}

//Skrur på hendelser i documentet til gdMapFrame
function initializeEvents() {
    if (isNS) {
            document.captureEvents(Event.MOUSEMOVE | Event.MOUSEDOWN | Event.MOUSEUP);
    }
    document.onmousemove = getMouse;
    document.onmousedown = mapTool;
    document.onmouseup = chkMouseUp;
}

//Dersom hendelsen mousedown inntrer
//Setter kun de globale variable for rektangel, x1, x2, y1, y2
function mapTool (e) {
    //Finner først ut om venstre eller høyre musetast er klikket    
    var rightClick = true;
    if (isNS) {
        if (e.which == 1) {
            rightClick = false;
        }
    }
    else {
        if (event.button == 1) {
            rightClick = false;
        }
        if (event.button == 0) { //opera 8 og w3c standard
            rightClick = false;
        }        
    }
    if (!rightClick) { 
        getImageXY(e);
        if ((mouseX<imgWidth) && (mouseY<imgHeigth) && (mouseX>=0) && (mouseY>=0)) {
            x1=mouseX;
            y1=mouseY;
            x2=x1+1;
            y2=y1+1;      
            navigating = true;
        }
    }     
    return false;
}

//get the coords at mouse position
function getMouse(e) {
    if(navigating){
        getImageXY(e);
        if ((mouseX>imgWidth) || (mouseY>imgHeigth) || (mouseX<=0) ||(mouseY<=0)) { 
                chkMouseUp(e);
        } else {
                x2=mouseX;
                y2=mouseY;
                setClip();
        }
    }        
    return false;
}

//Dersom hendelsen mouseup inntrer
//sjekker om det skal zoomes eller panoreres.
function chkMouseUp(e) {    
    //sjekker om det er dratt et rektangel eller gitt et klikk i kartet
    if(navigating){
        if ((Math.abs(x1 - x2) <= 1) && (Math.abs(y1 - y2) <= 1)) {//klikk i kartet, skal panorere
            //En viss forskyvelse når vi beveger oss mot høyre i kartet -  forskyvning på fire piksler
            //x1 = Math.round(x1 - (x1 / imgWidth) * 4);
            //y1 = Math.round(y1 - (y1 / imgHeigth) * 4);
            centerCoordX = xPixToCoord(x1);//konverter bildepixler til koordinater
            centerCoordY = yPixToCoord(y1);
            makeEnvelopeFromPoint();  
        }
        else{// det er dratt ett rektangel     
            if(x1<x2){
                maxX = xPixToCoord(x2);
                minX = xPixToCoord(x1);
            }    
            else{
                maxX = xPixToCoord(x1);
                minX = xPixToCoord(x2);
            }
            if(y1<y2){
                maxY = yPixToCoord(y1);
                minY = yPixToCoord(y2);
            }    
            else{
                maxY = yPixToCoord(y2);
                minY = yPixToCoord(y1);
            }
            makeEnvelope();
            calcMapScale();
            getZoomlevel();
            setInitZoomBarImg();
        }
        getUrl();
        adjustIcons("true");
    }
    navigating = false;//avslutter navigerings modus
    return false;
}

//get cursor location
function getImageXY(e) {
    //trenger e for Netscape
    if (isNS) {
            mouseX=e.pageX;
            mouseY=e.pageY;
    } else {
            mouseX=event.clientX + document.body.scrollLeft;
            mouseY=event.clientY + document.body.scrollTop;
    }
    //subtract offsets from page left and top
    mouseX = mouseX-xMapOffset;
    mouseY = mouseY-yMapOffset;
}

function xPixToCoord(xpix){
    var xtmp = Math.round(minX + (dX/imgWidth) * xpix);  
    return xtmp;
}

function yPixToCoord(ypix){
    var ytmp = Math.round(maxY - (dY/imgHeigth) * ypix);    
    return ytmp;
}

//panorering i himmelretningene
function pan(direction){
    var tmpY = dY * panfactor;
    var tmpX = dX * panfactor
    //setPrevExtent();
    if(direction == 'n'){
	maxY = maxY + tmpY;
	minY = minY + tmpY;
    }
    if(direction == 's'){
	maxY = maxY - tmpY;
	minY = minY - tmpY;
    }
    if(direction == 'e'){
	maxX = maxX + tmpX;
	minX = minX + tmpX;
    }
    if(direction == 'w'){
	maxX = maxX - tmpX;
	minX = minX - tmpX;
    }
    if(direction == 'ne'){
	maxY = maxY + tmpY;
	minY = minY + tmpY;
	maxX = maxX + tmpX;
	minX = minX + tmpX;
    }
    if(direction == 'se'){
	maxY = maxY - tmpY;
	minY = minY - tmpY;
	maxX = maxX + tmpX;
	minX = minX + tmpX;
    }
    if(direction == 'nw'){
	maxY = maxY + tmpY;
	minY = minY + tmpY;
	maxX = maxX - tmpX;
	minX = minX - tmpX;
    }
    if(direction == 'sw'){
	maxX = maxX - tmpX;
	minX = minX - tmpX;
	maxY = maxY - tmpY;
	minY = minY - tmpY;
    }                            
    url = contextPath + "/map/?maxX=" + maxX + "&minX=" + minX + "&maxY=" + maxY + "&minY=" + minY;  
    adjustIcons("true");//juster kartikoner iforhold til utført kartnavigering
    setImageCenter();//setter nye verdier for bildesentrum
    getImage(url);     
}

function setImageCenter(){
    centerCoordX = minX + (dX/2);
    centerCoordY = minY + (dX/2);  
}

function previous(){
    url = contextPath + "/map/?maxX=" + prevMaxX + "&minX=" + prevMinX + "&maxY=" + prevMaxY + "&minY=" + prevMinY; 
    var tmpX = maxX;
    var tmpY = maxY;
    var tmpX2 = minX;
    var tmpY2 = minY;
    var tmplevel = currentZoomLevel;                    
    maxX = prevMaxX;
    minX = prevMinX;
    maxY = prevMaxY;
    minY = prevMinY;  
    currentZoomLevel = prevZoomLevel;                    
    prevMaxX = tmpX;
    prevMinX = tmpX2;
    prevMaxY = tmpY;
    prevMinY = tmpY2;
    prevZoomLevel = tmplevel; 
    setNewDeltaXY();
    setImageCenter();
    adjustIcons("true");
    setZoomBarImg(currentZoomLevel); 
    getImage(url);
}

function zoomlevel(level){
    if(currentZoomLevel == level){
	return;
    }
    var tmpScale = currentZoomScale;
    var zoomscale = getZoomScale(level);
    currentZoomLevel = level;
    dX = dX * (zoomscale/tmpScale);
    dY = dY * (zoomscale/tmpScale);     
    zoom();                                      
    setZoomBarImg(currentZoomLevel);                    
}

//hjelpemetode for zoomin/zoomout ved bruk av en gitt zoomfactor. Nye max/min coord verdier settes utifra nye beregnede deltaX/deltaY'er
function zoom(){                   
    //setPrevExtent();//tar var på forrige max/min verdier.
    makeEnvelopeFromPoint();
    setNewDeltaXY();   
    //juster ikoner
    adjustIcons("true");
    url = contextPath + "/map/?maxX=" + maxX + "&minX=" + minX + "&maxY=" + maxY + "&minY=" + minY; 
    getImage(url);
}

//dersom zoomin knappen er benyttet
function zoomin(typezoom){    
    if (typezoom == "usefactor"){//dersom det skal zoomes inn etter gitt zoomfactor
	//må finne ny deltaX, deltaY
	dX = Math.round(dX * (1/zoomfactor));
	dY = Math.round(dY * (1/zoomfactor));      
	zoom();   
    }   
    else{ //det skal zoomes til gitt zoomlevel
	if (currentZoomLevel > 1){
	    level = currentZoomLevel - 1;
	    zoomlevel(level); 
	}
    }
}

function zoomout(typezoom){    
    if (typezoom == "usefactor"){//dersom det skal zoomes inn etter gitt zoomfactor
	dX = Math.round(dX * zoomfactor);
	dY = Math.round(dY * zoomfactor);     
	zoom(); 
    }
    else{ //det skal zoomes til gitt zoomlevel
	if (currentZoomLevel < countZoomLevels){
	    level = currentZoomLevel + 1;
	    zoomlevel(level);
	}
    }
}
   
function setZoomBarImg(zoomlevel){      
    document.images["zoombar"].src = "../images/mapicon/zoombar_" + zoomlevel + ".jpg";
}               

function setInitZoomBarImg(){
    setZoomBarImg(currentZoomLevel);
}                

function getZoomScale(zoomLevel){  
    var zoomscale = currentZoomScale;
    for (var i = 0; i < arrZoomLevels.length; i++){                        
	var tmp = parseInt(i + 1); 
	if(tmp == zoomLevel){
	    zoomscale = arrZoomLevels[i];
	    currentZoomScale = zoomscale;
	    return zoomscale;
	}
    }
    return zoomscale;
}

function getZoomlevel(){
    var zoomscale;
    for (var i = 0; i < arrZoomLevels.length; i++){  
        zoomscale = arrZoomLevels[i];
        if(currentZoomScale <= zoomscale){
            currentZoomLevel = i+1;
            break;
        }
    }
}

function calcMapScale(){                  
    currentZoomScale = Math.round(dX/imgMeterSize);       
}

//lager nytt mapenvelope utifra et punkt og eksisterende deltaX og deltaY
function makeEnvelopeFromPoint(){  
    maxX = Math.round(centerCoordX + dX/2);
    minX = Math.round(centerCoordX - dX/2);
    maxY = Math.round(centerCoordY + dY/2);
    minY = Math.round(centerCoordY - dY/2);
}

//lager nytt mapenvelope utifra fire hjørnekoordinater
function makeEnvelope(){
    setNewDeltaXY();
    setImageCenter();
    //må beregne nytt mapenvelope
    var factorHW = imgWidth/imgHeigth;
    var factor_dXdY = dX/dY;     
    if (factor_dXdY < factorHW ){//høyden er iforhold til bildehøyde/bredde mindre enn bredde. Må utvide bredde for å få riktige proposisjoner
        dX = dX * (factorHW/factor_dXdY);
    }
    else if(factor_dXdY > factorHW){//høyde er større enn bredde. Må utvide bredde for å få riktige proposisjoner
        dY = dY * (factor_dXdY/factorHW);   
    }     
    makeEnvelopeFromPoint();    
}

//tar var på forrige extent
function setPrevExtent(){
    prevMaxX = maxX;
    prevMinX = minX;
    prevMaxY = maxY;
    prevMinY = minY; 
    prevZoomLevel = currentZoomLevel;
}

//beregner nye Delta X, deltaY
function setNewDeltaXY(){
    dX = maxX - minX;
    dY = maxY - minY; 
}

function getUrl(){    
    var url = contextPath + "/map/?maxX=" + maxX + "&minX=" + minX + "&maxY=" + maxY + "&minY=" + minY;
    getImage(url);
}           

    
//kaller på mapservlet for generering av bilde
function getImage(url){ 
    document.images["map"].src = "../images/mapicon/loading.gif";
    document.images["map"].src = url;
}               

                