package no.sesat.search.result;

import java.util.List;

public final class CompanyAccounting extends BasicResultItem {

	private int lederlonn;
	private String orgnr;
	private List<ShareHolder> shareholders;
	private List<AccountingYear> accountingYears;
	public int getLederlonn() {
		return lederlonn;
	}
	public void setLederlonn(int lederlonn) {
		this.lederlonn = lederlonn;
	}
	public String getOrgNr() {
		return orgnr;
	}
	

	public void setOrgNr(String orgnr) {
		this.orgnr = orgnr;
	}
	public List<ShareHolder> getShareholders() {
		return shareholders;
	}
	public void setShareholders(List<ShareHolder> shareholders) {
		this.shareholders = shareholders;
	}
	public List<AccountingYear> getAccountingYears() {
		return accountingYears;
	}
	public void setAccountingYears(List<AccountingYear> accountingYears) {
		this.accountingYears = accountingYears;
	}
	
}
