// Copyright (2007) Schibsted Søk AS
/*
 * StaticCommandConfig.java
 *
 * Created on May 18, 2006, 10:50 AM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;

/**
 * Configuration for "static" search commands. That is, commands that do not
 * need to do a search but produces static HTML.
 *
 * @author maek
 * @version $Id$
 */
@Controller("StaticSearchCommand")
public class StaticCommandConfig extends CommandConfig {}
