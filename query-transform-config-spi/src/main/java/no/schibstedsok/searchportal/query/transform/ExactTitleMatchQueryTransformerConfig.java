// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;


/** Transforms the query into <br/>
 * titles:^"query"$
 * <br/>
 *   Ensures that only an exact match within the titles field is returned.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
@Controller("ExactTitleMatchQueryTransformer")
public final class ExactTitleMatchQueryTransformerConfig extends AbstractQueryTransformerConfig {}
