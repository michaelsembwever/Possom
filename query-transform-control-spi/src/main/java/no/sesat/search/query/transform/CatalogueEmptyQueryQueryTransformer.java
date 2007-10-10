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
