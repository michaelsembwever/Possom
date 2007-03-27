// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import java.util.ArrayList;
import java.util.List;
import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;



/**
 * Defines a default weather search pattern.
 *
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
@Controller("WeatherQueryTransformer")
public final class WeatherQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private List<String> defaultLocations = new ArrayList<String>();

    public void setDefaultLocations(String[] strings) {
        if(strings.length > 0 && strings[0].trim().length() >0){
            for (String location : strings) {
                defaultLocations.add(location.trim());
            }
        }
    }

    public List<String> getDefaultLocations() {
		return defaultLocations;
	}

    @Override
    public WeatherQueryTransformerConfig readQueryTransformer(final Element qt){

        super.readQueryTransformer(qt);
        setDefaultLocations(qt.getAttribute("default-locations").split(","));
        return this;
    }
}
