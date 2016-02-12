package jaybone.jbnetworking.route;

import jaybone.jbnetworking.network.RequestType;

/**
 * All of the different Communikey API routes.
 * <p/>
 * An API route is formed by taking the API prefix (mPrefix) and tacking it onto the provided endpoint (the argument to the enum).
 * Convenience methods are available for applying arguments to the endpoints (each '%s' token gets replaced).
 * <p/>
 * Created by Justin on 8/31/15.
 */
public final class RouteUtil {

    // The API prefix.
    private static String mPrefix;

    /* Sets the API prefix for the requests being made. Can be used to swap between servers. */
    public static void setApiPrefix(String prefix) {
        mPrefix = prefix;
    }

    /**
     * Returns the routes full URL, with no parameters applied.
     * Throws an exception is parameters are required.
     *
     * @return The full URL of the route.
     */
    public static String getRoute(final Route route) {
        if (requiresParameters(route)) {
            throw new IllegalArgumentException("Error: This route requires parameters.");
        }

        return mPrefix + route.getEndpoint();
    }

    /**
     * Returns the routes full URL, with parameters applied.
     *
     * @param route      The route to apply parameters to.
     * @param parameters An array of strings to substitute into the URL as parameters. Must be in the order in which they are applied to the string, from left to right.
     * @return The full, substituted route.
     */
    public static String getFullRouteWithParameters(final Route route, String[] parameters) {
        return mPrefix + String.format(route.getEndpoint(), (Object[]) parameters);
    }

    /**
     * @return True if the route requires parameters (contains %s at least once basically).
     */
    public static boolean requiresParameters(final Route route) {
        return !String.format(route.getEndpoint(), "test").equals(route.getEndpoint());
    }

    /**
     * Makes an endpoint, to avoid creating classes implementing Route over and over again.
     *
     * @param url  The endpoint of the URL.
     * @param type The endpoint HTTP request type.
     * @return The Route.
     */
    public static Route makeRoute(final String url, final RequestType type) {
        return new Route() {
            @Override
            public String getEndpoint() {
                return url;
            }

            @Override
            public RequestType getRequestType() {
                return type;
            }
        };
    }
}
