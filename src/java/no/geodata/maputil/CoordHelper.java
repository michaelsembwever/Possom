// Copyright (2005-2006) Schibsted Søk AS
/*
 * CoordHelper.java
 *
 * Created on 21. august 2005, 22:50
 *
 */

package no.geodata.maputil;

import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

import java.util.*;

/**
 *
 * @author hanst
 */
public class CoordHelper {

    public final static long zoom_1 = 2000;
    public final static long zoom_2 = 6000;
    public final static long zoom_3 = 20000;
    public final static long zoom_4 = 60000;
    public final static long zoom_5 = 300000;
    public final static long zoom_6 = 800000;
    public final static long zoom_7 = 3000000;

    public final static int zoomCount = 7;

    public final static int defaultZoom = 2;
    public static int imgWidth = 363; //bildestørrelse i pixler, bredde
    public static int imgHeight = 363; //bildestørrelse i pixler, høyde
    public final static int envFactor = 30; //faktor for å lage rom rundt envelope. Angis i pixler

    public final static int iconPxSizeHeigth = 32;
    public final static int iconPxSizeWidth = 29;

    public final static int iconOffsetHeigth = 34; //plassering av ikon, avvik fra top. Positiv verdi
    public final static int iconOffsetWidth = 14; //plassering av ikon, avvik fra venstre. Positiv verdi
    public final static int iconOverlapOffsetWidth = 12; //hvor mange pixler skal ikonet flyttes for å unngå overlap. Verdi 0 tillater at ikonene kan ha samme plassering
    public final static int iconOverlapOffsetHeigth = 12; //hvor mange pixler skal ikonet flyttes for å unngå overlap. Verdi 0 tillater at ikonene kan ha samme plassering
    public static int mapCenterPxX = imgWidth / 2; //kartets midtpunkt i pixler, bredde
    public static int mapCenterPxY = imgHeight / 2; //kartets midtpunkt i pixler - iconets st�rrelse/2, høyde
    public final static double panFactor = 0.45; //faktor som forteller hvor mye kartsentrum skal flyttet iforhold til kartets deltaX og y ved panning.
    public final static double zoomFactor = 2; //faktor som forteller hvor mye kartextentet skal minskes/utvides ved zoom inn/ut. zoom inn = 1/zoomFactor. Brukes ikke dersom fastezoomlevels benyttes.
    public final static double defaultNoCoord = -9999999;

    public double maxX = 1100000; //initielle verdier. Envelope som dekker hele Norge.

    public double minX = -83000;
    public double maxY = 7950000;
    public double minY = 6440000;
    public double mapCenterCoordX;
    public double mapCenterCoordY;

    public static final double METERS_PR_INCH = 0.0254;
    public static final int DPI = 96;

    public static final double pixelSize = METERS_PR_INCH / DPI;
    public static double imgMeterSize = pixelSize * imgWidth; //st�rrelse på kart på skjermen i meter.

    /** Creates a new instance of CoordHelper */
    public CoordHelper() {
    }

    /**
     * @param sCoord Coordinate string, x1,y1;x2,y2;x3,y3;x4,y4...osv.
     * @return Vector containing MapPoint objects.
     */
    public Vector parseCoordString(String sCoord) {
        Vector vMapPoint = new Vector();
        int i = 0;
        int j = 0;
        int k = 0;
        int length = sCoord.length();
        if (length > 0) {
            sCoord = sCoord + ";";
        }
        String sMP = new String();
        double x, y;
        int counter = 0;
        boolean reachEnd = false;
        while (!reachEnd) {
             counter++;
             j = sCoord.indexOf(";", i);
             if (j == -1)//siste punkt
                 reachEnd = true;
             else {
                 sMP = sCoord.substring(i, j);
                 k = sMP.indexOf(",");
                 x = Double.parseDouble(sMP.substring(0, k));
                 y = Double.parseDouble(sMP.substring(k + 1));
                 MapPoint mp = new MapPoint();
                 mp.setX(x);
                 mp.setY(y);
                 mp.setId(counter);
                 vMapPoint.add(mp);
                 i = j + 1;
             }
        }
        return vMapPoint;
    }

