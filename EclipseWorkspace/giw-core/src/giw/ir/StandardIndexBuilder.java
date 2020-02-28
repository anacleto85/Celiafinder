package giw.ir;

import java.util.List;
import org.apache.lucene.index.IndexWriter;
import giw.datastorage.*;
import giw.model.DataRecord;
import giw.util.*;
import giw.ir.DataRecordAnalysisFacade;

public class StandardIndexBuilder {
	
	private IndexWriter writer;
	private DataStorageFacade storageFacade;
	
	public StandardIndexBuilder() throws IndexBuilderException {
		try {
			this.storageFacade = DataStorageFacade.getInstance();
			this.writer = DataRecordAnalysisFacade.getIndexWriter();
		}
		catch (IndexAccessException iae) {
			throw new IndexBuilderException(iae);
		}
	}
	
	/**
	 * Metodo per la costruzione dell'indice a partire dai dati contenuti nello storage
	 * 
	 * @throws DataRecordIndexException
	 */
	public int buildIndex() throws IndexBuilderException {
		/* costruisco l'indice a partire dai dati contenuti nello storage */
		DataStreamReader streamReader = this.storageFacade.getDataStreamReader();
		List<String> files = streamReader.getDataFileList();
		
		System.out.println("# Start indexing ...... ");
		int count = 1;
		/* costruisco l'indice a partire dai dati contenuti nello storage */
		for (String path : files) {
			/* apro il file in lettura */
			try {
				streamReader.openStreamReader(path);
				DataRecord record = null;
				while((record = streamReader.parseRecord()) != null) {
					/* indicizzo i dati contenuti nel record */
					DataRecordAnalysisFacade.indexDataRecord(this.writer, record);
					count++;
				}
			}
			catch (Exception e) {
				throw new IndexBuilderException(e);
			}
			finally {
				streamReader.closeStreamReader();
			}
		}
		
		try {
			/* termino la costruzione dell'indice */
			this.writer.close();
			System.out.println("# ...... Stop, index built [#"+count+" indexed documents]");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		/* restituisco il numero di documenti indicizzati */
		return count;
	}
}
