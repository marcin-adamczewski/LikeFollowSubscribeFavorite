package com.appunite.likefollowsubscribe;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class PostsListActivity extends AppCompatActivity {

    private FavoritesManager favoritesManager;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);

        favoritesManager = FavoritesManager.getInstance();

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.posts_rv);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setRecycleChildrenOnDetach(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new PostsAdapter());
    }

    private class PostsAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            return new PostsViewHolder(LayoutInflater.from(PostsListActivity.this).inflate(R.layout.item_post, parent, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            ((PostsViewHolder) holder).bind();
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        class PostsViewHolder extends RecyclerView.ViewHolder {

            private final TextView titleTv;
            private final ImageView favIconIv;
            private CompositeSubscription subscriptions;

            public PostsViewHolder(final View itemView) {
                super(itemView);
                titleTv = (TextView) itemView.findViewById(R.id.item_post_title_tv);
                favIconIv = (ImageView) itemView.findViewById(R.id.item_post_fav_iv);
            }

            public void bind() {
                final int postId = getAdapterPosition();
                final String title = "Post with id: " + postId;
                titleTv.setText(title);

                subscriptions = new CompositeSubscription(

                        // Listen and react for post favorite value change
                        favoritesManager.getIsFavoriteObservable(postId)
                                .subscribe(isFavorite -> favIconIv.setImageDrawable(getResources().getDrawable(
                                        isFavorite ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp))),

                        // On click make API request
                        RxView.clicks(favIconIv)
                                .throttleFirst(1, TimeUnit.SECONDS) // Prevents from crazy people fast clicks
                                .observeOn(AndroidSchedulers.mainThread())
                                .switchMap(ignore -> favoritesManager.addOrRemoveFromFavs(postId))  // Of course it should be done in presenter and not here
                                .subscribe(responseOrError -> {
                                    if (responseOrError.isError()) {
                                        Toast.makeText(PostsListActivity.this, "Request error", Toast.LENGTH_SHORT).show();
                                    }
                                }),

                        RxView.clicks(itemView)
                        .subscribe(aVoid -> startActivity(PostDetailsActivity.newIntent(PostsListActivity.this, postId)))

                );

            }

            private void clear() {
                if (subscriptions != null) {
                    subscriptions.unsubscribe();
                }
            }
        }

        @Override
        public void onViewRecycled(final RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            ((PostsViewHolder) holder).clear();
        }
    }
}