    /**
     * Generates MapEnvelope from given point, scale, imgwidth and imgheight
     *
     * @param mp MapPoint, centerpoint in map
     * @return MapEnvelope,
     */
    public MapEnvelope makeEnvelope(final double x, final double y, final int zoom) {
        long zoomscale = getZoomScale(zoom);

        double metersPrPixel = zoomscale * pixelSize;
        this.maxX = Math.round(x + metersPrPixel * imgHeight);
        this.minX = Math.round(x - metersPrPixel * imgHeight);
        this.maxY = Math.round(y + metersPrPixel * imgWidth);
        this.minY = Math.round(y - metersPrPixel * imgWidth);
        MapEnvelope mapEnvelope = new MapEnvelope(this.maxX, this.minX, this.maxY, this.minY);

        return mapEnvelope;
    }

    /**
     * Returns MapEnvelope from given company object containing x and y coordinate.
     *
     * @return MapEnvelope
     */
    public MapEnvelope getEnvelope(final BasicSearchResultItem item) {
        MapPoint mp = new MapPoint();
        try  {
            mp.x = Double.parseDouble(item.getField("xcoord"));
            mp.y = Double.parseDouble(item.getField("ycoord"));
            int zoomnivaa = defaultZoom;
            return makeEnvelope(mp.x, mp.y, zoomnivaa);
        }
        catch (Exception e) {
            mp.x = defaultNoCoord;
            mp.x = defaultNoCoord;
            return new MapEnvelope();
        }
        /*double x = Double.parseDouble(item.getField("xcoord"));
        double y = Double.parseDouble(item.getField("ycoord"));
        MapPoint mp = new MapPoint();
        mp.setX(x);
        mp.setY(y);
        int zoomnivaa = defaultZoom;
        return makeEnvelope(mp, zoomnivaa);*/
    }

    /**
     * Returns MapEnvelope from given list of company objects and converts coords to pixCoords
     * @param companies
     * @return MapEnvelope
     */
    public MapEnvelope getEnvelope(final List companies) {
        Vector mps = extractMapPoints(companies);
        return makeEnvelope(mps);
    }

    /**
     * lopper igjennom listetreffet for å sjekke om noen av treffene inneholder koordinater
     */
    public boolean checkCoords(final List companies) {
        SearchResultItem company;
        for (int i = 0; i < companies.size(); i++) {
            MapPoint mp = new MapPoint();
            company = (SearchResultItem) companies.get(i);
            try  {
                mp.x = Double.parseDouble(company.getField("xcoord"));
                mp.y = Double.parseDouble(company.getField("ycoord"));
                return true;
            }
            catch (Exception e) {
            }
        }
        return false;
    }

    /**
     * Returns Arraylist of MapPoints including pix coordinates.
     * @param companies
     * @param companies
     */
    public ArrayList getMapPoints(final List companies, final MapEnvelope me) {
        Vector mps = extractMapPoints(companies);
        //MapEnvelope me = makeEnvelope(mps);
        return convertToPixelCoord(me, mps, true);
    }

    /**
     * Extracts coordinates from a list of Company objects. Returns av vector of MapPoints.
     *
     */
    private Vector extractMapPoints(final List companies) {
        Vector mps = new Vector();
        SearchResultItem company;
        boolean hasCoords;
        for (int i = 0; i < companies.size(); i++) {
            MapPoint mp = new MapPoint();
            hasCoords = false;
            company = (SearchResultItem) companies.get(i);
            try  {
                mp.x = Double.parseDouble(company.getField("xcoord"));
                mp.y = Double.parseDouble(company.getField("ycoord"));
                hasCoords = true;
            }
            catch (Exception e) {
            }
            if (!hasCoords) {
                mp.x = defaultNoCoord;
                mp.y = defaultNoCoord;
            }
            mps.add(mp);
        }
        return mps;
    }

     /**
     * Returns MapEnvelope from given vector of company objects containing x and y coordinates.
     *
     * @return MapEnvelope
     */
    public MapEnvelope getEnvelope() {
        MapEnvelope me = new MapEnvelope();
        return me;
    }


