import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintStream;

public class Crawler {

    public static void main(String[] args) {

        try {
        System.out.println("Wpisz adres strony, którą chcesz pobrać, bez części https://www.");
        Scanner scanner = new Scanner(System.in);
        String url = "https://www." + scanner.nextLine();
        crawl(1, url, new ArrayList<>());
            PrintStream out = new PrintStream(String.valueOf(new FileWriter("info.txt")));
            PrintStream err = new PrintStream(String.valueOf(new FileWriter("errors.txt")));
            System.setOut(out);
            System.setErr(err);

        } catch (IOException e) {
            System.err.println("Wystąpił błąd: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void crawl(int level, String url, ArrayList<String> visited)
    {
        if(level <= 3) {
            Document doc = request(url, visited);
            if(doc != null) {
                for(Element link : doc.select("a[href]")) {
                    String next_link = link.absUrl("href");
                    if(!visited.contains(next_link)) {
                        crawl(level ++, next_link, visited);
                    }
                }
            }

        }
    }

    private static Document request(String url, ArrayList<String> v) {
        try {
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            if(con.response().statusCode() == 200) {
                System.out.println("Link: " + url);
                System.out.println(doc.title());
                v.add(url);

                return doc;

            }
            return null;
        }
        catch(IOException e) {
            System.err.println("Błąd przy łączeniu z: " + url + " - " + e.getMessage());
            return null;

        }
    }

}
