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
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException {
        FileInputStream inputFile = new FileInputStream("resources/url_health_chk.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(inputFile);
        XSSFSheet sheet = workbook.getSheetAt(0);
        int size = sheet.getLastRowNum();
        //read from sheet
        System.out.println("Total Rows: " + size);
        for (int i = 1; i <= size;) {
            try {
                String url = sheet.getRow(i).getCell(0).getStringCellValue();
                System.out.println(i + ". Processing URL " + url);
                Document doc = Jsoup.connect(url).get();
                sheet.getRow(i).getCell(1).setCellValue("200:OK");
                Elements links = doc.select("a[href]");
                //canonical url
                sheet.getRow(i).getCell(2).setCellValue(checkForCanonicalURL(doc, url));
                //metatag check
                sheet.getRow(i).getCell(3).setCellValue(metatags(doc,"noindex"));
                sheet.getRow(i).getCell(4).setCellValue(metatags(doc,"nofollow"));
                String result = "";
                for (int j = 0; j < links.size(); j++) {
                    try {
                        result += checkURLHealth(links.get(j).attr("href"));
                    } catch (Exception e) {
                        j++;
                    }
                }
                //write data to sheet
                sheet.getRow(i).getCell(5).setCellValue(result);
                i++;
            } catch (IOException io) {
                sheet.getRow(i).getCell(1).setCellValue("Error");
                io.printStackTrace();
                i++;
            }

        }


        //save data to sheet
        FileOutputStream os = new FileOutputStream("resources/url_health_chk.xlsx");
        workbook.write(os);
        System.out.println("Writing on XLSX file Finished ...");
        System.out.println("URL Health Check Complete\n\n");

    }

    //captures all a[href's]
    public static String checkURLHealth(String url) {
        String result = "";
        Connection.Response response = null;
        try {
             Document d = Jsoup.connect(url).get(); //checks for URL
             response = Jsoup.connect(url).execute();
            result += url + " :: 200 OK \n";
        } catch (Exception exception) {
            result += url + "::" +response.statusCode()+  "\n";
        }
        return result;
    }

    public static String checkForCanonicalURL(Document doc, String parentURL) {
        String cssQuery = "link[rel=canonical][href="+parentURL+"]";
        return doc.select(cssQuery).first() == null ? "No Canonical URL Present" : parentURL;
    }

    public static boolean metatags(Document doc, String meta){
        String cssQuery="meta[content*="+meta+"]";
        Elements e = doc.select(cssQuery);
        return e.isEmpty()?false:true;
    }

}