    /**
     * Generates MapEnvelope from a vector of points, imgwidth and imgheight.
     * @param vMapPoints vector containg MapPoints
     * @return MapEnvelope,
     */
    public MapEnvelope makeEnvelope(Vector vMapPoints) {
        //loope igjennom vector for � finne max/ min verdier
        MapPoint mp = new MapPoint();
        MapPoint mp2 = new MapPoint();
        MapEnvelope me = new MapEnvelope();
        if (vMapPoints.size() > 0) {
            boolean hasCoords = false;
            boolean equalCoords = false;
            me.maxX = 0;
            me.maxY = 0;
            int counter = 0;
            for (int i = 0; i < vMapPoints.size(); i++ ) {
                mp = (MapPoint) vMapPoints.get(i);
                if (!hasCoords) { //dersom mappoint er en dummy, m� initielle verdier settes p� nytt n�r f�rste riktige koordinater kommer
                    me.maxX = mp.x;
                    if (me.maxX != defaultNoCoord) {
                        hasCoords = true;
                        me.minX = mp.x;
                        me.maxY = mp.y;
                        me.minY = mp.y;
                    }
                }
                if (mp.x != defaultNoCoord) { //m� se bort fra punkt som er tilordnet en dummy kooordinat verdi
                    counter++; //teller hvor mange i listen som har riktige koordinater
                    if (mp.x > me.maxX)
                        me.maxX = mp.x;
                    else if (mp.x < me.minX)
                        me.minX = mp.x;
                    if (mp.y > me.maxY)
                        me.maxY = mp.y;
                    else if (mp.y < me.minY)
                        me.minY = mp.y;
                    //sjekker om det finnes like koordinater i lista
                    if (!equalCoords) {
                        for (int j = i + 1; j < vMapPoints.size(); j++ ) {
                            if (!equalCoords) {
                                if (i != j) { //sjekker ikke mot seg selv
                                    mp2 = (MapPoint) vMapPoints.get(j);
                                    if (mp.x == mp2.x && mp.y == mp2.y)
                                        equalCoords = true;
                                }
                            }
                        }
                    }
                }
            }
            long zoomscale;
            double centerX, centerY, deltaX, deltaY;
            if (!hasCoords) { //dersom listetreffet inneholder treff men ingen av disse har koordinater.
                return me;
            }
            else if (counter == 1) { //kun ett punkt i lista inneholder riktige koordinater
                me = makeEnvelope(me.maxX, me.maxY, defaultZoom);
                return me;
            }
            else if ((me.maxX - me.minX) == 0) { //har flere koordinater men alle har samme verdi
                zoomscale = getZoomScale(defaultZoom);
                //beregner fiktive max/min koordinater.
                centerX = me.maxX;
                centerY = me.maxY;
                //m� finne deltaX
                deltaX = Math.round(pixelSize * imgWidth * zoomscale);
                deltaY = Math.round(pixelSize * imgHeight * zoomscale);
                me.maxX = centerX + deltaX / 2;
                me.minX = centerX - deltaX / 2;
                me.maxY = centerY + deltaY / 2;
                me.minY = centerY - deltaY / 2;
                me.centerX = centerX;
                me.centerY = centerY;
                return me;
            }
            else {
                //utvider envelop'en litt slik at ingen punkt blir liggende i kartkanten
                deltaX = (me.maxX - me.minX);
                deltaY = (me.maxY - me.minY);
                if (equalCoords) { //sjekker om noen har like koordinater, is�fall forflyttes de en faktor ift hverandre. Nye max/min koordinater m� beregnes.
                    zoomscale = Math.round(deltaX / imgMeterSize);
                    vMapPoints = checkEqualCoords(vMapPoints, zoomscale);
                    me = getMaxMinCoords(vMapPoints);
                    deltaX = me.maxX - me.minX;
                    deltaY = me.maxY - me.minY;
                }
                me.centerX = me.minX + deltaX / 2;
                me.centerY = me.minY + deltaY / 2;
                if (deltaX == 0 && deltaY == 0) { //dersom deltax og deltay er null, betyr det at alle punkt har samme koordinater.
                    me = makeEnvelope(me.centerX, me.centerY, defaultZoom);
                    return me;
                }
                else  {
                    if (deltaX == 0)//kan ikke ha nullverdi for deltaverdiene. Alle treff med koordinater m� i dette tilfelle ha forskjellig X men lik Y, eller omvendt. Skal litt til f�r dette sl�r til, men kj�rer allikevel en sjekk.
                        deltaX = 1;
                    else if (deltaY == 0)
                        deltaY = 1;
                    //beregner delta x og y slik at de har et forhold som er lik imgHeight/imgWidth
                    double factorHW = (double) imgWidth / (double) imgHeight;
                    double factor_dXdY = deltaX / deltaY;
                    if (factor_dXdY < factorHW ) { //h�yden er iforhold til bildeh�yde/bredde mindre enn bredde. M� utvide bredde for � f� riktige proposisjoner
                        deltaX = deltaX * (factorHW / factor_dXdY);
                    }
                    else if (factor_dXdY > factorHW) { //h�yde er st�rre enn bredde. M� utvide bredde for � f� riktige proposisjoner
                        deltaY = deltaY * (factor_dXdY / factorHW);
                    }
                    //utvider kartenvelop med hensyn til st�rrelsen p� ikon og offset, slik at ikonene ikke kommer i kartkanten.
                    double extFact = deltaX / imgWidth;
                    double extendTop = Math.round((iconOffsetHeigth + envFactor) * extFact);
                    double extendBottom = Math.round((Math.abs(iconPxSizeHeigth - iconOffsetHeigth) + envFactor) * extFact);
                    double extendLeft = Math.round((iconOffsetWidth + envFactor) * extFact);
                    double extendRigth = Math.round((Math.abs(iconPxSizeWidth - iconOffsetWidth) + envFactor) * extFact);
                    me.maxX = Math.round(me.centerX + (deltaX / 2 + extendRigth));
                    me.minX = Math.round(me.centerX - (deltaX / 2 + extendLeft));
                    me.maxY = Math.round(me.centerY + (deltaY / 2 + extendTop));
                    me.minY = Math.round(me.centerY - (deltaY / 2 + extendBottom));
                    //me = new MapEnvelope(me.maxX, me.minX, me.maxY, me.minY);
                    return me;
                }
            }
        }
        return me;
    }

