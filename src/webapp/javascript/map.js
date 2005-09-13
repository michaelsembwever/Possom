           

var url = "";
//kartets plassering               

//icon i kartet
var imageNormal = new Object();
imageNormal["info"] = new Image(10, 10);
imageNormal["info"].src = "../images/mapicon/ballong.gif";

var imageHighlite = new Object();
imageHighlite["info"] = new Image(17, 17);
imageHighlite["info"].src = "../images/mapicon/ballong_2.gif";

function setImage(iconName, type, imgId){
    if (document.images) {
	if(type == "highlite"){
	    document.images[imgId].src = imageHighlite[iconName].src;
	    return true;
	}
	else if(type == "normal"){
	    document.images[imgId].src = imageNormal[iconName].src;
	    return true;
	}
    }
    return false;
}
//slutt ikon i kartet         

function pan(direction){
    var tmpY = dY * panfactor;
    var tmpX = dX * panfactor
    //tar vare på forrige max/min koordinater
    setPrevExtent();
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
	//kun Y+
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
    adjustIcons();//juster kartikoner iforhold til utført kartnavigering
    setImageCenter();//setter nye verdier for bildesentrum
    getImage(url);     
}

function setImageCenter(){
    centerCoordX = minX + (dX/2);
    centerCoordY = minY + (dX/2);  
    //alert("centerCoordX " + centerCoordX + ", centerCoordY " + centerCoordY);
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
    //sett currentZoomLevel
    //må sjekke om ikoner
    adjustIcons();
    setZoomBarImg(currentZoomLevel); 
    getImage(url);
}

function zoomlevel(level){
    //alert("currentzoomlevel = "+ currentZoomLevel + ", newlevel = " +level)
    if(currentZoomLevel == level){
	return;
    }
    var tmpScale = currentZoomScale;
    var zoomscale = getZoomScale(level);
    //alert(zoomscale + ", " + tmpScale + ", " + dX + ", " + dY);
    //beregn ny deltax, y
    currentZoomLevel = level;
    dX = dX * (zoomscale/tmpScale);
    dY = dY * (zoomscale/tmpScale);     
    //alert(dX + ", " + dY);
    //må beregne nye max/min koordinater utifra ny scale.                    
    zoom();                                      
    setZoomBarImg(currentZoomLevel);                    
}

//hjelpemetode for zoomin/zoomout ved bruk av en gitt zoomfactor. Nye max/min coord verdier settes utifra nye beregnede deltaX/deltaY'er
function zoom(){                   
    setPrevExtent();//tar var på forrige max/min verdier.
    maxX = centerCoordX + (dX/2);
    minX = centerCoordX - (dX/2);
    maxY = centerCoordY + (dY/2);
    minY = centerCoordY - (dY/2);                                     
    setNewDeltaXY();   
    //juster ikoner
    adjustIcons();
    //alert("dx: " + dX + ", Dy: " + dY + ". maxX " + maxX + ". prevmaxX " + prevMaxX+ ". minX "  + minX + ". prevminX " + prevMinX +" maxY: " + maxY + ". prevmaxY " + prevMaxY +" minY :" + minY+ ". prevminY " + prevMinY);
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

//dersom zoombar er benyttet                
function calcMapScale(){                  

}

function setZoomBarImg(zoomlevel){                              
    document.images["zoombar"].src = "../images/mapicon/zoombar_" + zoomlevel + ".jpg";
    //alert(zoomlevel);
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

function makeEnvelope(){                                  
   /*
    long zoomscale = getZoomLevel(zoom);
    double metersPrPixel = zoomscale * pixelSize;        
    this.maxX = Math.round(mp.getX() + metersPrPixel * this.imgHeigth);
    this.minX = Math.round(mp.getX() - metersPrPixel * this.imgHeigth);
    this.maxY = Math.round(mp.getY() + metersPrPixel * this.imgWidth);
    this.minY = Math.round(mp.getY() - metersPrPixel * this.imgWidth);        
    MapEnvelope mapEnvelope = new MapEnvelope(this.maxX,this.minX,this.maxY,this.minY);           
    return mapEnvelope;
    */                
}

//plassèr gif'ene ift kartextent               
function adjustIcons(){
    //looper igjennom koordinatobjektarrayen og fjerner de som ikke er innenfor det nye extentet
    var tmpCoordObj = new Object();
    var tmpPixCoord = new Array()
    arrCompanyToShow = new Array();
    for (var i=0; i<arrCompany.length; i++) {
	alert("arrCompany[i].x " + arrCompany[i].x +", arrCompany[i].y: " +arrCompany[i].y);
	tmpCoordObj.x = arrCompany[i].x;
	tmpCoordObj.y = arrCompany[i].y;
	tmpCoordObj.id = arrCompany[i].id;
	if (isInsideExtent(tmpCoordObj)){
	    arrCompanyToShow[i] = tmpCoordObj;                       
	}
    }
    if(arrCompanyToShow.length > 0){//skal konvertere til bilde pixler og tegne opp de ikonene som er innenfor extentet.
	tmpPixCoord = coordToPix(arrCompanyToShow);//beregn nye pix verdier                            
	var id = "";
	var imgName = "";
	for (var i=0; i<tmpPixCoord.length; i++) {
	    id = tmpPixCoord[i].id;
	    imgName = imgIconName + id;
	    alert("adjustIcons(): imgName = " + imgName + ", tmpPixCoord[i].y: " + tmpPixCoord[i].y + ", tmpPixCoord[i].x: " + tmpPixCoord[i].x);
	    document.images[imgName].style.top = tmpPixCoord[i].y+'px';
	    document.images[imgName].style.left = tmpPixCoord[i].x+'px';
	}
    }
}    

//sjekker om gitt punkt er innenfor map extent
function isInsideExtent(compObj){
    //alert("isInsideExtent(): " + compObj.id)
    var tmpCoordX = compObj.x;
    var tmpCoordY = compObj.y;
    var inside = true;           
    var imgName = imgIconName + compObj.id;
    //alert("imgName "+ imgName +", tmpCoordY " + tmpCoordY + " tmpCoordX " + tmpCoordX);
    if (tmpCoordX >= maxX)
	inside = false;
    else if (tmpCoordX <= minX)
	inside = false;
    if (tmpCoordY >= maxY)
	inside = false;
    else if (tmpCoordY <= minY)
	inside = false;
    if (inside == false){
	//alert("ikon er utenfor extent");
	document.images[imgName].style.visibility = "hidden";
	return false;
    }
    else{
	document.images[imgName].style.visibility = "visible";
	//alert("isInsideExtent");
	return true;
    }                    
}   

//konverterer et array med real-world koordinater til et array med bildepixel koordinater
function coordToPix(coordArray){    
    var tmpPixArray = new Array();                    
    var xFactor = imgWidth/dX;//meter pr pixel
    var yFactor = imgHeigth/dY;//meter pr pixel  
    for (var i=0; i<coordArray.length; i++) {
	tmpPixCoordPair = coordArray[i];
	tmpPixCoordPair.x = Math.round((coordArray[i].x - minX) * xFactor);
	tmpPixCoordPair.y = Math.round((maxY-coordArray[i].y) * yFactor);                        
	tmpPixArray[i] = tmpPixCoordPair;
    }
    //alert("coordToPix(): " + tmpPixArray.length + ", tmpPixArray[0].id: " + tmpPixArray[0].id);
    return tmpPixArray;
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
    alert(url);                
    document.images["map"].src = url;
}               

                