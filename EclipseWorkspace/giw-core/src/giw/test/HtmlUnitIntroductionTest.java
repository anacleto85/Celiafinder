package giw.test;

import java.util.List;
import org.junit.*;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlUnitIntroductionTest {

	/**
	 * In the first example we create the web client and have it load the home page 
	 * from the HtmlUnit web-site. Then we verify that this page has the correct title.
	 * Note that getPage() can returns different types of pages based on the content type
	 * of the returned data. In this case we are expecting a content type of text/html so we
	 * cast the result to an com.gargoylesoftware.htmlunit.html.HtmlPage
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void homePage() throws Exception {
		
		final WebClient webClient = new WebClient();
		final HtmlPage page = webClient.getPage("http://htmlunit.sourceforge.net");
		Assert.assertEquals("Welcome to HtmlUnit", page.getTitleText());
		
		final String pageAsXml = page.asXml();
		Assert.assertTrue(pageAsXml.contains("<body class=\"composite\">"));
		
		final String pageAsText = page.asText();
		Assert.assertTrue(pageAsText.contains("Support for the HTTP and HTTPS protocols"));
		
		webClient.closeAllWindows();
	}
	
	
	/**
	 * Often you will want to simulate a specific browser. This is done by passing a 
	 * com.gargoylesoftware.htmlunit.BrowserVersion into the WebClient constructor. 
	 * Constants have been provided for some common browsers.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void homePage_Firefox() throws Exception {
		
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
		HtmlPage page = webClient.getPage("http://htmlunit.sourceforge.net");
		Assert.assertEquals("Welcome to HtmlUnit", page.getTitleText());
		
		webClient.closeAllWindows();
	}
	
	
	/**
	 * Once you have a reference to HtmlPage, you can search a specific HtmlElement by
	 * one of "get" methods, or by using XPath. Below is an example of finding a "div" 
	 * by an ID, and getting an anchor by name. 
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void getElements() throws Exception {
		
		final WebClient client = new WebClient();
		final HtmlPage page = client.getPage("http://htmlunit.sourceforge.net");
		final HtmlDivision div = page.getHtmlElementById("breadcrumbs");
		Assert.assertEquals("/html/body/div[2]", div.getCanonicalXPath());
		
		client.closeAllWindows();
	}
	
	/**
	 * XPath is the suggested way for more complex searches
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void xpath() throws Exception {
		
		WebClient client = new WebClient();
		HtmlPage page = client.getPage("http://htmlunit.sourceforge.net");
		
		// get list of all divs
		List<?> divs = page.getByXPath("//div");
		Assert.assertNotNull(divs);
		// get div which has a "name" attribute of "John"
		HtmlDivision div = (HtmlDivision) page.getByXPath("//div[@id = 'breadcrumbs']").get(0);
		Assert.assertNotNull(div);

		client.closeAllWindows();
	}
}