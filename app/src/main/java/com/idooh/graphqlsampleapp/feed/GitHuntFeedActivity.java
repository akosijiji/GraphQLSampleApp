package com.idooh.graphqlsampleapp.feed;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloCallback;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.idooh.graphqlsampleapp.GitHuntApplication;
import com.idooh.graphqlsampleapp.R;
import com.idooh.graphqlsampleapp.api.FeedQuery;
import com.idooh.graphqlsampleapp.api.type.FeedType;
import com.idooh.graphqlsampleapp.detail.GitHuntEntryDetailActivity;

import javax.annotation.Nonnull;

public class GitHuntFeedActivity extends AppCompatActivity implements GitHuntNavigator {

    private static final String TAG = GitHuntFeedActivity.class.getSimpleName();

    private static final int FEED_SIZE = 5;

    GitHuntApplication application;
    ViewGroup content;
    ProgressBar progressBar;
    GitHuntFeedRecyclerViewAdapter feedAdapter;
    Handler uiHandler = new Handler(Looper.getMainLooper());
    ApolloCall<FeedQuery.Data> githuntFeedCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_githunt_feed);

        application = (GitHuntApplication) getApplication();

        content = findViewById(R.id.content_holder);
        progressBar = findViewById(R.id.loading_bar);

        RecyclerView feedRecyclerView = findViewById(R.id.rv_feed_list);
        feedAdapter = new GitHuntFeedRecyclerViewAdapter(this);
        feedRecyclerView.setAdapter(feedAdapter);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchFeed();
    }

    private ApolloCall.Callback<FeedQuery.Data> dataCallback = new ApolloCallback<>(new ApolloCall.Callback<FeedQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<FeedQuery.Data> response) {
            feedAdapter.setFeed(response.data().feedEntries());
            progressBar.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }, uiHandler);

    private void fetchFeed() {
        final FeedQuery feedQuery = FeedQuery.builder()
                .limit(FEED_SIZE)
                .type(FeedType.HOT)
                .build();
        githuntFeedCall = application.apolloClient()
                .query(feedQuery)
                .cacheControl(CacheControl.NETWORK_FIRST);
        githuntFeedCall.enqueue(dataCallback);
    }

    @Override
    public void startGitHuntActivity(String repoFullName) {
        final Intent intent = GitHuntEntryDetailActivity.newIntent(this, repoFullName);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (githuntFeedCall != null) {
            githuntFeedCall.cancel();
        }
    }

}