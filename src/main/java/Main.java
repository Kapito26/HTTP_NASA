import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
    public static final String REMOTE_SERVICE_URI = "https://api.nasa.gov/planetary/apod?api_key=4AAWgRQ09FjcfGKMFckBkKJpyYtn4HgZCoIVN22G";
    public static ObjectMapper mapper = new ObjectMapper();

    public static void saveInFile(byte[] buffer, String file) {
        try (FileOutputStream out = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(out)) {
// производим запись от 0 до последнего байта из массива
            bos.write(buffer, 0, buffer.length);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build()) {

            // создание объекта запроса с произвольными заголовками
            HttpGet request = new HttpGet(REMOTE_SERVICE_URI);
            request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

            // отправка запроса
            try (CloseableHttpResponse response = httpClient.execute(request)) {

                //    String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                //    System.out.println(body);

                NASAResponse nasaResponse = mapper.readValue(
                        response.getEntity().getContent(), new TypeReference<NASAResponse>() {
                        }
                );
                System.out.println(nasaResponse);

                // чтение содержимого по указанному URL
                String URL = nasaResponse.getUrl();
                HttpGet requestURL = new HttpGet(URL);
                try (CloseableHttpResponse responseURL = httpClient.execute(requestURL)) {
                    byte[] bodyURL = responseURL.getEntity().getContent().readAllBytes();
                    //System.out.println(URL);
                    String[] urlArr = URL.split("/");
                    String fileName = urlArr[urlArr.length - 1];

                    // сохранение в файл
                    saveInFile(bodyURL, fileName);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
