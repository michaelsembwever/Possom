// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;


/**
 * Add iypcompanyid to the front of the original query, which is a companyid.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version $Revision:$
 */
@Controller("CatalogueInfopageQueryTransformerConfig")
public final class CatalogueInfopageQueryTransformerConfig extends AbstractQueryTransformerConfig {
}
