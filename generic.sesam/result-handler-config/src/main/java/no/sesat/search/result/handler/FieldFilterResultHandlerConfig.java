/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
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
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @version $Id$
 */
@Controller("FieldFilter")
public class FieldFilterResultHandlerConfig extends AbstractResultHandlerConfig {
    private String recursiveField;
    private String filterSrc;
    private String matchList;
    private String removeFields;
    private String srcPrefixes;
    private Set<String> matchListSet;
    private String[] removeFieldsArray;


    public String getRecursiveField() {
        return recursiveField;
    }

    public void setRecursiveField(String string) {
        recursiveField = string;
    }

    public String getFilterSrc() {
        return filterSrc;
    }

    public void setFilterSrc(String string) {
        filterSrc = string;
    }

    public Set<String> getMatchListSet() {
        if (matchListSet == null) {
            matchListSet = new HashSet<String>();
            final String[] mlArray = matchList.split(",");
            if (mlArray != null) {
                for (String s : mlArray) {
                    matchListSet.add(s.toLowerCase());
                    // Handle srcPrefixes
                    final String[] srcPref = srcPrefixes.split(",");
                    if (srcPref != null) {
                        for (String prefix : srcPref) {
                            matchListSet.add(prefix.toLowerCase() + s.toLowerCase());
                        }
                    }
                }
            }
        }
        return matchListSet;
    }

    public String[] getRemoveFieldsArray() {
        if (removeFieldsArray == null) {
            removeFieldsArray = removeFields.split(",");
        }
        return removeFieldsArray;
    }

    public String getMatchList() {
        return matchList;
    }

    public void setMatchList(String string) {
    	matchList = string;
    }

    public String getRemoveFields() {
        return removeFields;
    }

    public void setRemoveFields(String string) {
    	removeFields = string;
    }

    public String getSrcPrefixes() {
		return srcPrefixes;
	}

	public void setSrcPrefixes(String srcPrefixes) {
		this.srcPrefixes = srcPrefixes;
	}

	@Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        recursiveField = element.getAttribute("recursive-field");
        filterSrc = element.getAttribute("filter-src");
        matchList = element.getAttribute("match-list");
        removeFields = element.getAttribute("remove-fields");
        srcPrefixes = element.getAttribute("src-prefixes");
        return this;
    }
}
