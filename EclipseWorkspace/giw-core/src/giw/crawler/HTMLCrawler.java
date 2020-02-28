package giw.crawler;

import giw.util.HTMLCrawlerException;
import java.util.*;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

public class HTMLCrawler extends WebCrawler {

	private int threadPoolSize;
	
	public HTMLCrawler(String url) {
		super(url);
		this.threadPoolSize = Runtime.getRuntime().availableProcessors();
	}
	
	public void crawlData() {
		WebClient client = new WebClient();
		List<Thread> pool = null;
		try {
			pool = new ArrayList<Thread>(this.threadPoolSize);
			
			/* faccio dei tentativi per collegarmi alle pagine */
			HtmlPage page = this.getPage(client, this.getDataSourceURL());
			/* estraggo la lista delle opzioni relative alle regioni */
			HtmlSelect select = (HtmlSelect) page.getByXPath("//select").get(0);
			List<HtmlOption> options = select.getOptions();
			int jobsNumber = options.size() / this.threadPoolSize;
			int[] jobs = new int[jobsNumber];
			int assignedJobs = 0;			// numero di job assegnati
			
			for(int i = 1; i < options.size(); i++) {
				/* determino i job da assegnare ai thread */
				jobs[assignedJobs] = i;
				assignedJobs++;
				/* verifico se ho ottenuto il numero di job prefissato oppure se sono terminati */
				if (assignedJobs == jobsNumber || (i+1) == options.size()) {
					// creo il nuovo thread lo aggiungo alla lista e lo mando in esecuzione
					Thread t = new Thread(new HTMLCrawlerTask(this.getDataSourceURL(), jobs));
					t.start();
					pool.add(t);
					System.out.println("# Avviato nuovo Thread (#jobs : "+assignedJobs+")");
					
					/* azzero le variabili per assegnare i job al successivo thread */
					jobs = new int[jobsNumber];
					assignedJobs = 0;
				}
			}
			// attendo che i thread terminino l'esecuzione
			for(Thread t : pool) {
				try {
					t.join();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (HTMLCrawlerException e) {
			/* errore nell'accedere alla pagina */
			System.err.println("@ HTMLCrawlerException : Unable to access the web page");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		client.closeAllWindows();
		System.out.println("# Processo Terminato");
	}
}
