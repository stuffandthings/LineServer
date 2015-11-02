## LineServer

The LineServer consists of two main componenets:
- A Tomcat Servlet that serves up a line in a file
- A file processor that creates an index for the file to serve

The Servlet is pretty basic, and extends the HTTPServlet class, overriding the init method
to instantiate the file processor class so we can create the index upon starting the server.
Since ths Tomcat Servlet doesn't exactly run as a simple command line tool and needs an
xml file, we use this xml file to input any command line arguments, using sed in the run.sh
script to substitute the user provided file name as a parameter to the Servlet. The doGet
method uses the FileProcessor object to jump to the provided index in constant time using
fille address pointer offsets stored in the index file. The status code is set to 200 and 
the line at the requested index is returned unless the index is out of bounds in the file, 
in which case the status code is set to 413 and nothing is given back to the client.

The File Processor checks to see if we already have an index for the provided file. This
check is not thorough, it assumes that any test files used multiple times will not change 
over different runs. Supposing a fresh first time run, the file is opened and traversed
linearly in order to measure the address offsets for each line. These offsets are then
stored as 8 byte longs in a newly created index file. Since the given file can be more
or less arbitrarily large creating this index can take some time (O(n) time to traverse
the entire file, and some additional time k for all the file i/o stuff). 

## System Performance:

The system should perform reasonably well for the client. In other words, the client
will receive the requested line with an acceptable delay. The server itself will require
some time for large text files, since the first run will need to create an index for the
file. This means the entire file would need to be walked through byte by byte, per line, 
till it sees the newline delimitter and knows to calculate the offset. The larger the file,
the longer the initial index building will take. Once the server is up and running however,
since we have an index file where each offset is of constant length (8 bytes) we can
calculate the offset of any particular index without having to walk through the entire 
index file. We measure the offset in the index file by multiplying the given line number
by 8 and jump directly to it using the Java RandomAccessFile seek method. This gives us the 
offset in the main text file at that line number, which we can now directly access using
the seek method.
### Performance estimates:
Assumption - each line is 32 bytes
1 GB file = 31,250,000 lines
It would take O(31,250,000) + k, where k is some time constant, time to process the file.
All other operations (like the doGet method and all methods called from doGet) will be
some in constant time as well.
We can generalize the time taken to be:
O(n+k), for any number of lines n.
Thus the time taken for a 10 GB or 100 GB file should be in direct linear
correlation.
Additionally, we can control the buffer size for opening files and make it larger
for larger files giving us some additional speed.

Since the library I used (Tomcat HTTPServlet) to handle the web server handles multiple clients, 
I did not have to implement any kind of threading. Issues could arise from shared resources (like 
the text file and the index file), but since the requests will only be reading from these files 
we don't need to synchronize access to these resources or worry about race conditions. There
is obvious overhead and performance degradation with an increased number of clients.
I have not modified the Tomcat default load settings, and so suppose this limit is 200;
after 200 concurrent requests the server will start queing up requests. With something like
1,000,000 users, the first requests to come in would be pretty fast and the last requests
for take a while to serve up.
 
How this server can be improved:
As mentioned above for a large number or concurrent requests there is a considerable performance
cost. This can be drastically reduced by using load balancers, and having multiple servers
start serving up requests after the first server load gets too much to handle for one machine.
While the line retrieval performance cost is reasonably low, there is a considerable cost of
opening and closing files for each request. Implementing a cache would reduce this average cost
depending on how many of the clients are requesting similar lines (most of them probably want
line number 42 anyway). 
To further optimize this system to perform on extremely large files a map-reduce framework 
could be implemented. Essentially, the large file could be processed into many smaller chunks.
Several worker servers could each handle one of these chunks. A master server could map 
incoming requests to the correct worker server. Building a cache on top of this would give us a
very performant server capable of enterprise level line serving.
If I had unlimited more time, I would probably prioritize making small tweaks and better code
quality first, and then researching existing frameworks and libraries for caching and map-reduce.

### List of Resources used:

http://stackoverflow.com/questions/8231631/creating-a-simple-index-on-a-text-file-in-java

http://docs.oracle.com/javase/8/docs/api/java/io/RandomAccessFile.html

https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletResponse.html

http://docs.oracle.com/javaee/6/tutorial/doc/bnadr.html

https://tomcat.apache.org/tomcat-7.0-doc/jndi-resources-howto.html

http://people.apache.org/~mturk/docs/article/ftwai.html

http://tutorials.jenkov.com/java-servlets/web-xml.html

Additionally, the #Java channel on IRC was very helpful in conceptually explaining servlets.
