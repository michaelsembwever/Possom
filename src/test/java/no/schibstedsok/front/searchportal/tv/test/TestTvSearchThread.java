package no.schibstedsok.front.searchportal.tv.test;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;
import no.schibstedsok.common.persistence.dal.service.GlobalSessionFactory;
import no.schibstedsok.common.persistence.dal.service.ThreadSessionHolder;
import no.schibstedsok.front.searchportal.configuration.BaseSearchConfiguration;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.filters.tv.TvSearchConsumer;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.VelocityTemplates;

import org.hibernate.cfg.Configuration;

public class TestTvSearchThread extends TestCase {

	String query = "Sommer";

	long maxTime = 2000;

	int maxResults = 10;

	int offset = 0;

	SearchConfiguration config;

	StringWriter w = new StringWriter();

	Writer writer = new BufferedWriter(w);

	protected void setUp() throws Exception {
		super.setUp();
		
		Configuration cfg = new Configuration();
        cfg.addClass(no.schibstedsok.tv.persistence.Program.class);
        cfg.addClass(no.schibstedsok.tv.persistence.Category.class);
        cfg.addClass(no.schibstedsok.tv.persistence.Channel.class);

		ThreadSessionHolder.set(GlobalSessionFactory.getHSQLInstance().newSession(cfg));

		config = new BaseSearchConfiguration();
		config.setLanguage("en");
		config.setQuery(query);
		config.setTemplate(VelocityTemplates.TV_SEARCH);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		ThreadSessionHolder.invalidateSession();
	}

	public void testTVSearchThread() throws Exception {

		SearchConsumer consumer = new TvSearchConsumer(writer, config);
		consumer.run();
		writer.flush();
		System.out.println(w);
		assertNotSame("", w.toString());
	}

}
