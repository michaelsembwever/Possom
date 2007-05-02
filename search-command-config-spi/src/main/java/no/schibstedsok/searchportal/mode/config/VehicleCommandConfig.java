// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Configuration for the Vehicle Search command.
 * 
 * @version $Id$
 */
@Controller("VehicleSearchCommand")
public final class VehicleCommandConfig extends CommandConfig {

    private Map<String,String> carsMap = new HashMap<String,String>();

    private Set<String> accessoriesSet = new HashSet<String>();

    private static final String VEHICLE_ACCESSORIES_PROPERTIES = "vehicle_accessories.xml";
    private static final String VEHICLE_CARS_PROPERTIES = "vehicle_cars.xml";

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
     * @return 
     */
    public String getAccessoriesFileName() {
        return VEHICLE_ACCESSORIES_PROPERTIES;
    }

    /**
     * 
     * @return 
     */
    public Set<String> getAccessriesSet() {
        
        return Collections.unmodifiableSet(accessoriesSet);
    }


    @Override
    public VehicleCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);

        //Read vehicle specific properties for bytbil.com and blocket.se
        if(element.getElementsByTagName("accessory-search-words").getLength() > 0){
            final Element root = (Element) element.getElementsByTagName("accessory-search-words").item(0);

            final NodeList accList = root.getElementsByTagName("accessory");

            /**
             * Put car accessory search words from xml in a set
             */
            for (int i = 0; i < accList.getLength(); ++i) {
                final Element wordElement = (Element) accList.item(i);
                final String acc = wordElement.getTextContent();
                accessoriesSet.add(acc);
            }
        }else if(null != inherit && inherit instanceof VehicleCommandConfig){
            
            accessoriesSet.addAll(((VehicleCommandConfig)inherit).getAccessriesSet());
        }
        
        if(element.getElementsByTagName("car-search-words").getLength() > 0){
            // Put car words from xml into a map
            final Element root2 = (Element) element.getElementsByTagName("car-search-words").item(0);
            final NodeList carList = root2.getElementsByTagName("car");

            for (int i = 0; i < carList.getLength(); ++i) {
                final Element wordElement = (Element) carList.item(i);
                final String brand = wordElement.getAttribute("brand");
                final String model = wordElement.getAttribute("model");
                final String car = wordElement.getTextContent();
                carsMap.put(car, brand + ";" + model);   // "volvo p 1800" , "volvo;p 1800"
            }
        }else if(null != inherit && inherit instanceof VehicleCommandConfig){
            
            carsMap.putAll(((VehicleCommandConfig)inherit).getCarsMap());
        }

        return this;
    }
    
    

}
