package giw.crawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import giw.datastorage.*;
import giw.model.*;
import giw.util.HTMLCrawlerException;
import java.util.*;


public class HTMLCrawlerTask extends WebCrawler implements Runnable {

	private int[] jobs;
	private DataStorageFacade storageFacade;
	
	public HTMLCrawlerTask(String rootPageURL, int[] jobs) {
		super(rootPageURL);
		this.jobs = jobs;
		this.storageFacade = DataStorageFacade.getInstance();
	}
	
	/**
	 * Il thread scarica le informazioni relative a tutti gli esercizi presenti in una determinata
	 * regione, quindi relativi a tutte le province di una determinata regione
	 *  
	 */
	public void run() {
		HtmlPage page = null;
		HtmlSelect select = null;
		HtmlOption option = null;
		DataStreamWriter connection = null;
		WebClient client = new WebClient(BrowserVersion.FIREFOX_3);
		/* eseguo i job assegnati al thread in esecuzione */
		for(int j = 0; j < this.jobs.length; j++) {
			int currentJob = this.jobs[j];
			try {
				/* faccio dei tentativi per collegarmi alle pagine */
				page = getPage(client, this.getDataSourceURL());
				/* seleziono l'opzione relativa alla regione assegnata al task */
				select = (HtmlSelect) page.getByXPath("//select").get(0);
				option = select.getOption(currentJob);
				option.setSelected(true);
				String regione = option.asText();
				/* seleziono l'opzione ed ottengo la pagina aggiornata */
				page = (HtmlPage) select.fireEvent(Event.TYPE_CHANGE).getNewPage();
				
				/* estraggo i dati e li salvo nello "storage" */
				connection = this.storageFacade.getDataStreamWriter();
				connection.openStreamWriter();
				this.extractData(client, connection, page, regione);
			}
			catch (HTMLCrawlerException e) {
				System.err.println("@ HTMLCrawlerException : Extracting Data Error");
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				//client.closeAllWindows();
				if (connection != null)
					connection.closeStreamWriter();
			}
		}
		client.closeAllWindows();
	}

