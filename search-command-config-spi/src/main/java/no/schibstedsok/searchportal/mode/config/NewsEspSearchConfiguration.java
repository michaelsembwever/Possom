package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

@Controller("NewsEspSearchCommand")
public class NewsEspSearchConfiguration extends NavigatableESPFastConfiguration {
    public static final String ALL_MEDIUMS = "all";
    private String mediumPrefix = "medium";
    private String defaultMedium = "webnewsarticle";
    private String mediumParameter = "medium";


    public NewsEspSearchConfiguration(final SearchConfiguration asc) {
        super(asc);
        if (asc instanceof NewsEspSearchConfiguration) {
            final NewsEspSearchConfiguration nesc = (NewsEspSearchConfiguration) asc;
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
