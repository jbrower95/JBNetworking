# JBNetworking
JayboneNetworking - An HTTP library for Android for using APIs easily.

- Setup:

    1. Make sure you've included 'Otto' in your project. This is an easy 1-line setup with gradle
              (http://square.github.io/otto/)
              
    2.  
          a) I already have an application subclass in my project!
                - Make your application subclass inherit from JayboneApp
          b) I don't have an application subclass in my project!
                - Set up an application subclass  
                          1. Go to your AndroidManifest.xml
                          2. Find your <application ...> tag
                          3. Add "android:name=?" , where ? is jbnetworking.JayboneApp
    3. You have routes that you're trying to get data from easily. Represent those in code.
          a) Create a collection somewhere of Routes. Use RouteUtil.makeRoute() to quickly
             make routes. 
             
                  final Route google_search = RouteUtil.makeRoute("/q=%s", RequestType.GET);
                  
          b) Set the API prefix for your project. 
          
                  RouteUtil.setApiPrefix("http://google.com");
                  
          c) Create a 'Consumer' for the response. Subclassing Consumer will allow you to provide custom behaviour
            for accepting the response of the server (and providing behaviour for failed connections).
            
          d) Make a Request. The Request class uses a builder pattern -- Check out the .Builder class for more information.
          
          Request webRequest = Request.Builder.withRoute(google_search)
                         .withParameters(new String[]{"test"})
                         .withAcceptType("text/html")
                         .withConsumer(new SearchConsumer())
                         .build();
            
          e) Fire off the request. Call webRequest.start()! 
          
          f) To actually get the results of your network call, your consumer will need to use
            
                JayboneApp.getBus().post(  .... )
                
            With a subclass of event (BaseApiFailureEvent | BaseApiSuccessEvent)
          
          g) In your activity / fragment,
          
                During onStart: Call JayboneApp.getBus().register(this)
                During onStop: Call JayboneApp.getBus().unregister(This)
                
                Add methods using Otto's @Subscribe annotation to listen for specific ApiEvent messages.
                
    4. Add more routes, and build up your app!
                    
          
    
    






