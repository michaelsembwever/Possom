package no.schibstedsok.front.searchportal.navigation;


/**
 * @author Lars Johansson
 *
 */
public class NavigationElement {

	String name = "";
	String value = "";
	int count;

	public NavigationElement(String name, String value, int count) {
		this.name = name;
		this.value = value;
		this.count = count;

	}

	public String toString() {
		StringBuffer buf = new StringBuffer("NavigationElement ").append("Name: ").append(name)
				.append(", Value: ").append(value).append(", Count:").append(count);
		return buf.toString();
	}

	public int getCount() {
		return count;
	}


	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof NavigationElement)) {
			return false;
		}
		NavigationElement rhs = (NavigationElement) object;
		return rhs.name.equals(this.name) && rhs.value.equals(this.value) && rhs.count == this.count;
	}
	
}

