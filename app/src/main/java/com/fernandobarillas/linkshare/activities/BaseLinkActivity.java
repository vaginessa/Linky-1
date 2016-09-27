package com.fernandobarillas.linkshare.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.fernandobarillas.linkshare.LinksApp;
import com.fernandobarillas.linkshare.R;
import com.fernandobarillas.linkshare.api.LinkService;
import com.fernandobarillas.linkshare.configuration.AppPreferences;
import com.fernandobarillas.linkshare.databases.LinkStorage;
import com.fernandobarillas.linkshare.exceptions.InvalidApiUrlException;
import com.fernandobarillas.linkshare.ui.Snacks;
import com.fernandobarillas.linkshare.utils.ShareHandler;

import io.realm.Realm;

public class BaseLinkActivity extends AppCompatActivity {

    protected final String LOG_TAG = getClass().getSimpleName();

    protected LinksApp       mLinksApp;
    protected Realm          mRealm;
    protected LinkStorage    mLinkStorage;
    protected LinkService    mLinkService;
    protected AppPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG,
                "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);

        // Initialize the database
        mLinksApp = (LinksApp) getApplicationContext();
        mRealm = Realm.getDefaultInstance();
        mLinkStorage = new LinkStorage(mRealm);
        mPreferences = mLinksApp.getPreferences();
    }

    @Override
    protected void onStop() {
        Log.v(LOG_TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "onDestroy()");
        if (mRealm != null) {
            mRealm.close();
            mRealm = null;
        }
        if (mLinkStorage != null) mLinkStorage = null;
        super.onDestroy();
    }

    public void openUrlExternally(final String url) {
        Log.v(LOG_TAG, "openUrlExternally() called with: " + "url = [" + url + "]");
        if (url == null) return;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // Make sure that we have applications installed that can handle this intent
        if (browserIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(browserIntent);
        } else {
            showSnackError(getString(R.string.open_url_error), true);
        }
    }

    public void shareUrl(final String title, final String url) {
        Log.v(LOG_TAG, "shareUrl() called with: " + "title = [" + title + "], url = [" + url + "]");
        if (url == null || !ShareHandler.share(title, url, this)) {
            showSnackError(getString(R.string.share_intent_error), true);
        }
    }

    public void showSnackError(final String message, final boolean showDismissAction) {
        if (showDismissAction) {
            Snacks.Action dismissAction =
                    new Snacks.Action(R.string.dismiss, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Perform no action so the SnackBar gets dismissed on click
                        }
                    });
            showSnackError(message, dismissAction);
        } else {
            showSnackError(message, null);
        }
    }

    public void showSnackError(final String message, final Snacks.Action action) {
        View view = findViewById(android.R.id.content);
        if (view == null) return;
        Snacks.showError(view, message, action);
    }

    public void showSnackSuccess(final String message) {
        View view = findViewById(android.R.id.content);
        if (view == null) return;
        Snacks.showMessage(view, message);
    }

    protected void launchLoginActivity() {
        Log.v(LOG_TAG, "launchLoginActivity()");
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    protected void serviceSetup() {
        Log.v(LOG_TAG, "serviceSetup()");
        String authToken = mPreferences.getAuthString();
        if (TextUtils.isEmpty(authToken)) {
            Log.i(LOG_TAG, "serviceSetup: No refresh token stored, starting LoginActivity");
            launchLoginActivity();
        }
        try {
            mLinkService = mLinksApp.getLinkService();
        } catch (InvalidApiUrlException e) {
            Log.e(LOG_TAG, "serviceSetup: Invalid API URL, launching login activity", e);
            launchLoginActivity();
        }
    }

    protected void setToolbarTitle(String title, String subTitle) {
        Log.v(LOG_TAG, "setToolbarTitle() called with: "
                + "title = ["
                + title
                + "], subTitle = ["
                + subTitle
                + "]");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setTitle(title);
        actionBar.setSubtitle(subTitle);
    }
}
