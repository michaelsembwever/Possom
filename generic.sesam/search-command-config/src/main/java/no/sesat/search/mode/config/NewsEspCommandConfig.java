/* Copyright (2007-2008) Schibsted Søk AS
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
 * Configuration for a NewsEspSearchCommand.
 *
 * @version $Id$
 */
@Controller("NewsEspSearchCommand")
public class NewsEspCommandConfig extends NavigatableEspFastCommandConfig {

    private static final long serialVersionUID = -8717500908531092289L;

    /** Constant for medium value representing all mediums. */
    public static final String ALL_MEDIUMS = "all";

    private String mediumPrefix = "medium";
    private String defaultMedium = "webnewsarticle";
    private String mediumParameter = "medium";

    private String nestedResultsField = "entries";;
    private int collapsingMaxFetch = 10;

    /** Name of the sort parameter used in the url. */
    private String userSortParameter = "sort";
    /** Sort field used when sort is not relevance. */
    private String sortField = "publishedtime";
    /** Default sort direction if no sort is configured. */
    private String defaultSort = "descending";
    /** Sort field to use when relevance sort is used. */
    private String relevanceSortField = "freshnessprofile";
    /** Sort field that can override <code>relevanceSortField</code> in a single term search. */
    private String relevanceSingleTermSortField;
    /** Sort field that can override <code>relevanceSortField</code> in a multiple term search. */
    private String relevanceMultipleTermSortField;

    private String maxAge;

    private int maxAgeAmount;
    private String ageField = "publishedtime";
    private char ageSymbol;

    /** Getter for the userSortParameter property.
     * @return the userSortParameter value
     */
    public String getUserSortParameter() {
        return userSortParameter;
    }

    /** Setter for the userSortParameter property.
     * @param userSortParameter the new userSortParameter value
     */
    public void setUserSortParameter(final String userSortParameter) {
        this.userSortParameter = userSortParameter;
    }

    /** Getter for the sortField property.
     * @return the sortField value
     */
    public String getSortField() {
        return sortField;
    }

    /** Setter for the sortField property.
     * @param sortField the new sortField value
     */
    public void setSortField(final String sortField) {
        this.sortField = sortField;
    }


    /** Getter for the defaultSort property.
     * @return the defaultSort value
     */
    public String getDefaultSort() {
        return defaultSort;
    }

    /** Setter for the defaultSort property.
     * @param defaultSort the new defaultSort value
     */
    public void setDefaultSort(final String defaultSort) {
        this.defaultSort = defaultSort;
    }

    /** Getter for the collapsingMaxFetch property.
     * @return the collapsingMaxFetch value
     */
    public int getCollapsingMaxFetch() {
        return collapsingMaxFetch;
    }

    /** Setter for the collapsingMaxFetch property.
     * @param collapsingMaxFetch the new collapsingMaxFetch value
     */
    public void setCollapsingMaxFetch(final int collapsingMaxFetch) {
        this.collapsingMaxFetch = collapsingMaxFetch;
    }

    /** Getter for the nestedResultsField property.
     * @return the nestedResultsField value
     */
    public String getNestedResultsField() {
        return nestedResultsField;
    }

    /** Setter for the nestedResultsField property.
     * @param nestedResultsField the new nestedResultsField value
     */
    public void setNestedResultsField(final String nestedResultsField) {
        this.nestedResultsField = nestedResultsField;
    }

    /** Getter for the mediumPrefix property.
     * @return the mediumPrefix value
     */
    public String getMediumPrefix() {
        return mediumPrefix;
    }

    /** Setter for the mediumPrefix property.
     * @param mediumPrefix the new mediumPrefix value
     */
    public void setMediumPrefix(final String mediumPrefix) {
        this.mediumPrefix = mediumPrefix;
    }

    /** Getter for the defaultMedium property.
     * @return the defaultMedium value
     */
    public String getDefaultMedium() {
        return defaultMedium;
    }

    /** Setter for the defaultMedium property.
     * @param defaultMedium the new defaultMedium value
     */
    public void setDefaultMedium(final String defaultMedium) {
        this.defaultMedium = defaultMedium;
    }

    /** Getter for the mediumParameter property.
     * @return the mediumParameter value
     */
    public String getMediumParameter() {
        return mediumParameter;
    }

