import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    private static final int INTERVAL = 2; //in seconds
    public static void main(String [] args) throws IOException, InterruptedException{
        Controller controller = new Controller();
        FileInputStream serviceAccount = new FileInputStream("C:/Users/sypa1/Desktop/Inżynierka/klucz.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://smarthouse-cccb7.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);
        while (true){
            controller.work();
            Thread.sleep(INTERVAL*1000);
        }
    }

}
