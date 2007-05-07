package no.schibstedsok.searchportal.result;

public class BlogSearchResultItem extends BasicSearchResultItem { // Extend BasicSearchResultItem
	
	/* Path to blogtipsicon */
	String blogTipsIcon;
	
	/* Display this date */
	String displayDate;


	public String getBlogTipsIcon() {
		return blogTipsIcon;
	}


	public void setBlogTipsIcon(String blogTipsIcon) {
		this.blogTipsIcon = blogTipsIcon;
	}


	public String getDisplayDate() {
		return displayDate;
	}


	public void setDisplayDate(String displayDate) {
		this.displayDate = displayDate;
	}
	
	
	

}