    /** Setter for the mediumParameter property.
     * @param mediumParameter the new mediumParameter value
     */
    public void setMediumParameter(final String mediumParameter) {
        this.mediumParameter = mediumParameter;
    }

    /** Getter for the relevanceSortField property.
     * @return the relevanceSortField value
     */
    public String getRelevanceSortField() {
        return relevanceSortField;
    }

    /** Setter for the relevanceSortField property.
     * @param relevanceSortField the new relevanceSortField value
     */
    public void setRelevanceSortField(final String relevanceSortField) {
        this.relevanceSortField = relevanceSortField;
    }

    /** Getter for the relevanceSingleTermSortField property.
     * @return the relevanceSortSingleTermField value
     */
    public String getRelevanceSingleTermSortField() {
        return relevanceSingleTermSortField;
    }

    /** Setter for the relevanceSingleTermSortField property.
     * @param relevanceSingleTermSortField the new relevanceSingleTermSortField value
     */
    public void setRelevanceSingleTermSortField(final String relevanceSingleTermSortField) {
        this.relevanceSingleTermSortField = relevanceSingleTermSortField;
    }

    /** Getter for the relevanceMultipleTermSortField property.
     * @return the relevanceMultipleTermSortField value
     */
    public String getRelevanceMultipleTermSortField() {
        return relevanceMultipleTermSortField;
    }

    /** Setter for the relevanceMultipleTermSortField property.
     * @param relevanceMultipleTermSortField the new relevanceMultipleTermSortField value
     */
    public void setRelevanceMultipleTermSortField(final String relevanceMultipleTermSortField) {
        this.relevanceMultipleTermSortField = relevanceMultipleTermSortField;
    }

    /** Getter for the maxAge property.
     * @return the maxAge value
     */
    public String getMaxAge() {
        return maxAge;
    }

    /** Setter for the maxAge property.
     * @param maxAge the new maxAge value
     */
    public void setMaxAge(final String maxAge) {
        this.maxAge = maxAge;
    }

    /** Getter for the ageField property.
     * @return the ageField value
     */
    public String getAgeField() {
        return ageField;
    }

    /** Setter for the ageField property.
     * @param ageField the new ageField value
     */
    public void setAgeField(final String ageField) {
        this.ageField = ageField;
    }

    /** Getter for the ageSymbol property.
     * @return the ageSymbol value
     */
    public char getAgeSymbol() {
        return ageSymbol;
    }

    /** Setter for the ageSymbol property.
     * @param ageSymbol the new ageSymbol value
     */
    public void setAgeSymbol(final char ageSymbol) {
        this.ageSymbol = ageSymbol;
    }

    /** Getter for the maxAgeAmount property.
     * @return the maxAgeAmount value
     */
    public int getMaxAgeAmount() {
        return maxAgeAmount;
    }

    @Override
    public FastCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit,
            final Context context) {

        super.readSearchConfiguration(element, inherit, context);

        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "mediumPrefix", ParseType.String, element, "medium");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "defaultMedium", ParseType.String, element, "webnewsarticle");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "mediumParameter", ParseType.String, element, "medium");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "nestedResultsField", ParseType.String, element, "entries");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "collapsingMaxFetch", ParseType.Int, element, "10");

        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "sortField", ParseType.String, element, "publishedtime");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "defaultSort", ParseType.String, element, "descending");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "userSortParameter", ParseType.String, element, "sort");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "relevanceSortField", ParseType.String, element, "freshnessprofile");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "relevanceSingleTermSortField", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "relevanceMultipleTermSortField", ParseType.String, element, "");

        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "maxAge", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(
                this, inherit, "ageField", ParseType.String, element, "publishedtime");

        // maxAge is of the format 10h (for 10 hours).
        if (maxAge != null) {
            ageSymbol = maxAge.charAt(maxAge.length() - 1);
            maxAgeAmount = Integer.parseInt(maxAge.substring(0, maxAge.length() - 1));
        }

        return this;
    }

    protected void readSearchConfigurationAfter(Element element, SearchConfiguration inherit) {
        // maxAge is of the format 10h (for 10 hours).
        if (maxAge != null) {
            ageSymbol = maxAge.charAt(maxAge.length() - 1);
            maxAgeAmount = Integer.parseInt(maxAge.substring(0, maxAge.length() - 1));
        }

        super.readSearchConfigurationAfter(element, inherit);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
