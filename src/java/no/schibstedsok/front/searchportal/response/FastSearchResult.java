/**
 * 
 */
package no.schibstedsok.front.searchportal.response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import no.fast.ds.search.IDocumentSummary;
import no.fast.ds.search.IDocumentSummaryField;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 * 
 * A verbose FAST response version. Not all fileds in use, add elements you need
 * by setting them in the constructor.
 * 
 * @author Lars Johansson
 * 
 */
public class FastSearchResult extends SearchResultElement {

	// Spelling suggestion if availbable
	private String spellingSuggestion = null;

	private String companies; // associated companies

	private String locations; // geographic associations

	private String personnames; // person name associations (TODO: unsure about
								// the difference)

	private String lastnames; // last-name assocations

	private String firstnames; // first-name associations

	private String fullnames; // full name associations

	private String topics; // topic associations

	private String emails; // email associations

	private String taxonomy; // taxonomy path

	private String poststeder; // post areas

	private String fornavn; // single name

	private String etternavn; // single lastname

	private String coadresse; // care of address

	private String adresse; // address

	private String postnr; // postal number

	private String poststed; // postal code

	private String allinfo; // all info (TODO: unsure about this one)

	private String ocfscore; // ocf score (TODO: unsure about this one)

	private String spamscore; // spam score (0-10)

	private String contentsource; // source

	private String collapseid; // collapsed id

	private String suggestedQuery; // suggested query from result

	private String newsSource; // media news-source
	
	private String docDateTime; //document creation

	/** Logger for this class. */
	private Logger log = Logger.getLogger(this.getClass());

	public FastSearchResult(IDocumentSummary summary) {

		// populate standard fields
		setSummary(summary.getSummaryField("body").getSummary());
		if (summary.getSummaryField("title").getSummary() != null)
			setTitle(summary.getSummaryField("title").getSummary());
		else {
			int length = summary.getSummaryField("url").getSummary().length();
			if (length > 50)
				setTitle(summary.getSummaryField("url").getSummary().substring(0, 50) + "...");
			else
				setTitle(summary.getSummaryField("url").getSummary());
		}
		setUrl(summary.getSummaryField("url").getSummary());
		setClickUrl(summary.getSummaryField("contentid").getSummary());
		
		
		//for media results
		if(summary.getSummaryField("newssource") != null)
			setNewsSource(summary.getSummaryField("newssource").getSummary());
		if(summary.getSummaryField("docdatetime") != null)
			setDocDateTime(summary.getSummaryField("docdatetime").getSummary());
		/**
		 * populate al corresponing fields by introspection
		 * 
		 */

		/*
		 * NOT IN USE // Populate all fields using reflection for (Iterator iter =
		 * summary.summaryFields(); iter.hasNext();) { IDocumentSummaryField
		 * element = (IDocumentSummaryField) iter.next(); set(element.getName(),
		 * element); }
		 */

	}

