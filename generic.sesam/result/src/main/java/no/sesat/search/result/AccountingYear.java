package no.sesat.search.result;


public final class AccountingYear extends BasicResultItem {
	private long sumDriftsinntekter;
	private long driftsresultat;
	private long resultatFoerSkatt;
	private long aarsresultat;
	private long sumAnleggsmidler;
	private long sumOmloepsmidler;
	private long sumEgenkapital;
	private long langsiktigGjeld;
	private long kortsiktigGjeld;
	private long sumEgenkapitalOgGjeld;
	private long sumEiendeler;
	private int year;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public long getSumDriftsinntekter() {
		return sumDriftsinntekter;
	}

	public void setSumDriftsinntekter(long sumDriftsinntekter) {
		this.sumDriftsinntekter = sumDriftsinntekter;
	}

	public long getDriftsresultat() {
		return driftsresultat;
	}

	public void setDriftsresultat(long driftsresultat) {
		this.driftsresultat = driftsresultat;
	}

	public long getResultatFoerSkatt() {
		return resultatFoerSkatt;
	}

	public void setResultatFoerSkatt(long resultatFoerSkatt) {
		this.resultatFoerSkatt = resultatFoerSkatt;
	}

	public long getAarsresultat() {
		return aarsresultat;
	}

	public void setAarsresultat(long aarsresultat) {
		this.aarsresultat = aarsresultat;
	}

	public long getSumAnleggsmidler() {
		return sumAnleggsmidler;
	}

	public void setSumAnleggsmidler(long sumAnleggsmidler) {
		this.sumAnleggsmidler = sumAnleggsmidler;
	}

	public long getSumOmloepsmidler() {
		return sumOmloepsmidler;
	}

	public void setSumOmloepsmidler(long sumOmloepsmidler) {
		this.sumOmloepsmidler = sumOmloepsmidler;
	}

	public long getSumEgenkapital() {
		return sumEgenkapital;
	}

	public void setSumEgenkapital(long sumEgenkapital) {
		this.sumEgenkapital = sumEgenkapital;
	}

	public long getLangsiktigGjeld() {
		return langsiktigGjeld;
	}

	public void setLangsiktigGjeld(long langsiktigGjeld) {
		this.langsiktigGjeld = langsiktigGjeld;
	}

	public long getKortsiktigGjeld() {
		return kortsiktigGjeld;
	}

	public void setKortsiktigGjeld(long kortsiktigGjeld) {
		this.kortsiktigGjeld = kortsiktigGjeld;
	}

	public long getSumEgenkapitalOgGjeld() {
		return sumEgenkapitalOgGjeld;
	}

	public void setSumEgenkapitalOgGjeld(long sumEgenkapitalOgGjeld) {
		this.sumEgenkapitalOgGjeld = sumEgenkapitalOgGjeld;
	}
	
	public long getSumGjeld(){
		return this.kortsiktigGjeld+this.langsiktigGjeld;
	}

	public long getSumEiendeler() {
		return sumEiendeler;
	}

	public void setSumEiendeler(long sumEiendeler) {
		this.sumEiendeler = sumEiendeler;
	}
}
