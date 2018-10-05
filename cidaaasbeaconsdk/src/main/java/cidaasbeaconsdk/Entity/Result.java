package cidaasbeaconsdk.Entity;

public interface Result<T> {
    public void onSuccess(T result);
    public void onError(ErrorEntity errorEntity);
}
