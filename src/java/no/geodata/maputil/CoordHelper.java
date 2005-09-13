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
    
    public final static long zoom_1 = 2000; //kartskala ved zoom til ett punkt
    public final static long zoom_2 = 5000;
    public final static long zoom_3 = 10000;
    public final static long zoom_4 = 50000;
    public final static long zoom_5 = 100000;
    public final static long zoom_6 = 500000;
    public final static int zoomCount = 6;
    public final static int defaultZoom = 1;    
    public final static double envFactor = 1.2; //faktor for å lage rom rundt envelope
    public final static int imgWidth = 350;//bildestørrelse i pixler, bredde
    public final static int imgHeigth = 400;//bildestørrelse i pixler, høyde
    public final static int iconPxSize = 10;
    public final static int mapCenterPxX = imgWidth/2 - iconPxSize/2;//kartets midtpunkt i pixler, bredde
    public final static int mapCenterPxY = imgHeigth/2 - iconPxSize/2;//kartets midtpunkt i pixler - iconets størrelse/2, høyde
    public final static double panFactor = 0.25;//faktor som forteller hvor mye kartsentrum skal flyttet iforhold til kartets deltaX og y ved panning. 
    public final static double zoomFactor = 2;//faktor som forteller hvor mye kartextentet skal minskes/utvides ved zoom inn/ut. zoom inn = 1/zoomFactor
    public final static double defaultNoCoord= -9999999;
    
    public double maxX;
    public double minX;
    public double maxY;
    public double minY;
    public double mapCenterCoordX;
    public double mapCenterCoordY;
    
    public static final double METERS_PR_INCH = 0.0254;
    public static final int DPI = 96;
    public static final double pixelSize = METERS_PR_INCH/DPI;
    public final static double imgMeterSize = pixelSize * imgWidth;//størrelse på kart på skjermen i meter. 
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
     * @param zoomscale int, requested zoomnivaa
     * @return MapEnvelope, 
     */
    public MapEnvelope makeEnvelope(MapPoint mp, int zoom){     
        long zoomscale = getZoom(zoom);
        double metersPrPixel = zoomscale * pixelSize;        
        this.maxX = Math.round(mp.getX() + metersPrPixel * this.imgHeigth);
        this.minX = Math.round(mp.getX() - metersPrPixel * this.imgHeigth);
        this.maxY = Math.round(mp.getY() + metersPrPixel * this.imgWidth);
        this.minY = Math.round(mp.getY() - metersPrPixel * this.imgWidth);        
        MapEnvelope mapEnvelope = new MapEnvelope(this.maxX,this.minX,this.maxY,this.minY);           
        return mapEnvelope;
    }  
    
    /**
     * Returns MapEnvelope from given company object containing x and y coordinate.
     *
     * @param company, FastCompaniesSearchResult 
     * @param zoomnivaa
     * @return MapEnvelope
     */
    public MapEnvelope getEnvelope(FastCompaniesSearchResult company){
        double x = Double.parseDouble(company.getX());
        double y = Double.parseDouble(company.getY());
        MapPoint mp = new MapPoint();
        mp.setX(x);
        mp.setY(y);
        int zoomnivaa = this.defaultZoom;
        return makeEnvelope(mp, zoomnivaa);
    }
   
    /**
     * Returns MapEnvelope from given list of company objects.
     * @param companies
     * @return MapEnvelope
     */
    public MapEnvelope getEnvelope(List companies){
        //må beregne kartenvelope
        Vector mps = new Vector();
        
        FastCompaniesSearchResult company;
        boolean hasCoords;
        for (int i = 0; i < companies.size(); i++){
            MapPoint mp = new MapPoint();
            hasCoords = false;
            company = (FastCompaniesSearchResult) companies.get(i);
            try{
                mp.x = Double.parseDouble(company.getX());
                mp.y = Double.parseDouble(company.getY());
                hasCoords = true;
            }
            catch(Exception e){
                System.out.println("Warning: Finnes ikke koordinater for company object " + company.getCompanyId());
            }
            if (!hasCoords){
                mp.x = defaultNoCoord;
                mp.x = defaultNoCoord;                                
            }
            mps.add(mp);            
        }
        return makeEnvelope(mps);
    }
    
     /**
     * Returns MapEnvelope from given vector of company objects containing x and y coordinates.
     *
     * @param companies
     * @return MapEnvelope
     */
    public MapEnvelope getEnvelope(){
        MapEnvelope me = new MapEnvelope();
        return me;
    }
    
    
    /**
     * Generates MapEnvelope from a vector of points, imgwidth and imgheight.
     * @param vMapPoints vector containg MapPoints
     * @param envFactor double, factor to enlarge envelope 
     * @param imgWidth int, image pixel width
     * @param imgHeigth int, image pixel height
     * @return MapEnvelope, 
     */
    public MapEnvelope makeEnvelope(Vector vMapPoints){        
        //loope igjennom vector for å finne max/ min verdier
        MapPoint mp = new MapPoint(); 
        MapEnvelope mapEnvelope = new MapEnvelope();
        
        if(vMapPoints.size()>0){
            mp = (MapPoint) vMapPoints.get(0);
            boolean initiert = false;
            double maxX = mp.getX();
            double minX = mp.getX();
            double maxY = mp.getY();
            double minY = mp.getY();
            if(maxX != defaultNoCoord)
                initiert = true;
            double tempX, tempY;
            for (int i=1; i < vMapPoints.size(); i++ ){   
                if(!initiert){ //dersom første mappoint var en dummy, må initielle verdier settes på nytt.                   
                    maxX = mp.getX();
                    minX = mp.getX();
                    maxY = mp.getY();
                    minY = mp.getY();
                    if(maxX != defaultNoCoord)
                        initiert = true;
                }
                mp = (MapPoint) vMapPoints.get(i);
                tempX = mp.getX();
                tempY = mp.getY();
                if(tempX != defaultNoCoord){//må se bort fra punkt som er tilordnet en dummy kooordinat verdi
                    if (tempX > maxX)
                        maxX = tempX;
                    else if (tempX < minX)
                        minX = tempX;
                    if (tempY > maxY)
                        maxY = tempY;
                    else if (tempY < minY)
                        minY = tempY;
                }
            }
            //utvider envelop'en litt slik at ingen punkt blir liggende i kartkanten
            double deltaX = maxX-minX;
            double deltaY = maxY-minY;

            //beregner delta x og y slik at de har et forhold som er lik imgHeight/imgWidth
            double factorHW = this.imgHeigth/this.imgWidth;
            double factor_dXdY = deltaY/deltaX;
            if (factor_dXdY < factorHW ){//bredden er større enn høyde. Må utvide høyde for å få riktige proposisjoner
                deltaY = (deltaY*factor_dXdY)/factorHW;
                System.out.println("makeEnvelope(): deltaY = (deltaY "+ deltaY + "*factor_dXdY " + factor_dXdY + ")/factorHW "+factorHW);
            }
            else if(factor_dXdY > factorHW){//høyde er større enn bredde. Må utvide bredde for å få riktige proposisjoner
                deltaX = (deltaX*factor_dXdY)/factorHW;                
                System.out.println("makeEnvelope(): deltaX = (deltaX "+ deltaX + "*factor_dXdY " + factor_dXdY + ")/factorHW "+factorHW);
            }        
            double middleX = minX + (deltaX)/2;
            double middleY = minY + (deltaY)/2;
            this.maxX = Math.round(middleX + (deltaX * this.envFactor)/2);
            this.minX = Math.round(middleX - (deltaX * this.envFactor)/2);
            this.maxY = Math.round(middleY + (deltaY * this.envFactor)/2);
            this.minY = Math.round(middleY - (deltaY * this.envFactor)/2);        

            mapEnvelope = new MapEnvelope(maxX,minX,maxY,minY);     
        }
        return mapEnvelope;
    }
    
    /**
     * Converts real world coordinates to imagepixel coordinates. Eliminates points outside given MapEnvelope.
     * @param vMapPoints Vector of real world map point coordinates
     * @param imgHeigth image heigth
     * @param imgWidth image width
     */
    public Vector convertToPixelCoord(MapEnvelope me, Vector vMapPoints){
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
        double yFactor = this.imgHeigth/deltaY;//meter pr pixel
        double xFactor = this.imgWidth/deltaX;//meter pr pixel
        //double deltaX0Xn, deltaY0yn;
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
    public long getZoom(int zoom){
        long zoomscale = 0;
        if (zoom == 1)
            zoomscale = this.zoom_1;
        else if (zoom == 2)
            zoomscale = this.zoom_2;
        else if (zoom == 3)
            zoomscale = this.zoom_3;
        else if (zoom == 4)
            zoomscale = this.zoom_4;
        else if (zoom == 5)
            zoomscale = this.zoom_5;   
        else if (zoom == 6)
            zoomscale = this.zoom_6; 
        return zoomscale;
    }
    
    public ArrayList getZoomLevels(){ 
        ArrayList zoomLevels = new ArrayList(zoomCount);
        //long[] zoomLevels = new long[zoomCount];
        zoomLevels.add(Long.toString(zoom_1));
        zoomLevels.add(Long.toString(zoom_2));
        zoomLevels.add(Long.toString(zoom_3));
        zoomLevels.add(Long.toString(zoom_4));
        zoomLevels.add(Long.toString(zoom_5));
        zoomLevels.add(Long.toString(zoom_6));        
        return zoomLevels;     
    }
    
    public int getDefaultZoom(){
        return this.defaultZoom;        
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
    
    public int getZoomCount(){
        return this.zoomCount;
    }
    public double getPanFactor(){
        return this.panFactor;
    }
    public double getZoomFactor(){
        return this.zoomFactor;
    }
    public double getImgMeterSize(){
        return this.imgMeterSize;
    }
    
    
    public double getMapCenterPxX(){
        return this.mapCenterPxX;
    }
    public double getMapCenterPxY(){
        return this.mapCenterPxY;
    }
    public double getMapCenterCoordX(){
        return this.mapCenterCoordX;
    }
    public double getMapCenterCoordY(){
        return this.mapCenterCoordY;
    }
    public double getDefaultNoCoord(){
        return this.defaultNoCoord;
    }
 }
