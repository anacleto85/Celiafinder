package giw;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import giw.crawler.*;
import giw.ir.StandardIndexBuilder;

public class Main 
{
	private static final String DATA_SOURCE_URL = "http://www.celiachia.it/DIETA/afc/RistorantiHotel.aspx?SS=95&M=99";
	private static final String LOG_DATA_FOLDER = "/opt/celiafinder/log";
	private static final long DOWN_TIME = ((1000 * 60) * 60) * 24;			// eseguo il processo ad intervalli di 3h (in millisecondi)
	
	public static void main(String [] args) {
		/* Il processo resta sempre in esecuzione, scarica i dati periodicamente */
		while (true) {
			/* per prima cosa indirizzo gli stream di errore e di output verso il file di log */
			try {
				/* scrivo nel nome del file la data in cui viene creato */
				Date d = Calendar.getInstance().getTime();
				/* formato data year-month-day-hour-minute */
				SimpleDateFormat df = new SimpleDateFormat("yMd-k-m");
				String filename = df.format(d);
				File logFile = new File(LOG_DATA_FOLDER+"/"+filename+".log");
				PrintStream out = new PrintStream(logFile);
				PrintStream err = new PrintStream(logFile);
				System.setOut(out);		// imposto lo stream di output verso il file log
				System.setErr(err);		// imposto lo stream di errore verso il file log
			
				/* estraggo i dati e creo l'indice */
				/* estraggo i dati dalle pagine web */
				HTMLCrawler crawler = new HTMLCrawler(DATA_SOURCE_URL);
				crawler.crawlData();
				/* avvio la creazione dell'indice */
				StandardIndexBuilder ibuilder = new StandardIndexBuilder();
				ibuilder.buildIndex();
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			
			/* metto in pausa il processo prima di scaricare nuovamente i dati */
			try {
				Thread.sleep(DOWN_TIME);
			}
			catch (Exception e) {
				/* ignoro l'eccezione */
			}
		}
	}
}
