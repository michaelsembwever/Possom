/* Copyright (2006-2007) Schibsted Søk AS
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
package no.sesat.search.result;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class Enrichment implements Comparable<Enrichment> {

    private float analysisResult;
    private String name;

    public Enrichment(final float analysisResult, final String name) {
        this.analysisResult = analysisResult;
        this.name = name;
    }

    public int getAnalysisResult() {
        return (int)analysisResult;
    }

    public void setAnalysisResult(final float analysisResult) {
        this.analysisResult = analysisResult;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int compareTo(final Enrichment e) {
        return (int)(e.analysisResult - analysisResult);
    }
}
