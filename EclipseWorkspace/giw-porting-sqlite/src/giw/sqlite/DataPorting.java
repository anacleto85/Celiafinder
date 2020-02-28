package giw.sqlite;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

import giw.datastorage.*;
import giw.model.*;
import giw.util.DataStorageException;

public class DataPorting {

	// struttura del db sqlite
	private static final String SQLITEDB = "data/celiafinder.sqlite";
	private static final String DBSTRUCT = "conf/celiafinder_struct.sql";

	private static int idEsercizio = 0;
	private static int idTipologia = 0;
	private static int idRegione = 0;
	private static int idCitta = 0;
	
	public static void main(String[] args) 
	{
		// accedo allo storage in lettura
		DataStorageFacade sfc = DataStorageFacade.getInstance();
		DataStreamReader sreader = sfc.getDataStreamReader();
		// sql objects
		Connection conn = null;
		PreparedStatement prepStat = null;
		Statement stat = null;
		ResultSet result = null;
		String sql = "";
		
		try 
		{
			// carico i driver JDBC per SQLITE
			Class.forName("org.sqlite.JDBC");
			// stabilisco una connessione con il db
			try 
			{
				conn = DriverManager.getConnection("jdbc:sqlite:"+SQLITEDB);
				System.out.println(">>> Connection to db established");
				/* Creare la struttura del db (se necessario cancellare i dati pre-esistenti, utilizzando DBSTRUCT
				 * FIXME : doesn't work
				System.out.println(">>> Creating db");
				Scanner scanner = new Scanner(new FileInputStream(DBSTRUCT));
				sql = "";
				while (scanner.hasNextLine())
					sql += scanner.nextLine();
				stat = conn.createStatement();
				stat.execute(sql);
				// done
				stat.close();
				stat = null;
				scanner.close();
				scanner = null;
				*/
			} 
			catch (Exception sqle) {
				System.err.println("Impossibile stabilire la connessione con il db");
			}
			
			// lista dei file di input
			List<String> flist = sreader.getDataFileList();
			for (String fpath : flist)
			{
				System.out.println(">>>>> File Source : "+fpath);
				try 
				{
					sreader.openStreamReader(fpath);
					// leggo i record contenuti nel file
					DataRecord record = null;
					while ((record = sreader.parseRecord()) != null)
					{	
						System.out.println(">>>> Porting : "+record);
						// dati del record da inserire nel db
						String nomeEsercizio = record.getNome();
						String indirizzo = record.getIndirizzo();
						String citta = record.getCitta();
						String regione = record.getRegione();
						String telefono = record.getTelefono();
						String referente = record.getReferente();
						String url = record.getSitoWeb();
						
						// imposto l'id della citta dell'esercizio corrente
						int cittaId = -1;
						stat = conn.createStatement();
						sql = "SELECT id FROM CITTA WHERE nome=\""+citta+"\"";
						result = stat.executeQuery(sql);
						cittaId = result.getInt("id");
						// chiudo gli elementi utilizzati
					//	stat.close(); stat = null;
					//	result.close(); result = null;
						
						// imposto l'id della regione dell'esercizio corrente
						int regioneId = -1;
						stat = conn.createStatement();
						sql = "SELECT id FROM REGIONE WHERE nome=\""+regione+"\"";
						result = stat.executeQuery(sql);
						regioneId = result.getInt("id");
						// chiudo gli elementi utilizzati
					//	stat.close(); stat = null;
					//	result.close();  result = null;
						
						// verifi la corretta assegnazione dell'id
						if (cittaId < 0)	
						{
							cittaId = idCitta;
							idCitta++;
							// inserisco i dati nel db
							sql = "INSERT INTO CITTA (id,nome) VALUES ("+cittaId+",'"+citta+"')";
							stat = conn.createStatement();
							stat.execute(sql);
						//	stat.close(); stat = null;
						}
						
						if (regioneId < 0) 
						{
							regioneId = idRegione;
							idRegione++;
							// inserisco i dati nel db
							sql = "INSERT INTO REGIONE (id,nome) VALUES ("+regioneId+",'"+regione+"')";
							stat = conn.createStatement();
							stat.execute(sql);
							//stat.close(); stat = null;
						}
						
						
						// inserisco i dati nel db
						sql = "INSERT INTO ESERCIZIO (id,nome,indirizzo,citta,regione,telefono,referente,url) " +
								"VALUES ("+idEsercizio+",'"+nomeEsercizio+"','"+indirizzo+"','"+cittaId+"',"+regioneId+",'"+telefono+"','"+referente+"','"+url+"')";
						stat = conn.createStatement();
						stat.execute(sql);
						
						// incremento idEsercizio in seguito all'inserimento effettuato
						idEsercizio++;
						// resetto il riferimento al record
						//record = null;
						//stat = null;
						//result = null;
						//prepStat = null;
					}
				}
				catch (Exception e) 
				{
					System.err.println("Impossibile accedere al data storage : "+fpath);
					e.printStackTrace();
					sreader.closeStreamReader();
					try {
						if (prepStat != null)
							prepStat.close();
						if (stat != null)
							stat.close();
						if (result != null)
							result.close();
					}
					catch (SQLException sqle) {
						sqle.printStackTrace();
					}
				}
				
				System.out.println("############################################################");
				// chiudo lo stream di lettura del file
				sreader.closeStreamReader();
				
			} // fine ciclo for
			
			
			
			try {
				// chiudo la connessione con il db
				if (conn != null)
					conn.close();
				if (prepStat != null)
					prepStat.close();
				if (stat != null)
					stat.close();
				if (result != null)
					result.close();
			}
			catch (SQLException e) {}
		}
		catch (ClassNotFoundException e) {
			System.err.println("Impossibile Caricare il Driver JDBC-SQLITE");
			System.exit(1);
		}
	}
}
