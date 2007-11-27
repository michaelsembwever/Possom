/* Copyright (2007) Schibsted Søk AS
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
package no.sesat.search.result.handler;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.Modifier;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to combine modifiers into new ones in a new navigator.
 *
 * @author Geir H. Pettersen
 * @version $Id$
 */
public class CombineModifiersResultHandler implements ResultHandler {
    private static final Logger LOG = Logger.getLogger(CombineModifiersResultHandler.class);
    private CombineModifiersResultHandlerConfig config;


    public CombineModifiersResultHandler(ResultHandlerConfig config) {
        this.config = (CombineModifiersResultHandlerConfig) config;
    }

    public void handleResult(Context cxt, DataModel datamodel) {
        // Checking parameters
        if (!(cxt.getSearchResult() instanceof FastSearchResult)) {
            LOG.debug(CombineModifiersResultHandler.class.getName() + " can only be applied to fast search results. Result is " + cxt.getSearchResult().getClass().getName());
            return;
        }

        // Initializing
        final FastSearchResult result = (FastSearchResult) cxt.getSearchResult();
        final List<Modifier> sourceModifierList = result.getModifiers(config.getSourceNavigatorName());
        final LinkedHashMap<String, Modifier> targetModifierMap = new LinkedHashMap<String, Modifier>();
        final Map<String, String> modifierMap = config.getModifierMap();
        Modifier defaultModifier = null;
        if (config.getDefaultModifierName() != null) {
            defaultModifier = new Modifier(config.getDefaultModifierName(), 0, null);
        }

        // The business
        if (sourceModifierList != null && sourceModifierList.size() > 0) {
            for (Modifier sourceModifier : sourceModifierList) {
                final String targetModifierName = modifierMap.get(sourceModifier.getName());
//                LOG.debug("Combining modifier: " + sourceModifier.getName() + "=" + sourceModifier.getCount() + " to " + targetModifierName);
                if (targetModifierName != null) {
                    Modifier targetModifier = targetModifierMap.get(targetModifierName);
                    if (targetModifier == null) {
                        targetModifier = new Modifier(targetModifierName, 0, null);
                        targetModifierMap.put(targetModifierName, targetModifier);
                    }
                    targetModifier.addCount(sourceModifier.getCount());
                } else if (defaultModifier != null) {
                    defaultModifier.addCount(sourceModifier.getCount());
                }
            }

            // Add the result
            if (config.getAllModifierName() != null) {
                result.addModifier(config.getTargetNavigatorName(), new Modifier(config.getAllModifierName(), result.getHitCount(), null));
            }
            for (Modifier modifier : targetModifierMap.values()) {
                result.addModifier(config.getTargetNavigatorName(), modifier);
            }
            if (defaultModifier != null) {
                result.addModifier(config.getTargetNavigatorName(), defaultModifier);
            }
        }
    }


}
