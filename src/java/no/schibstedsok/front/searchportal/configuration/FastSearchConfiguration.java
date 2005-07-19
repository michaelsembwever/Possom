/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.util.SearchConfiguration;

/**
 * A FastSearchConfiguration.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public interface FastSearchConfiguration extends SearchConfiguration {

	/**
	 * 
	 * Create a +meta.collection filter based on which collection we are looking
	 * at.
	 * 
	 * @param targetCollection
	 * @return
	 */
	public String constructCollectionFilter();

	public String getQRServerURL();

	public void setQRServerURL(String serverURL);

	public String getCollection();

	public void setCollection(String collection);

	public String getNavigatorString();

	public void setNavigatorString(String navigatorString);

	public int getDocsToReturn();

	public void setDocsToReturn(int docsToReturn);

	public long getMaxTime();

	public void setMaxTime(long maxTime);

	public int getOffSet();

	public void setOffSet(int offSet);

	public boolean isSpellcheck();
	public void setSpellcheck(boolean spellcheck);


}
