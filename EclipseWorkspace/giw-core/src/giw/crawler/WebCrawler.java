package giw.crawler;

import giw.util.HTMLCrawlerException;
import java.util.Random;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.javascript.host.Event;

public abstract class WebCrawler {

	private String dataSourceURL;
	private static final int NUMBER_OF_ATTEMPTS = 5;
	private static final int REQUEST_RATE = 1000;
	
	public WebCrawler(String url) {
		this.dataSourceURL = url;
	}
	
	public String getDataSourceURL() {
		return this.dataSourceURL;
	}
	
	public void waitRandom() {
		Random rand = new Random();
		int time = rand.nextInt() % REQUEST_RATE;
		try {
			Thread.sleep(time);
		}
		catch (Exception ie) { /* ignoro l'eccezione */ }
	}
	
	/**
	 * Metodo che restituisce una pagina caricata dinamicamente selezionando una opzione di una select
	 * 
	 * @param selectXPath
	 * @param optionIndex
	 * @return
	 * @throws HTMLCrawlerException
	 */
	public HtmlPage getPageOnSelect(HtmlPage page, String selectXpath, int optionIndex) throws HTMLCrawlerException {
		HtmlPage dataPage = null;
		int ttl = NUMBER_OF_ATTEMPTS;
		/* cerco di accedere alla pagina in piu tentativi se necessario */
		while (ttl > 0 && dataPage == null) {
			try {
				/* estraggo la select */
				HtmlSelect select = (HtmlSelect) page.getByXPath(selectXpath).get(0);
				HtmlOption option = (HtmlOption) select.getOption(optionIndex);
				option.setSelected(true);
				dataPage = (HtmlPage) select.fireEvent(Event.TYPE_CHANGE).getNewPage();
			}
			catch (Exception e) {
				if (ttl == 0) {
					throw new HTMLCrawlerException(e);
				}
				else {
					/* altro tentativo */
					ttl--;
					/* aspetto prima di effettuare un nuovo tentativo */
					try {
						/* aspetto prima di effettuare il nuovo tentativo */
						Thread.sleep(REQUEST_RATE);
					}
					catch (Exception ei) { /* ignoro l'eccezione */ }
				}
			}
		}
		return dataPage;
	}
	
	public HtmlPage getPage(WebClient client, String url) throws HTMLCrawlerException {
		/* in caso di errore faccio piu tentativi prima di lanciare l'eccezzione */
		HtmlPage page = null;
		int ttl = NUMBER_OF_ATTEMPTS;
		while(ttl > 0 && page == null) {
			try {
				page = client.getPage(url);
			}
			catch (Exception e) {
				/* faccio un altro tentativo o lancio un errore */
				if (ttl == 0) {
					throw new HTMLCrawlerException(e);
				}
				else {	
					ttl--;
					/* aspetto prima di effettuare un nuovo tentativo */
					try {
						/* aspetto prima di effettuare il nuovo tentativo */
						Thread.sleep(REQUEST_RATE);
					}
					catch (Exception ei) { /* ignoro l'eccezione */ }
				}
			}
		}
		return page;
	}
}
