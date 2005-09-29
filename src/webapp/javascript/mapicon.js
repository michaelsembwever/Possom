var arrPixCoord = new Array();
var imgIconName = "icon_";//ikonene i kartet har dette navnet etterfulgt av id nummer, eks icon_1.

//icon i kartet
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


//plassèr kart ikoner ift kartextent               
function adjustIcons(isInitiated){
    //alert("adjustIcons(). coordArray length = " + arrCompany.length);
    //looper igjennom koordinatobjektarrayen og fjerner de som ikke er innenfor det nye extentet   
    var arrCompanyToShow = new Array();
    if (isInitiated != "false"){
        //alert('arrCompany.length ' + arrCompany.length);
        var j = -1;
        for (var i=0; i<arrCompany.length; i++) {
            //alert("loop = " + i + ", arrCompany[i].id = " + arrCompany[i].id + "arrCompany[i].x " + arrCompany[i].x +", arrCompany[i].y: " +arrCompany[i].y);
            if (isInsideExtent(arrCompany[i])){ 
                j = j + 1;
                arrCompanyToShow[j] = arrCompany[i];  
                //alert("isInside: loop = " + i + ", arrCompany[i].id = " + arrCompany[i].id + "arrCompany[i].x " + arrCompany[i].x +", arrCompany[i].y: " +arrCompany[i].y);
            }            
        }
    }
    else 
        arrCompanyToShow = arrCompany;   
    //alert('arrCompanyToShow.length ' + arrCompanyToShow.length);
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
                //alert("adjustIcons(): imgName = " + imgName + ", arrPixCoord[i].y: " + arrPixCoord[i].y + ", arrPixCoord[i].x: " + arrPixCoord[i].x);
                document.images[imgName].style.top = arrPixCoord[i].y+'px';
                document.images[imgName].style.left = arrPixCoord[i].x+'px';
                //document.images[imgName].style.clip = "rect("+ this.imgHeigth + "px, "+this.imgWidth+"px, 0px, 0px)";
                /*
                if((imgWidth-iconWidth)<arrPixCoord[i].x || (imgHeigth-iconHeigth)<arrPixCoord[i].y || arrPixCoord[i].x  < 0 || arrPixCoord[i].y < 0 ){                    
                    if((imgWidth-iconWidth)<arrPixCoord[i].x)
                        right = imgWidth - arrPixCoord[i].x;
                    if((imgHeigth-iconHeigth)<arrPixCoord[i].y)
                        top = imgHeigth - arrPixCoord[i].y; 
                    if(arrPixCoord[i].x  < 0)    
                        left =  iconWidth - arrPixCoord[i].x;
                    if(arrPixCoord[i].y < 0)    
                        bottom =  iconHeigth - arrPixCoord[i].y;
                    //alert("rect("+ top +"px, " + right + "px, " + bottom + "px, " + left + "px)");
                    document.images[imgName].style.clip = "rect("+ top +"px, " + right + "px, " + bottom + "px, " + left + "px)";
                }
                else {
                    document.images[imgName].style.clip = "rect("+ top +"px, " + right + "px, 0px, 0px)";
                }
                */
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
        //alert('compObj.id ' + compObj.id +', utenfor extent');
	document.images[imgName].style.visibility = "hidden";
	return false;
    }
    else{
        //alert('compObj.id ' + compObj.id +', innenfor extent, visible '+document.getElementById(imgName).style.visibility);
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
    return tmpPixArray;
}
