<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.*" %>
<%@ page import="giw.model.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link rel="stylesheet" type="text/css" href="css/ir.css" />
	<link rel="icon" type="image/png" href="img/favicon.jpg" />
	<title>Celiafinder : Mangia Senza Glutine!!!</title>
</head>
<script type="text/javascript">

	function sendRequest() {
		var querystr = document.searchform.query.value;
		if (querystr.length > 1)
			document.searchform.submit();
	}
	
	function displayNextPage(qstr, npage) {
		/* imposto i valori della form */
		document.searchform.query.value = qstr;
		document.searchform.displayPage.value = npage;
		document.searchform.submit();
	}
	
	function openLink(url) {
		window.open(url);
	}
	

</script>
<%
			/* controllo dalla sessione se e stata effettuata una ricerca */
			List<DescrittoreEsercizio> list = (List<DescrittoreEsercizio>) session.getAttribute("result");
			String query = (String) session.getAttribute("query");
			final int MAX_RESULT_PAGE = 10;
			int currentPage = 1;
			try {
				currentPage = Integer.parseInt((String) session.getAttribute("displayPage"));	
			}
			catch (Exception e) {}
			
			/* calcolo il numero di pagine da visualizzare */
			int numPage = 1;
			try {
				int size = list.size();
				numPage =  size / MAX_RESULT_PAGE;
				if (size % MAX_RESULT_PAGE != 0)
					numPage++;
			}
			catch (Exception e) {}
%>
<body>
	<div class="container">
			<div class="banner">
				<img class="logo" title="Trova Ristoranti,Alberghi e Pub con cucina Celiaca in Italia!" src="img/banner.png" />
			</div>
			<div class="queryform">
				<form name="searchform" action="search" method="get" onsubmit="javascript:sendRequest()">
					<div class="inputbox">
						<input type="hidden" name="displayPage" />
						<input class="textinput" type="text" name="query" />
						<input class="button" type="button" title="Trova Ristoranti,Alberghi e Pub con cucina Celiaca in Italia!" value="Cerca" onclick="javascript:sendRequest()" />
					</div>
				</form>
			</div>
			<div class="separator">
			</div>
			<%
				/* se viene sollevata un'eccezione significa che non sono stati trovati risultati */
				try {
			%>
				<div class="result">
					<div class="result-header">
						<span class="result-info"><%=list.size() %> Risultati per : <%=query %></span>
					</div>
					<div class="result-list">
					<%
						int start = ((currentPage - 1) * MAX_RESULT_PAGE);
						int end = start + MAX_RESULT_PAGE;
						for (int i = start; (i < list.size()) && (i < end); i++) {
							DescrittoreEsercizio desc = list.get(i);
							String dettaglio = "";
							dettaglio += desc.getTipologie();
							if (desc.getReferente() != null)
								dettaglio += " - Referente: "+desc.getReferente();
					%>
					<div class="result-row">
						<div class="data">
							<span class="nome">
								<%=desc.getNome() %>
							</span>
							<span class="indirizzo">
								<%="Indirizzo: "+desc.getIndirizzo()+", "+desc.getCitta() %>
							</span>
							<span class="telefono">
								<%="- Tel: "+desc.getTelefono() %>
							</span>
							<span class="dettaglio">
									<%=dettaglio %>
							</span>
							<span class="sitoweb" title="Visit <%=desc.getSitoWeb() %>"  onclick="openLink('<%="http://"+desc.getSitoWeb()%>')">
								<%=desc.getSitoWeb() %>
							</span>
						</div>
					</div>
					<% 	
						}  	// fine ciclo for
					%>
					</div>	
					<div class="result-header">
						<%
							if (numPage > 1) {
						%>
								<!-- Visualizzo le pagine disponibili -->
								<span class="pagine">Pagine: 
									<% 
										for (int i = 1; i <= numPage; i++) {
											if (currentPage == i) {
												
									%>
											<span class="paginacorrente"><%=i %></span>
									<% 
											}
											else {
									%>
											<span class="pagina" onclick="displayNextPage('<%=query%>', '<%=i%>')"><%=i %></span>
									<%
											}
									%>
								</span>
					<%	
								} 	// fine ciclo for
							}	
					%>
						
					</div>
				</div>
				<div class="separator">
				</div>
			<%	
				} 
				catch (Exception e) {}
			%>
			<div class="container-footer">
				<span title="Sito Ufficiale AIC" onclick="openLink('http://celiachia.it')">fonte : www.celiachia.it</span>
			</div>
		</div>
</body>
	<%
		/* libero la sessione */
		session.removeAttribute("result");
		session.removeAttribute("query");
		session.removeAttribute("displayPage");
	%>
</html>