function getTMqs(furl, dotl, tmac, tmcc, enc, sec) {
    var n = new Date();
    var loc = window.location.href;
    var prot = window.location.protocol;

    if (furl.indexOf('R>') == 0) {
        furl = furl.substring(2);
	loc = furl;
    }

    if (loc.length > 256) {
        loc = loc.substring(0,253) + '...';
    }

    if (sec) {
        if(loc.indexOf('?') != -1) {
           loc += '&' + sec.join('&');
        } else {
           loc += '?' + sec.join('&');
        }
    }

    if (window.parent != window.self) {
        var ref = parent.document.referrer;
    } else {
        var ref = document.referrer;
    }

    var qs = loc + '*' + furl + '*' + dotl + '*' + ref + '*' + navigator.javaEnabled() + '/';
    if (window.screen) {
        qs += screen.width+'x'+screen.height + '/' + screen.colorDepth + '/';
    } else {
        qs += '//';
    };
    qs += tmac + '/' + tmcc + '/' + enc + '/' + n.getTime() + '/';
    
    var tmImg1 = '<img name="tmImg1" border="0" height="1" width="1" onLoad="nxtImg(\'tmImg2\', \'' + prot + '\', \'' + qs + '\')">';
    var tmImg2 = '<img name="tmImg2" border="0" height="1" width="1">';
    document.write(tmImg1);
    document.write(tmImg2);
    nxtImg('tmImg1', prot, n.getTime()); 
}

var tmLoc = 'statistik-gallup.net';
var tmVer = 'V11';
var tmLd = false;
function nxtImg(img, prot, qs)
{
   if((!document.images) || tmLd) return;
   tmLd = true;
   var n = '';
   if(img == 'tmImg1') {
      n = prot + '//' + tmLoc + '/VC' + qs;
   }
   else if(img == 'tmImg2') {
      n = prot + '//' + tmLoc + '/' + tmVer + qs;
   }
   if(document.images[img].src.indexOf(n)<0) {
      document.images[img].src = n;
   }
   tmLd = false;
}
