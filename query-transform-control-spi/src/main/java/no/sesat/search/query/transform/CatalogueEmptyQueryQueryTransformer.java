/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.query.transform;


import no.sesat.search.datamodel.generic.StringDataObjectSupport;
import no.sesat.search.query.LeafClause;


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
    }

}
