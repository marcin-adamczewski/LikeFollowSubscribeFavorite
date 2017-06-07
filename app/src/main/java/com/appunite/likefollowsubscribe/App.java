package com.appunite.likefollowsubscribe;


import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final FavoritesManager favoritesManager = FavoritesManager.getInstance();
        // Fetching favs from API and saving to cache
        favoritesManager.fetchAllFavs()
                .subscribe();
    }
}
