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
public final class CatalogueQueryTransformer extends AbstractQueryTransformer {

	private static final Logger LOG = Logger
			.getLogger(CatalogueQueryTransformer.class);

	/** TODO comment me. * */
	protected void visitImpl(final LeafClause clause) {
		if (!getTransformedTerms().get(clause).equals("*")) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			sb.append("iypcfphnavn:" + getTransformedTerms().get(clause)
					+ " ANY ");
			sb.append("lemiypcfkeywords:" + getTransformedTerms().get(clause)
					+ " ANY ");
			sb.append("lemiypcfkeywordslow:"
					+ getTransformedTerms().get(clause));
			sb.append(")");
			getContext().getTransformedTerms().put(clause, sb.toString());
		}
	}

	/**
	 * Legg til iypcfnavn forran alle ord.
	 * 
	 */
	protected void visitImpl(final PhraseClause clause) {

		if (!getTransformedTerms().get(clause).equals("*")) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			sb.append("iypcfnavn:" + getTransformedTerms().get(clause)
					+ " ANY ");
			sb.append("lemiypcfkeywords:" + getTransformedTerms().get(clause)
					+ " ANY ");
			sb.append("lemiypcfkeywordslow:"
					+ getTransformedTerms().get(clause));
			sb.append(")");
			getContext().getTransformedTerms().put(clause, sb.toString());
		}
	}

	private Map<Clause, String> getTransformedTerms() {
		return getContext().getTransformedTerms();
	}

	protected void visitImpl(final AndClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void visitImpl(final OrClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void visitImpl(final DefaultOperatorClause clause) {
		clause.getFirstClause().accept(this);
		clause.getSecondClause().accept(this);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void visitImpl(final NotClause clause) {

		final String childsTerm = clause.getFirstClause().getTerm();
		if (childsTerm != null && childsTerm.length() > 0) {
			clause.getFirstClause().accept(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void visitImpl(final AndNotClause clause) {
		clause.getFirstClause().accept(this);
	}
}
