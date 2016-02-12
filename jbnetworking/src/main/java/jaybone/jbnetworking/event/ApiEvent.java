package jaybone.jbnetworking.event;

/**
 * Represents an API response, parametrized on some expected data type T.
 * Created by Justin on 9/3/15.
 */
public class ApiEvent<T> {

    private T mData;

    public ApiEvent(T data) {
        mData = data;
    }

    /**
     * Returns the data associated with this ApiEvent.
     * @return The data.
     */
    public T getData() {
        return mData;
    }

}
