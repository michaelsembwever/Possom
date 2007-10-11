/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.query.transform;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;

/**
 * Transforms the query into <br/> iypnavnvisningnorm:^"query"$ <br/> Ensures that only an
 * exact match within the titles field is returned.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Revision:$</tt>
 */
@Controller("CatalogueExactTitleMatchQueryTransformer")
public final class CatalogueExactTitleMatchQueryTransformerConfig extends AbstractQueryTransformerConfig {}
