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

	private transient boolean writtenStart = false;

	private transient Boolean visitingLast = null;

	private transient StringBuffer sb = new StringBuffer();

	/**
	 * 
	 * @param clause
	 *            The clause to prefix.
	 */
	public void visitImpl(final LeafClause clause) {

		if (!writtenStart) {

			sb = new StringBuffer();
			sb.append("iypnavnvisningnorm:\""
					+ getTransformedTerms().get(clause));

			writtenStart = true;
			// also, if we got here without giving visitingLast a value then
			// this is the only LeafClause in the query
			visitingLast = null == visitingLast;

			getTransformedTerms().put(clause, "");
		}

		if (!visitingLast && !visitingLast)
			sb.append(getTransformedTerms().get(clause));

		if (visitingLast) {
			sb.append(" " + getTransformedTerms().get(clause) + "\"");
			getTransformedTerms().put(clause, sb.toString().trim());
		}

	}

	private Map<Clause, String> getTransformedTerms() {
		return getContext().getTransformedTerms();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void visitImpl(final DefaultOperatorClause clause) {
		// remember what visitingLast was
		final Boolean original = visitingLast;
		// turn it off. left child can never be the last term in the query.
		visitingLast = false;
		clause.getFirstClause().accept(this);
		// restore visitingLast.
		visitingLast = original;
		if (null == visitingLast) {
			// if it is yet to be assigned an value (ie this is the topmost
			// DoubleOperatorClause) then assign true.
			visitingLast = true;
		}
		clause.getSecondClause().accept(this);
	}
}
