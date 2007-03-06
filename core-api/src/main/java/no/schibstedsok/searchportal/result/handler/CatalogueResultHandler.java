// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.Properties;
import javax.naming.InitialContext;
import no.schibstedsok.alfa.external.service.CompanyService;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.ProductResultItem;
import no.schibstedsok.searchportal.result.ProductSearchResult;
import no.schibstedsok.searchportal.result.ProductSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;
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
	 *
	 * @param cxt
	 *            the context in which the resulthandler is executed in.
	 * @param parameters
	 *            sent to the resulthandler.
	 */
	public void handleResult(final Context cxt, final DataModel datamodel) {
		LOG.info("Starter Catalogue ResultHandler.");

		final SiteConfiguration siteConf = datamodel.getSite().getSiteConfiguration();

		String url = (String) siteConf.getProperties().get(
				"alfa_remote_service_url");
		String jndi = (String) siteConf.getProperties().get(
				"alfa_remote_service_jndi_name");

		SearchResult searchResult = cxt.getSearchResult();

		if (searchResult.getResults().iterator().hasNext()) {

			CatalogueSearchResultItem cat = (CatalogueSearchResultItem) searchResult
					.getResults().get(0);

			String sCompanyId = cat.getField("iypcompanyid");
			int intCompanyId = -1;

			intCompanyId = Integer.valueOf(sCompanyId);

			// kall sl-bean i salesadmin with company id parameter.
			try {
				Properties properties = new Properties();
				properties.put("java.naming.factory.initial",
						"org.jnp.interfaces.NamingContextFactory");
				properties.put("java.naming.factory.url.pkgs",
						"org.jboss.naming:org.jnp.interfaces");
				properties.put("java.naming.provider.url", url);

				LOG.info("Url: " + url);
				LOG.info("JNDI_NAME: " + jndi);
				LOG.info("CompanyId: " + intCompanyId);

				InitialContext ctx = new InitialContext(properties);
				CompanyService service = (CompanyService) ctx.lookup(jndi);
				no.schibstedsok.alfa.external.dto.ProductSearchResult eksternt = (no.schibstedsok.alfa.external.dto.ProductSearchResult) service
						.getProductDataForCompany(intCompanyId);

				/**
				 * Hent ut alle produkter som er lagt inn på infosiden.
				 *
				 */
				ProductSearchResult internalResult = new ProductSearchResult();
				if (eksternt.hasInfoPageProducts()) {
					for (no.schibstedsok.alfa.external.dto.ProductResultItem prodItem : eksternt
							.getInfoPageProducts()) {
						ProductResultItem item = new ProductSearchResultItem();

						if (prodItem.getFields().size() > 0) {
							item.setFields(prodItem.getFields());
							LOG.info("Field: " + prodItem.getFields());
							internalResult.addInfoPageResult(item);
						}
					}
				} else {
					LOG.info("Firmaet har ingen info page produkter.");
				}

				/**
				 * Hent ut alle produkter som er lagt inn på søkeresultatet.
				 *
				 */
				if (eksternt.hasListingProducts()) {
					for (no.schibstedsok.alfa.external.dto.ProductResultItem prodItem : eksternt
							.getListingProducts()) {
						ProductResultItem item = new ProductSearchResultItem();

						item.setFields(prodItem.getFields());
						if (prodItem.getFields().size() > 0) {
							LOG.info("Field: " + prodItem.getFields());
							internalResult.addListingResult(item);
						}

					}
				} else {
					LOG.info("Firmaet har ingen result page produkter.");
				}

				cat.addProducts(internalResult);

			} catch (Exception e) {
				System.out.print(e);
				e.printStackTrace();
			}
		}
	}
}
