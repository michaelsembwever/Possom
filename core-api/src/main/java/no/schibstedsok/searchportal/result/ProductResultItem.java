package no.schibstedsok.searchportal.result;

public interface ProductResultItem {

	void addField(String field, String value);
    String getField(String field);
}
