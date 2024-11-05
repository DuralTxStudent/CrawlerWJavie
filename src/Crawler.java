import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

public class Crawler {

    public static void main(String[] args)
    {

        try (PrintStream out = new PrintStream(new FileOutputStream("linki.txt"));
             PrintStream err = new PrintStream(new FileOutputStream("errors.txt"));
             Scanner scanner = new Scanner(System.in))
        {

            System.out.println("Wpisz adres strony, którą chcesz pobrać, bez części https://www.");

            String input = scanner.nextLine();
            String url = "https://" + input;
            System.setOut(out);
            System.setErr(err);
            crawl(1, url, input, new ArrayList<>());
            // Flush służy do natychmiastowego zrzutu zawartości przed zamknięciem, wcześniej zawartość była zrzucana po zakończeniu crawlingu. Jeśli nie będę chciał by zawartośc szła w trakcie, dezaktywować te dwie linijki
            out.flush();
            err.flush();


        }

        catch (IOException e)
        {
            System.err.println("Wystąpił błąd: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    }

    public static void crawl(int level, String url, String input,  ArrayList<String> visited)
    {
        if(level <= 3)
        {
            Document doc = request(url, input, visited);
            if(doc != null)
            {
                for(Element link : doc.select("a[href]"))
                {
                    String next_link = link.absUrl("href");
                    if(!visited.contains(next_link))
                    {
                        crawl(level +1, next_link, input, visited);
                    }
                }
            }
        }
    }

    public static Document request(String url, String input, ArrayList<String> v) {
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

                    // Tworzenie folderu o nazwie wprowadzona strona + data
                    String folderName = generateFolderName(input);
                    createDirectory(folderName);


                    String fileName = generateUniqueFileName(imgUrl, imageCount);
                    downloadImage(imgUrl, folderName + File.separator + fileName);
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
    private static String generateFolderName(String input) {
        String safeName = input.replaceAll("[^a-zA-Z0-9]", "_");
        String timestamp = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
        return safeName + "_" + timestamp;
    }

    private static void createDirectory(String folderName) {
        File dir = new File(folderName);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Folder utworzony: " + folderName);
            } else {
                System.err.println("Nie udało się utworzyć folderu: " + folderName);
            }
        } else {
            System.out.println("Folder już istnieje: " + folderName);
        }
    }

    private static String generateUniqueFileName(String imgUrl, int count) {
        String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
        fileName = fileName.replaceAll("[^a-zA-Z0-9.]", "_");

        // Licznik czasu by uczynić to łatwiejszym do indeksowania
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return fileName.isEmpty() ? "_Pobrany_obraz_" + count + "_" + timestamp + ".jpg" : fileName + "_" + timestamp + ".jpg";
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

