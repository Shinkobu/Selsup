import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        CrptApi.JsonDocument jsonDocument = CrptApi.JsonHandler.convertJsonToDocument("test.json");
        CrptApi crptApi = new CrptApi(Duration.ofSeconds(1), 1, "https://ismp.crpt.ru/api/v3/lk/documents/create")
                .requestLimiter();


        for (int i = 1; i <= 10; i++) {
            System.out.println("Attempt - " + i);
            crptApi.createDocument(jsonDocument);
        }

//      конвертация объекта в json и запись в файл
//        String s = CrptApi.JsonHandler.convertToJson(jsonDocument);
//        try (FileWriter fileWriter = new FileWriter("result.json")) {
//            fileWriter.write(s);
//        }
//      тест на корректность создания json
//        System.out.println(filesCompareByByte(Path.of("test.json"), Path.of("result.json")));

    }

    public static long filesCompareByByte(Path path1, Path path2) throws IOException {
        try (BufferedInputStream fis1 = new BufferedInputStream(new FileInputStream(path1.toFile()));
             BufferedInputStream fis2 = new BufferedInputStream(new FileInputStream(path2.toFile()))) {

            int ch = 0;
            long pos = 1;
            while ((ch = fis1.read()) != -1) {
                if (ch != fis2.read()) {
                    return pos;
                }
                pos++;
            }
            if (fis2.read() == -1) {
                return -1;
            } else {
                return pos;
            }
        }
    }
}
