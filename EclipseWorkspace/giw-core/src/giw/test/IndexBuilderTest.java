package giw.test;

import giw.util.IndexBuilderException;
import giw.ir.*;
import org.junit.*;

public class IndexBuilderTest {

	@Test
	public void testIndexBuilderRun() {
		StandardIndexBuilder ibuilder = null;
		try {
			ibuilder = new StandardIndexBuilder();
			Assert.assertNotNull(ibuilder);
			int count = ibuilder.buildIndex();
			Assert.assertNotNull(count);
			Assert.assertTrue(count > 0);
		}
		catch (IndexBuilderException e) {
			System.err.println("@ IndexBuilderException : Error building index");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
