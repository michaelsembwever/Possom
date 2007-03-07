// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import java.util.Map;
import java.util.Set;

/**
 * Configuration for the Vehicle Search command.
 */
public class VehicleSearchConfiguration extends AbstractSearchConfiguration {

    private Map carsMap;

    private Set accessoriesSet;

    private static final String VEHICLE_ACCESSORIES_PROPERTIES = "vehicle_accessories.xml";
    private static final String VEHICLE_CARS_PROPERTIES = "vehicle_cars.xml";

    /**
     * Creates a new instance of this search configuration.
     */
    public VehicleSearchConfiguration(final SearchConfiguration sc) {
        super(sc);
    }

    public String getCarsPropertiesFileName() {
        return VEHICLE_CARS_PROPERTIES;
    }

    public Map getCarsMap() {
        return carsMap;
    }

    public void setCarsMap(Map bmap) {
        carsMap = bmap;
    }

    public String getAccessoriesFileName() {
        return VEHICLE_ACCESSORIES_PROPERTIES;
    }

    public Set getAccessriesSet() {
        return accessoriesSet;
    }

    public void setAccessoriesSet(Set acc) {
        accessoriesSet = acc;
    }

}
