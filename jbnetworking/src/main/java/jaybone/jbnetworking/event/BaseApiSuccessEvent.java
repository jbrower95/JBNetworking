package jaybone.jbnetworking.event;

/**
 * This class contains some sort of processed data that was generated by a consumer.
 * The purpose of this class is for users to @subscribe to methods accepting this
 * using otto.
 * Created by Justin on 9/3/15.
 */
public class BaseApiSuccessEvent<T> extends ApiEvent<T> {
    public BaseApiSuccessEvent(T result) {
        super(result);
    }
}
