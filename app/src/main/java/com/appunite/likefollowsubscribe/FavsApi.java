package com.appunite.likefollowsubscribe;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class FavsApi {

    public Observable<String> addOrRemoveFromFavs(final boolean isFavorite) {
        final int random0To9 = new Random().nextInt(10);
        final Observable<String> favRequest;

        if (random0To9 == 0) { // if random value is 0 we fake an error response
            favRequest = Observable.error(new Throwable("Fake request fail"));
        } else {
            favRequest = isFavorite ?
                    Observable.just("Fake unfavorite request") :
                    Observable.just("Fake favorite request");
        }
        return favRequest
                .delay(1, TimeUnit.SECONDS) // delay to simulate request
                .observeOn(AndroidSchedulers.mainThread());
    }

    // Fake API request for list of all favs
    public Observable<List<Integer>> fetchFavorites() {
        final List<Integer> favs = new ArrayList<>();
        favs.add(0);
        favs.add(2);
        favs.add(4);
        favs.add(6);

        return Observable.just(favs);
    }
}
