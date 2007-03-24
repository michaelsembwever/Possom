// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import no.schibstedsok.alfa.external.service.CompanyService;
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
 * It calls a ejb3 stateless session bean in the sales system which load all
 * product information to be presented in the infopage for katalog.
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

                        
                        ProductSearchResult internalResult = null;
                        no.schibstedsok.alfa.external.dto.ProductSearchResult eksternt = null;
                        
                        try {
                            Properties properties = new Properties();
                            properties.put("java.naming.factory.initial",
                                            "org.jnp.interfaces.NamingContextFactory");
                            properties.put("java.naming.factory.url.pkgs",
                                            "org.jboss.naming:org.jnp.interfaces");
                            properties.put("java.naming.provider.url", url);

                            LOG.debug("Url: " + url);
                            LOG.debug("JNDI_NAME: " + jndi);
                            LOG.debug("CompanyId: " + intCompanyId);

                            InitialContext ctx = new InitialContext(properties);
                            
                            // hent ut remoteinterfacet til server side ejbn vi vil kalle
                            CompanyService service = (CompanyService) ctx.lookup(jndi);
                            
                            // kall sl-bean i salesadmin with company id parameter.                            
                            eksternt = (no.schibstedsok.alfa.external.dto.ProductSearchResult) service
                                            .getProductDataForCompany(intCompanyId);
                            
                        } catch (NamingException ex) {
                            LOG.error("Jndi-lookup failed, "+ex.getMessage(),ex);
                        }

                        
                        
                        /**
                         *  Hent ut alle produkter som er lagt inn på infosiden.
                         */
                        internalResult = new ProductSearchResult();
                        if (eksternt.hasInfoPageProducts()) {
                                LOG.debug("Fant info page products, hent ut.");
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
                            LOG.debug("Firmaet har ingen info page produkter.");
                        }

                        
                        
                        /**
                         * Hent ut alle produkter som er lagt inn på søkeresultatet.
                         *
                         */
                        if (eksternt.hasListingProducts()) {
                                LOG.debug("Fant listing page products, hent ut.");
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
                                LOG.debug("Firmaet har ingen result page produkter.");
                        }
                        cat.addProducts(internalResult);    
		}
	}
}
