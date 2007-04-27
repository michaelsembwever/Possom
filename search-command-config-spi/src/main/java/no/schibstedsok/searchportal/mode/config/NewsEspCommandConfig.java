package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;

@Controller("NewsEspSearchCommand")
public class NewsEspCommandConfig extends NavigatableEspFastCommandConfig {
    public static final String ALL_MEDIUMS = "all";
    private String mediumPrefix = "medium";
    private String defaultMedium = "webnewsarticle";
    private String mediumParameter = "medium";

    public NewsEspCommandConfig(){}

    public NewsEspCommandConfig(final SearchConfiguration asc) {

        if (asc instanceof NewsEspCommandConfig) {
            final NewsEspCommandConfig nesc = (NewsEspCommandConfig) asc;
            mediumPrefix = nesc.getMediumPrefix();
            defaultMedium = nesc.getDefaultMedium();
            mediumParameter = nesc.getMediumParameter();
        }
    }

    public String getMediumPrefix() {
        return mediumPrefix;
    }

    public void setMediumPrefix(String mediumPrefix) {
        this.mediumPrefix = mediumPrefix;
    }

    public String getDefaultMedium() {
        return defaultMedium;
    }

    public void setDefaultMedium(String defaultMedium) {
        this.defaultMedium = defaultMedium;
    }

    public String getMediumParameter() {
        return mediumParameter;
    }

    public void setMediumParameter(String mediumParameter) {
        this.mediumParameter = mediumParameter;
    }
}
