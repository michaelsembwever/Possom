// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import java.util.Map;

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
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.WordClauseImpl;

import org.apache.log4j.Logger;

/**
 * Transforms the query into <br/> titles:^"query"$ <br/> Ensures that only an
 * exact match within the titles field is returned.
 * 
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
public final class CatalogueExactTitleMatchQueryTransformer extends
		AbstractQueryTransformer {

	private static final Logger LOG = Logger
			.getLogger(CatalogueExactTitleMatchQueryTransformer.class);

	@Override
	public String getTransformedQuery() {
		return "iypnavnvisningnorm:^\""+super.getTransformedQuery()+"\"$";
	}
	
}
