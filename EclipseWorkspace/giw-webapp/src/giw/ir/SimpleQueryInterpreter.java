package giw.ir;

import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import giw.ir.DataRecordAnalysisFacade;
import giw.model.DescrittoreEsercizio;
import giw.util.IndexAccessException;
import giw.util.QueryInterpreterException;

public class SimpleQueryInterpreter {
	
	private static final String SEARCH_DEFAULT_FIELD = "content";		// field di default dei documenti su cui effettuare la ricerca
	private IndexSearcher searcher;
	private QueryParser queryParser;									// interpreta la stringa inserita dall'utente e restituisce una Query
	
	public SimpleQueryInterpreter() throws QueryInterpreterException {
		try {
			Analyzer analyzer = DataRecordAnalysisFacade.getQueryAnalyzer();
			this.queryParser = new QueryParser(Version.LUCENE_30, SEARCH_DEFAULT_FIELD, analyzer);
			this.searcher = DataRecordAnalysisFacade.getIndexSearcher();
		}
		catch (IndexAccessException e) {
			throw new QueryInterpreterException(e);
		}
	}
	
	public void setIndexSearcher(IndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	public Set<DescrittoreEsercizio> evaluate(String query) throws QueryInterpreterException {
		Set<DescrittoreEsercizio> resultSet = new HashSet<DescrittoreEsercizio>();
		try {
			/* effettuo la ricerca tramite il query parser */
			resultSet = DataRecordAnalysisFacade.queryParserEvaluation(this.queryParser, this.searcher, query);
			/* verifico se sono stati trovati dei documenti */
			if (resultSet.isEmpty()) {
				/* se il query parser non ha dato risultati effettuo una query CNF - Conjunctive Normal Form */
				resultSet = DataRecordAnalysisFacade.cnfQueryEvaluation(searcher, query);
			}
		}
		catch (Exception e) {
			throw new QueryInterpreterException(e);
		}
		
		return resultSet;
	}
}
