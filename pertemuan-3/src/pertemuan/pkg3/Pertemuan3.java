package pertemuan.pkg3;

/**
 *
 * @author fadils
 */
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
public class Pertemuan3 {

    /**
     * @param args the command line arguments
     */
  
        public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();
String APIKEY = "1b932005eeba5e1c9bb6560908bdf2c0";
        Request request = new Request.Builder()
                .url("https://api.exchangerate.host/live?access_key=1b932005eeba5e1c9bb6560908bdf2c0&currencies=USD,AUD,CAD,PLN,MXN&format=1")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Response: " + response.body().string());
        }
    }

    
}
