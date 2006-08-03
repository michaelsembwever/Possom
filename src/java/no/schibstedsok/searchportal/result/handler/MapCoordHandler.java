package no.schibstedsok.searchportal.result.handler;

import java.util.Map;
import org.apache.log4j.Logger;

import no.geodata.maputil.MapCoordCalc;
import no.geodata.maputil.MapPoint;
import no.schibstedsok.searchportal.command.FastSearchCommand;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * Converts lat/long values into X/Y for map coordinate-system.
 * 
 * @author larsj
 *
 */
public class MapCoordHandler implements ResultHandler {

	private static final int UTM_ZONE = 33;

    private static final Logger LOG = Logger.getLogger(MapCoordHandler.class);

	public void handleResult(Context cxt, Map parameters) {
		
		for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
        
        	try {
        		String tmp = null;
        		double latitude = Double.parseDouble(item.getField("lat").replace(',', '.'));
        		double longitude = Double.parseDouble(item.getField("long").replace(',', '.'));
        		
            	MapCoordCalc coordCalculator = new MapCoordCalc();
            	MapPoint point = coordCalculator.DD2UTM(longitude, latitude, UTM_ZONE);
                item.addField("xcoord", point.getX() + "");
                item.addField("ycoord", point.getY() + "");
        	} catch (NumberFormatException e) {
				// silent fail
        		e.printStackTrace();
        		LOG.error("Unable to parse latitude/longitude from Storm weather service " + e);
        	}
        	
        }
	
	}

}
