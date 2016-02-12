package jaybone.jbnetworking.network;

import android.text.TextUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jaybone.jbnetworking.consumer.Consumer;
import jaybone.jbnetworking.route.Route;
import jaybone.jbnetworking.route.RouteUtil;

/* The HTTP method of the request being made. */


/**
 * A thread for making network connections and reporting them back.
 * <p/>
 * Usage:
 * To build the thread:
 * Use the Builder static class w/ method chaining, followed by a call to .build().
 * See API for sample usages.
 * <p/>
 * To run the thread: call networkThread.start()
 * This will spawn a thread and start doing work. The consumer that the user
 * supplied will be invoked once the network request has been serviced.
 * <p/>
 * To cancel the thread: call networkThread.cancel()
 * Cancellation does not necessarily imply that
 * the thread will be cancelled. There are several cancellation
 * points throughout the execution of the thread, but the actual HTTP
 * request is currently not one of them. A future implementation may wish
 * to cancel the network request (need to change HTTP libraries for this).
 * <p/>
 * To discern between different requests:
 * NetworkThreads have request_ids associated with them. This is drawn from an atomic pool (see NetworkThread.request_id)
 * for thread safety.
 * <p/>
 * Created by Justin on 8/31/15.
 */
public class Request implements Runnable {

    // Count up to MAX_VALUE - 1 before resetting our transaction ids.
    private static final int MAX_TRANSACTION = Integer.MAX_VALUE - 10;
    private static AtomicInteger request_id = new AtomicInteger(0);
    private String url;
    private String jsonToSend;
    private RequestType method;
    private Consumer consumer;
    private HttpUriRequest mRequest;
    private Thread mThread;
    private final AtomicBoolean mAbortFlag = new AtomicBoolean(false);
    private String mAcceptType;
    private String mContentType;
    private Map<String, String> mHeaders;
    private int transaction_id;

    private boolean mLoggingEnabled = false;

    /**
     * Sets whether or not the API will log requests.
     *
     * @param enabled true if the API should log requests o.w false.
     */
    public void setLoggingEnabled(boolean enabled) {
        mLoggingEnabled = enabled;
    }

    /**
     * Determines whether or not a {@link Request} is running.
     *
     * @param thread The thread to check.
     * @return True if it is running.
     */
    public static boolean isRunning(final Request thread) {
        return (thread != null && thread.mThread.isAlive());
    }

    /**
     * Cancels the network request.
     */
    public void cancel() throws UnsupportedOperationException {
        if (mRequest != null) {
            // the thread is already chugging along.
            mRequest.abort();
        } else {
            synchronized (mAbortFlag) {
                mAbortFlag.set(true);
            }
        }
    }

    @Override
    public void run() {

        try {
            final HttpClient client = new DefaultHttpClient();

            if (mLoggingEnabled) {
                System.out.println("[Network] (" + transaction_id + ") Connecting to url: " + url);
            }

            StringEntity jsonEntity = null;

            if (!TextUtils.isEmpty(jsonToSend)) {
                jsonEntity = new StringEntity(jsonToSend);
                jsonEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            }

            if (mAbortFlag.get()) {
                return;
            }

            switch (method) {
                case METHOD_GET:
                    if (mLoggingEnabled) {
                        System.out.println("[Network] (" + transaction_id + ")  HTTP Type: GET");
                    }
                    mRequest = new HttpGet(url);
                    break;
                case METHOD_PUT:
                    if (mLoggingEnabled) {
                        System.out.println("[Network] (" + transaction_id + ")  HTTP Type: PUT");
                        System.out.println("[Network] JSON: ");
                    }
                    System.out.println(jsonToSend);
                    mRequest = new HttpPut(url);
                    ((HttpPut) mRequest).setEntity(jsonEntity);
                    break;
                case METHOD_POST:
                    if (mLoggingEnabled) {
                        System.out.println("[Network] (" + transaction_id + ")  HTTP Type: POST");
                    }
                default:
                    if (mLoggingEnabled) {
                        System.out.println("[Network] (" + transaction_id + ")  Request payload (JSON): ");
                        System.out.println("[Network] JSON: " + jsonToSend);
                    }
                    mRequest = new HttpPost(url);
                    ((HttpPost) mRequest).setEntity(jsonEntity);
                    break;
            }

            mRequest.setHeader("Accept", mAcceptType);
            mRequest.setHeader("Content-Type", mContentType);

            if (mHeaders != null) {
                // Apply custom headers.
                for (String key : mHeaders.keySet()) {
                    mRequest.setHeader(key, mHeaders.get(key));
                }
            }

            if (mLoggingEnabled) {
                System.out.println("[Network] (" + transaction_id + ")  Created request..");
            }

            if (mAbortFlag.get()) {
                if (mLoggingEnabled) {
                    System.out.println("[Network] (" + transaction_id + ")  Aborted request.");
                }
                return;
            }

            final HttpResponse response = client.execute(mRequest);
            final HttpEntity responseObject = response.getEntity();

            final String statusCode = Integer.toString(response.getStatusLine().getStatusCode());
            if (mLoggingEnabled) {
                System.out.println("[Network] (" + transaction_id + ")  Received response (" + statusCode + ")..");
            }

            if (mAbortFlag.get()) {
                if (mLoggingEnabled) {
                    System.out.println("[Network] (" + transaction_id + ")  Aborted request.");
                }
                return;
            }

            consumer.consume(responseObject.getContent(), response, response.getStatusLine().getStatusCode(), new URL(url), transaction_id);
        } catch (final Exception e) {
            if (mLoggingEnabled) {
                System.out.println("[Network] (" + transaction_id + ")  Connection failed.");
                e.printStackTrace();
            }
            consumer.failed(e, transaction_id);
        }
    }

