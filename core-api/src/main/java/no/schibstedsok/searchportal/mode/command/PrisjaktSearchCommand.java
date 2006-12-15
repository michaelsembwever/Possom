package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.InfrastructureException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This command implements the integration with prisjakt.
 */
public class PrisjaktSearchCommand extends AbstractWebServiceSearchCommand {

    
	
	
	
	
	private static final String SOAP_ENDPOINT = "http://www.prisjakt.nu/sesam/soap.php";
    private static final Logger LOG = Logger.getLogger(OverturePPCSearchCommand.class);
    
    /**
     * Format för siffror utan decimaler med tusenavskiljare.
     */
    private static DecimalFormat NUMMER_AVSK_FORMAT;
    static
    {
       DecimalFormatSymbols symbols = new DecimalFormatSymbols();
       symbols.setGroupingSeparator(' ');
       NUMMER_AVSK_FORMAT = new DecimalFormat("#,##0", symbols);
       NUMMER_AVSK_FORMAT.setGroupingSize(3);
    }


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
        
    	
    	final TokenEvaluationEngine engine = context.getTokenEvaluationEngine();
    	/*Butiker?*/
    	final boolean isChain = engine.evaluateQuery(TokenPredicate.CHAIN, context.getQuery());
    	final boolean isService = engine.evaluateQuery(TokenPredicate.SHOP_CATEGORY, context.getQuery());
    	final boolean isElectronics = engine.evaluateQuery(TokenPredicate.PRODUCT_ELECTRONIC, context.getQuery());
    	final boolean isHousehold = engine.evaluateQuery(TokenPredicate.PRODUCT_HOUSEHOLD, context.getQuery());
    	final boolean isMusic = engine.evaluateQuery(TokenPredicate.PRODUCT_MUSIC, context.getQuery());
    	
    	final SearchResult result = new BasicSearchResult(this);
    	
    	if(isChain||isService||isElectronics||isHousehold||isMusic)
    	{
    		
    		final PrisjaktLocator service = new PrisjaktLocator();
    	
    	
	        LOG.debug("Executing prisjact command");
	
	        try 
	        {
	
	            final PrisjaktPortType port = service.getPrisjaktPort(new java.net.URL(SOAP_ENDPOINT));
	            final Resultat prisjaktResult= port.getData(getTransformedQuery());
	            
	            String query = getTransformedQuery();
	            result.addField("searchquery", query);
	            
	            Produkt[] products = prisjaktResult.getProdukter();
	            Butik[] butiker = prisjaktResult.getButiker();
	            Kategori[] kategorier = prisjaktResult.getKategorier();
	            
	            
	           
	            
	            
	            if(isElectronics||isHousehold||isMusic)
	            {
	            	if(products!=null)
	            	{
	            		result.addField("searchtype", "productsearch");
	            		result.setHitCount(products.length);
	            	}
	            	else
	            	{
	            		result.setHitCount(0);
	                	return result;
	            	}
	            }
	            else if(isService)
	            {
	            	if(kategorier!=null)
	            	{
	            		result.addField("searchtype", "categorysearch");
	            		result.setHitCount(kategorier.length);
	            	}
	            	else
	            	{
	            		result.setHitCount(0);
	                	return result;
	            	}
	            }
	            else
	            {
	            	if(butiker!=null)
	            	{
	            		result.addField("searchtype", "storesearch");
	            		result.setHitCount(butiker.length);
	            	}
	            	else
	            	{
	            		result.setHitCount(0);
	                	return result;
	            	}
	            }
	            
	            LOG.debug("Number of results " + result.getHitCount());
	            
	            
	            
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
    	result.setHitCount(0);
    	return result;
    }

    
    private void productConverter(SearchResult result, Produkt [] products)
    {
    	for (final Produkt product : products) 
    	{
            final SearchResultItem item = new BasicSearchResultItem();
            item.addField("productName", product.getProduktnamn());
            int lPris = product.getLagstaPris();
            String cf = nummerAvskFormat(lPris);
            item.addField("lowestPrice", cf);
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
    	for (final Kategori katag : kategorier) 
    	{
            final SearchResultItem item = new BasicSearchResultItem();
            item.addField("numberofProducts", Integer.toString(katag.getAntalProdukter()));
            item.addField("categorieURL", katag.getUrl());
            item.addField("categorieName", katag.getKategorinamn());
            result.addResult(item);
        }
    }
    
    private void storeConverter(SearchResult result, Butik [] stores)
    {
    	for (final Butik store : stores) 
    	{
            final SearchResultItem item = new BasicSearchResultItem();
            item.addField("numberofProducts", Integer.toString(store.getAntalProdukter()));
            item.addField("storeURL", store.getUrl());
            item.addField("storeName", store.getButiksnamn());
            result.addResult(item);
        }
    }
    
    /**
     * Formatera nummer med tusenavskiljare utan decimaler.
     */
    private static String nummerAvskFormat(Number nummer)
    {
       if (nummer == null)
       {
          return "";
       }
       return NUMMER_AVSK_FORMAT.format(nummer.doubleValue());
    }
    
    /**
     * Formatera med tusenavskiljare och lägger till decimaldel med komma-tecken
     * om decimaldel förekommer. Tar bort alla inledande och avslutande blanksteg
     * samt ev inledande '+'-tecken Ex: +5555510.35->5 555 510,35 +5555510,35->5
     * 555 510,35 +10002000 ->10 002 000
     * 
     * @param numStr <code>String</code> Strängen som ska formateras
     * @return En sträng som har formaterats enligt ovan.
     */
    private static String nummerAvskFormat(String numStr)
    {
       if (numStr == null || numStr.equals(""))
       {
          return "";
       }
       // Bort med alla inledande och avslutande blanksteg samt
       // ev inledande '+'-tecken
       numStr = numStr.trim();
       numStr = removeBlanks(numStr);
       numStr = numStr.indexOf("+") > 0 ? numStr.substring(1) : numStr;
       // Spara undan ev. decimaldelen och se till att den alltid består av ett
       // komma-tecken om den förekommer.
       String decPart = numStr.indexOf(',') > 0 ? numStr.substring(numStr
          .indexOf(',')) : numStr.indexOf('.') > 0 ? numStr.substring(
          numStr.indexOf('.')).replaceFirst(".", ",") : "";
       // Substringa ut heltalsdelen
       double d;
       try
       {
          d = Double.valueOf(
             numStr.substring(0, numStr.length() - decPart.length()))
             .doubleValue();
       }
       catch (NumberFormatException e)
       {
          e.printStackTrace();
          return "";
       }

       return NUMMER_AVSK_FORMAT.format(d).concat(decPart);
    }
    /**
     * Tar bort blanktecken ur strängen
     */
    private static String removeBlanks(String str)
    {
       if (isEmpty(str))
       {
          return str;
       }
       return StringUtils.deleteSpaces(str);
    }
    /**
     * Kontrollerar om strängargumentet är null eller tom sträng.
     */
    private static boolean isEmpty(String s)
    {
       return StringUtils.isEmpty(s);
    }
}
