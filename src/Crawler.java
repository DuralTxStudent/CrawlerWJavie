import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

public class Crawler {

    public static void main(String[] args)
    {

        try
        {
            System.out.println("Wpisz adres strony, którą chcesz pobrać, bez części https://www.");
            Scanner scanner = new Scanner(System.in);
            String url = "https://www." + scanner.nextLine();
            crawl(1, url, new ArrayList<>());
            PrintStream out = new PrintStream(new FileOutputStream("linki.txt"));
            PrintStream err = new PrintStream(new FileOutputStream("errors.txt"));
            System.setOut(out);
            System.setErr(err);

        }

        catch (IOException e)
        {
            System.err.println("Wystąpił błąd: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    }

    public static void crawl(int level, String url, ArrayList<String> visited)
    {
        if(level <= 3)
        {
            Document doc = request(url, visited);
            if(doc != null)
            {
                for(Element link : doc.select("a[href]"))
                {
                    String next_link = link.absUrl("href");
                    if(!visited.contains(next_link))
                    {
                        crawl(level +1, next_link, visited); //Można też zastosować level +1
                    }
                }
            }
        }
    }

    public static Document request(String url, ArrayList<String> v) {
        try {
            Connection con = Jsoup.connect(url);
            Document doc = con.get();
            Elements images = doc.select("img");
            int imageCount = 0;

            if (con.response().statusCode() == 200)
            {
                System.out.println("Link: " + url);
                System.out.println(doc.title());
                v.add(url);

                for (Element img : images)
                {
                    String imgUrl = img.absUrl("src");
                    System.out.println("Pobieramy: " + imgUrl);

                    // Tworzenie nazwy pliku z numeracją
                    String fileName = "Pobrany_obraz_" + imageCount + ".jpg";
                    downloadImage(imgUrl, fileName);
                    imageCount++;
                }

                return doc;
            }
            return null;
        }
        catch (IOException e)
        {
            System.err.println("Błąd przy łączeniu z: " + url + " - " + e.getMessage());
            return null;
        }
    }
    public static void downloadImage(String imageUrl, String fileName)
    {
        try {
            if (imageUrl == null || imageUrl.isEmpty())                         /* Unikamy błędu: Exception in thread "main" java.lang.IllegalArgumentException: URI is not absolute */
            {
                System.err.println("Niepoprawny adres URL obrazu: " + imageUrl);
                return;
            }
            URI uri = new URI(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.connect();


            if (connection.getResponseCode() == 200)
            {
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(fileName);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1)
                {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
                System.out.println("Obraz został poprawnie pobrany: " + fileName);
            } else {
                System.out.println("Błąd z pobraniem: " + connection.getResponseCode());
            }
        } catch (URISyntaxException e) {
            System.err.println("Błąd URI: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Błąd IO: " + e.getMessage());
        }
    }
}

