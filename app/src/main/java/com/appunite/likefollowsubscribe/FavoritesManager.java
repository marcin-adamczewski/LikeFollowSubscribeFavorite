package com.appunite.likefollowsubscribe;

import android.support.annotation.NonNull;

import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.subjects.BehaviorSubject;

// It is a SINGLETON !
public class FavoritesManager {
    private static FavoritesManager INSTANCE;

    private final Cache<Integer, BehaviorSubject<Boolean>> favoritesCache;
    private final FavsApi favsApi;

    public static synchronized FavoritesManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FavoritesManager();
        }

        return INSTANCE;
    }

    private FavoritesManager() {
        favsApi = new FavsApi();
        favoritesCache = new Cache<>(new Cache.CacheProvider<Integer, BehaviorSubject<Boolean>>() {
            @Nonnull
            @Override
            public BehaviorSubject<Boolean> load(@Nonnull final Integer postId) {
                return BehaviorSubject.create(false);
            }
        });
    }

    private void updateFavoritesCache(final @NonNull List<Integer> favs) {
        if (favs.isEmpty()) {
            favoritesCache.invalidate();
        }
        for (Integer favId : favs) {
            updateFavorite(favId, true);
        }
    }

    private void updateFavorite(final @NonNull Integer id, final boolean favorite) {
        favoritesCache.get(id).onNext(favorite);
    }

    public Observable<ResponseOrError<String>> addOrRemoveFromFavs(final int postId) {
        return getIsFavoriteObservable(postId)
                .take(1)
                .doOnNext(isCurrentlyFavorite -> {
                    updateFavorite(postId, !isCurrentlyFavorite); // toggle value before request for better User Experience
                })
                .switchMap(isCurrentlyFavorite -> favsApi.addOrRemoveFromFavs(isCurrentlyFavorite)
                        .compose(ResponseOrError.toResponseOrErrorObservable()) // it's changing error event to onNext event to not finish a chain
                        .doOnNext(responseOrError -> {
                            if (responseOrError.isError()) {
                                updateFavorite(postId, isCurrentlyFavorite); // set back to previous value because of error
                            }
                        }));
    }

    public Observable<List<Integer>> fetchAllFavs() {
        return favsApi.fetchFavorites()
                .doOnNext(this::updateFavoritesCache);
    }

    public Observable<Boolean> getIsFavoriteObservable(final @NonNull Integer id) {
        return favoritesCache.get(id);
    }

}
