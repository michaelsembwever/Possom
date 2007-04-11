// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;



import org.apache.log4j.Logger;

/**
 * Transforms the query into <br/> iypnavnvisningnorm:^"query"$ <br/> Ensures that only an
 * exact match within the titles field is returned.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Revision:$</tt>
 */
public final class CatalogueExactTitleMatchQueryTransformer extends AbstractQueryTransformer {

	private static final Logger LOG = Logger.getLogger(CatalogueExactTitleMatchQueryTransformer.class);

    /**
     *
     * @param config
     */
    public CatalogueExactTitleMatchQueryTransformer(final QueryTransformerConfig config){}

	@Override
	public String getTransformedQuery() {
            return "iypnavnvisningnorm:^\""+super.getTransformedQuery()+"\"$ OR "+
                   "iypnavnvisning:^\""+super.getTransformedQuery()+"\"$";
	}

}
