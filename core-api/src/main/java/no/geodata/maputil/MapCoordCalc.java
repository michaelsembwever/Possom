// Copyright (2007) Schibsted Søk AS
/*
 * CoordCalc.java
 *
 * Created on 21. november 2002, 14:07
 */

package no.geodata.maputil;

//import org.apache.log4j.Category;

//
/** Class for projecting and unprojection coordinates.
 * @author joachim
 */
public class MapCoordCalc {
    
    //Category logger = Category.getInstance(MapCoordCalc.class);
    //
    
    /** Creates a new instance of MapCoordCalc. */    
    public MapCoordCalc() {
    }

    /**
     * Calculates the UTM zone for a DD point
    */    
    private int getUTMZone(double lonDD) { 
        return (int)Math.floor((lonDD + 180.0) / 6) + 1; 
    }

    /** Converts a point in decimal degrees to the "optimal" UTM zone.
     * @param lon Longitude (X) of the point to convert.
     * @param lat Latitude (Y) of the point to convert.
     * @return MapPoint object with the projected point (UTM).
     */
    public MapPoint DD2UTM(double lon, double lat) {
        double lonRad = DegToRad(lon);
        double latRad = DegToRad(lat);
        int utmZone = getUTMZone(lon);
        double cmeridian = getUTMCentralMeridian(utmZone);
        return convertDD2UTM(lonRad, latRad, cmeridian);
    }
    
    /** Converts a point in decimal degrees to the given UTM zone.
     * @param lon Longitude (X) of the point to convert.
     * @param lat Latitude (Y) of the point to convert.
     * @param utmZone UTM zone used in the projection.
     * @return MapPoint object with the projected point (UTM).
     */
    public MapPoint DD2UTM(double lon, double lat, int utmZone) {
        double lonRad = DegToRad(lon);
        double latRad = DegToRad(lat);
        double cmeridian = getUTMCentralMeridian(utmZone);
        return convertDD2UTM(lonRad, latRad, cmeridian);
    }
    
    /**
     * Converts a DD point (radians) to UTM
    */
    private MapPoint convertDD2UTM(double lonRad, double latRad, double cmeridian) {
        double sm_a = 6378137.0;
        double sm_b = 6356752.314;
        double sm_EccSquared = 6.69437999013e-03;
        double UTMScaleFactor = 0.9996;
        
        // phi = latRad, lambda = lonRad, lambda0 = cmeridian, xy)
        // recalculate ep2 
        double ep2 = (Math.pow (sm_a, 2.0) - Math.pow (sm_b, 2.0)) / Math.pow (sm_b, 2.0);

        // Precalculate nu2 
        double nu2 = ep2 * Math.pow (Math.cos (latRad), 2.0);
    
        // Precalculate N 
        double N = Math.pow (sm_a, 2.0) / (sm_b * Math.sqrt (1 + nu2));
    
        // Precalculate t 
        double t = Math.tan (latRad);
        double t2 = Math.pow(t, 2.0);
        
        // Precalculate l
        double l = lonRad - cmeridian;
        
        // Precalculate coefficients for l**n in the equations below
        // so a normal human being can read the expressions for easting and northing
        // -- l**1 and l**2 have coefficients of 1.0 
        double l3coef = 1.0 - t2 + nu2;
        double l4coef = 5.0 - t2 + 9 * nu2 + 4.0 * (nu2 * nu2);
        double l5coef = 5.0 - 18.0 * t2 + (t2 * t2) + 14.0 * nu2 - 58.0 * t2 * nu2;
        double l6coef = 61.0 - 58.0 * t2 + (t2 * t2) + 270.0 * nu2 - 330.0 * t2 * nu2;
        double l7coef = 61.0 - 479.0 * t2 + 179.0 * (t2 * t2) - (t2 * t2 * t2);
        double l8coef = 1385.0 - 3111.0 * t2 + 543.0 * (t2 * t2) - (t2 * t2 * t2);

        // Calculate easting (x)
        double utmX = N * Math.cos (latRad) * l + 
                     (N / 6.0 * Math.pow (Math.cos (latRad), 3.0) * l3coef * Math.pow (l, 3.0)) + 
                     (N / 120.0 * Math.pow (Math.cos (latRad), 5.0) * l5coef * Math.pow (l, 5.0)) + 
                     (N / 5040.0 * Math.pow (Math.cos (latRad), 7.0) * l7coef * Math.pow (l, 7.0));
    
        // Calculate northing (y)
        double utmY = ArcLengthOfMeridian (latRad) + 
                     (t / 2.0 * N * Math.pow (Math.cos (latRad), 2.0) * Math.pow (l, 2.0)) + 
                     (t / 24.0 * N * Math.pow (Math.cos (latRad), 4.0) * l4coef * Math.pow (l, 4.0)) + 
                     (t / 720.0 * N * Math.pow (Math.cos (latRad), 6.0) * l6coef * Math.pow (l, 6.0)) + 
                     (t / 40320.0 * N * Math.pow (Math.cos (latRad), 8.0) * l8coef * Math.pow (l, 8.0));
        
        // Adjust easting and northing for UTM system
        utmX = utmX * UTMScaleFactor + 500000.0;
        utmY = utmY * UTMScaleFactor;
        if (utmY < 0.0) { utmY = utmY + 10000000.0; }
        
        MapPoint pointUTM = new MapPoint();
        pointUTM.setY(utmY);
        pointUTM.setX(utmX);
        return pointUTM;
    }
    
    private double DegToRad(double deg) { return (deg / 180.0 * Math.PI); }    
    
    private double getUTMCentralMeridian(int utmZone) { return DegToRad(-183.0 + (utmZone * 6.0)); }  
    
    /*
    * ArcLengthOfMeridian
    *   Computes the ellipsoidal distance from the equator to a point at a given latitude.
    *   Inputs:
    *     phi - Latitude of the point, in radians.
    *   Returns:
    *     The ellipsoidal distance of the point from the equator, in meters.
    */
    private double ArcLengthOfMeridian (double latRad) {
        double sm_a = 6378137.0;
        double sm_b = 6356752.314;
        double result;
        
        // Precalculate n
        double n = (sm_a - sm_b) / (sm_a + sm_b);

        // Precalculate alpha
        double alpha = ((sm_a + sm_b) / 2.0) * (1.0 + (Math.pow (n, 2.0) / 4.0) + (Math.pow (n, 4.0) / 64.0));

        // Precalculate beta
        double beta = (-3.0 * n / 2.0) + (9.0 * Math.pow (n, 3.0) / 16.0) + (-3.0 * Math.pow (n, 5.0) / 32.0);

        // Precalculate gamma 
        double gamma = (15.0 * Math.pow (n, 2.0) / 16.0) + (-15.0 * Math.pow (n, 4.0) / 32.0);
    
        // Precalculate delta
        double delta = (-35.0 * Math.pow (n, 3.0) / 48.0) + (105.0 * Math.pow (n, 5.0) / 256.0);
    
        // Precalculate epsilon
        double epsilon = (315.0 * Math.pow (n, 4.0) / 512.0);
    
        // Now calculate the sum of the series and return
        result = alpha * 
                (latRad + (beta * Math.sin (2.0 * latRad)) + 
                (gamma * Math.sin (4.0 * latRad)) +
                (delta * Math.sin (6.0 * latRad)) + 
                (epsilon * Math.sin (8.0 * latRad)));

        return result;
    }
}
