/*
 * $Id:$
 */
package no.schibstedsok.searchportal.util;

import javax.measure.units.SI;


import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;

import org.apache.log4j.Logger;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;
/**
 * Utility class for GEO search.
 * 
 * @author <a href="mailto:anders@sesam.no">Anders Johan Jamtli</a>
 * @author Stian Hegglund.
 * @version $Revision:$
 */
public class GeoSearchUtil {
    
    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(GeoSearchUtil.class);
    
    /** Constants for selected map rectangel. */
    private static final String MIN_X = "minX";
    private static final String MAX_X = "maxX";
    private static final String MIN_Y = "minY";
    private static final String MAX_Y = "maxY";
    
    /** Measure unit to use. */
    public static final String RADIUS_MEASURE_UNIT_TYPE = "km";
    
    /** The sort by to use when the search is a geo search. */
    public static final String GEO_SORT_BY = "geo_spec_sortable";
    
    /** Utility class, should be used by calling static methods. */
    private GeoSearchUtil(){
        
    }
    
     
    /**
     * Calcluates a center point from minX,maxX,minY,maxY parameters.
     * @param requestParameters Parameters for the request.
     * @return The center point in latlong format.
     */
    public static String getCenter(final ParametersDataObject requestParameters){
        
        if(!isGeoSearch(requestParameters)){
            throw new IllegalArgumentException("Given requestParameter object must contain parameters: minX,maxX,minY,maxY");
        }
        
        final int minX = Integer.parseInt((String) requestParameters.getValue(MIN_X).getString());
        final int maxX = Integer.parseInt((String) requestParameters.getValue(MAX_X).getString());
        final int minY = Integer.parseInt((String) requestParameters.getValue(MIN_Y).getString());
        final int maxY = Integer.parseInt((String) requestParameters.getValue(MAX_Y).getString());
        
        final UTM utmMin = UTM.valueOf(33, 'W', minX, minY, SI.METER);
        final UTM utmMax = UTM.valueOf(33, 'W', maxX, maxY, SI.METER);
        
        final LatLong llMin = UTM.utmToLatLong(utmMin, ReferenceEllipsoid.WGS84);
        final LatLong llMax = UTM.utmToLatLong(utmMax, ReferenceEllipsoid.WGS84);

        final double llMinX = llMin.getOrdinate(1);
        final double llMaxX = llMax.getOrdinate(1);
        final double llMinY = llMin.getOrdinate(0);
        final double llMaxY = llMax.getOrdinate(0);

        LOG.debug("(" + minX + "," + minY + ") (" + llMinX + "," + llMinY + ")");
        LOG.debug("(" + maxX + "," + maxY + ") (" + llMaxX + "," + llMaxY + ")");
        
        final String center = new StringBuilder("(").append(llMinX + (llMaxX - llMinX) / 2).append(",")
                .append(llMinY + (llMaxY - llMinY) / 2).append(")").toString();
        return center;

    }
    
  
    
    /**
     * Responsible for checking if a ParameterDataObject is a geosearch.
     * @param requestParameters
     * @return true if the given parameter object contains minX,maxX,minY,maxY values.
     */
    public static boolean isGeoSearch(final ParametersDataObject requestParameters) {
        
        if (requestParameters.getValue(MIN_X) == null || requestParameters.getValue(MAX_X) == null || requestParameters.getValue(MIN_Y) == null
                || requestParameters.getValue(MAX_Y) == null) {
            return false;
        }
        
        if (requestParameters.getValue(MIN_X).getString().length() == 0 || requestParameters.getValue(MAX_X).getString().length() == 0 || requestParameters.getValue(MIN_Y).getString().length() == 0 
                || requestParameters.getValue(MAX_Y).getString().length() == 0) {
            return false;
        }
        return true;
    }
}