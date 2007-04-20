// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;

/**
 * A Storm result handler that looks into nested searchresults for
 * the field to modify.
 *
 * @author larsj
 * @version $Id$
 *
 */
@Controller("ForecastDateHandler")
public final class ForecastDateResultHandlerConfig extends WeatherDateResultHandlerConfig {}
