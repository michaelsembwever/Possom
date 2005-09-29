//Create a DHTML layer
function createLayer(name, inleft, intop, width, height, visible, content) {
	document.writeln('<div id="' + name + '" name="' + name + '" style="z-index: 1000;position:absolute; overflow:hidden; left:' + inleft + 'px; top:' + intop + 'px; width:' + width + '; height:' + height + ';' + '; visibility:' + (visible ? 'visible;' : 'hidden;') +  '">');
	document.writeln(content);
	document.writeln('</div>');
	//alert(getLayer(name));
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
	
	//Keep original image size when panning
	if (!pan) {
		var newWidth = clipright - clipleft;
		var newHeight = clipbottom - cliptop;
		layer.height = newHeight;
		layer.width	= newWidth;
	}	

	layer.top	= cliptop;
	layer.left	= clipleft;
}

function boxIt(theLeft,theTop,theRight,theBottom) {
	theTop = theTop + xMapOffset;
	theBottom = theBottom + xMapOffset;
	theLeft = theLeft + yMapOffset;
	theRight = theRight + yMapOffset;

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

