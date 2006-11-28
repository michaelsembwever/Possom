package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.InfrastructureException;

import java.util.Map;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import nu.prisjakt.www.wsdl.Butik;
import nu.prisjakt.www.wsdl.Kategori;
import nu.prisjakt.www.wsdl.PrisjaktLocator;
import nu.prisjakt.www.wsdl.PrisjaktPortType;
import nu.prisjakt.www.wsdl.Resultat;
import nu.prisjakt.www.wsdl.Produkt;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * This command implements the integration with prisjakt.
 */
public class PrisjaktSearchCommand extends AbstractSearchCommand {

    private static final String SOAP_ENDPOINT = "http://www.prisjakt.nu/sesam/soap.php";
    private static final Logger LOG = Logger.getLogger(OverturePPCSearchCommand.class);


    /**
     * Creates a new instance of this class.
     *
     * @param cxt The context to execute in.
     * @param parameters The search parameters.
     */
    public PrisjaktSearchCommand(final Context cxt, final Map<String, Object> parameters) {
        super(cxt, parameters);
    }

    /** {@inheritDoc} */
    public SearchResult execute() {
        final SearchResult result = new BasicSearchResult(this);

        final PrisjaktLocator service = new PrisjaktLocator();

        LOG.debug("Executing");

        try 
        {

            final PrisjaktPortType port = service.getPrisjaktPort(new java.net.URL(SOAP_ENDPOINT));
            final Resultat prisjaktResult= port.getData(getTransformedQuery());
            
            String query = getTransformedQuery();
            result.addField("searchquery", query);
            
            Produkt[] products = prisjaktResult.getProdukter();
            Butik[] butiker = prisjaktResult.getButiker();
            Kategori[] kategorier = prisjaktResult.getKategorier();
            
            result.setHitCount(products.length);
            /*
             * FIXME: Ta bort hårdkodningen för söktyp
             */
            
            
            result.addField("searchtype", "productsearch");
            //result.addField("searchtype", "storesearch");
            //result.addField("searchtype", "categorysearch");
            
            LOG.debug("Number of results " + result.getHitCount());
            
            /*
             * FIXME: Ta bort hårdkodning för söktyp
             */
            
            if((products!=null) && (result.getField("searchtype")=="productsearch"))
            {
            	productConverter(result, products);
            }
            if(kategorier!=null && (result.getField("searchtype")=="categorysearch"))
            {
            	catagorieConverter(result, kategorier);
            }
            if(butiker!=null && (result.getField("searchtype")=="storesearch"))
            {
            	storeConverter(result, butiker);
            }
            
            return result;
        }
        catch (ServiceException e) 
        {
            throw new InfrastructureException(e);
        } 
        catch (MalformedURLException e) 
        {
            throw new InfrastructureException(e);
        } 
        catch (RemoteException e) 
        {
            throw new InfrastructureException(e);
        }
    }

    
    private void productConverter(SearchResult result, Produkt [] products)
    {
    	for (final Produkt product : products) {
            final SearchResultItem item = new BasicSearchResultItem();
            item.addField("productName", product.getProduktnamn());
            item.addField("lowestPrice", Integer.toString(product.getLagstaPris()));
            item.addField("numberofStores", Integer.toString(product.getAntalButiker()));
            /* &st=4 tillägget gör att vi får en liten bild ifrån prisjakt*/
            item.addField("pruductPicture", product.getBild()+"&st=4");
            item.addField("categorieURL", product.getKategoriurl());
            item.addField("pruductURL", product.getUrl());
            item.addField("categorieName", product.getKategorinamn());
            
            result.addResult(item);
        }
    }
    
    private void catagorieConverter(SearchResult result, Kategori [] kategorier)
    {
    	for (final Kategori katag : kategorier) {
            final SearchResultItem item = new BasicSearchResultItem();
            item.addField("numberofProducts", Integer.toString(katag.getAntalProdukter()));
            item.addField("categorieURL", katag.getUrl());
            item.addField("categorieName", katag.getKategorinamn());
            
            result.addResult(item);
        }
    }
    
    private void storeConverter(SearchResult result, Butik [] stores)
    {
    	for (final Butik store : stores) {
            final SearchResultItem item = new BasicSearchResultItem();
            item.addField("numberofProducts", Integer.toString(store.getAntalProdukter()));
            item.addField("storeURL", store.getUrl());
            item.addField("storeName", store.getButiksnamn());
            
            result.addResult(item);
        }
    }
    
}
