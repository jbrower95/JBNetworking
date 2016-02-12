package jaybone.jbnetworking.consumer;


import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.net.URL;

import jaybone.jbnetworking.Utils;

/**
 * The Consumer is used with Request to handle the content of network responses.
 * <p/>
 * A successful network request invokes .consume(),
 * while failed ones will invoke an implementation of .failed().
 * <p/>
 * Typically, consume(String, int) will be overridden
 * by a Consumer subclass for access to the string contents of the response.
 * <p/>
 * However, If raw data is needed (such as in the BitmapConsumer)
 * .consume(InputStream,HttpResponse,int) can be overridden for access to the network stream.
 * <p/>
 * Created by Justin on 8/31/15.
 */
public abstract class Consumer {
    /**
     * Tells the consumer to consume a response. This could lead to a db insert or an otto bus post.
     *
     * @param response     The response to parse.
     * @param responseCode the server response code (500, etc.)
     * @param connectionId the id of the particular connection being consumed
     */
    public abstract void consume(String response, int responseCode, int connectionId);

    /**
     * Tells the consumer to consume a response. This is a lower level call and is handled automatically most of the time.
     * Allows the overrider access to the bytes of the response if necessary.
     *
     * @param response     The response to parse.
     * @param responseCode the server response code (500, etc.)
     * @param location     The URL that was requested.
     * @param connectionId The id of the connection.
     */
    public void consume(InputStream stream, HttpResponse response, int responseCode, URL location, int connectionId) {
        final String result = Utils.consumeInputStream(stream);

        if (result != null) {
            consume(result, response.getStatusLine().getStatusCode(), connectionId);
        } else {
            failed(response.getStatusLine().getStatusCode(), connectionId);
        }
    }

    /**
     * Tells the consumer that an error has occurred.
     *
     * @param e            The exception that occurred.
     * @param connectionId The id of the connection that experienced an exception
     */
    public abstract void failed(Exception e, int connectionId);

    /**
     * Tells the consumer that an error occurred during normal
     * execution.
     *
     * @param statusCode   The HTTP status of the connection
     * @param connectionId The id of the particular connection.
     */
    public abstract void failed(int statusCode, int connectionId);
}
