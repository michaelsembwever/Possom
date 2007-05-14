/*
 * Copyright (2005-2007) Schibsted Søk AS
 */


package no.schibstedsok.searchportal.mode.command;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.util.GeoSearchUtil;

import org.apache.log4j.Logger;

/**
 * Search command responsible for address search.
 * 
 * @author <a href="mailto:anders@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Id$</tt>
 */
public class AddressSearchCommand extends AbstractSimpleFastSearchCommand{
    
    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(AddressSearchCommand.class);

    /** 
     * Constructor for AddressSearchCommand.
     * @param cxt Context to initialize the command.
     */
    public AddressSearchCommand(final Context cxt) {
        super(cxt);
    }

    /**
     * @see no.schibstedsok.searchportal.mode.command.AbstractSearchCommand#execute()
     */
    public final ResultList<? extends ResultItem> execute() {
        
        final ResultList<ResultItem> sr = (ResultList<ResultItem>) super.execute();

        if (getSearchConfiguration().isCollapsing()) {
            String prevCollapseId = "";
            for (ResultItem item : sr.getResults()) {
                if (item.getField("collapseid").equals(prevCollapseId)) {
                    sr.getResults().remove(item);
                } else if (item.getField("streetHash") != null) {
                    final HashMap<String,String> streetNumbers = new LinkedHashMap<String,String>();
                    for (String record : item.getField("streetHash").split(";")) {
                        final String[] values = record.split(":");
                        streetNumbers.put(values[0], values[1]);
                    }
                    sr.replaceResult(item, item.addObjectField("streetHashList", streetNumbers));
                }
            }
        }
        
        return sr;
    }
    
    /**
     * If the search is a GEO search, add required GEO search parameters.
     * @param params 
     * @see no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand#setAdditionalParameters(ISearchParameters)
     */
    @Override
    protected void setAdditionalParameters(final ISearchParameters params) {
        super.setAdditionalParameters(params);
        final ParametersDataObject pdo = datamodel.getParameters();
       
        if(!GeoSearchUtil.isGeoSearch(pdo)){
            return;
        }
     
        final String center = GeoSearchUtil.getCenter(pdo);
        
        LOG.debug("center : " + center);

        params.setParameter(new SearchParameter("qtf_geosearch:unit", GeoSearchUtil.RADIUS_MEASURE_UNIT_TYPE));
        params.setParameter(new SearchParameter("qtf_geosearch:radius", GeoSearchUtil.getRadiusRestriction(pdo)));
        params.setParameter(new SearchParameter("qtf_geosearch:center", center));
    }
 
    /**
     * Override sortby when the search is  a geo search.
     * @return 
     * @see no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand#getSortBy()
     */
    @Override
    protected String getSortBy(){
        final ParametersDataObject pdo = datamodel.getParameters();
        if(GeoSearchUtil.isGeoSearch(pdo)){
            return "geo_spec_sortable";
        }
        return super.getSortBy();
    }
}
