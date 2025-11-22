import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;



@WebServlet("/SPARQLEndpointServlet")
public class SPARQLEndpointServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String repoUrl = request.getParameter("repoUrl");
        String repoId = request.getParameter("repoId");
        String sparqlQuery = request.getParameter("query");

        response.setContentType("text/html");
        StringBuilder htmlResult = new StringBuilder();

        Repository repo = new HTTPRepository(repoUrl, repoId);
        repo.init();

        try (RepositoryConnection conn = repo.getConnection()) {
            Query query = conn.prepareQuery(QueryLanguage.SPARQL, sparqlQuery);

            if (query instanceof TupleQuery) {
                TupleQueryResult result = ((TupleQuery) query).evaluate();
                htmlResult.append("<table><tr>");
                result.getBindingNames().forEach(name -> htmlResult.append("<th>").append(name).append("</th>"));
                htmlResult.append("</tr>");

                while (result.hasNext()) {
                    htmlResult.append("<tr>");
                    BindingSet bs = result.next();
                    result.getBindingNames().forEach(name -> htmlResult.append("<td>").append(bs.getValue(name)).append("</td>"));
                    htmlResult.append("</tr>");
                }
                htmlResult.append("</table>");
            } else if (query instanceof BooleanQuery) {
                boolean boolResult = ((BooleanQuery) query).evaluate();
                htmlResult.append("<p>ASK Result: ").append(boolResult).append("</p>");
            } else if (query instanceof GraphQuery) {
                GraphQueryResult gResult = ((GraphQuery) query).evaluate();
                htmlResult.append("<pre>");
                gResult.forEach(triple -> htmlResult.append(triple.toString()).append("\n"));
                htmlResult.append("</pre>");
            }

        } catch (Exception e) {
            htmlResult.append("<p style='color:red;'>Error: ").append(e.getMessage()).append("</p>");
        } finally {
            repo.shutDown();
        }

        response.getWriter().write(htmlResult.toString());
    }
}
