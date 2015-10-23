package lineserver;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Omar
 * The FileProcessor class is instantiated with a file name to process,
 * this file is walked through linearly to calculate the offsets of each
 * line and store this in an index file. The index file stores each offset
 * into an 8 byte value (long). A convenient getLine method is available
 * to retrieve a line at any given index using these offsets. This allows
 * random access to an address in the file without having to linearly
 * traverse through the entire file.
 */
public class FileProcessor {
	private static String readFileName = null;
	private static File readFile = null;
	
	private static String indexFileName = null;
	private static File indexFile = null;
	
	public FileProcessor(String name) {
		readFileName = name;
		readFile = new File(readFileName);
		indexFileName = "index_" + name;
		indexFile = new File(indexFileName);
		
		/**
		 * If the index file already exists there is no need to re-create it.
		 * This assumes that the contents of any text file with a given name
		 * will not change. 
		 */
		if (indexFile.isFile()) {
			System.out.println("The index file has already been created!");
		} else {
			createIndexFile();	
		}
	}
	
	/**
	 * This method uses the index file to retrieve the offset of the address
	 * pointing to the line at that index and returns this line. The expense
	 * of this method is computationally O(1) i.e. the constant time required
	 * to open the two RandomAccessFile's and seek the position in the file.
	 * @param n the nth line to get
	 * @return the line at index n
	 * @throws IndexOutOfBoundsException if the requested index is less than
	 *         the total length of the file.
	 */
	public String getLine(int n) {
		String line = null;
		RandomAccessFile r = null;
		RandomAccessFile i = null;
		
		try {
			r = new RandomAccessFile(readFile, "r");
			i = new RandomAccessFile(indexFile, "r");
			
			long indexPos = (long)(n*8);
			i.seek(indexPos);
			
			long readPos = i.readLong();
			r.seek(readPos);
			
			line = r.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		
		/* The RandomAccessFile's need to be closed when we're done. */
		} finally {
			if ((r != null) & (i != null)) {
				try {
					r.close();
					i.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (line != null) {
			return line;
		}
		/* Throw an index out of bounds exception to be caught by the server */
		else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	/**
	 * This method linearly goes through each line of the input file, measuring
	 * the offset for each line and stores it into an index file. If the line
	 * number is the key, then the offset is the value. This is calculated in
	 * the getLine method.
	 * Computationally this method runs in O(n)+k, where n is the number of 
	 * lines in the file and k is a constant covering the expense of other
	 * file io operations (opening, reading, calculating length)
	 */
	private void createIndexFile() {
		RandomAccessFile r = null;
		RandomAccessFile i = null;
		try {
			r = new RandomAccessFile(readFile, "r");
			i = new RandomAccessFile(indexFile, "rw");
			Long len = r.length();
			
			while (r.getFilePointer() < len) {
				Long ptr = r.getFilePointer();
				r.readLine();
				i.writeLong(ptr);
				i.seek(i.length());
			}
		} catch (IOException e) {
			e.printStackTrace();
			
		/* The RandomAccessFile's need to be closed when we're done. */
		} finally {
			if ((r != null) & (i != null)) {
				try {
					r.close();
					i.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
