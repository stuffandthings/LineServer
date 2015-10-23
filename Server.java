package lineserver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Omar
 * This class extends the HttpServlet, overriding the init() method to get the
 * file name that was passed in through the run.sh script and create a 
 * fileprocessor class to handle the main lineserver filesystem logic.
 */
public class Server extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String fileName = null;
	private FileProcessor fp = null;
	
	@Override
	public void init() throws ServletException {
		ServletContext context = getServletContext(); 
		fileName = context.getInitParameter("inputFileName"); 
		fp = new FileProcessor(fileName);
	}
	
	/**
	 * As the name suggests, doGet takes care of the GET request by parsing
	 * out the index, and fetching the line from the file. If the line is 
	 * found in the file the status code is set to 200, and the line is sent
	 * to the client. If the index is out of bounds, the status code is set
	 * to 413.
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) 
				throws ServletException, IOException {
	
		String sp = req.getPathInfo();
		int lineNum = Integer.parseInt(sp.substring(1));
		String line = null;
		
		try {
			line = fp.getLine(lineNum);
			res.setStatus(200);
			PrintWriter pw = res.getWriter();
			pw.println(line);
			pw.close();
			
		} catch (IndexOutOfBoundsException e) {
			res.setStatus(413);
			PrintWriter pw = res.getWriter();
			pw.println("Requested index out of bounds.");
			pw.close();
		}	
	}
}
