//Create a DHTML layer

function createLayer(name, inleft, intop, width, height, visible, content) {
    document.writeln('<div id="' + name + '" name="' + name + '" style="position:absolute; overflow:hidden; z-index: 2; left:' + inleft + 'px; top:' + intop + 'px; width:' + width + 'px; height:' + height + 'px;' + '; visibility:' + (visible ? 'visible;' : 'hidden;') +  '">');
    document.writeln(content);
    document.writeln('</div>');
}

//Must set the zoombox layers positions after the page has loaded, ettersom disse ikke vites for

function initZoomBoxLayers(){
    document.getElementById("zoomBoxTop").style.top = yMapOffset;
    document.getElementById("zoomBoxLeft").style.left = xMapOffset;
    document.getElementById("zoomBoxBottom").style.top = yMapOffset;
    document.getElementById("zoomBoxRight").style.left = xMapOffset;
}

//get the layer object called "name"
function getLayer(name) {
	var theObj = document.getElementById(name);
	return theObj.style;
}

//move layer to x,y
function moveLayer(name, x, y) {
	var layer = getLayer(name);
	layer.left = x + "px";
	layer.top  = y + "px";
}

//set layer background color
function setLayerBackgroundColor(name, color) {
	var layer = getLayer(name);
	layer.backgroundColor = color;
}		
		
//toggle layer to invisible
function hideLayer(name) {		
	var layer = getLayer(name);
	layer.visibility = "hidden";
}		
		
//toggle layer to visible
function showLayer(name) {		
	var layer = getLayer(name);
	layer.visibility = "visible";
}
		
//clip layer display to clipleft, cliptip, clipright, clipbottom
function clipLayer(name, clipleft, cliptop, clipright, clipbottom) {
	var layer = getLayer(name);
        var newWidth = clipright - clipleft;
        var newHeight = clipbottom - cliptop;
        layer.height = newHeight + "px";
        layer.width	= newWidth + "px";	
	layer.top = cliptop + "px";
	layer.left = clipleft + "px";
}

function boxIt(theLeft,theTop,theRight,theBottom) {
        theTop = theTop + yMapOffset;
	theBottom = theBottom + yMapOffset;
	theLeft = theLeft + xMapOffset;
	theRight = theRight + xMapOffset;        
	clipLayer("zoomBoxTop",theLeft,theTop,theRight,theTop+ovBoxSize);
	clipLayer("zoomBoxLeft",theLeft,theTop,theLeft+ovBoxSize,theBottom);
	clipLayer("zoomBoxRight",theRight-ovBoxSize,theTop,theRight,theBottom);
	clipLayer("zoomBoxBottom",theLeft,theBottom-ovBoxSize,theRight,theBottom);	
	showLayer("zoomBoxTop");
	showLayer("zoomBoxLeft");
	showLayer("zoomBoxRight");
	showLayer("zoomBoxBottom");
}

//clip zoom box layer to mouse coords
function setClip() {	
	var tempX=x1;
	var tempY=y1;
	if (x1>x2) {
		zright=x1;
		zleft=x2;
	} else {
		zleft=x1;
		zright=x2;
	}
	if (y1>y2) {
		zbottom=y1;
		ztop=y2;
	} else {
		ztop=y1;
		zbottom=y2;
	}
	if ((x1 != x2) && (y1 != y2)) {
		boxIt(zleft,ztop,zright,zbottom);
	}
}

