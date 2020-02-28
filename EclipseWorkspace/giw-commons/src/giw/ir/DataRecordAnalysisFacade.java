package giw.ir;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

import giw.model.DataRecord;
import giw.model.DescrittoreEsercizio;
import giw.util.*;

/**
 * Classe statica contenente il codice per l'indicizzazione dei dati
 * 
 * @author alessandro
 *
 */
public class DataRecordAnalysisFacade {

	private static final int MAX_RETRIEVED_DOCS = 100;						// numero massimo di documenti restituiti
	private static final int TOKEN_LEN_THRESHOLD = 3;						// lunghezza minima dei token sui cui effettuo ricerche fuzzy
	private static final String SEARCH_DEFAULT_FIELD = "content";			// field di default dei documenti su cui effettuare la ricerca
	private static final String SEARCH_FIELD_NOME = "nome";
	private static final String SEARCH_FIELD_INDIRIZZO = "indirizzo";
	private static final String SEARCH_FIELD_CITTA = "citta";
	private static final float MIN_FUZZY_SIMILARITY = 0.6F;					// livello minimo di "vicinanza" per le fuzzy query
	private static final String CONTENT_FIELD_NAME = "content";
	private static final String INDEX_STORAGE_DIRECTORY = "/opt/celiafinder/index";
	
	/**
	 * Restituisce l'analyzer per l'interpretazione delle query
	 * @return
	 */
	public static Analyzer getQueryAnalyzer() {
		return new StandardAnalyzer(Version.LUCENE_30);
	}
	