	private void set(String name, IDocumentSummaryField summary) {
		char c = Character.toUpperCase(name.charAt(0));
		Class[] params = { String.class };
		try {
			Method setMethod = getClass().getMethod(
					"set" + c + name.substring(1), params);
			Object[] args = new Object[] { summary.getSummary() };
			setMethod.invoke(this, args);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("directoryCategory",
				this.getDirectoryCategory()).append("title", this.getTitle())
				.append("summary", this.getSummary()).append(
						"relatedInformationPresent",
						this.isRelatedInformationPresent()).append("clickUrl",
						this.getClickUrl()).append("url", this.getUrl())
				.append("\n").toString();
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getAllinfo() {
		return allinfo;
	}

	public void setAllinfo(String allinfo) {
		this.allinfo = allinfo;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getCoadresse() {
		return coadresse;
	}

	public void setCoadresse(String coadresse) {
		this.coadresse = coadresse;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getCollapseid() {
		return collapseid;
	}

	public void setCollapseid(String collapseid) {
		this.collapseid = collapseid;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getCompanies() {
		return companies;
	}

	public void setCompanies(String companies) {
		this.companies = companies;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getContentsource() {
		return contentsource;
	}

	public void setContentsource(String contentsource) {
		this.contentsource = contentsource;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getEtternavn() {
		return etternavn;
	}

	public void setEtternavn(String etternavn) {
		this.etternavn = etternavn;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getFirstnames() {
		return firstnames;
	}

	public void setFirstnames(String firstnames) {
		this.firstnames = firstnames;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getFornavn() {
		return fornavn;
	}

	public void setFornavn(String fornavn) {
		this.fornavn = fornavn;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getFullnames() {
		return fullnames;
	}

	public void setFullnames(String fullnames) {
		this.fullnames = fullnames;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getLastnames() {
		return lastnames;
	}

	public void setLastnames(String lastnames) {
		this.lastnames = lastnames;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getLocations() {
		return locations;
	}

	public void setLocations(String locations) {
		this.locations = locations;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getOcfscore() {
		return ocfscore;
	}

	public void setOcfscore(String ocfscore) {
		this.ocfscore = ocfscore;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getPersonnames() {
		return personnames;
	}

	public void setPersonnames(String personnames) {
		this.personnames = personnames;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getPostnr() {
		return postnr;
	}

	public void setPostnr(String postnr) {
		this.postnr = postnr;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getPoststed() {
		return poststed;
	}

	public void setPoststed(String poststed) {
		this.poststed = poststed;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getPoststeder() {
		return poststeder;
	}

	public void setPoststeder(String poststeder) {
		this.poststeder = poststeder;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getSpamscore() {
		return spamscore;
	}

	public void setSpamscore(String spamscore) {
		this.spamscore = spamscore;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getSuggestedQuery() {
		return suggestedQuery;
	}

	public void setSuggestedQuery(String suggestedQuery) {
		this.suggestedQuery = suggestedQuery;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(String taxonomy) {
		this.taxonomy = taxonomy;
	}

	/**
	 * 
	 * Not in use for now
	 * 
	 * @deprecated Not implemented until needed
	 * @return the coresponding field from FAST response
	 */
	public String getTopics() {
		return topics;
	}

	public void setTopics(String topics) {
		this.topics = topics;
	}

	/**
	 * 
	 * 
	 * Use the getSpellingSuggestions() in the
	 * <code>FastSearchResponseImpl</code> to retrieve a list over available
	 * spelling-suggestions.
	 * 
	 * @deprecated Use getSpellingSuggestions() in
	 *             <code>FastSearchResponseImpl</code>
	 * @return the coresponding field from FAST response
	 */
	public String getSpellingSuggestion() {
		return spellingSuggestion;
	}

	public void setSpellingSuggestion(String spellingSuggestion) {
		this.spellingSuggestion = spellingSuggestion;
	}

	/**
	 * 
	 * From what source the content (Media) is originating from.
	 * 
	 * @return String source
	 */
	public String getNewsSource() {
		return newsSource;
	}
	

	public void setNewsSource(String newsSource) {
		this.newsSource = newsSource;
	}

	/**
	 * 
	 *  Return the complete docDateTime String
	 * 
	 * @return docDatetime
	 */
	public String getDocDateTime() {
		return docDateTime;
	}
	

	public void setDocDateTime(String docDateTime) {
		this.docDateTime = docDateTime;
	}
	
	/**
	 * 
	 * Return the specific age for field "days","hours" or "minutes"
	 * 
	 * @param field
	 * @return age as int
	 */
	private int getDocumentAge(String field){
		if(getDocDateTime() != null){
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	//FAST FORMAT 2005-06-13T10:46:57Z
			try {
				String docDateTime = getDocDateTime().replace('T', ' ').replace('Z',' ').trim();
//				System.out.println(docDateTime);
				long ageInMillis = System.currentTimeMillis() - df.parse(docDateTime).getTime();
				return calculateAge(ageInMillis, field);
			} catch (ParseException e) {
				log.error("Error: Unable to parse FAST docdatetime: " + getDocDateTime(), e);
			}
		}
		return 0;
	}

	
	/** 
	 * Return the document age in number of days
	 * 
	 * @return int days
	 */
	public int getDocumentAgeDays() {
		return getDocumentAge("days");
	}

	/** 
	 * Return the document age in number of hours
	 * 
	 * @return int hours
	 */
	public int getDocumentAgeHours() {
		return getDocumentAge("hours");
	}

	/** 
	 * Return the document age in number of minutes
	 * 
	 * @return int minutes
	 */
	public int getDocumentAgeMinutes() {
		return getDocumentAge("minutes");
	}

    /**
     * 
     * Does a long to hour, minute or day conversion.
     * 
     * @param time
     * @return
     */
    private int calculateAge(long millisec, String field) {
        
        if (millisec == 0)
            return 0;
        
        if(field.equals("days"))
			return (int)((millisec/3600000)/24);
		if(field.equals("hours"))
        	return (int)((millisec/3600000) % 24);
        if(field.equals("minutes"))
			return (int)((millisec/60000) % 60);
		
		return 0;
     }

	/**
	 * 
	 * Compare results by docDate time (used for sorting retriever results)
	 * 
	 */
	public int compareTo(Object o) {
		FastSearchResult anotherResult = null;
		try {
			anotherResult = (FastSearchResult)o;
		} catch (Exception e) {
			log.error("Trying illegal compare " + o);
		}
		
		if(this.docDateTime != null && anotherResult.getDocDateTime() != null)
			return anotherResult.getDocDateTime().compareTo(this.docDateTime);
		
		return 0;
	}
}
