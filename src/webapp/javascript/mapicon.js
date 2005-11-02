var arrPixCoord = new Array();
var imgIconName = "icon_";//ikonene i kartet har dette navnet etterfulgt av id nummer, eks icon_1.

var imageNormal = new Object();
imageNormal["info"] = new Image(10, 10);
imageNormal["info"].src = "../images/mapicon/ballong.gif";

var imageHighlite = new Object();
imageHighlite["info"] = new Image(17, 17);
imageHighlite["info"].src = "../images/mapicon/ballong_2.gif";

function coordObj(x, y, id){
    this.x = x;
    this.y = y;
    this.id = id;
}
function setImage(iconName, type, imgId){
     if (document.images) {
	if(type == "highlite"){
	    document.getElementById(imgId).src = imageHighlite[iconName].src;
	    return true;
	}
	else if(type == "normal"){
	    document.getElementById(imgId).src = imageNormal[iconName].src;
	    return true;
	}
    }
    return false;
}

//plassèr kart ikoner ift kartextent
function adjustIcons(isInitiated, currentZoomScale){
    //looper igjennom koordinatobjektarrayen og fjerner de som ikke er innenfor det nye extentet
    var arrCompanyToShow = new Array();
    var arrCompanyTmp = new Array();
    for (var k=0; k<arrCompany.length; k++) { 
        mp = new coordObj(arrCompany[k].x, arrCompany[k].y, arrCompany[k].id );
        arrCompanyTmp[k] = mp; 
    }
    //arrCompanyTmp = arrCompany;
    if (isInitiated != "false"){
        var j = -1;
        //justere ikoner som har samme koordinat
        arrCompanyTmp = checkEqualCoords(arrCompanyTmp);
        for (var i=0; i<arrCompanyTmp.length; i++) {            
            //alert("loop = " + i + ", arrCompanyTmp[i].id = " + arrCompanyTmp[i].id + "arrCompanyTmp[i].x " + arrCompanyTmp[i].x +", arrCompanyTmp[i].y: " +arrCompanyTmp[i].y);
            if (isInsideExtent(arrCompanyTmp[i])){
                j = j + 1;
                arrCompanyToShow[j] = arrCompanyTmp[i];
                //alert("isInside: loop = " + i + ", arrCompanyTmp[i].id = " + arrCompanyTmp[i].id + "arrCompanyTmp[i].x " + arrCompanyTmp[i].x +", arrCompanyTmp[i].y: " +arrCompanyTmp[i].y);
            }
        }
    }
    else
        arrCompanyToShow = arrCompany;
    if(arrCompanyToShow.length > 0){//skal konvertere til bilde pixler og tegne opp de ikonene som er innenfor extentet.
	arrPixCoord = coordToPix(arrCompanyToShow);//beregn nye pix verdier
	if (isInitiated == "true"){
            var imgName = "";
            var right;
            var top;
            var bottom;
            var left;
            for (var i=0; i<arrPixCoord.length; i++) {
                right = imgWidth;
                top = imgHeigth;
                bottom = 0;
                left = 0;
                imgName = imgIconName + arrPixCoord[i].id;
                document.getElementById(imgName).style.top = arrPixCoord[i].y+'px';
                document.getElementById(imgName).style.left = arrPixCoord[i].x+'px';
            }
        }
    }
}

//sjekker om gitt punkt er innenfor map extent
function isInsideExtent(compObj){
    var inside = true;
    var imgName = imgIconName + compObj.id;
    if (compObj.x >= maxX)
	inside = false;
    else if (compObj.x <= minX)
	inside = false;
    if (compObj.y >= maxY)
	inside = false;
    else if (compObj.y <= minY)
	inside = false;
    if (!inside){
	document.getElementById(imgName).style.visibility = "hidden";
	return false;
    }
    else{
        //document.getElementById(imgName).style.visibility = "hidden";
        document.getElementById(imgName).style.visibility = "visible";
	return true;
    }
}

