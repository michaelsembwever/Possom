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
    
    public final static long zoom_1 = 2000; 
    public final static long zoom_2 = 7000;
    public final static long zoom_3 = 25000;
    public final static long zoom_4 = 70000;
    public final static long zoom_5 = 300000;
    public final static long zoom_6 = 800000;
    public final static long zoom_7 = 3000000;
    
    public final static int zoomCount = 7;
    public final static int defaultZoom = 1;    
    public final static int imgWidth = 363;//bildestørrelse i pixler, bredde
    public final static int imgHeigth = 363;//bildestørrelse i pixler, høyde    
    public final static int envFactor = 30; //faktor for å lage rom rundt envelope. Angis i pixler
    public final static int iconPxSizeHeigth = 34;
    public final static int iconPxSizeWidth = 28;
    public final static int iconOffsetHeigth = 0;//plassering av ikon, avvik fra top. Positiv verdi
    public final static int iconOffsetWidth = 0;//plassering av ikon, avvik fra venstre. Positiv verdi 
    public final static int mapCenterPxX = imgWidth/2;//kartets midtpunkt i pixler, bredde
    public final static int mapCenterPxY = imgHeigth/2;//kartets midtpunkt i pixler - iconets størrelse/2, høyde
    public final static double panFactor = 0.45;//faktor som forteller hvor mye kartsentrum skal flyttet iforhold til kartets deltaX og y ved panning. 
    public final static double zoomFactor = 2;//faktor som forteller hvor mye kartextentet skal minskes/utvides ved zoom inn/ut. zoom inn = 1/zoomFactor
    public final static double defaultNoCoord= -9999999;
    
    public double maxX = 1100000;//initielle verdier. Envelope som dekker hele Norge.
    public double minX = -83000;
    public double maxY = 7950000;
    public double minY = 6440000;
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
        long zoomscale = getZoomScale(zoom);
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
     * Returns MapEnvelope from given list of company objects and converts coords to pixCoords
     * @param companies
     * @return MapEnvelope
     */
    public MapEnvelope getEnvelope(List companies){
        Vector mps = extractMapPoints(companies);
        return makeEnvelope(mps);
    }
    
    /**
     * Returns Arraylist of MapPoints including pix coordinates.
     * @param companies
     * @param companies
     */
    public ArrayList getMapPoints(List companies, MapEnvelope me){
        Vector mps = extractMapPoints(companies);
        //MapEnvelope me = makeEnvelope(mps);
        return convertToPixelCoord(me, mps, true);
    }
    
    /**
     * Extracts coordinates from a list of Company objects. Returns av vector of MapPoints.
     *
     */
    private Vector extractMapPoints(List companies){
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
            System.out.println("extractMapPoints(): koordinater object: "+i+", x = "+mp.x+", y = "+mp.y );
            mps.add(mp);            
        }
        return mps;
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
        if(vMapPoints.size()>1){
            mp = (MapPoint) vMapPoints.get(0);
            boolean initiert = false;
            double tmpMaxX = mp.getX();
            double tmpMinX = mp.getX();
            double tmpMaxY = mp.getY();
            double tmpMinY = mp.getY();
            if(tmpMaxX != defaultNoCoord)
                initiert = true;
            double tempX, tempY;
            int counter = 0;
            for (int i=0; i < vMapPoints.size(); i++ ){   
                mp = (MapPoint) vMapPoints.get(i);
                if(!initiert){ //dersom mappoint er en dummy, må initielle verdier settes på nytt når første riktige koordinater kommer                  
                    tmpMaxX = mp.getX();
                    tmpMinX = mp.getX();
                    tmpMaxY = mp.getY();
                    tmpMinY = mp.getY();
                    if(tmpMaxX != defaultNoCoord)
                        initiert = true;
                }                
                tempX = mp.getX();
                tempY = mp.getY();
                if(tempX != defaultNoCoord){//må se bort fra punkt som er tilordnet en dummy kooordinat verdi
                    counter++;//teller hvor mange i listen som har riktige koordinater
                    if (tempX > tmpMaxX)
                        tmpMaxX = tempX;
                    else if (tempX < tmpMinX)
                        tmpMinX = tempX;
                    if (tempY > tmpMaxY)
                        tmpMaxY = tempY;
                    else if (tempY < tmpMinY)
                        tmpMinY = tempY;
                }
            }
            if(!initiert){ //ingen punkter i lista inneholder riktige koordinater
                System.out.println("Companylisten inneholder ingen objekter med koordinater");
                mapEnvelope.maxX = this.maxX;
                mapEnvelope.maxY = this.maxY;
                mapEnvelope.minX = this.minX;
                mapEnvelope.minY = this.minY;
            }
            else if(counter == 1){ //kun ett punkt i lista inneholder riktige koordinater     
                System.out.println("Companylisten inneholder kun ett objekt med koordinater");
                mapEnvelope = makeEnvelope((MapPoint)vMapPoints.get(0), defaultZoom);
            }
            else {                
                //utvider envelop'en litt slik at ingen punkt blir liggende i kartkanten
                double deltaX = (tmpMaxX-tmpMinX);
                double deltaY = (tmpMaxY-tmpMinY);
                double centerX = tmpMinX + deltaX/2;
                double centerY = tmpMinY + deltaY/2;
                //beregner delta x og y slik at de har et forhold som er lik imgHeight/imgWidth
                double factorHW = (double)this.imgWidth/ (double)this.imgHeigth;
                double factor_dXdY = deltaX/deltaY;
                if (factor_dXdY < factorHW ){//høyden er iforhold til bildehøyde/bredde mindre enn bredde. Må utvide bredde for å få riktige proposisjoner
                    deltaX = deltaX * (factorHW/factor_dXdY);
                    System.out.println("makeEnvelope(): deltaY = (deltaY "+ deltaY + "*factor_dXdY " + factor_dXdY + ")/factorHW "+factorHW);
                }
                else if(factor_dXdY > factorHW){//høyde er større enn bredde. Må utvide bredde for å få riktige proposisjoner
                    deltaY = deltaY * (factor_dXdY/factorHW);                
                    System.out.println("makeEnvelope(): deltaX = (deltaX "+ deltaX + "*factor_dXdY " + factor_dXdY + ")/factorHW "+factorHW);
                }     
                //utvider kartenvelop med hensyn til størrelsen på ikon og offset, slik at ikonene ikke kommer i kartkanten.            
                double extFact = deltaX/imgWidth;
                double extendRigth = (Math.abs(iconPxSizeHeigth-iconOffsetHeigth) + envFactor)*extFact;
                double extendLeft = (iconOffsetHeigth + envFactor)*extFact;
                double extendTop = (iconOffsetWidth + envFactor)*extFact;
                double extendBottom = (Math.abs(iconPxSizeWidth-iconOffsetWidth) + envFactor)*extFact;
                mapEnvelope.maxX = Math.round(centerX + (deltaX/2 + extendRigth));
                mapEnvelope.minX = Math.round(centerX - (deltaX/2 + extendLeft));
                mapEnvelope.maxY = Math.round(centerY + (deltaY/2 + extendTop));
                mapEnvelope.minY = Math.round(centerY - (deltaY/2 + extendBottom));                 
                //System.out.println("makeEnvelope(), utvidede max/min koordverdier: maxX = " + maxX + ", minX = " + minX +", maxY = " + maxY + ", minY = " + minY);
            }
        }
        System.out.println("makeEnvelope(), utvidede max/min koordverdier: maxX = " + mapEnvelope.maxX + ", minX = " + mapEnvelope.minX +", maxY = " + mapEnvelope.maxY + ", minY = " + mapEnvelope.minY);
        mapEnvelope = new MapEnvelope(mapEnvelope.maxX, mapEnvelope.minX, mapEnvelope.maxY, mapEnvelope.minY);
        return mapEnvelope;
    }
    
    /**
     * Converts real world coordinates to imagepixel coordinates. Eliminates points outside given MapEnvelope.
     * @param vMapPoints Vector of real world map point coordinates
     * @param me
     * @param init
     */
    public ArrayList convertToPixelCoord(MapEnvelope me, Vector vMapPoints, boolean init){
        ArrayList arrList = new ArrayList(vMapPoints.size());        
        double maxX = me.getMaxX();
        double minX = me.getMinX();
        double maxY = me.getMaxY();
        double minY = me.getMinY();
        System.out.println("convertToPixelCoord(), max/min koordverdier: maxX = " + maxX + ", minX = " + minX +", maxY = " + maxY + ", minY = " + minY);
         //finne UTM koordinater for øvre høyre samt nedre venstre.
        double upperLeftX = me.getMinX();
        double upperLeftY = me.getMaxY();
        double lowerRightX = me.getMaxX();
        double lowerRightY = me.getMinY();
        //må først sjekke hvilke punkt som ligger innenfor eksisterende MapEnvelope (kan ha kommet utenfor kartextentet etter navigering)
        double tempX, tempY;
        MapPoint mp = new MapPoint();
        if (!init){//dersom det er navigering i kartet må det sjekkes om punktene faller utenfor nytt extent            
            Vector vMapPointsNew = new Vector();
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
                System.out.println("convertToPixelCoord(): pixelkoordinat object: "+i+", x = "+tempX+", y = "+tempY );
            }     
            vMapPoints = vMapPointsNew;
        }
        //beregne bildepixel koordinater
        double deltaX = maxX-minX;
        double deltaY = maxY-minY;
        double yFactor = this.imgHeigth/deltaY;//meter pr pixel
        double xFactor = this.imgWidth/deltaX;//meter pr pixel
        //double deltaX0Xn, deltaY0yn;
        long pixValueHeight, pixValueWidth;        
        for (int j = 0; j < vMapPoints.size(); j++){
            mp = (MapPoint) vMapPoints.get(j);
            tempX = mp.getX();
            pixValueWidth = Math.round((tempX - upperLeftX)*xFactor);
            mp.setPxX(pixValueWidth);
            tempY = mp.getY();
            pixValueHeight = Math.round((upperLeftY-tempY)*yFactor);
            mp.setPxY(pixValueHeight);
            arrList.add(j, mp);
            System.out.println("convertToPixelCoord(): pixelkoordinat object: "+j+", x = "+pixValueHeight+", y = "+pixValueWidth );
        }       
        return arrList;
    }    
    
    
    /**
     * Returns zoomscale for given zoomfactor
     * @param me
     * @return zoomscale
     */
    public long getZoomScale(MapEnvelope me){
        long zoomscale = 0;
        double deltaX = me.getMaxX()-me.getMinX();
        //double deltaY = me.getMaxY()-me.getMinY();
        zoomscale = Math.round(deltaX/imgMeterSize);
        //finne delta x/y
        return zoomscale;
    }
    
     /**
     * Returns zoomlevel for given zoomscale
     * @param zoomscale
     * @return zoomlevel
     */
    public int getZoomLevel(MapEnvelope me){
        long zoomscale = getZoomScale(me);
        int zoomlevel;
        if (zoomscale <= this.zoom_1)
            zoomlevel = 1;
        else if (zoomscale <= this.zoom_2)
            zoomlevel = 2;
        else if (zoomscale <= this.zoom_3)
            zoomlevel = 3;
        else if (zoomscale <= this.zoom_4)
            zoomlevel = 4;
        else if (zoomscale <= this.zoom_5)
            zoomlevel = 5;     
        else if (zoomscale <= this.zoom_6)
            zoomlevel = 6; 
        else 
            zoomlevel = 7;
        System.out.println("getZoomLevel(): innverdi zoomscale = " + zoomscale + ", returnert verdi = " + zoomlevel);
        return zoomlevel;
    }
    
    /**
     * Returns zoomscale for given zoomfactor
     * @param zoom
     * @return zoomscale
     */
    public long getZoomScale(int zoom){
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
    
    /**
     * Returns ArrayList containing all defined zoomlevels
     * @param zoom
     * @return zoomscale
     */    
    public ArrayList getZoomLevels(){ 
        ArrayList zoomLevels = new ArrayList(zoomCount);
        //long[] zoomLevels = new long[zoomCount];
        zoomLevels.add(Long.toString(zoom_1));
        zoomLevels.add(Long.toString(zoom_2));
        zoomLevels.add(Long.toString(zoom_3));
        zoomLevels.add(Long.toString(zoom_4));
        zoomLevels.add(Long.toString(zoom_5));
        zoomLevels.add(Long.toString(zoom_6));  
        zoomLevels.add(Long.toString(zoom_7)); 
        return zoomLevels;     
    }
      
    public String getDefaultZoom(){
        return Integer.toString(this.defaultZoom);        
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
        return this.mapCenterPxX - this.iconOffsetWidth;
    }
    public double getMapCenterPxY(){
        return this.mapCenterPxY - this.iconOffsetHeigth;
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
    public String getDefaultNoCoordString(){
        String tmp = Double.toString(this.defaultNoCoord);
        return tmp.substring(0,(tmp.length()-2));
    }
    public double getPixelsize(){
        return this.pixelSize;
    }
    public double getIconPxSizeHeigth(){
        return this.iconPxSizeHeigth;
    }
    public double getIconPxSizeWidth(){
        return this.iconPxSizeWidth;
    }
    public double getIconOffsetHeigth(){
        return this.iconOffsetHeigth;
    }
    public double getIconOffsetWidth(){
        return this.iconOffsetWidth;
    }
 }
