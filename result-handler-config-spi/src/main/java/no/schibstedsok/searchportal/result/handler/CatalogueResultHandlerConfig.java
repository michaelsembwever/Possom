// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;

/**
 * Resulthandler to fetch sales from the catalogue sales system. This class is
 * called after a search command with <catalogue/> resulthandler tag is defined.
 * It calls a ejb3 stateless session bean in the sales system which load all
 * product information to be presented in the infopage for katalog.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version <tt>$Revision: 3436 $</tt>
 */
@Controller("CatalogueResultHandler")
public final class CatalogueResultHandlerConfig extends AbstractResultHandlerConfig {}