    /**
     * Starts the network connection.
     */
    public Request start() {
        // assign us a transaction id
        transaction_id = request_id.getAndIncrement();
        request_id.compareAndSet(MAX_TRANSACTION, 0);

        mThread = new Thread(this);
        mThread.start();

        return this;
    }

    /**
     * Used to construct instances of Request.
     */
    public static class Builder {

        private Route mRoute = null;
        private String mUrl;
        private String mJsonToSend;
        private String[] mParameters;
        private RequestType mMethod;
        private String mContentType = "application/json";
        private Consumer mConsumer;
        private String mAcceptType = "application/json";
        private Map<String, String> mHeaders = new HashMap<>();

        /**
         * The route to send the request to. Non optional.
         *
         * @param route The route to request.
         * @return Builder for chaining.
         */
        public Builder withRoute(final Route route) {
            mRoute = route;
            mMethod = route.getRequestType();
            return this;
        }

        /**
         * Sets the route parameters to be applied when generating a URL.
         *
         * @param parameters The URL parameters to apply.
         */
        public Builder withParameters(final String[] parameters) {
            mParameters = parameters;
            return this;
        }

        /**
         * Applies extra HTTP headers to the request. Content Type and Accept are already placed.
         *
         * @param headers the headers to apply. Replaces existing headers.
         * @return The builder for chaining.
         */
        public Builder withAdditionalHeaders(final Map<String, String> headers) {
            mHeaders = headers;
            return this;
        }

        /**
         * Sets the content header for what data to accept back.
         *
         * @param accept The accept header (e.g 'application/json'). application/json is present by default.
         * @return Builder for chaining.
         */
        public Builder withAcceptType(final String accept) {
            mAcceptType = accept;
            return this;
        }

        /**
         * Sets the data to post.
         *
         * @param data        Some data to post.
         * @param contentType The content type of the data.
         * @return Builder for chaining.
         */
        public Builder withData(final String data, final String contentType) {
            mJsonToSend = data;
            mContentType = contentType;
            return this;
        }

        /**
         * Specifies the Consumer to consume this requests' response.
         *
         * @param consumer The consumer to consume this response.
         * @return Builder for chaining.
         */
        public Builder withConsumer(final Consumer consumer) {
            mConsumer = consumer;
            return this;
        }

        /**
         * Attempts to construct a Request. will fail if the object has not been configured properly.
         *
         * @return The NetworkThread. Otherwise, throw IllegalArgumentException
         */
        public Request build() {

            if (mRoute == null) {
                throw new IllegalArgumentException("Must specify a route for connection.");
            }

            if (mParameters != null) {
                mUrl = RouteUtil.getFullRouteWithParameters(mRoute, mParameters);
            } else {
                mUrl = RouteUtil.getRoute(mRoute);
            }

            final Request thread = new Request();

            if (TextUtils.isEmpty(mUrl)) {
                throw new IllegalArgumentException("Url cannot be null in a network request.");
            }

            thread.url = mUrl;
            thread.jsonToSend = mJsonToSend;
            thread.method = mMethod;
            thread.consumer = mConsumer;
            thread.mAcceptType = mAcceptType;
            thread.mContentType = mContentType;
            thread.mHeaders = mHeaders;

            return thread;
        }
    }

}
