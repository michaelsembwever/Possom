/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.geodata.maputil;

/*
 * MapPoint.java
 *
 * Created on 15. november 2002, 13:01
 */

/** Class that defines a map point. */
public class MapPoint {
    double x;//UTM koordinat
    double y;//UTM koordinat
    long pxX;//bildepix koord
    long pxY;//bildepix koord
    int id;
    //String name = new String();
    //String url = new String();

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
    /** Gets single value.
     * @param y X-coordinate (longitude) for the map point.
     */
    public double getX(){ return this.x; }

    /** Sets a single value.
     * @return X-coordinate (longitude) for the map point.
     */
    public void setY(double y) { this.y = y; }
    /** Gets a single value.
     * @return X-coordinate (longitude) for the map point.
     */
    public double getY(){ return this.y; }

    /** Sets single value.
     * @param pxX imagepixel-coordinate for the map point.
     */
    public void setPxX(long pxX) { this.pxX = pxX; }
    /** Gets single value.
     * @param pxX imagepixel-coordinate for the map point.
     */
    public long getPxX(){ return this.pxX; }

    /** Sets single value.
     * @param pxY imagepixel-coordinate for the map point.
     */
    public void setPxY(long pxY) { this.pxY = pxY; }
    /** Gets single value.
     * @param pxY imagepixel-coordinate for the map point.
     */
    public long getPxY(){ return this.pxY; }

    /** Sets a single value.
     * @return X-coordinate (longitude) for the map point.
     */

    /** Sets single value.
     * @param id identity number.
     */
    public void setId(int id){
        this.id = id;
    }
    /** Gets single value.
     * @return id identity number.
     */
    public int getId(){
        return this.id;
    }
    // Test/debug-funksjoner...
    /** Gets a string representation of the map point.
     * @return x @ y.
     */
    public String toString() { return this.x + "@" + this.y; }
}
