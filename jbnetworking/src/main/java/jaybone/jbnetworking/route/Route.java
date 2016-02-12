package jaybone.jbnetworking.route;

import jaybone.jbnetworking.network.RequestType;

/**
 * Represents a route.
 *
 * A route is categorized by its endpoint ("/target/endpoint") and the method used
 * to reach this endpoint (GET, POST, PUT).
 *
 * Created by Justin on 2/12/16.
 */
public interface Route {
    /* Returns the endpoint of this route. This can contain format specifiers! (only %s is supported)
    *   Ex: "/cookies/bake"
    *       "/auth/%s"
    */
    String getEndpoint();

    /**
     * Returns the HTTP request type of this request.
     *
     * @return An HTTPRequestType (PUT, GET, POST)
     */
    RequestType getRequestType();
}
