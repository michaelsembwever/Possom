package no.geodata.maputil;

/*
 * MapEnvelope.java
 *
 * Created on 22. august 2005, 12:55
 *
 * @author hanst
 */
public class MapEnvelope {
    MapPoint upperLeft = new MapPoint();
    MapPoint lowerRight = new MapPoint();    
    double maxX;
    double minX;
    double maxY;
    double minY;
    
    /** Creates a new instance of MapPoint. */
    public MapEnvelope() {
    }
    
    
    /** Creates a new instance of MapEnvelope      
     * @param maxX double
     * @param minX double
     * @param maxY double
     * @param minY double
     */
    public MapEnvelope(double maxX, double minX, double maxY, double minY){
        this.maxX = maxX;
        this.minX = minX;
        this.maxY = maxY;
        this.minY = minY;
    }
     /** Sets single value.
     * @param maxX double.
     */    
    public void setMaxX(double maxX){
        this.maxX = maxX;
    }
    /** Gets a single value.
     *  @return maxX double 
     */
    public double getMaxX(){
        return this.maxX;
    }
     /** Sets single value.
     * @param minX double.
     */    
    public void setMinX(double minX){
        this.minX = minX;
    }
     /** Gets a single value.
     *  @return minX double 
     */
    public double getMinX(){
        return this.minX;
    }
     /** Sets single value.
     * @param maxY double.
     */    
    public void setMaxY(double maxY){
        this.maxY = maxY;
    }
     /** Gets a single value.
     *  @return maxY double 
     */
    public double getMaxY(){
        return this.maxY;
    }
     /** Sets single value.
     * @param minY double.
     */    
    public void setMinY(double minY){
        this.minY = minY;
    }
     /** Gets a single value.
     *  @return minY double 
     */
    public double getMinY(){
        return this.minY;
    }
    
    /** Creates a new instance of MapEnvelope      
     * @param upperLeft UpperLeft corner of Envelope
     * @param lowerRight LowerRight corner of Envelope.
     */
    public MapEnvelope(MapPoint ul, MapPoint lr) {
        this.upperLeft = ul;
        this.lowerRight = lr;
    }    
    /** Sets single value.
     * @param ul UpperLeft corner of Envelope.
     */    
    public void setUpperLeft(MapPoint ul) { 
        this.upperLeft = ul;         
    }
    /** Sets single value.
     * @param lr LowerRight corner of Envelope.
     */    
    public void setLowerRight(MapPoint lr) { 
        this.lowerRight = lr; 
    }    
    /** Gets a single value.
     *  @return MapPoint upperLeft 
     */
    public MapPoint getUpperLeft(){
        return this.upperLeft;
    }    
    /** Gets a single value.
     *  @return MapPoint lowerRight 
     */
    public MapPoint getLowerRight(){
        return this.lowerRight;
    }
    
}