	/**
	 * Metodo di supporto che estrae dati a partire dalla pagina passata come parametro
	 * 
	 * @param page
	 * @param queue
	 * @param provincia
	 * @throws HTMLCrawlerException
	 */
	private void extractData(WebClient client, DataStreamWriter connection, HtmlPage page, String regione) throws HTMLCrawlerException {
		/* scarico dati relativi ad eventuali pagine successive */
		List<?> links = page.getByXPath("//div[@id = 'divPaginazione']/span/a");
		int i = -1;
		do {
				/* ottengo la tabella da cui estrarre i dati */
				HtmlTable table = (HtmlTable) page.getByXPath("//table[@class = 'elenco']").get(0);
				/* righe tabella */
				List<HtmlTableRow> rows = table.getRows();
				for(int j = 1; j < rows.size(); j++) {
					HtmlTableRow r = rows.get(j);
					DataRecord record = null;
					System.out.println("# Parsing dei dati per [Regione: "+regione+"; Record: "+r.asText()+"]");
					record = this.parseData(client, page, r, regione);
					/* memorizzo il record nello storage */
					connection.writeDataRecord(record);
					/* aspetto un po prima di effettuare una nuova richiesta */
					this.waitRandom();
				}
				
				/* se ci sono altre pagine, per ogni link successivo scarico i dati dalle nuove pagine */
				i++;
				page = null;	
				if(i < links.size() && links.size() > 0) {
					HtmlAnchor currentLink = (HtmlAnchor) links.get(i);
					String url = this.getDataSourceURL()+currentLink.getHrefAttribute();
					try {
						page = this.getPage(client, url);
					}
					catch (Exception ioe) {
						ioe.printStackTrace();
					}
				}
		}
		while(i < links.size() && page != null);
	}
	
	
	/**
	 * Metodo di supporto che fa il parsing dei dati relativi ad un esercizio contenuti nella pagina
	 * e restituisce un oggetto di dominio rappresentante le informazioni estratte 
	 * 
	 * @param page
	 * @param r
	 * @param provincia
	 * @return
	 * @throws HTMLCrawlerException
	 */
	private DataRecord parseData(WebClient client, HtmlPage page, HtmlTableRow r, String regione) throws HTMLCrawlerException {
		
		DataRecord record = null;																// creo l'istanza del record inizializzando i campi
		List<HtmlTableCell> cells = r.getCells();												// dati contenuti nella riga
		HtmlAnchor a = (HtmlAnchor) page.getByXPath(r.getCanonicalXPath()+"//a").get(0);		// link alle informazioni di dettaglio
		HtmlPage dataPage = null;
		try {
			String url = this.getDataSourceURL()+a.getHrefAttribute();
			dataPage = this.getPage(client, url);
			if(dataPage != null) {
				/* estraggo i dati dalla pagina */
				HtmlSpan dettaglio = (HtmlSpan) dataPage.getByXPath("//div[@class = 'entry']/span").get(0);
				String txt = dettaglio.asText();
				String[] data = txt.split("\\b.+:\\s\\b");
				// estraggo i dati di base di ciascun record
				String citta = cells.get(0).asText();
				String nome = cells.get(2).asText();
				String indirizzo = data[1].replace('\n', ' ');
				String telefono = data[2].replace('\n', ' ');
				/* creo l'istanza con i dati base */
				record = new DataRecord(nome, citta, regione, indirizzo, telefono);
				
				/* estraggo le tipologie dell'esercizio corrente ed aggiorno il record*/
				List<String> tipologie = this.parseTipologie(cells.get(1));
				record.setTipologie(tipologie);
				
				String referente = "";
				String sitoWeb = "";
				try {
					referente = data[3].replace('\n', ' ');
					sitoWeb = data[6].replace('\n', ' ');
				}
				catch (Exception e) {}	
				/* aggiorno il record */
				record.setReferente(referente);
				record.setSitoWeb(sitoWeb);
				/* cerco di estrarre i dati relativi alle coordinate dell'esercizio */
				String coordinate = "";
				try {
					HtmlTableCell mappa = (HtmlTableCell) cells.get(3);
					HtmlAnchor link = (HtmlAnchor) page.getByXPath(mappa.getCanonicalXPath()+"/a").get(0);
					coordinate = link.getHrefAttribute().split("q=")[1];
				}
				catch (Exception e) {}
				/* aggiorno i record */
				record.setCoordinate(coordinate);
			}
		}
		catch (Exception e) {
			throw new HTMLCrawlerException(e);
		}
		/* restituisco il record opportunamente costruito */
		return record;
	}
	
	private List<String> parseTipologie(HtmlTableCell cell) {
		/* cella contenente le tipologie dell'esercizio separate da " " */
		String[] tipi = cell.asText().split(" ");
		List<String> tipologie = new ArrayList<String>();
		for(int i = 0; i < tipi.length; i++) {					// leggo le tipologie e le porto in un formato comune
			
			String t = tipi[i];	
			if(t.equals("P"))
				tipologie.add("Pizzeria");
			else if(t.equals("Bar"))
					tipologie.add("Bar");
			else if(t.equals("Pub"))
					tipologie.add("Pub");
			else if(t.equals("Agr"))
					tipologie.add("Agriturismo");
			else if(t.equals("Gel"))
					tipologie.add("Gelateria");
			else if(t.equals("Ct"))
					tipologie.add("Catering");
			else if(t.equals("Tr") || t.equals("Ost"))
					tipologie.add("Trattoria");
			else if(t.equals("Crep"))
					tipologie.add("Creperia");
			else if(t.equals("SR"))
					tipologie.add("SalaRicevimento");
			else if(t.equals("H"))
					tipologie.add("Hotel");
			else if(t.equals("Alb"))
					tipologie.add("Albergo");
			else if(t.equals("Eno"))
				tipologie.add("Enoteca");
			else if(t.equals("Tc"))
				tipologie.add("TavolaCalda");
			else
				tipologie.add("Ristorante");
		}
		return tipologie;
	}
}
