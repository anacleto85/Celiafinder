package giw.datastorage;

import giw.model.DataRecord;
import giw.util.DataStorageException;

import java.io.*;

public class DataStreamWriter {

	private PrintWriter dataWriter;
	private String path;
	
	public DataStreamWriter(String dataPath) {
		this.path = dataPath;
	}
	
	/**
	 * Apro uno stream di scrittura verso lo storage per poter scrivere dei dati
	 * 
	 * @throws DataStorageException
	 */
	public void openStreamWriter() throws DataStorageException {
		try {
			FileWriter f = new FileWriter(this.path);
			this.dataWriter = new PrintWriter(f);
		}
		catch (Exception e) {
			throw new DataStorageException(e);
		}
	}

	/**
	 * Chiude lo stream di scrittura verso il data storage
	 */
	public void closeStreamWriter() {
		this.dataWriter.close();
	}
	
	/**
	 * scrive dei dati nello storage attraverso lo stream di scrittura
	 * 
	 * @param record
	 */
	public void writeDataRecord(DataRecord record) {
		/* verifico il riferimento passato come parametro */
		if (record != null) {
			/* formatto la stringa da scrivere */
			String txt = record.getRegione();
			txt += "#"+record.getCitta();
			txt += "#"+record.getIndirizzo();
			txt += "#"+record.getNome();
			txt += "#"+record.getTelefono();
			txt += "#"+record.getReferente();
			txt += "#";
			/* formatto la lista delle tipologie di esercizio */
			for (String tipo : record.getTipologie())
				txt += "$"+tipo;
			/* 
			 *  i campi relativi al sito web e alle coordinate non sono presenti per tutti
			 *  i record, comunque devono avere un numero di campi costante 
			 */
			txt += "#"+record.getSitoWeb();
			txt += "#"+record.getCoordinate().replace(",", "$");		// separo latitutdine e longitudine con '$'
			/* scrivo il recorod formattato sul file di output */
			this.dataWriter.println(txt);
		}
	}
}
