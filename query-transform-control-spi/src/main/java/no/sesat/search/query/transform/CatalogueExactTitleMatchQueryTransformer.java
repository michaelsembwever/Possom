/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.query.transform;



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