    /**
     * finner max/min koordinater fra gitt vektor med koordinatpunkter
     *
     */
    private MapEnvelope getMaxMinCoords(final Vector vMapPoints) {
        MapEnvelope me = new MapEnvelope();
        MapPoint mp = new MapPoint();
        boolean initiert = false;
        for (int i = 0; i < vMapPoints.size(); i++ ) {
            mp = (MapPoint) vMapPoints.get(i);
            if (!initiert) { //dersom mappoint er en dummy, m� initielle verdier settes p� nytt n�r f�rste riktige koordinater kommer
                me.maxX = mp.x;
                if (me.maxX != defaultNoCoord) {
                    initiert = true;
                    me.minX = mp.x;
                    me.maxY = mp.y;
                    me.minY = mp.y;
                }
            }
            if (mp.x != defaultNoCoord) { //m� se bort fra punkt som er tilordnet en dummy kooordinat verdi
                if (mp.x > me.maxX)
                    me.maxX = mp.x;
                else if (mp.x < me.minX)
                    me.minX = mp.x;
                if (mp.y > me.maxY)
                    me.maxY = mp.y;
                else if (mp.y < me.minY)
                    me.minY = mp.y;
            }
        }
        return me;
    }

    /**
     * Sjekker om det finnes identiske koordinatpar. Dersom dette er tilfelle, flyttes punktene en gitt offset(iconOverlapOffsetWidth, iconOverlapOffsetHeigth) fra hverandre
     *
     **/
    private Vector checkEqualCoords(final Vector vMapPoints, final long zoomscale) {
        MapPoint mp1 = new MapPoint();
        MapPoint mp2 = new MapPoint();
        double adjustX = iconOverlapOffsetWidth;
        double adjustY = iconOverlapOffsetHeigth;
        if (zoomscale > 20000) { //iconOverlapOffset justeres avhengig av m�lestokk, dersom denne er mindre enn 20000.
            adjustX = iconOverlapOffsetWidth * 20000 / zoomscale;
            if (adjustX < 5)//Skal uansett forflyttes et gitt antall pixler
               adjustX = 5;
            adjustY = iconOverlapOffsetHeigth * 20000 / zoomscale;
            if (adjustY < 5)//Skal uansett forflyttes et gitt antall pixler
               adjustY = 5;
        }
        double xOffset = (zoomscale * pixelSize) * adjustX;
        double yOffset = (zoomscale * pixelSize) * adjustY;
        int counter = 1; //holder rede p� antall like
        //boolean right = true;//flytter f�rste like til h�yre.
        for (int i = 0; i < vMapPoints.size(); i++) {
            mp1 = (MapPoint) vMapPoints.get(i);
            if (mp1.x != defaultNoCoord) {
                for (int j = i + 1; j < vMapPoints.size(); j++) {
                    if (i != j) { //sjekker ikke mot seg selv
                        mp2 = (MapPoint) vMapPoints.get(j);
                        if (mp1.x == mp2.x && mp1.y == mp2.y) { //vi har like koordinater
                            //if(right){
                                mp2.x = mp1.x + (xOffset * counter);
                                mp2.y = mp1.y + (yOffset * counter);
                                counter++;
                                /*right = false;
                            }
                            else{
                                mp1.x = mp1.x - (xOffset * counter);
                                mp1.y = mp1.y + (yOffset * counter);
                                right = true;
                                counter++;
                            }
                                 */
                        }
                    }
                }
            }
        }
        return vMapPoints;
    }


