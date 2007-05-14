// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import org.apache.log4j.Logger;
import no.geodata.maputil.MapCoordCalc;
import no.geodata.maputil.MapPoint;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;

/**
 * Converts lat/long values into X/Y for map coordinate-system.
 *
 * @author larsj
 * @version $Id$
 */
public final class MapCoordHandler implements ResultHandler {

	private static final int UTM_ZONE = 33;

    private static final Logger LOG = Logger.getLogger(MapCoordHandler.class);
    
    private final MapCoordResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public MapCoordHandler(final ResultHandlerConfig config){
        this.config = (MapCoordResultHandlerConfig)config;
    }

    /** {@inherit} **/
	public void handleResult(final Context cxt, final DataModel datamodel) {

		for (final ResultItem item : cxt.getSearchResult().getResults()) {

        	try {
        		final double latitude = Double.parseDouble(item.getField("lat").replace(',', '.'));
        		final double longitude = Double.parseDouble(item.getField("long").replace(',', '.'));

            	final MapCoordCalc coordCalculator = new MapCoordCalc();
            	final MapPoint point = coordCalculator.DD2UTM(longitude, latitude, UTM_ZONE);
                
                cxt.getSearchResult().replaceResult(
                        item, 
                        item.addField("xcoord", point.getX() + "").addField("ycoord", point.getY() + "")
                    );
                
        	} catch (NumberFormatException e) {
				// silent fail
        		LOG.error("Unable to parse latitude/longitude from Storm weather service " + e);
        	}

        }

	}

}
