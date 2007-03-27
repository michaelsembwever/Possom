// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;

/**
 * Transforms the query into <br/> iypnavnvisningnorm:^"query"$ <br/> Ensures that only an
 * exact match within the titles field is returned.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Revision:$</tt>
 */
@Controller("CatalogueExactTitleMatchQueryTransformer")
public final class CatalogueExactTitleMatchQueryTransformerConfig extends AbstractQueryTransformerConfig {}