    /**
     * Converts real world coordinates to imagepixel coordinates. Eliminates points outside given MapEnvelope.
     * @param vMapPoints Vector of real world map point coordinates
     * @param me
     * @param init
     */
    public ArrayList convertToPixelCoord(final MapEnvelope me, Vector vMapPoints, final boolean init) {
        /*double maxX = me.getMaxX();
        double minX = me.getMinX();
        double maxY = me.getMaxY();
        double minY = me.getMinY();*/
        long zoomscale = Math.round((me.maxX - me.minX) / imgMeterSize);
        vMapPoints = checkEqualCoords(vMapPoints, zoomscale);

        ArrayList arrList = new ArrayList(vMapPoints.size());

         //finne UTM koordinater for �vre h�yre samt nedre venstre.
        double upperLeftX = me.getMinX();
        double upperLeftY = me.getMaxY();
        //m� f�rst sjekke hvilke punkt som ligger innenfor eksisterende MapEnvelope (kan ha kommet utenfor kartextentet etter navigering)
        //double tempX, tempY;
        MapPoint mp = new MapPoint();
        if (!init) { //dersom det er navigering i kartet m� det sjekkes om punktene faller utenfor nytt extent. Dette skjer ikke slik l�sningen er idag.
            Vector vMapPointsNew = new Vector();
            boolean inside;
            for (int i = 0; i < vMapPoints.size(); i++) {
                inside = true; //
                mp = (MapPoint) vMapPoints.get(i);
                //tempX = mp.x;
                //tempY = mp.y;
                if (mp.x > me.maxX)
                    inside = false;
                else if (mp.x < me.minX)
                    inside = false;
                if (mp.y > me.maxY)
                    inside = false;
                else if (mp.y < me.minY)
                    inside = false;
                if (inside)//dersom punktet ligger innenfor envelop'en tas det var p�.
                    vMapPointsNew.add(mp);
            }
            vMapPoints = vMapPointsNew;
        }
        //beregne bildepixel koordinater. M� justere ikoner som har samme koordinatverdi.
        double deltaX = me.maxX - me.minX;
        double deltaY = me.maxY - me.minY;
        double yFactor = imgHeight / deltaY; //meter pr pixel
        double xFactor = imgWidth / deltaX; //meter pr pixel
        //double deltaX0Xn, deltaY0yn;
        long pixValueHeight, pixValueWidth;
        for (int j = 0; j < vMapPoints.size(); j++) {
            mp = (MapPoint) vMapPoints.get(j);
            //tempX = mp.getX();
            if (mp.x == defaultNoCoord) {
                mp.setPxX(0);
                mp.setPxY(0);
            }
            else  {
                pixValueWidth = Math.round((mp.x - upperLeftX) * xFactor) - iconOffsetWidth;
                mp.setPxX(pixValueWidth);
                //tempY = mp.getY();
                pixValueHeight = Math.round((upperLeftY - mp.y) * yFactor) - iconOffsetHeigth;
                mp.setPxY(pixValueHeight);
            }
            arrList.add(j, mp);
        }
        return arrList;
    }


