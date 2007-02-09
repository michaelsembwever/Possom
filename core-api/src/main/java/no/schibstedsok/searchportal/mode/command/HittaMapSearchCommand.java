package no.schibstedsok.searchportal.mode.command;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.rmi.RemoteException;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.StringHolder;
import javax.xml.validation.Schema;

import org.apache.axis.message.MessageElement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Document;


import se.blocket.www2.search.SearchLocator;
import se.blocket.www2.search.SearchPortType;
import se.hitta.www.HittaService.GetPlaceStreetDetailsResponseGetPlaceStreetDetailsResult;
import se.hitta.www.HittaService.GetPlaceStreetDetailsWithLevelResponseGetPlaceStreetDetailsWithLevelResult;
import se.hitta.www.HittaService.GetStreetResponseGetStreetResult;
import se.hitta.www.HittaService.HittaService;
import se.hitta.www.HittaService.HittaServiceLocator;
import se.hitta.www.HittaService.HittaServiceSoap;
import no.schibstedsok.searchportal.InfrastructureException;

import no.schibstedsok.searchportal.mode.config.BlocketSearchConfiguration;
import no.schibstedsok.searchportal.mode.config.HittaMapSearchConfiguration;

import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;

public class HittaMapSearchCommand extends AbstractWebServiceSearchCommand {

	private static final Logger LOG = Logger
			.getLogger(BlocketSearchCommand.class);

	private static final String ERR_FAILED_HITTAMAP_SEARCH = "Failed Map search command";

	private static final String ERR_FAILED_ENCODE_HITTAMAP = "Failed to encode Map search query";

	public HittaMapSearchCommand(final Context cxt,
			final Map<String, Object> parameters) {
		super(cxt, parameters);
	}

	@Override
	public SearchResult execute() {

		HittaMapSearchConfiguration bsc = (HittaMapSearchConfiguration) context
				.getSearchConfiguration();
		
		final TokenEvaluationEngine engine = context.getTokenEvaluationEngine();
		/*Butiker*/
		final boolean isGEOGLOBAL = engine.evaluateQuery(
				TokenPredicate.GEOGLOBAL, context.getQuery());
		final boolean isGEOLOCAL = engine.evaluateQuery(
				TokenPredicate.GEOLOCAL, context.getQuery());
		
		
		final SearchResult result = new BasicSearchResult(this);
		if(isGEOGLOBAL||isGEOLOCAL)
		{
		try
		{
			HittaServiceLocator locator = new HittaServiceLocator();
	        HittaServiceSoap service = locator.getHittaServiceSoap();
			String key = "f7964f9f-c798-47b0-9705-29737dabecef";
			
			String transQuery = getTransformedQuery();
			String encTransQuery = URLEncoder.encode(transQuery, "iso-8859-1");
			
			 int traff=service.getPlaceStreetAmount(transQuery, key);
			
			if(traff==1)
			{
				GetPlaceStreetDetailsResponseGetPlaceStreetDetailsResult gpsdr = new GetPlaceStreetDetailsResponseGetPlaceStreetDetailsResult();
				
				//Hämtar koordinater för den stora kart visningen.
				gpsdr = service.getPlaceStreetDetails(transQuery, key);
				org.apache.axis.message.MessageElement[] fields = gpsdr.get_any(); 
				String respXML= fields[1].getAsString();
				
				
				//org.w3c.dom.Document doc  = fields[1].getAsDocument();
				
				int beginIndex = respXML.indexOf("X=");
				String x = respXML.substring(beginIndex+1);
				int endIndex =  x.indexOf('"',3);
				String xx = x.substring(2, endIndex);
				
				int beginIndexy = respXML.indexOf("Y=");
				String y = respXML.substring(beginIndexy+1);
				int endIndexy =  y.indexOf('"',3);
				String yy = y.substring(2, endIndexy); 
				
				
			
				
				
				
				String hittaURL = "http://www.hitta.se/SearchCombi.aspx?SearchType=4&UCSB%3aWflWhite=1a1b&UCSB%3aWflPink=4a&UCSB%3aTextBoxWho=&UCSB%3aTextBoxWhere="+encTransQuery;
				String hittaBigMapURL = "http://www.hitta.se/LargeMap.aspx? pointX="+yy+"&pointY="+xx+"&cx="+yy+"&cy="+xx+"&z=3&name="+encTransQuery;
				//String hittaBigMapURL = "http://www.hitta.se/LargeMap.aspx?%20pointX=1628131&pointY=6582373&cx=1628131&cy=6582373&z=3&name=Sveav.%20100";
				result.addField("hittaURL", hittaURL);
				result.addField("hittaBigMapURL", hittaBigMapURL);
				result.addField("searchquery", transQuery);
				result.setHitCount(1);
				result.addField("searchtype", "one");
				
			}
			else if(traff>1)
			{
				
				String hittaURL = "http://www.hitta.se/SearchCombi.aspx?SearchType=4&UCSB%3aWflWhite=1a1b&UCSB%3aWflPink=4a&UCSB%3aTextBoxWho=&UCSB%3aTextBoxWhere="+encTransQuery;
				result.addField("hittaURL", hittaURL);
				result.addField("searchquery", transQuery);
				result.setHitCount(traff);
				result.addField("searchtype", "many");
			}
			else
			{
				result.setHitCount(0);
			}
			
		}
		
		catch (ServiceException ex) {
            LOG.error(ERR_FAILED_HITTAMAP_SEARCH, ex);
        } catch (RemoteException ex) {
            LOG.error(ERR_FAILED_HITTAMAP_SEARCH, ex);
        }
        catch (UnsupportedEncodingException usee) {
			
			LOG.error(ERR_FAILED_ENCODE_HITTAMAP,usee);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
		

		return result;
	}
}
