/* Copyright (2007) Schibsted ASA
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
/*
 * StaticCommandConfig.java
 *
 * Created on May 18, 2006, 10:50 AM
 *
 */

package no.sesat.search.mode.config;

import no.sesat.search.mode.config.CommandConfig.Controller;

/**
 * Configuration for "static" search commands. That is, commands that do not
 * need to do a search but produces static HTML.
 *
 *
 * @version $Id$
 */
@Controller("StaticSearchCommand")
public class StaticCommandConfig extends CommandConfig {}
