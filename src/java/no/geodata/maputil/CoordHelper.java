/*
 * CoordHelper.java
 *
 * Created on 21. august 2005, 22:50
 *
 */

package no.geodata.maputil;

import java.util.*;
import no.schibstedsok.front.searchportal.response.FastCompaniesSearchResult;

/**
 *
 * @author hanst
 */
public class CoordHelper {
    
    final static long zoomLevel_1 = 10000; //kartskala ved zoom til ett punkt
    final static long zoomLevel_2 = 20000;
    final static long zoomLevel_3 = 50000;
    final static long zoomLevel_4 = 150000;
    final static long zoomLevel_5 = 500000;
    final static double envFactor = 1.2; //faktor for å lage rom rundt envelope
    final static int imgWidth = 350;//bildestørrelse i pixler, bredde
    final static int imgHeigth = 400;//bildestørrelse i pixler, høyde
    final static int iconPxSize = 10;
    final static int mapCenterPxX = imgWidth/2 - iconPxSize/2;//kartets midtpunkt i pixler, bredde
    final static int mapCenterPxY = imgHeigth/2 - iconPxSize/2;//kartets midtpunkt i pixler - iconets størrelse/2, høyde
    
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
        int counter = 0;
        boolean reachEnd = false;
        while(!reachEnd){
             counter++;
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
                 mp.setId(counter);
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
    public MapEnvelope makeEnvelope(MapPoint mp, long zoomscale, long imgWidth, long imgHeigth){             
        double metersPrPixel = zoomscale*pixelSize;        
        double maxX = mp.getX() + metersPrPixel*imgHeigth;
        double minX = mp.getX() - metersPrPixel*imgHeigth;
        double maxY = mp.getY() + metersPrPixel*imgWidth;
        double minY = mp.getY() - metersPrPixel*imgWidth;        
        MapEnvelope mapEnvelope = new MapEnvelope(maxX,minX,maxY,minY);           
        return mapEnvelope;
    }  
    
    /**
     * Returns MapEnvelope from given company object containing x and y coordinate.
     *
     * @param company, FastCompaniesSearchResult 
     * @return MapEnvelope
     */
    public MapEnvelope getEnvelope(FastCompaniesSearchResult company){
        double x = Double.parseDouble(company.getX());
        double y = Double.parseDouble(company.getY());
        MapPoint mp = new MapPoint();
        mp.setX(x);
        mp.setY(y);
        return makeEnvelope(mp, zoomLevel_2, imgWidth, imgHeigth);
    }
   
     /**
     * Returns MapEnvelope from given vector of company objects containing x and y coordinates.
     *
     * @param companies
     * @return MapEnvelope
     */
    //public MapEnvelope getEnvelope()
    
    
    /**
     * Generates MapEnvelope from a vector of points, imgwidth and imgheight.
     * @param vMapPoints vector containg MapPoints
     * @param envFactor double, factor to enlarge envelope 
     * @param imgWidth int, image pixel width
     * @param imgHeigth int, image pixel height
     * @return MapEnvelope, 
     */
    public MapEnvelope makeEnvelope(Vector vMapPoints, long imgWidth, long imgHeigth, double envFactor){        
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
    
    /**
     * Converts real world coordinates to imagepixel coordinates. Eliminates points outside given MapEnvelope.
     * @param vMapPoints Vector of real world map point coordinates
     * @param imgHeigth image heigth
     * @param imgWidth image width
     */
    public Vector convertToPixelCoord(MapEnvelope me, Vector vMapPoints, long imgHeigth, long imgWidth){
        Vector vMapPointsNew = new Vector();
        double maxX = me.getMaxX();
        double minX = me.getMinX();
        double maxY = me.getMaxY();
        double minY = me.getMinY();
         //finne UTM koordinater for øvre høyre samt nedre venstre.
        double upperLeftX = me.getMinX();
        double upperLeftY = me.getMaxY();
        double lowerRightX = me.getMaxX();
        double lowerRightY = me.getMinY();
        //må først sjekke hvilke punkt som ligger innenfor eksisterende MapEnvelope (kan ha kommet utenfor kartextentet etter navigering)
        double tempX, tempY;
        MapPoint mp = new MapPoint();
        boolean inside;
        for (int i = 0; i < vMapPoints.size(); i++){
            inside = true;//
            mp = (MapPoint) vMapPoints.get(i);
            tempX = mp.getX();
            tempY = mp.getY();
            if (tempX > maxX)
                inside = false;
            else if (tempX < minX)
                inside = false;
            if (tempY > maxY)
                inside = false;
            else if (tempY < minY)
                inside = false;            
            if(inside)//dersom punktet ligger innenfor envelop'en tas det var på.
                vMapPointsNew.add(mp);
        }        
        //beregne bildepixel koordinater
        double deltaX = maxX-minX;
        double deltaY = maxY-minY;
        double yFactor = imgHeigth/deltaY;//meter pr pixel
        double xFactor = imgWidth/deltaX;//meter pr pixel
        double deltaX0Xn, deltaY0yn;
        long pixValueHeight, pixValueWidth;        
        for (int j = 0; j < vMapPointsNew.size(); j++){
            mp = (MapPoint) vMapPointsNew.get(j);
            tempX = mp.getX();
            pixValueWidth = Math.round((tempX - upperLeftX)*xFactor);
            mp.setPxX(pixValueWidth);
            tempY = mp.getY();
            pixValueHeight = Math.round((upperLeftY-tempY)*yFactor);
            mp.setPxY(pixValueHeight);
        }       
        return vMapPointsNew;
    }
    
    public long getZoomLevel_1(){
        return this.zoomLevel_1;
    }
    public long getZoomLevel_2(){
        return this.zoomLevel_2;
    }
    public long getZoomLevel_3(){
        return this.zoomLevel_3;
    }
    public long getZoomLevel_4(){
        return this.zoomLevel_4;
    }
    public long getZoomLevel_5(){
        return this.zoomLevel_5;
    }
    public int getImgWidth(){
        return this.imgWidth;
    }
     public int getImgHeigth(){
        return this.imgHeigth;
    }
    public double getEnvFactor(){
        return this.envFactor;
    }
    public double getMapCenterPxX(){
        return this.mapCenterPxX;
    }
    public double getMapCenterPxY(){
        return this.mapCenterPxY;
    }
}
