/*
 * CoordHelper.java
 *
 * Created on 21. august 2005, 22:50
 *
 */

package no.geodata.maputil;

import java.util.*;

/**
 *
 * @author hanst
 */
public class CoordHelper {
    
    public static final double METERS_PR_INCH = 0.0254;
    public static final int DPI = 96;
    public static final double pixelSize = METERS_PR_INCH/DPI;
    /** Creates a new instance of CoordHelper */
    public CoordHelper() {
    }
    /**
     * @param sCoord Coordinate string, x1,y1;x2,y2;x3,y3;x4,y4...osv.
     * @return Vector containing MapPoint objects.
     */
    public Vector parseCoordString(String sCoord){
        System.out.println("parseCoordString(String sCoord): entered");
        Vector vMapPoint = new Vector();        
        int i = 0;
        int j = 0;
        int k = 0;
        int length = sCoord.length();
        if (length > 0){
            sCoord = sCoord + ";";
        }            
        String sMP = new String();
        double x,y;       
        boolean reachEnd = false;
        while(!reachEnd){
             j = sCoord.indexOf(";", i);  
             System.out.println("j = "+j+", i = "+i);
             if (j == -1)//siste punkt 
                 reachEnd = true;
             else {
                 sMP = sCoord.substring(i,j);
                 k = sMP.indexOf(",");
                 System.out.println("sMP = "+sMP);
                 x = Double.parseDouble(sMP.substring(0, k));
                 y = Double.parseDouble(sMP.substring(k+1));
                 MapPoint mp = new MapPoint();
                 mp.setX(x);
                 mp.setY(y);
                 vMapPoint.add(mp);
                 System.out.println("x: "+x+", y: "+y);
                 i = j+1;
             }                    
        }        
        return vMapPoint;
    }
    
    /**
     * Generates MapEnvelope from given point, scale, imgwidth and imgheight
     *
     * @param mp MapPoint, centerpoint in map
     * @param zoomscale long, requested mapscale
     * @param imgWidth int, image pixel width
     * @param imgHeigth int, image pixel height
     * @return MapEnvelope, 
     */
    public MapEnvelope makeEnvelope(MapPoint mp, long zoomscale, int imgWidth, int imgHeigth){             
        double metersPrPixel = zoomscale*pixelSize;        
        double maxX = mp.getX() + metersPrPixel*imgHeigth;
        double minX = mp.getX() - metersPrPixel*imgHeigth;
        double maxY = mp.getY() + metersPrPixel*imgWidth;
        double minY = mp.getY() - metersPrPixel*imgWidth;        
        MapEnvelope mapEnvelope = new MapEnvelope(maxX,minX,maxY,minY);           
        return mapEnvelope;
    }
    
    /**
     * Generates MapEnvelope from a vector of points, imgwidth and imgheight.
     * @param vMapPoints vector containg MapPoints
     * @param envFactor double, factor to enlarge envelope 
     * @param imgWidth int, image pixel width
     * @param imgHeigth int, image pixel height
     * @return MapEnvelope, 
     */
    public MapEnvelope makeEnvelope(Vector vMapPoints, int imgWidth, int imgHeigth, double envFactor){        
        //loope igjennom vector for å finne max/ min verdier
        MapPoint mp = new MapPoint();
        mp = (MapPoint) vMapPoints.get(0);
        double maxX = mp.getX();
        double minX = mp.getX();
        double maxY = mp.getY();
        double minY = mp.getY();
        double tempX, tempY;
        for (int i=1; i < vMapPoints.size(); i++ ){
            mp = (MapPoint) vMapPoints.get(i);
            tempX = mp.getX();
            tempY = mp.getY();
            if (tempX > maxX)
                maxX = tempX;
            else if (tempX < minX)
                minX = tempX;
            if (tempY > maxY)
                maxY = tempY;
            else if (tempY < minY)
                minY = tempY;
        }
        //utvider envelop'en litt slik at ingen punkt blir liggende i kartkanten
        double deltaX = maxX-minX;
        double deltaY = maxY-minY;
        double middleX = minX + (deltaX)/2;
        double middleY = minY + (deltaY)/2;
        maxX = middleX + (deltaX*envFactor)/2;
        minX = middleX - (deltaX*envFactor)/2;
        maxY = middleY + (deltaY*envFactor)/2;
        minY = middleY - (deltaY*envFactor)/2;        
        
        MapEnvelope mapEnvelope = new MapEnvelope(maxX,minX,maxY,minY);     
        
        return mapEnvelope;
    }
    
}
