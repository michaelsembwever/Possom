package no.sesat.search.result;


public final class ShareHolder extends BasicResultItem {

	
	private String id;
	private String name;
	private Float shares;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Float getShares() {
		return shares;
	}
	public void setShares(Float shares) {
		this.shares = shares;
	}
}
