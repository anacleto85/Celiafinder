package giw.test;

import giw.crawler.HTMLCrawler;

import java.util.*;
import org.junit.*;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.Event;

public class HtmlCrawlerTest {

	private String url;
	
	@Before
	public void init() {
		this.url = "http://www.celiachia.it/DIETA/afc/ristoranti_etc.aspx";
	}
	
	/**
	 * Test per una generica interazione con il sito web per l'estrazione 
	 * dei dati dalle tabelle visualizzate 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGenericDataExtraction() throws Exception {
		
		WebClient client = new WebClient();
		HtmlPage page = client.getPage(this.url);
		HtmlSelect select = (HtmlSelect) page.getByXPath("//select").get(0);
		List<HtmlOption> options = select.getOptions();
		for (HtmlOption option : options) {
			System.out.println("#### "+option.asText());
			if (option.asText().equals("Lazio")){
				option.setSelected(true);
				System.out.println("######## Selezionato ---------> "+option.asText());
			}
		}
		System.out.println();
		System.out.println("--------------------------------------------------------");		
		
		// scateno l'evento onChange per aggiornare la pagina con i dati necessari
		ScriptResult result = select.fireEvent(Event.TYPE_CHANGE);
		page = (HtmlPage) result.getNewPage();
		Assert.assertNotNull(page);
		// ottengo la tabella da cui interessa estrarre i dati
		HtmlTable table = (HtmlTable) page.getByXPath("//table[@class = 'elenco']").get(0);
		Assert.assertNotNull(table);
		// ottengo le righe della tabella
		List<HtmlTableRow> rows = table.getRows();
		Assert.assertNotNull(rows);
		Assert.assertTrue(rows.size() > 0);
		// stampo le righe della tabella
		for(int i = 1; i < rows.size(); i++) {
			HtmlTableRow r = rows.get(i);
			// stampo i dati della riga
			System.out.println("@@@@ Riga Tabella : "+ r.asText());
			
			// ottengo il link associato alla riga
			HtmlAnchor l = (HtmlAnchor) page.getByXPath(r.getCanonicalXPath()+"//a").get(0);
			Assert.assertNotNull(l);
			// seguo il link
			HtmlPage newpage = client.getPage(this.url+l.getHrefAttribute());
			Assert.assertNotNull(newpage);
			System.out.println("========>> "+this.url+l.getHrefAttribute());
			
			// estraggo i dati dalla tabella di dettaglio
			HtmlSpan dettaglio = (HtmlSpan) newpage.getByXPath("//div[@class = 'entry']/span").get(0);
			Assert.assertNotNull(dettaglio);
			String txt = dettaglio.asText();
			String[] data = txt.split("\\b.+: \\b");
			System.out.println("//////////////////////");
			for(int k = 0; k < data.length; k++)
				System.out.print(data[k]);
			System.out.println("//////////////////////");
		}
	
		// passo a stampare gli elementi nella pagina successiva della tabella
		List<?> pages = page.getByXPath("//div[@id = 'divPaginazione']/span/a");
		Assert.assertTrue(pages.size() > 0);
		for(int i = 0; i < pages.size(); i++) {
			// pagina corrente da cui scaricare i dati
			HtmlAnchor current = (HtmlAnchor) pages.get(i);
			Assert.assertNotNull(current);
			// seguo il link ed aggiorno la pagina
			current.blur();
			page = (HtmlPage) current.click();
			System.out.println();
			System.out.println("----------- Pagina Corrente "+ current.asText() +"------------------------------");

			// ottengo i nuovi dati nella tabella
			table = (HtmlTable) page.getByXPath("//table[@class = 'elenco']").get(0);
			// scarico le righe della tabella e le stampo
			rows = table.getRows();
			Assert.assertTrue(rows.size() > 0);
			Assert.assertNotNull(rows);
			
			for(int j = 1; j < rows.size(); j++) {
				HtmlTableRow r = rows.get(j);
				int cellnums = r.getCells().size();
				System.out.println("@@@@ Riga Tabella ["+cellnums+"] : "+r.asText());
				Assert.assertNotNull(r);
			}
		}		
		
		client.closeAllWindows();
	}
	
	@Test
	public void testHtmlCrawlerRun() {
		
		HTMLCrawler crawler = new HTMLCrawler(this.url);
		crawler.crawlData();
	}
}
