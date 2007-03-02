/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.SearchParameter;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.config.CatalogueSearchConfiguration;
import no.schibstedsok.searchportal.mode.config.HittaSearchConfiguration;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;

import org.apache.log4j.Logger;

/**
 * 
 */
public class CatalogueSearchCommand extends AdvancedFastSearchCommand {

	private static final Logger LOG = Logger
			.getLogger(CatalogueSearchCommand.class);

	private String queryTwo = null;

	private String userSortBy = "kw"; // defualtsøket er på keyword

	private static final String DEBUG_CONF_NFO = "CatalogueSearch Conf details --> ";

	/**
	 * Creates a new catalogue search command.
	 * 
	 */
	public CatalogueSearchCommand(final Context cxt, final DataModel datamodel) {
		super(cxt, datamodel);

		final CatalogueSearchConfiguration conf = (CatalogueSearchConfiguration) cxt
				.getSearchConfiguration();
		LOG.debug(DEBUG_CONF_NFO + conf.getSearchBy() + ' '
				+ conf.getQueryParameterWhere());

		// hvis "where" parametern er sendt inn, så tar vi og leser inn query
		// fra den
		if (getSingleParameter("where") != null) {
			final ReconstructedQuery rq = createQuery(getSingleParameter("where"));

			GeoVisitor geo = new GeoVisitor();
			geo.visit(rq.getQuery().getRootClause());

			queryTwo = geo.getQueryRepresentation();
			LOG.info("Dette ble det: " + queryTwo);
		}

		if (getSingleParameter("userSortBy") != null
				&& getSingleParameter("userSortBy").length() > 0
				&& getSingleParameter("userSortBy").equals("name")) {
			userSortBy = "name";
		} else {
			userSortBy = "kw";
		}

		if ("exact".equals(conf.getSearchBy())) {
			userSortBy = "exact";
		}
	}

	/** TODO comment me. * */
	public SearchResult execute() {
		// kjør søk
		SearchResult result = super.execute();

		// konverter til denne.
		List<CatalogueSearchResultItem> nyResultListe = new ArrayList<CatalogueSearchResultItem>();

		// TODO: get all keys to lookup and execute one call instead of
		// iterating like this...
		Iterator iter = result.getResults().listIterator();

		while (iter.hasNext()) {
			BasicSearchResultItem basicResultItem = (BasicSearchResultItem) iter
					.next();

			CatalogueSearchResultItem resultItem = new CatalogueSearchResultItem();
			for (Object o : basicResultItem.getFieldNames()) {
				String s = (String) o;
				String v = basicResultItem.getField(s);
				resultItem.addField(s, v);
			}

			nyResultListe.add(resultItem);
		}

		// fjern de gamle BasicResultItems, og erstatt dem med nye
		// CatalogueResultItems.
		result.getResults().clear();
		result.getResults().addAll(nyResultListe);

		return result;
	}

	@Override
	public String getTransformedQuery() {
		String query = super.getTransformedQuery();

		if (queryTwo != null && queryTwo.length() > 0 && !query.equals("*")) {
			query += ") " + QL_AND + " (" + queryTwo + ")";
			query = "(" + query;
		} else if (query.equals("*")) {
			query = queryTwo;
		}

		return query;
	}



	@Override
	protected String getSortBy() {
		// hvis man søker etter firmanavn, sorterer vi etter "iyprpnavn"
		// ellers søker vi etter keywords, og da sorterer vi etter "iyprpkw"
		// istedet.
		String sortBy = "iyprpkw";
		if ("name".equalsIgnoreCase(userSortBy)
				|| "exact".equalsIgnoreCase(userSortBy)) {
			sortBy = "iyprpnavn";
		}
		return sortBy;
	}

	/**
	 * Query builder for creating a query syntax similar to sesam's own.
	 */
	private final class GeoVisitor extends AbstractReflectionVisitor {

		// AbstractReflectionVisitor overrides
		// ----------------------------------------------
		private final StringBuilder sb = new StringBuilder();

		/**
		 * Returns the generated query.
		 * 
		 * @return The query.
		 */
		String getQueryRepresentation() {
			return sb.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		protected void visitImpl(final LeafClause clause) {
			if (clause.getTerm() != null && clause.getTerm().length() > 0) {
				sb.append("iypcfgeo:" + clause.getTerm());
			}
		}

		protected void visitImpl(final AndClause clause) {
			clause.getFirstClause().accept(this);
			if (!(clause.getSecondClause() instanceof NotClause)) {
				sb.append(QL_AND);
			}
			clause.getSecondClause().accept(this);
		}

		/**
		 * {@inheritDoc}
		 */
		protected void visitImpl(final OrClause clause) {
			sb.append("(");
			clause.getFirstClause().accept(this);

			sb.append(QL_OR);

			clause.getSecondClause().accept(this);
			sb.append(")");
		}

		/**
		 * {@inheritDoc}
		 */
		protected void visitImpl(final DefaultOperatorClause clause) {
			clause.getFirstClause().accept(this);
			if (!(clause.getSecondClause() instanceof NotClause)) {
				sb.append(QL_AND);
			}
			clause.getSecondClause().accept(this);
		}

		/**
		 * {@inheritDoc}
		 */
		protected void visitImpl(final NotClause clause) {

			final String childsTerm = clause.getFirstClause().getTerm();
			if (childsTerm != null && childsTerm.length() > 0) {
				sb.append(QL_ANDNOT);
				clause.getFirstClause().accept(this);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		protected void visitImpl(final AndNotClause clause) {
			sb.append(QL_ANDNOT);
			clause.getFirstClause().accept(this);
		}
	}
}
