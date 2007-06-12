package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to combine modifiers into new ones in a new navigator.
 *
 * @author Geir H. Pettersen
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
                LOG.debug("Combining modifier: " + sourceModifier.getName() + "=" + sourceModifier.getCount());
                final String targetModifierName = modifierMap.get(sourceModifier.getName());
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
