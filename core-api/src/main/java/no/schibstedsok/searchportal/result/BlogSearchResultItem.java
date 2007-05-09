package no.schibstedsok.searchportal.result;

import no.schibstedsok.searchportal.view.StringChopper;

public class BlogSearchResultItem extends BasicSearchResultItem { // Extend BasicSearchResultItem
	
	/* Path to blogtipsicon */
	private String blogTipsIcon;
	
	/* Display this date */
	private String displayDate;

    private String blogTitle;

    private String contentTitle;

    private String parentUrl;

    private String url;

    private String comments;

    private String body;
    
    private String moreHits;

    private String author;


    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }


    /**
     * Return first non-null value of contentTitle, blogTitle or url
     * @return linktitle
     */
    // Transient
    public String getLinkTitle() {
        if(contentTitle != null) {
            return contentTitle;
        }
        if(blogTitle != null){
            return blogTitle;
        }
        return url;
    }

    /**
     * Trim linktitle
     * @return trimmed linktitle
     */
    public String getLinkTitleTrimmed() {
        return StringChopper.chop(getLinkTitle(), 70);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getMoreHits() {
        return moreHits;
    }

    public void setMoreHits(String moreHits) {
        this.moreHits = moreHits;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    private boolean isHitInBody() {
        if(body == null) {
            return false;
        }
        return body.indexOf("<b>") > -1;
    }

    /**
     * isHitInComments()
     * @return true if hit in comments
     */
    private boolean isHitInComments() {
        if(comments == null  || isHitInBody()) {
            return false;
        }
        return comments.indexOf("<b>") > -1;
     }

    /**
     * Get the title of the blog
     * @return title of the blog
     */
    public String getBlogTitle() {
        return blogTitle;

    }

    /**
     * Set the title for the blog.
     * @param title for blog
     */
    public void setBlogTitle(String title) {
         blogTitle = title;
    }

    /**
     * Get body or comment, depends on where the search matched.
     * @return body or comment
     */
    public String getBodyOrComments() {
        if(isHitInBody()) {
            return body;
        }
        return comments;
    }

    /**
     * hasAuthor() checks if the blog entry has an authorinformation.
     * @return true if has author information.
     */
    public boolean hasAuthor() {
        return author != null;
    }

    /**
     * hasMoreHits() can be used to determine if we have severeal hits
     * from blog
     * @return true if has more hits
     */
    public boolean hasMoreHitsFromBlog() {
        return moreHits != null;
    }

    /**
     * If the blog result does not refert to a parentUrl it means that
     * we have the frontpage of the article. Otherwise it may a comment
     * from the article.
     * @return if entry is blog frontpage.
     */
    public boolean isFrontPage() {

        if(parentUrl == null) {
            return true;
        }
        return parentUrl.equals(url);
    }
}
