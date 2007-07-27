// Copyright (2007) Schibsted Sök AB
package no.schibstedsok.searchportal.mode.command;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;

import no.schibstedsok.searchportal.result.BasicResultItem;
import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import nu.prisjakt.www.wsdl.Produkt;

import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;

import com.tasteline.www.Sesam.ArrayOfRecipeHit;
import com.tasteline.www.Sesam.RecipeHit;
import com.tasteline.www.Sesam.SearchResults;
import com.tasteline.www.Sesam.TastelineSearchLinks;
import com.tasteline.www.Sesam.TastelineSearchLinksLocator;
import com.tasteline.www.Sesam.TastelineSearchLinksSoap;
import com.tasteline.www.Sesam.TastelineSearchLinksSoapStub;

/**
 * This command implements the integration with tasteline.
 */
public final class TastelineSearchCommand extends
		AbstractWebServiceSearchCommand 
{

	private static final Logger LOG = Logger
			.getLogger(TastelineSearchCommand.class);

	/** Creates a new instance of TastelineSearchCommand
	 *
	 * @param cxt Search command context.
	 */
	public TastelineSearchCommand(final Context cxt) 
	{
		super(cxt);
	}

	/** {@inheritDoc} */
	@Override
	public ResultList<? extends ResultItem> execute() 
	{

		TastelineSearchLinks tsl = new TastelineSearchLinksLocator();
		final ResultList<ResultItem> result = new BasicResultList<ResultItem>();
		try 
		{
			TastelineSearchLinksSoap tsls = new TastelineSearchLinksSoapStub(
					new java.net.URL(tsl.getTastelineSearchLinksSoapAddress()),
					tsl);
			
			final String query = getTransformedQuery();
			
			//Get search result from tasteline
			SearchResults sr = tsls.getSearchResults(query);
			//System.out.println("Antalet träffar " + sr.getTotalHits());
			
			
			
			//Get main result
			result.addField("linkToSearch", sr.getLinkToSearch());
			result.addField("totalHits", Integer.toString(sr.getTotalHits()));
			result.setHitCount(sr.getTotalHits());
			
			//add search query to result 
			result.addField("searchquery", query);
			
			//Get individual recipet
			ArrayOfRecipeHit aorh =  sr.getRecipeHits();
			RecipeHit rh [] = aorh.getRecipeHit();
			recieptConverter(result,rh);
			
		} 
		catch (MalformedURLException e) 
		{
			LOG.error("MalformedURLException:", e);
			throw new InfrastructureException(e);
		} 
		catch (AxisFault e) 
		{
			LOG.error("AxisFaultException:", e);
			throw new InfrastructureException(e);
		} 
		catch (RemoteException e) 
		{
			LOG.error("RemoteException:", e);
			throw new InfrastructureException(e);
		}

		
		return result;
	}
	
	private void recieptConverter(final ResultList<ResultItem> result, final RecipeHit [] rh) {
        if (rh != null) {
            for (final RecipeHit recipeHit : rh) {
                ResultItem item = new BasicResultItem();
                item = item.addField("recipeLink", recipeHit.getRecipeLink());
                item = item.addField("recipeName", recipeHit.getRecipeName());
                item = item.addField("recipePicture", recipeHit.getRecipePicture());
                result.addResult(item);
            }
        }
    }

}
