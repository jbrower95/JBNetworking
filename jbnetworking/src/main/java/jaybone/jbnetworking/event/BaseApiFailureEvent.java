package jaybone.jbnetworking.event;

import jaybone.jbnetworking.Utils;

/**
 * A subclassable notation for Api failure. For each type of Api Failure,
 * subclass this.
 * Created by Justin on 9/3/15.
 */
public class BaseApiFailureEvent extends ApiEvent<Exception> {

    /**
     * Initializes an ApiFailure with an exception.
     *
     * @param e The associated exception.
     */
    public BaseApiFailureEvent(Exception e) {
        super(e);
    }

    /**
     * Initializes an ApiFailure from an error reason.
     *
     * @param e The type of failure experienced.
     */
    public BaseApiFailureEvent(String e) {
        super(new Exception(e));
    }

    /**
     * Shows an error by producing a toast.
     */
    public void showError() {
        Utils.showToast(getData().getLocalizedMessage());
    }
}
