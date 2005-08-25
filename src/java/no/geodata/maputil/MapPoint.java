package no.geodata.maputil;

/*
 * MapPoint.java
 *
 * Created on 15. november 2002, 13:01
 */

/** Class that defines a map point. */
public class MapPoint {
    double x;
    double y;
    
    /** Creates a new instance of MapPoint. */
    public MapPoint() {
    }

    /** Creates a new instance of MapPoint.
     * @param x X-coordinate (longitude) for the map point.
     * @param y Y-coordinate (latitude) for the map point.
     */
    public MapPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /** Sets single value.
     * @param x X-coordinate (longitude) for the map point.
     */    
    public void setX(double x) { this.x = x; }
    /** Sets single value.
     * @param y X-coordinate (longitude) for the map point.
     */    
    public void setY(double y) { this.y = y; }

    /** Gets a single value.
     * @return X-coordinate (longitude) for the map point.
     */    
    public double getX(){ return this.x; }
    /** Gets a single value.
     * @return X-coordinate (longitude) for the map point.
     */    
    public double getY(){ return this.y; }
    
    // Test/debug-funksjoner...
    /** Gets a string representation of the map point.
     * @return x @ y.
     */    
    public String toString() { return this.x + "@" + this.y; }
}
