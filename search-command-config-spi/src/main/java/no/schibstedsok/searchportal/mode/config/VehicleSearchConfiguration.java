// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * Configuration for the Vehicle Search command.
 * 
 * @version $Id$
 */
@Controller("VehicleSearchCommand")
public class VehicleSearchConfiguration extends AbstractSearchConfiguration {

    private Map carsMap;

    private Set accessoriesSet;

    private static final String VEHICLE_ACCESSORIES_PROPERTIES = "vehicle_accessories.xml";
    private static final String VEHICLE_CARS_PROPERTIES = "vehicle_cars.xml";

    /**
     * Creates a new instance of this search configuration.
     * @param sc 
     */
    public VehicleSearchConfiguration(final SearchConfiguration sc) {
        super(sc);
    }

    /**
     * 
     * @return 
     */
    public String getCarsPropertiesFileName() {
        return VEHICLE_CARS_PROPERTIES;
    }

    /**
     * 
     * @return 
     */
    public Map getCarsMap() {
        return carsMap;
    }

    /**
     * 
     * @param bmap 
     */
    public void setCarsMap(Map bmap) {
        carsMap = bmap;
    }

    /**
     * 
     * @return 
     */
    public String getAccessoriesFileName() {
        return VEHICLE_ACCESSORIES_PROPERTIES;
    }

    /**
     * 
     * @return 
     */
    public Set getAccessriesSet() {
        return accessoriesSet;
    }

    /**
     * 
     * @param acc 
     */
    public void setAccessoriesSet(Set acc) {
        accessoriesSet = acc;
    }

}
