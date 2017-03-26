import java.util.*;
import java.io.*;
import java.net.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
public class Scraper {
   //The url of the website
     private static final String webSiteURL = "http://wallpaperswide.com/animals-desktop-wallpapers.html";
   
     //The path of the folder that you want to save the images to
     private static String folderPath = "C:/Users/Siddharth/workspace/Image Search Engine/bin/ImagesToAdd";
     static Integer lastPage = 10;
     static Integer LP = 10;
   
     public static void main(String[] args) throws UnknownHostException {
   
     Scanner in = new Scanner(System.in);
     //System.out.print("Please enter the folder path: ");
     final String path = folderPath; //in.nextLine();
     folderPath = path;
   
     System.out.print("Please enter the amount of pages to be scraped: ");
     final Integer lp = in.nextInt();
     LP = lp;
   
     // start downloading loop
     for (int i = 1; i <= LP; i++) {
         try {
   
             //Connect to the website and get the html - take a look at "page/" THis is how teh pages are seperated
             Document doc = Jsoup.connect(webSiteURL + "page/" + i).get();
   
             //Get all elements with img tag ,
             Elements img = doc.getElementsByTag("img");
   
             for (Element el : img) {
   
                 //for each element get the srs url
                 String src = el.absUrl("src");
   
                 System.out.println("Image Found!");
                 System.out.println("src attribute is : " + src);
   
                 getImages(src);
   
             }
   
         } catch (IOException ex) {
               System.err.println("There was an error: " + ex);
         }
      }
     in.close();
   }
   
     private static void getImages(String src) throws IOException {
   
        int indexname = src.lastIndexOf("/");
   
        if (indexname == src.length()) {
            src = src.substring(1, indexname);
        }
   
        indexname = src.lastIndexOf("/");
        String name = src.substring(indexname, src.length());
   
        System.out.println(name);
   
        //Open a URL Stream
        URL url = new URL(src);
        InputStream in = url.openStream();
   
        OutputStream out = new BufferedOutputStream(new FileOutputStream(folderPath + name));
   
        for (int b; (b = in.read()) != -1;) {
             out.write(b);
        }
        out.close();
        in.close();
    }

}