/* Copyright (2009) Schibsted ASA
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
package no.sesat.search.mode.config;

import no.sesat.search.mode.config.CommandConfig.Controller;

/**
 * Search against google search API
 * http://code.google.com/apis/youtube/developers_guide_protocol.html#Searching_for_Videos
 */
@Controller("GoogleSearchCommand")
public class GoogleCommandConfig extends AbstractRestfulSearchConfiguration {

    private String path = "googleWebPath";

    public boolean getLargeResults() {
        return 4 < getResultsToReturn();
    }

    public String getPath(){
        return path;
    }

    public void setPath(final String path){
        this.path = path;
    }

}
