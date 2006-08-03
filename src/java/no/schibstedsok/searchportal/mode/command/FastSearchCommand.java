/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.searchportal.mode.command;

import java.util.Map;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
public class FastSearchCommand extends AbstractSimpleFastSearchCommand {

    private static final Logger LOG = Logger.getLogger(FastSearchCommand.class);

    /** Creates a new instance of FastSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public FastSearchCommand(final Context cxt, final Map parameters) {

        super(cxt, parameters);
    }

}
