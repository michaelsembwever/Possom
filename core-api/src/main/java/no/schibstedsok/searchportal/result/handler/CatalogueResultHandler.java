// Copyright (2006-2007) Schibsted Søk AS

package no.schibstedsok.searchportal.result.handler;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import no.schibstedsok.alfa.external.service.CompanyService;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.ProductResultItem;
import no.schibstedsok.searchportal.result.ProductSearchResult;
import no.schibstedsok.searchportal.result.ProductSearchResultItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import org.apache.log4j.Logger;

/**
 * Resulthandler to fetch sales from the catalogue sales system. This class is
 * called after a search command with <catalogue/> resulthandler tag is defined.
 * It calls a ejb3 stateless session bean in the sales system which load all
 * product information to be presented in the infopage for katalog.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Id$</tt>
 */
public final class CatalogueResultHandler implements ResultHandler {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(CatalogueResultHandler.class);

    /** Configuration for this result handler. */
    private final CatalogueResultHandlerConfig config;

    /**
     * Constructor.
     * @param config The configuration for the result handler. 
     */
    public CatalogueResultHandler(final ResultHandlerConfig config) {
        this.config = (CatalogueResultHandlerConfig) config;
    }

    /**
     * Responsible for enrich the search result with data from a remote service.
     * If the remote service is not available, the index data will be used.
     * 
     * @see ResultHandler#handleResult(no.schibstedsok.searchportal.result.handler.ResultHandler.Context, DataModel)
     * @param ctx The search context containing the search result.
     * @param datamodel the datamodel containining the site configuration.
     */
    public void handleResult(final Context ctx, final DataModel datamodel) {

        LOG.info("Starting Catalogue ResultHandler.");

        final SiteConfiguration siteConf = datamodel.getSite().getSiteConfiguration();
        final ResultList<ResultItem> searchResult = ctx.getSearchResult();

        if (searchResult.getResults().iterator().hasNext()) {

            final CatalogueSearchResultItem cat = (CatalogueSearchResultItem) searchResult.getResults().get(0);
            final int companyId = Integer.valueOf(cat.getField("iypcompanyid"));

            ProductSearchResult internalResult = null;
            no.schibstedsok.alfa.external.dto.ProductSearchResult companyServiceResult = null;

            try {
                companyServiceResult 
                        = (no.schibstedsok.alfa.external.dto.ProductSearchResult) getCompanyService(siteConf)
                        .getProductDataForCompany(companyId);
                
                internalResult = new ProductSearchResult();
                        
                if (companyServiceResult.hasInfoPageProducts()) {
                    LOG.debug("Found info page products for companyId: "+ companyId);
                    
                    for (no.schibstedsok.alfa.external.dto.ProductResultItem externalProductResultItem 
                            : companyServiceResult.getInfoPageProducts()) {

                        final ProductResultItem item = new ProductSearchResultItem();

                        if (externalProductResultItem.getFields().size() > 0) {
                            item.setFields(externalProductResultItem.getFields());
                            LOG.info("Field: " + externalProductResultItem.getFields());
                            internalResult.addInfoPageResult(item);
                        }
                    }
                } else {
                    LOG.debug("No info page products for companyId: " + companyId);
                }
                cat.addProducts(internalResult);
                
                // the content enrichment was done from remote service, return.
                return;
                
            } catch (NamingException e) {
                LOG.error("Unable to lookup remote service, index will be used for info page result " , e);
            } catch(EJBException e){
                LOG.error("Error occured calling remote service, index will be used for info page result ", e);
            } catch(UndeclaredThrowableException e){
                LOG.error("Error occured calling remote service, index will be used for info page result ", e);
            }
          
            final ProductSearchResult internalIndexResult = new ProductSearchResult();      
            internalIndexResult.addInfoPageResult(createIndexInfoPageItem(cat));
            
            //internalIndexResult.addInfoPageResult(indexInfoPageItem);
            cat.addProducts(internalIndexResult);

        }
    }
    
    /**
     * Populate info page product based on index data.
     * @param cat The search result item.
     * @return The infopage product based on index data.
     */
    private ProductResultItem createIndexInfoPageItem(CatalogueSearchResultItem cat) {
        ProductResultItem indexInfoPageItem = new ProductSearchResultItem();
        
        indexInfoPageItem.addField("businessname", cat.getField("iypnavnvisning"));
        indexInfoPageItem.addField("marketname", cat.getField("iypnavnmarked"));
        indexInfoPageItem.addField("organizationno", cat.getField("iyporgnr"));
        indexInfoPageItem.addField("phone1", cat.getField("iypnrtelefon"));
        indexInfoPageItem.addField("mobile", cat.getField("iypnrtelefonmobil"));
        indexInfoPageItem.addField("fax", cat.getField("iypnrfax"));
        indexInfoPageItem.addField("email", cat.getField("iypepost"));
        indexInfoPageItem.addField("logo", cat.getField("iyplogourl"));
      
        
        indexInfoPageItem.addField("mapX", cat.getField("iypxcoord"));
        indexInfoPageItem.addField("mapY", cat.getField("iypycoord"));
        indexInfoPageItem.addField("homepage", cat.getField("iypurl"));
        
        String postalAddress = cat.getField("iyppostadresse");
        String postalZipCode = cat.getField("iyppostpostnr");
        String postalCity = cat.getField("iyppostpostadresse");
        
        if(postalAddress != null && postalAddress.length() > 0 && postalZipCode != null && postalZipCode.length() > 0 && postalCity != null && postalCity.length() > 0 ){
            indexInfoPageItem.addField("fullPostalAddress", postalAddress + ", " + postalZipCode + " " + postalCity);
        }
        
        String visitAddress = cat.getField("iypadresse");
        String visitZipCode = cat.getField("iyppostnr");
        String visitCity = cat.getField("iyppoststed");
        
        if(visitAddress != null && visitAddress.length() > 0 && visitZipCode != null && visitZipCode.length() > 0 && visitCity != null && visitCity.length() > 0 ){
            indexInfoPageItem.addField("fullVisitAddress", visitAddress + ", " + visitZipCode + " " + visitCity);
        }
        return indexInfoPageItem;
    }

    /**
     * Utility method for lookup of remote service.
     * 
     * @param siteConf Configuration for url and jndi name of the service.
     * @return Reference to a remote CompanyService reference.
     * @throws NamingException If lookup of service in JNDI fails.
     */
    private CompanyService getCompanyService(final SiteConfiguration siteConf)
        throws NamingException {
        final String url = (String) siteConf.getProperties().get("alfa_remote_service_url");
        final String jndi = (String) siteConf.getProperties().get("alfa_remote_service_jndi_name");

        final Properties properties = new Properties();
        properties.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        properties.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        properties.put("java.naming.provider.url", url);

        LOG.debug("Url: " + url + " JNDI_NAME: " + jndi);

        final InitialContext ctx = new InitialContext(properties);
        return (CompanyService) ctx.lookup(jndi);
    }
}