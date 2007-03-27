// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;



/**
 * Defines a default weather search pattern.
 *
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
public final class WeatherQueryTransformer extends AbstractQueryTransformer {

    private final WeatherQueryTransformerConfig config;

    /**
     *
     * @param config
     */
    public WeatherQueryTransformer(final QueryTransformerConfig config){
        this.config = (WeatherQueryTransformerConfig) config;
    }

    /**
     * creates a default location filter with major cities as defined in modes.xml
     */
    @Override
    public String getFilter() {

		StringBuilder defaultLocationsFilter = new StringBuilder();
    	final boolean blankQuery = getContext().getQuery().isBlank();

    	if(blankQuery){

        //    defaultLocationsFilter.append("+(sgeneric4:By) +(title:");
            defaultLocationsFilter.append("+(");
            for (String location : config.getDefaultLocations() ) {
                defaultLocationsFilter.append(" igeneric1:");
                defaultLocationsFilter.append(location);
            }
            defaultLocationsFilter.append(") ");

    	}

    	return defaultLocationsFilter.toString();
    }

}
