package giw.datastorage;

import giw.model.DataRecord;
import giw.util.DataStorageException;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataStreamReader {

	private String dataDir;
	private Scanner fileScanner;
	
	public DataStreamReader(String path) {
		this.dataDir = path;
	}
	
	/**
	 * Restituisco la lista dei file contenuti nella directory di input
	 * dello storage
	 * 
	 * @return
	 */
	public List<String> getDataFileList() {
		List<String> fpaths = new ArrayList<String>();
		File dir = new File(this.dataDir);
		for(String fname : dir.list()) {
			File f = new File(fname);
			if (!f.isDirectory())
				fpaths.add(this.dataDir+"/"+fname);
		}
		return fpaths;
	}
	
	/**
	 * Restituisce un oggetto Scanner per leggere iterativamente il file
	 * passato come parametro
	 *  
	 * @param f
	 * @return
	 * @throws DataStorageException
	 */
	public void openStreamReader(String fpath) throws DataStorageException {
		try {
			this.fileScanner = new Scanner(new FileInputStream(fpath));
		}
		catch (Exception fnfe) {
			throw new DataStorageException(fnfe);
		}
	}
	
	/**
	 * Metodo che estrae il record corrente dal file dello storage, 
	 * se non ci sono piu record, restituisce un oggetto null
	 * 
	 * @return
	 */
	public DataRecord parseRecord() throws DataStorageException {
		/* estraggo i dati dal prossimo record del file */
		DataRecord record = null;
		String data = "";
		if (this.fileScanner.hasNextLine()) {
			data = this.fileScanner.nextLine();
			record = new DataRecord();
			String[] params = data.split("#");				
			// i campi di ciascun record sono separati da '#'
			record.setRegione(params[0]);
			record.setCitta(params[1]);
			record.setIndirizzo(params[2]);
			record.setNome(params[3]);
			record.setTelefono(params[4]);
			record.setReferente(params[5]);
			/* scompongo il campo relativo alle tipologie di esercizio */
			String[] tipologie = params[6].split("$");
			for (String t : tipologie)
				if(!t.equals(""))
					record.addTipologia(t.replace("$", " "));
			/* i campi relativi al sito web e alle coordinate sono opzionali e potrebbere dare problemi */
			
			try {
				/* provo a settare il campo relativo al sitoweb */
				record.setSitoWeb(params[7]);
			}
			catch (Exception e) {
				record.setSitoWeb("");			
			}
			try {
				/* provo a settare il campo relativo alle coordinate */
				record.setCoordinate(params[8].replace("$", ","));
			}
			catch (Exception e) {
				record.setCoordinate("");
			}
		}
		
		return record;
	}
	
	/**
	 * Chiudo lo Scanner per la lettura dal file dello storage
	 */
	public void closeStreamReader() {
		this.fileScanner.close();
	}
	
}
