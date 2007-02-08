// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.Map;

import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;

import org.apache.log4j.Logger;

/**
 * Resulthandler to fetch sales from the catalogue sales system. This class is
 * called after a search command with <catalogue/> resulthandler tag is defined.
 * It loops through all result items and load data with sql/jdbc from the sales
 * system.
 * 
 * @todo Add code to Jboss EJB3 stateless bean in salesadmin.
 * 
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Revision: 3436 $</tt>
 */
public final class CatalogueResultHandler implements ResultHandler {

	private static final Logger LOG = Logger
			.getLogger(CatalogueResultHandler.class);


	/**
	 * Handle the search result.
	 * @param cxt the context in which the resulthandler is executed in.
	 * @param parameters sent to the resulthandler.
	 */
	public void handleResult(final Context cxt, final Map parameters) {
		LOG.info("Starter Catalogue ResultHandler.");

		final SiteConfiguration siteConf = SiteConfiguration
				.valueOf(ContextWrapper.wrap(SiteConfiguration.Context.class,
						cxt));
	
	}
}
