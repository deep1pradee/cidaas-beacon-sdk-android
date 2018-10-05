package cidaasbeaconsdk.Service;


import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import timber.log.Timber;

public class BaseModel {


    public static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> subscriber) throws Exception {
                        try {
                            subscriber.onNext(func.call());
                        } catch (Exception ex) {
                            Timber.e("Error reading from the database : " + ex.getMessage());
                        }
                    }

                });
    }

}