//konverterer et array med real-world koordinater til et array med bildepixel koordinater
function coordToPix(coordArray){
    var tmpPixArray = new Array();
    var xFactor = imgWidth/dX;//meter pr pixel
    var yFactor = imgHeigth/dY;//meter pr pixel
    for (var i=0; i<coordArray.length; i++) {
        tmpPixArray[i] = new coordObj();
        tmpPixArray[i].x = Math.round((coordArray[i].x - minX) * xFactor) - iconOffsetWidth;
        tmpPixArray[i].y = Math.round((maxY-coordArray[i].y) * yFactor) - iconOffsetHeigth;
        tmpPixArray[i].id = coordArray[i].id;
    }
    //looper igjennom array for å sjekke om ikoner er plassert oppe på hverandre   
    /*
    for (var j=0; j<tmpPixArray.length; j++) {
        //tmpPixArray[j] = new coordObj();
    	tmpPixArray[j] = checkOverlap(tmpPixArray, j)
    }
    */
    return tmpPixArray;
}

function checkEqualCoords(coordArray){
        var mp1 = new coordObj(); 
        var mp2 = new coordObj();         
        var adjustX = iconOverlapOffsetWidth;
        var adjustY = iconOverlapOffsetHeigth;
        if(currentZoomScale > 20000){//iconOverlapOffset justeres avhengig av målestokk, dersom denne er mindre enn 20000.
            adjustX = iconOverlapOffsetWidth * 20000/currentZoomScale;
            if(adjustX < 5)//kan ikke være mindre enn 1
               adjustX = 5;
            adjustY = iconOverlapOffsetHeigth * 20000/currentZoomScale;       
            if(adjustY < 5)//kan ikke være mindre enn 1
               adjustY = 5;
        }
        var xOffset = (currentZoomScale * pixelSize)*adjustX;
        var yOffset = (currentZoomScale * pixelSize)*adjustY;
        var counter = 1;//holder rede på antall like
        for (var i = 0; i < coordArray.length; i++){
            mp1 = coordArray[i];
            if(mp1.x != defaultNoCoord){
                for (var j = i+1; j < coordArray.length; j++){   
                    mp2 = coordArray[j];
                    //alert('mp1.x ' +mp1.x+', mp2.x '+mp2.x);
                    if(mp1.x == mp2.x && mp1.y == mp2.y){//vi har like koordinater
                        //alert('titt');
                        mp2.x = mp1.x + (xOffset * counter);
                        mp2.y = mp1.y + (yOffset * counter);
                        counter++;    
                        coordArray[j] = mp2;
                    }
                }
            }
        }
        return coordArray;
    }

/*
function checkOverlap(coordpixArray, element){    
    var xtmp = coordpixArray[element].x;    
    for (var i=0; i < coordpixArray.length; i++) {
        tmpArrayElem[i] = new coordObj();
        if(i != element){//skal ikke sjekke mot seg selv
            var xtmp2 = coordpixArray[i].x;
            var moveleft = false;
            var moveright = false;
            if ((xtmp-xtmp2) =< 0 && Math.abs(xtmp-xtmp2) < iconOverlapOffsetWidth){//x1 ligger til venstre for x2 og i kortere avstand enn iconOverlapOffsetWidth.
                moveleft = true;
            if ((xtmp-xtmp2) > 0 && xtmp-xtmp2 < iconOverlapOffsetWidth){//x1 ligger til venstre for x2 og i kortere avstand enn iconOverlapOffsetWidth.
                moveright = true;
            if(moveleft || moveright)
                var ytmp = coordpixArray[element].y;
                var ytmp2 = coordpixArray[j].y;
                var moveup = false;
                var movedown = false;
                if ((ytmp-ytmp2) =< 0 && Math.abs(ytmp-ytmp2) < iconOverlapOffsetHeight){//y1 ligger til over y2 og i kortere avstand enn iconOverlapOffsetHeight.
                    moveup = true;
                }
                if ((ytmp-ytmp2) > 0 && ytmp-ytmp2 < iconOverlapOffsetHeigth){//x1 ligger til venstre for x2 og i kortere avstand enn iconOverlapOffsetWidth.
                    movedown = true;
                }
                if (movedown || moveup){//endelig, ikonet må flyttes.
                    var newx;
                    var newY;
                    
                }
        }
    }
}
*/