package jaybone.jbnetworking;

import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Some random utils.
 * Created by Justin on 8/31/15.
 */
public final class Utils {
    /**
     * Consumes an input stream by reading it into a string.
     * @param inputStream The input stream to consume
     * @return The contents represented as a string.
     */
    public static String consumeInputStream(final InputStream inputStream){
        try {
            final BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));

            String line = "";

            final StringBuilder result = new StringBuilder();

            while((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }

            inputStream.close();
            return result.toString();
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Shows a toast with a given message.
     * @param message The message to display.
     */
    public static void showToast(final String message) {
        Toast.makeText(JayboneApp.getAppContext(), message, Toast.LENGTH_LONG).show();
    }
}
