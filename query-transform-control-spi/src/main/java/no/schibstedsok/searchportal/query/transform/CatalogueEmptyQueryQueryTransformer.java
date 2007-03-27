// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;


import no.schibstedsok.searchportal.query.LeafClause;


import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Id: 3359 $</tt>
 */
public final class CatalogueEmptyQueryQueryTransformer extends AbstractQueryTransformer {

	private static final Logger LOG = Logger.getLogger(CatalogueEmptyQueryQueryTransformer.class);

	private static final String BLANK = "*";

    /**
     *
     * @param config
     */
    public CatalogueEmptyQueryQueryTransformer(final QueryTransformerConfig config){}

	/** TODO comment me. *
  * @param clause
  */
	protected void visitImpl(final LeafClause clause) {
		if (getContext().getQuery().isBlank()) {
			getContext().getTransformedTerms().put(clause, BLANK);
		}
	}

}
