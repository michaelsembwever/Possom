package no.schibstedsok.searchportal.mode.command;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.StringHolder;
import se.blocket.www2.search.SearchLocator;
import se.blocket.www2.search.SearchPortType;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.BlocketSearchConfiguration;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.UrlResourceLoader;


public class BlocketSearchCommand extends AbstractWebServiceSearchCommand {
	
	
	public BlocketSearchCommand(final Context cxt,
			final Map<String, Object> parameters) {
		super(cxt, parameters);}

	@Override
	public SearchResult execute() 
	{
		
		final TokenEvaluationEngine engine = context.getTokenEvaluationEngine();
		
		final boolean isElectronic = engine.evaluateQuery(TokenPredicate.PRODUCT_ELECTRONIC,context.getQuery());
		final boolean isHobby = engine.evaluateQuery(TokenPredicate.PRODUCT_HOBBY,context.getQuery());
		
		
		
		
		BlocketSearchConfiguration bsc = (BlocketSearchConfiguration) context.getSearchConfiguration();
		
		Map m = bsc.getBlocketMap();
		
		final SearchLocator service = new SearchLocator();
		
		final SearchResult result = new BasicSearchResult(this); 
		
		try 
		{
			final SearchPortType port = service.getsearchPort(new java.net.URL(service.getsearchPortAddress()));
			String query = getTransformedQuery();
		
			LongHolder lholder = new LongHolder();
			StringHolder sholder = new StringHolder();
			
			String categoryIndex =(String) m.get(query);
			if(categoryIndex!=null)
			{
				port.search(query, Integer.parseInt(categoryIndex), lholder, sholder);
			}
			
			String nads = Long.toString(lholder.value);
			result.addField("searchquery", query);
			result.addField("numberofads", nads);
			result.addField("blocketbackurl", sholder.value);
			
			result.setHitCount(1);
			
			
	
		}
		catch (ServiceException se) 
		{
			throw new InfrastructureException(se);
		} 
		catch (MalformedURLException murle) 
		{
			throw new InfrastructureException(murle);
		} 
		catch (RemoteException re) 
		{
			throw new InfrastructureException(re);
		}
		
		//return null;
		return result;
		
	}

}
