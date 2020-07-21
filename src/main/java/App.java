import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        FileInputStream inputFile = new FileInputStream("src/url_health_chk.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(inputFile);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int size = sheet.getLastRowNum();

        //read from sheet
        System.out.println("Total Rows: " + size);
        for (int i = 1; i <= size; i++) {
            try {
                //Row row=sheet.getRow(i);
                String url = sheet.getRow(i).getCell(0).getStringCellValue();
                System.out.println(i + ". Processing URL " + url);
                Document doc = Jsoup.connect(url).get();
                sheet.getRow(i).getCell(1).setCellValue("200:OK");

                Elements links = doc.select("a[href]");
                if (checkForCanonicalURL(doc, url))
                    sheet.getRow(i).getCell(2).setCellValue(true);
                else
                    sheet.getRow(i).getCell(2).setCellValue(false);

                String result = "";
                for (int j = 0; j < links.size(); j++) {
                    try {
                        result += checkURLHealth(links.get(j).attr("href"));
                    } catch (Exception e) {
                        //System.out.println(links.get(i).attr("href")+" :: 400 FAIL");;
                        j++;
                    }
                }
                //write data to sheet
                //System.out.println("Child Links : \n" + result);
                sheet.getRow(i).getCell(3).setCellValue(result);
            } catch (IOException io) {

                sheet.getRow(i).getCell(1).setCellValue("Error");
                io.printStackTrace();
                i++;
            }

        }


        //save data to sheet
        FileOutputStream os = new FileOutputStream("src/url_health_chk.xlsx");
        workbook.write(os);
        System.out.println("Writing on XLSX file Finished ...");


        System.out.println("URL Health Check Complete\n\n");

    }

    //captures all a[href's]
    public static String checkURLHealth(String url) {
        String result = "";
        try {
            Jsoup.connect(url).get(); //checks for URL
            //we are getting a valid response after hitting the URL:
            //System.out.println(url + " :: 200 OK \n\n");
            result += url + " :: 200 OK \n";
        } catch (Exception exception) {
            //System.out.println(url+ ":: Broken Link");
            //System.out.println(exception+"\n\n");
            result += url + ":: Broken Link \n";
        }
        return result;
    }

    public static boolean checkForCanonicalURL(Document doc, String parentURL) {
        Element e = doc.select("link[rel=canonical]").first();
        if (null != e)
            System.out.println(e);
        else
            System.out.println("No canocial URL present in " + parentURL);

        return e == null ? false : true;
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width - 1) + ".";
        else
            return s;
    }

}