    /**
     * Returns zoomscale for given mapenvelope
     * @param me
     * @return zoomscale
     */
    public long getZoomScale(final MapEnvelope me) {
        long zoomscale = 0;
        double deltaX = me.getMaxX() - me.getMinX();
        //double deltaY = me.getMaxY()-me.getMinY();
        zoomscale = Math.round(deltaX / imgMeterSize);
        //finne delta x/y
        return zoomscale;
    }

     /**
     * Returns zoomlevel for given zoomscale
     * @return zoomlevel
     */
    public int getZoomLevel(final MapEnvelope me) {
        long zoomscale = getZoomScale(me);
        int zoomlevel;
        if (zoomscale == 0)
            zoomlevel = 7;
        else if (zoomscale <= zoom_1)
            zoomlevel = 1;
        else if (zoomscale <= zoom_2)
            zoomlevel = 2;
        else if (zoomscale <= zoom_3)
            zoomlevel = 3;
        else if (zoomscale <= zoom_4)
            zoomlevel = 4;
        else if (zoomscale <= zoom_5)
            zoomlevel = 5;
        else if (zoomscale <= zoom_6)
            zoomlevel = 6;
        else
            zoomlevel = 7;
        return zoomlevel;
    }

    /**
     * Returns zoomscale for given zoomfactor
     * @param zoom
     * @return zoomscale
     */
    public long getZoomScale(final int zoom) {
        long zoomscale = 0;
        if (zoom == 1)
            zoomscale = zoom_1;
        else if (zoom == 2)
            zoomscale = zoom_2;
        else if (zoom == 3)
            zoomscale = zoom_3;
        else if (zoom == 4)
            zoomscale = zoom_4;
        else if (zoom == 5)
            zoomscale = zoom_5;
        else if (zoom == 6)
            zoomscale = zoom_6;
        return zoomscale;
    }

    /**
     * Returns ArrayList containing all defined zoomlevels
     * @return zoomscale
     */
    public ArrayList getZoomLevels() {
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

    public String getDefaultZoom() {
        return Integer.toString(defaultZoom);

    }
    public int getImgWidth() {

        return imgWidth;
    }
    
    public int getImgHeight() {

        return imgHeight;
    }

    public void setImgWidth(int imgWidth){
        this.imgWidth = imgWidth;
        this.mapCenterPxX = imgWidth / 2;
        this.imgMeterSize = pixelSize * imgWidth;
    }



    public void setImgHeight(int imgHeigth){
        this.imgHeight = imgHeigth;
        this.mapCenterPxY = imgHeigth / 2;
    }

    public double getEnvFactor(){
        return envFactor;
    }

    public int getZoomCount() {
        return zoomCount;
    }
    public double getPanFactor() {
        return panFactor;
    }
    public double getZoomFactor() {
        return zoomFactor;
    }
    public double getImgMeterSize() {
        return imgMeterSize;
    }


    public double getMapCenterPxX() {
        return mapCenterPxX - iconOffsetWidth;
    }
    public double getMapCenterPxY() {
        return mapCenterPxY - iconOffsetHeigth;
    }
    public double getMapCenterCoordX() {
        return this.mapCenterCoordX;
    }
    public double getMapCenterCoordY() {
        return this.mapCenterCoordY;
    }
    public double getDefaultNoCoord() {
        return defaultNoCoord;
    }
    public String getDefaultNoCoordString() {
        String tmp = Double.toString(defaultNoCoord);
        return tmp.substring(0, (tmp.length() - 2));
    }
    public double getPixelsize() {
        return pixelSize;
    }
    public double getIconPxSizeHeigth() {
        return iconPxSizeHeigth;
    }
    public double getIconPxSizeWidth() {
        return iconPxSizeWidth;
    }
    public double getIconOffsetHeigth() {
        return iconOffsetHeigth;
    }
    public double getIconOffsetWidth() {
        return iconOffsetWidth;
    }
    public double getIconOverlapOffsetWidth() {
        return iconOverlapOffsetWidth;
    }
    public double getIconOverlapOffsetHeigth() {
        return iconOverlapOffsetHeigth;
    }
 }
