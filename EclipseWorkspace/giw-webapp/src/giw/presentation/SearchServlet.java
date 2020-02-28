package giw.presentation;

import giw.ir.SimpleQueryInterpreter;
import giw.model.*;
import giw.util.QueryInterpreterException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SearchServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String NEXT_PAGE = "/index.jsp";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		List<DescrittoreEsercizio> result = null;									// lista documenti restituiti
		HttpSession session = request.getSession();
		SimpleQueryInterpreter qi = null;
		try {
			String query = request.getParameter("query");							// contenuto della query effettuata dall'utente
			qi = new SimpleQueryInterpreter();
			/* interrogo l'indice */
			result = new LinkedList<DescrittoreEsercizio>(qi.evaluate(query));
			
			/* setto la lista sulla sessione */
			session.setAttribute("result", result);
			session.setAttribute("query", query);
			session.setAttribute("displayPage", request.getParameter("displayPage"));
		}
		catch (QueryInterpreterException e) {
			session.setAttribute("error", e.getMessage());
		}
		
		/* indirizzo l'utente alla prossima pagina */
		ServletContext application = getServletContext();
		RequestDispatcher rd = application.getRequestDispatcher(NEXT_PAGE);
		rd.forward(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		this.doGet(request, response);
	}
}
