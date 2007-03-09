// Copyright (2006-2007) Schibsted Søk AS
/*
 * WebSearchCommand.java
 *
 * Created on March 7, 2006, 1:01 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.datamodel.DataModel;
import org.apache.log4j.Logger;

/**
 * 
 */
public class CatalogueAdsSearchCommand extends AdvancedFastSearchCommand {

	private static final Logger LOG = Logger
			.getLogger(CatalogueAdsSearchCommand.class);

	/**
	 * String that hold the original untransformed query supplied by the user.
	 */
	private String originalQuery;

	private String queryGeo = null;

	/**
	 * Two types of queries are run by this command each time the command is
	 * executed.
	 * 
	 * @author daniel
	 * 
	 */
	private enum QueryType {
		GEO, INGENSTEDS
	}

	private QueryType whichQueryToRun;

	/**
	 * Creates a new instance of WebSearchCommand.
	 * 
	 * @param cxt
	 *            Search command context.
	 * @param parameters
	 *            Search command parameters.
	 */
	public CatalogueAdsSearchCommand(final Context cxt,
			final DataModel datamodel) {

		super(cxt, datamodel);

		// hvis "where" parametern er sendt inn, så tar vi og leser inn query
		// fra
		// den.
		if (getSingleParameter("where") != null
				&& getSingleParameter("where").length() > 0) {
			final ReconstructedQuery rq = createQuery(getSingleParameter("where"));

			final Query query = rq.getQuery();

			queryGeo = query.getQueryString();
		} else {
			queryGeo = "ingensteds";
		}

		LOG.info("Search configuration name "
				+ getSearchConfiguration().getName());
	}

	@Override
	public SearchResult execute() {
		SearchResult r = null;
		SearchResult r2 = null;

		// Run first query, which fetch all sponsorlinks
		// for a given geographic place.
		whichQueryToRun = QueryType.GEO;
		r = super.execute();

		// if there was less than 5 sponsor links from a
		// specific geographic place, we could add sponsor
		// links that does not have any specific geographic.
		if (r.getHitCount() < 5) {

			SearchResultItem[] searchResults = new SearchResultItem[5];
			// sjekk om originalQuery finnes i noen av r2 sine
			// treff resultater. Må string tokenize kommaseparert string
			// i hvert treff med "ingensteds;"
			for (SearchResultItem item : r.getResults()) {

				Pattern p = Pattern.compile("^" + originalQuery
						+ queryGeo+".*|.*;" + originalQuery + queryGeo+".*");
			
				int i = 0; 
				boolean found = false;
				for(; i < 5 ; i++){
					if(item.getField("iypspkeywords"+(i+1))!=null){
						Matcher m = p.matcher(item.getField("iypspkeywords"+(i+1)).trim().toLowerCase());
						found = m.matches();
						
						if(found){
							break;
						}
					}
				}

				// hvis funnet, må vi sette inn vårt objekt på riktig plass
				// i søkeresultatet i første søket.
				if(found){
					searchResults[i] = item;
					LOG.info("Fant sponsortreff for plass " + (i+1) + ", " + item.getField("iypspkeywords"+(i+1)));
				}else{
					LOG.error("Fant IKKE sponsortreff, det er ikke mulig, " + item);
				}
			}			
			
			// run second query, which fetch all sponsorlinks
			// for a given set of keywords, without geographic.

			whichQueryToRun = QueryType.INGENSTEDS;
			performQueryTransformation();
			r2 = super.execute();

			// sjekk om originalQuery finnes i noen av r2 sine
			// treff resultater. Må string tokenize kommaseparert string
			// i hvert treff med "ingensteds;"
			for (SearchResultItem item : r2.getResults()) {

				Pattern p = Pattern.compile("^" + originalQuery
						+ "ingensteds.*|.*;" + originalQuery + "ingensteds.*");
				
				int i = 0; 
				boolean found = false;
				for(; i < 5 ; i++){
					if(item.getField("iypspkeywords"+(i+1))!=null){
						Matcher m = p.matcher(item.getField("iypspkeywords"+(i+1)).trim().toLowerCase());
						found = m.matches();
						
						if(found) break;
					}
				}

				// hvis funnet, må vi sette inn vårt objekt på riktig plass
				// i søkeresultatet i første søket.
				if(found && searchResults[i]==null){
					searchResults[i]=item;
					LOG.info("Fant sponsortreff for plass " + (i+1) + ", " + item.getField("iypspkeywords"+(i+1)));
				}
			}

			// erstatt resultatet fra første query som returneres ut herifra,
			// med det prossesserte resultatet 
			r.getResults().clear();
			for(SearchResultItem item:searchResults){
				if(item!=null){
					r.addResult(item);
				}
			}
			
			// plass 5, er de som betalt mest, og skal da først i lista.
			// plass 1 er de som betalt minst og skal da sist i lista.
			java.util.Collections.reverse(r.getResults());
			
		}
		
		return r;
	}

	@Override
	public String getTransformedQuery() {

		originalQuery = super.getTransformedQuery().replaceAll(" ", "").toLowerCase();
		String query = null;

		if (whichQueryToRun == QueryType.GEO) {
			query = super.getTransformedQuery().replaceAll(" ", "")
					+ queryGeo.replaceAll(" ", "");
		} else {
			query = super.getTransformedQuery().replaceAll(" ", "")
					+ "ingensteds";
		}
		return "iypspkeywords5:" + query + " OR iypspkeywords4:" + query + " OR iypspkeywords3:" + query
				+ " OR iypspkeywords2:" + query + " OR iypspkeywords1:" + query;
	}

	@Override
	protected void visitImpl(DefaultOperatorClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

	@Override
	protected void visitImpl(AndClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

	@Override
	protected void visitImpl(OrClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

}
