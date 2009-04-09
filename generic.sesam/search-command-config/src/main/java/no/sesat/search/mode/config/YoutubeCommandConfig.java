/* Copyright (2008) Schibsted ASA
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

import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * Search against googles youtube API
 * http://code.google.com/apis/youtube/developers_guide_protocol.html#Searching_for_Videos
 */
@Controller("YoutubeSearchCommand")
public class YoutubeCommandConfig extends AbstractXmlSearchConfiguration {

    private String format;
    private String racy;
    private String sortBy;

    public void setFormat(String format) {
        this.format = format;
    }
    public String getFormat() {
        return format;
    }

    public void setRacy(String racy) {
        this.racy = racy;
    }
    public String getRacy() {
        return racy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    public String getSortBy() {
        return sortBy;
    }
}
