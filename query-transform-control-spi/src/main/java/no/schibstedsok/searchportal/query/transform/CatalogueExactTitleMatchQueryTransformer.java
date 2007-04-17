// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;



import org.apache.log4j.Logger;

/**
 * Transforms the query into <br/> iypnavnvisningnorm:^"query"$ <br/> 
 * Ensures that only an
 * exact match within the titles field is returned.
 * 
 * @deprecated Use ExactMatchQueryTransformer instead.
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
        
            // get the search query as entered by the user, remove " characters
            // and use it to match against company name fields in the index.
            final String query = super.getTransformedQuery().replace("\"", "");
            
            return "iypnavnvisningnorm:^\""+query+"\"$ OR "+
                   "iypnavnvisning:^\""+query+"\"$";
	}

}
