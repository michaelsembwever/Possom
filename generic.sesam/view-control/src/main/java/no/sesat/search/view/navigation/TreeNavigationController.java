/**
 * Copyright (2008) Schibsted Søk AS
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
 *
 */
package no.sesat.search.view.navigation;

import no.sesat.search.result.NavigationItem;
import no.sesat.search.result.BasicNavigationItem;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;

import java.util.*;

import org.apache.log4j.Logger;

public class TreeNavigationController implements NavigationController {
    private final TreeNavigationConfig conf;

    /**
     *  Factor class for the TreeNavigationController
     */
    public static class Factory implements NavigationControllerFactory<TreeNavigationConfig> {
        public NavigationController get(final TreeNavigationConfig nav) {
            return new TreeNavigationController(nav);
        }
    }

    private TreeNavigationController(final TreeNavigationConfig nav) {
        this.conf = nav;
    }

    /**
     * Go through configuration defined in views.xml (the tree navigation element),
     * and build up a NavigationItem as result.
     *
     * @param context
     * @return the navigationItem coresponding to the tree structure defined in
     * views.xml.
     */
    public NavigationItem getNavigationItems(final Context context) {
        if (conf.getParent() instanceof TreeNavigationConfig) {
            return null;
        }

        final NavigationItem item = new BasicNavigationItem();

        final String url = context.getUrlGenerator().getURL(null, conf);

        final BasicNavigationItem i = new BasicNavigationItem(conf.getName(), url, -1);
        i.setSelected(true);
        item.addResult(i);

        getNavigationItemsHelper(context, i, conf.getChildNavs(), 1);

        return item;
    }

    private void getNavigationItemsHelper(final Context context, NavigationItem item, List<NavigationConfig.Nav> children, int depth) {

        if (children.isEmpty()) {
            return;
        }

        for (NavigationConfig.Nav child : children) {

            if(child instanceof TreeNavigationConfig) {
                final String url = context.getUrlGenerator().getURL(((TreeNavigationConfig)child).getValue(), child);
                final BasicNavigationItem i = new BasicNavigationItem(((TreeNavigationConfig)child).getName(), url, -1);
                i.setDepth(depth);
                final StringDataObject selectedValue = context.getDataModel().getParameters().getValue(child.getField());

                if (selectedValue != null && selectedValue.getString().equals(((TreeNavigationConfig)child).getValue())) {
                    i.setSelected(true);
                }

                item.addResult(i);

                // If an item is selected all other items on the same navigation level are excluded.
                if (i.isSelected()) {
                    StringDataObject selectedSubItem = context.getDataModel().getParameters().getValue(child.getField());
                    if (selectedSubItem != null && ((TreeNavigationConfig)child).getValue().equals(selectedSubItem.getString())) {
                        getNavigationItemsHelper(context, i, child.getChildNavs(), depth +1);
                    }
                }
            }
        }
    }
}