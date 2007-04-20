// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;


/**
 * A Storm result handler that looks into nested searchresults for the field to
 * modify. Needed because we get raw data from Storm.
 *
 * @author larsj
 * @version $Id$
 *
 */
@Controller("ForecastWindHandler")
public final class ForecastWindResultHandlerConfig extends AbstractResultHandlerConfig {}
