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
import no.schibstedsok.searchportal.query.transform.QueryTransformer.Context;
import no.schibstedsok.searchportal.query.transform.TokenMaskQueryTransformer.Mask;

import org.apache.log4j.Logger;

/**
 * 
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
public final class CatalogueEmptyQueryQueryTransformer extends AbstractQueryTransformer {

	private static final Logger LOG = Logger
			.getLogger(CatalogueEmptyQueryQueryTransformer.class);

	private static final String BLANK = "*";

	/** TODO comment me. * */
	protected void visitImpl(final LeafClause clause) {
		if (getContext().getQuery().isBlank()) {
			getContext().getTransformedTerms().put(clause, BLANK);
		}
	}
    

       
   private Map<Clause,String> getTransformedTerms() {
       return getContext().getTransformedTerms();
   }

}
