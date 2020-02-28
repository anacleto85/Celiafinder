package giw.model;

import java.util.*;

public class DataRecord {

	private String nome;
	private String citta;
	private String regione;
	private String indirizzo;
	private String telefono;
	private String referente;
	private String coordinate;											// coordinate per visualizza la mappa (latitudine-longitudine)
	private String sitoWeb;
	private List<String> tipologie;
	
	public DataRecord() {
		/* inizializzo semplicemente i campi */
		this.tipologie = new ArrayList<String>();
		this.nome = " ";
		this.citta = " ";
		this.regione = " ";
		this.indirizzo = " ";
		this.telefono = " ";
		this.referente = " ";
		this.coordinate = "";
		this.sitoWeb = " ";
	}
	
	public DataRecord(String nome, String citta, String regione, String indirizzo, String telefono) {
		this();
		this.nome = nome;
		this.citta = citta;
		this.regione = regione;
		this.indirizzo = indirizzo;
		this.telefono = telefono;
	}

	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCitta() {
		return this.citta;
	}

	public void setCitta(String citta) {
		this.citta = citta;
	}

	public String getRegione() {
		return this.regione;
	}

	public void setRegione(String regione) {
		this.regione = regione;
	}

	public String getIndirizzo() {
		return this. indirizzo;
	}

	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}

	public String getTelefono() {
		return this.telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getReferente() {
		return this.referente;
	}

	public void setReferente(String referente) {
		this.referente = referente;
	}

	public String getSitoWeb() {
		return this.sitoWeb;
	}

	public void setSitoWeb(String sitoWeb) {
		this.sitoWeb = sitoWeb;
	}

	public List<String> getTipologie() {
		return this.tipologie;
	}

	public void setTipologie(List<String> tipologie) {
		this.tipologie = tipologie;
	}
	
	public void addTipologia(String tipologia) {
		this.tipologie.add(tipologia);
		
	}
	
	public String getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(String coordinate) {
		this.coordinate = coordinate;
	}

	public String toString() {
		String str = " ";
		String tipologie = " ";
		for (String t : this.tipologie)
			tipologie += " "+t;
		
		str += this.nome+", "+this.indirizzo+", "+this.telefono+", "+this.citta+", "+this.regione+", "+tipologie;
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((citta == null) ? 0 : citta.hashCode());
		result = prime * result
				+ ((indirizzo == null) ? 0 : indirizzo.hashCode());
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		result = prime * result + ((regione == null) ? 0 : regione.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataRecord other = (DataRecord) obj;
		if (citta == null) {
			if (other.citta != null)
				return false;
		} else if (!citta.equals(other.citta))
			return false;
		if (indirizzo == null) {
			if (other.indirizzo != null)
				return false;
		} else if (!indirizzo.equals(other.indirizzo))
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (regione == null) {
			if (other.regione != null)
				return false;
		} else if (!regione.equals(other.regione))
			return false;
		return true;
	}
}
