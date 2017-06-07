package com.appunite.likefollowsubscribe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class PostDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_POST_ID = "extra_post_id";

    private int postId;

    private CompositeSubscription subscription;

    public static Intent newIntent(final Context context, final int postId) {
        return new Intent(context, PostDetailsActivity.class)
                .putExtra(EXTRA_POST_ID, postId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postId = getIntent().getIntExtra(EXTRA_POST_ID, -1);
        final FavoritesManager favoritesManager = FavoritesManager.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post with id: " + postId);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fav_fab);

        subscription = new CompositeSubscription(

                favoritesManager.getIsFavoriteObservable(postId)
                        .subscribe(isFavorite -> fab.setImageDrawable(getResources().getDrawable(isFavorite ?
                                R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp))),

                RxView.clicks(fab)
                        .throttleFirst(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .switchMap(ignore -> favoritesManager.addOrRemoveFromFavs(postId))
                        .subscribe(responseOrError -> {
                            if (responseOrError.isError()) {
                                Toast.makeText(PostDetailsActivity.this, "API error", Toast.LENGTH_SHORT).show();
                            }
                        })
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
