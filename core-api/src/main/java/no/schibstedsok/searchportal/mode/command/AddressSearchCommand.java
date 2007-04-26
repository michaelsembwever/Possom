/*
 * Copyright (2005-2007) Schibsted Søk AS
 */


package no.schibstedsok.searchportal.mode.command;

import javax.measure.units.SI;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

/**
 * @author <a href="mailto:anders@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Revision$</tt>
 */
public class AddressSearchCommand extends AbstractSimpleFastSearchCommand{

    private static final Logger LOG = Logger.getLogger(AddressSearchCommand.class);
    
    private static final String MIN_X = "minX";
    private static final String MAX_X = "maxX";
    private static final String MIN_Y = "minY";
    private static final String MAX_Y = "maxY";

    public AddressSearchCommand(final Context cxt) {
        super(cxt);
    }

    public final SearchResult execute() {
        SearchResult sr = super.execute();

        if (getSearchConfiguration().isCollapsing()) {
            String prevCollapseId = "";
            for (SearchResultItem item : sr.getResults()) {
                if (item.getField("collapseid").equals(prevCollapseId)) {
                    sr.getResults().remove(item);
                }
            }
        }
        
        return sr;
    }
    
    private final class Point {
        private final double x;
        private final double y;
        
        public Point(final double x, final double y){
            this.x = x;
            this.y = y;
        }
        
        public final double getX() {
            return x;
        }
        
        public final double getY() {
            return y;
        }
    }
    
    @Override
    protected void setAdditionalParameters(ISearchParameters params) {
        super.setAdditionalParameters(params);
        
        /* RETURNING, NOT FINISHED YET */
        if (true) return;
        
        final ParametersDataObject pdo = datamodel.getParameters();
        if (pdo.getValue(MIN_X) == null || pdo.getValue(MAX_X).getString() == null || pdo.getValue(MIN_Y) == null
                || pdo.getValue(MAX_Y) == null) {
            return;
        }

        final int minX = Integer.parseInt((String) pdo.getValue(MIN_X).getString());
        final int maxX = Integer.parseInt((String) pdo.getValue(MAX_X).getString());
        final int minY = Integer.parseInt((String) pdo.getValue(MIN_Y).getString());
        final int maxY = Integer.parseInt((String) pdo.getValue(MAX_Y).getString());
        
        final UTM utmMin = UTM.valueOf(33, 'W', minX, minY, SI.METER);
        final UTM utmMax = UTM.valueOf(33, 'W', maxX, maxY, SI.METER);
        
        final LatLong llMin = UTM.utmToLatLong(utmMin, ReferenceEllipsoid.WGS84);
        final LatLong llMax = UTM.utmToLatLong(utmMax, ReferenceEllipsoid.WGS84);

        final double llMinX = llMin.longitudeValue(SI.RADIAN);
        final double llMaxX = llMax.longitudeValue(SI.RADIAN);
        final double llMinY = llMin.latitudeValue(SI.RADIAN);
        final double llMaxY = llMax.latitudeValue(SI.RADIAN);

        LOG.debug("(" + minX + "," + minY + ") (" + llMinX + "," + llMinY + ")");
        LOG.debug("(" + maxX + "," + maxY + ") (" + llMaxX + "," + llMaxY + ")");
        
        final String center = new StringBuilder("(").append(llMinX + (llMaxX - llMinX) / 2).append(",")
                .append(llMinY + (llMaxY - llMinY) / 2).append(")").toString();
        final String filterbox = new StringBuilder("[(").append(llMinX).append(",").append(llMinY).append("):(")
                .append(llMaxX).append(",").append(llMaxY).append(")]").toString();
        
        params.setParameter(new SearchParameter("qtf_geosearch:center", center));
        params.setParameter(new SearchParameter("qtf_geosearch:filterbox", filterbox));
    }
    
    private final Point fromUtmToLatLong(final int x, final int y) {
        final UTM utm = UTM.valueOf(33, 'W', x, y, SI.METER);
        
        final LatLong ll = UTM.utmToLatLong(utm, ReferenceEllipsoid.WGS84);

        final double llX = Math.toDegrees(ll.longitudeValue(SI.RADIAN));
        final double llY = Math.toDegrees(ll.longitudeValue(SI.RADIAN));
        
        return new Point(llX, llY);
    }
}
