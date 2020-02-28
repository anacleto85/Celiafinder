package giw.datastorage;

public class DataStorageFacade {

	private static int counter = 0;
	private static DataStorageFacade instance = null;
	private static final String DATA_STORAGE_DIR = "/opt/celiafinder/input";
	
	/**
	 * Ottengo il riferimento all'unica istanza della classe 
	 * @return
	 */
	public static DataStorageFacade getInstance() {
		if (instance == null) 
			instance = new DataStorageFacade();
		
		return instance;
	}
	
	/**
	 * Apre una "connessione" in scrittura nello storage e restituisce
	 * un oggetto che incapsula le funzionalità di connessione
	 * per interagire con lo storage
	 * 
	 * @return
	 */
	public DataStreamWriter getDataStreamWriter() {
		return new DataStreamWriter(this.getDataFile());
	}
	
	/**
	 * Apre una "connessione" in lettura nello storage e restituisce
	 * un oggetto che incapsula le funzionalità di connessione per 
	 * interagire con lo storage
	 * 
	 * @return
	 */
	public DataStreamReader getDataStreamReader() {
		return new DataStreamReader(DATA_STORAGE_DIR);
	}
	
	/* Metodo di supporto per restituire un file per la connessione che si sta instaurando */
	private synchronized String getDataFile() {
		return DATA_STORAGE_DIR+"/part"+(counter++)+".txt";
	}
}
