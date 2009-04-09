/* Copyright (2007-2008) Schibsted ASA
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

import no.sesat.search.mode.config.*;
import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/** Configuration for a NewsClusteringESPFastCommand.
 *
 * @todo rename to NewsClusteringEspFastCommandConfig as it is News specific
 *
 *
 * @version $Id$
 */
@Controller("NewsClusteringESPFastCommand")
public class ClusteringEspFastCommandConfig extends NewsEspCommandConfig {

    private String clusterIdParameter = "clusterId";
    private int resultsPerCluster;
    private String clusterField = "cluster";
    private boolean clusteringDisabled = false;

    /** Getter for clusterIdParameter property.
     * @return the clusterIdParameter value
     */
    public String getClusterIdParameter() {
        return clusterIdParameter;
    }

    /** Setter for the clusterIdParameter.
     * @param clusterIdParameter the new clusterIdParameter value
     */
    public void setClusterIdParameter(final String clusterIdParameter) {
        this.clusterIdParameter = clusterIdParameter;
    }

    /** Getter for the clusterDisabled property.
     * @return the clusterDisabled value
     */
    public boolean isClusteringDisabled() {
        return clusteringDisabled;
    }

    /** Setter for the clusterDisabled property.
     * @return the clusterDisabled value
     */
    public void setClusteringDisabled(final boolean clusteringDisabled) {
        this.clusteringDisabled = clusteringDisabled;
    }

    /** Setter for the resultsPerCluster property.
     * @param resultsPerCluster the new resultsPerCluster value
     */
    public void setResultsPerCluster(final int resultsPerCluster) {
        this.resultsPerCluster = resultsPerCluster;
    }

    /** Getter for the resultsPerCluster property.
     * @return the resultsPerCluster value
     */
    public int getResultsPerCluster() {
        return resultsPerCluster;
    }

    /** Setter for the clusterField property.
     * @return the clusterField value
     */
    public String getClusterField() {
        return clusterField;
    }

    /** Setter for the clusterField proeprty.
     * @param clusterField the new clusterField value
     */
    public void setClusterField(final String clusterField) {
        this.clusterField = clusterField;
    }
}