	/**
	 * Restituisce l'analyzer per l'indicizzazione dei dati con la directory di default
	 * @return
	 */
	public static Analyzer getIndexAnalyzer() {
		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new SimpleAnalyzer());
		analyzer.addAnalyzer(CONTENT_FIELD_NAME, new StandardAnalyzer(Version.LUCENE_30));
		return analyzer;
	}
	
	/**
	 * Metodo che restituisce uno strumento per indicizzare i documenti in una particolare directory
	 * 
	 * @param indexDir
	 * @return
	 * @throws IndexAccessException
	 */
	public static IndexWriter getIndexWriter() throws IndexAccessException {
		IndexWriter writer = null;
		try {
			/* imposto l'analyzer da utilizzare per creare l'indice */
			Analyzer analyzer = getIndexAnalyzer();
			/* costruisco l'index writer */
			Directory dir = FSDirectory.open(new File(INDEX_STORAGE_DIRECTORY));
			writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
		}
		catch (IOException ioe) {
			throw new IndexAccessException(ioe);
		}
		return writer;
	}
	
	/**
	 * Restituisce un oggetto per eseguire le query sull'indice 
	 * 
	 * @return
	 * @throws IndexAccessException
	 */
	public static IndexSearcher getIndexSearcher() throws IndexAccessException {
		IndexSearcher searcher = null;
		try {
			Directory dir = FSDirectory.open(new File(INDEX_STORAGE_DIRECTORY));
			searcher = new IndexSearcher(dir, true);
		}
		catch (IOException e) {
			throw new IndexAccessException(e);
		}
		return searcher;
	}
	
	/**
	 * Metodo contenente la logica per indicizzare un documeto estraendo i dati da un DataRecord
	 * 
	 * @param writer
	 * @param record
	 */
	public static void indexDataRecord(IndexWriter writer, DataRecord record) throws IndexAccessException {
		try {
			/* costruisco i campi che costituiscono il documento da indicizzare */
			Field content = new Field(CONTENT_FIELD_NAME, record.toString(), Field.Store.NO, Field.Index.ANALYZED);
			Field nome = new Field("nome", record.getNome(), Field.Store.YES, Field.Index.NOT_ANALYZED);
			Field citta = new Field("citta", record.getCitta(), Field.Store.YES, Field.Index.NOT_ANALYZED);
			Field indirizzo = new Field("indirizzo", record.getIndirizzo(), Field.Store.YES, Field.Index.NOT_ANALYZED);
			Field telefono = new Field("telefono", record.getTelefono(), Field.Store.YES, Field.Index.NO);
			Field sitoweb = new Field("sitoweb", record.getSitoWeb(), Field.Store.YES, Field.Index.NO);
			Field referente = new Field("referente", record.getReferente(), Field.Store.YES, Field.Index.NO);
			Field coordinate = new Field("coordinate", record.getCoordinate(), Field.Store.YES, Field.Index.NO);
			/* modifico il peso di alcuni campi nell'indicizzazione (default 1.0) */
			nome.setBoost(1.5F);			
			citta.setBoost(1.4F);
			indirizzo.setBoost(1.2F);
			
			/* considero ciascun record come un documento da indicizzare */
			Document doc = new Document();
			/* aggiungo i campi al documento */
			doc.add(content);
			doc.add(nome);
			doc.add(citta);
			doc.add(telefono);
			doc.add(indirizzo);
			doc.add(sitoweb);
			doc.add(referente);
			doc.add(coordinate);
			/* aggiungo un campo con valori multipli per le ripologie */
			for (String t : record.getTipologie())
				doc.add(new Field("tipologia", t, Field.Store.YES, Field.Index.NO));
		
			/* indicizzo il documento */
			System.out.println("@ Indexing .... "+record);
			writer.addDocument(doc);
		}
		catch (IOException e) {
			throw new IndexAccessException(e);
		}
	}
	
	/**
	 * Metodo che interroga l'indice utilizzando semplicemente il QueryParser
	 * 
	 * @param parser
	 * @param searcher
	 * @param query
	 * @return
	 */
	public static Set<DescrittoreEsercizio> queryParserEvaluation(QueryParser parser, IndexSearcher searcher, String query) throws QueryParserEvaluationException {
		ScoreDoc[] hits = null;
		try {
			/* se  */
			if (!query.contains("\"")) {
				/* Se la query e un insieme di termini senza apici, metto in AND i termini tra loro e passo al query al parser */
				String[] qterms = query.split(" ");
				String strQuery = "";
				for (String t : qterms)
					strQuery += " +"+t;
				/* interrogo l'indice */
				hits = searcher.search(parser.parse(strQuery), null, MAX_RETRIEVED_DOCS, Sort.RELEVANCE).scoreDocs;
			}
			else {
				/* interrogo direttamente l'indice */
				hits = searcher.search(parser.parse(query), null, MAX_RETRIEVED_DOCS, Sort.RELEVANCE).scoreDocs;
			}
		}
		catch (Exception e) {
			throw new QueryParserEvaluationException(e);
		}
		
		/* restituisco l'insieme dei documenti ottenuti */
		return getResultSet(hits, searcher);
	}
	
	/**
	 * Metodo che costruisce una query come CNF, ossia combinazione di clausole in AND
	 * dove ogni clausola è composta da un insieme di termini in OR. I termini di questa
	 * clasusola sono FuzzyQuery, per ciascun termine della stringa con
	 * i principali campdi indicizzati dei documenti
	 * 
	 * @param searcher
	 * @param query
	 * @return
	 * @throws CNFQueryEvaluationException
	 */
	public static Set<DescrittoreEsercizio> cnfQueryEvaluation(IndexSearcher searcher, String query) throws CNFQueryEvaluationException {
		ScoreDoc[] hits = null;
		try {
			/* esprimo la query come una CNF in cui in ciascuna clausola metto in OR le fuzzy query per i field del documento */
			BooleanQuery boolQuery = new BooleanQuery();
			/* scompongo i termini e creo le clausole tramite fuzzy query */
			String[] qterms = query.split(" ");
			for (String t : qterms) {
				if (t.length() >= TOKEN_LEN_THRESHOLD) {
					/* costruisco la clausola da inserire nella query */
					BooleanQuery clausola = new BooleanQuery();
					/* metto piu fuzzy query in OR tra loro */
					clausola.add(new FuzzyQuery(new Term(SEARCH_DEFAULT_FIELD, t), MIN_FUZZY_SIMILARITY), BooleanClause.Occur.SHOULD);
					clausola.add(new FuzzyQuery(new Term(SEARCH_FIELD_NOME, t), MIN_FUZZY_SIMILARITY), BooleanClause.Occur.SHOULD);
					clausola.add(new FuzzyQuery(new Term(SEARCH_FIELD_INDIRIZZO, t), MIN_FUZZY_SIMILARITY), BooleanClause.Occur.SHOULD);
					clausola.add(new FuzzyQuery(new Term(SEARCH_FIELD_CITTA, t), MIN_FUZZY_SIMILARITY), BooleanClause.Occur.SHOULD);
					/* aggiungo la clausola nella query, le clausole sono in AND tra loro */
					boolQuery.add(clausola, BooleanClause.Occur.MUST);
				}
			}
			/* interrogo l'indice con la query costruita */
			hits = searcher.search(boolQuery, null, MAX_RETRIEVED_DOCS, Sort.RELEVANCE).scoreDocs;
		}
		catch (Exception e) {
			throw new CNFQueryEvaluationException(e);
		}
		/* restituisco l'insieme dei documenti ottenuti */
		return getResultSet(hits, searcher);
	}
	
	/* metodo di supporto che dati dei risultati restituisce un insieme */
	private static Set<DescrittoreEsercizio> getResultSet(ScoreDoc[] hits, IndexSearcher searcher) {
		Set<DescrittoreEsercizio> result = new HashSet<DescrittoreEsercizio>();
		try {
			/* costruisco l'insieme dei risultati ottenuti */ 
			for(int i = 0; i < hits.length; i++) {
				Document hitDoc = searcher.doc(hits[i].doc);
				/* costruisco l'istanza del descrittore per raprresentare le informazioni relative al documento */
				DescrittoreEsercizio descrittore = new DescrittoreEsercizio(hitDoc.get("nome"), hitDoc.get("indirizzo"), hitDoc.get("telefono"), hitDoc.get("citta"), hitDoc.get("regione"));
				descrittore.setReferente(hitDoc.get("referente"));
				descrittore.setSitoWeb(hitDoc.get("sitoweb"));
				descrittore.setTipologie(hitDoc.get("tipologia"));
				descrittore.setCoordinate(hitDoc.get("coordinate"));
				result.add(descrittore);
			}
		}
		catch (Exception e) {
			/* in caso di errori restituisco un insieme vuoto */
		}
		return result;
	}
}
