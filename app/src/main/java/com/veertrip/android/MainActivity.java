package com.veertrip.android;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
/*import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;*/
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freshchat.consumer.sdk.ConversationOptions;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatConfig;
import com.freshchat.consumer.sdk.FreshchatMessage;
import com.freshchat.consumer.sdk.FreshchatUser;
import com.freshchat.consumer.sdk.exception.MethodNotAllowedException;
//import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.veertrip.android.client.IOnRequestCompleted;
import com.veertrip.android.client.VersionClient;
import com.veertrip.android.utils.AdvancedWebView;
import com.veertrip.android.utils.QueryHelper;
import com.veertrip.android.utils.StorageHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements
        AdvancedWebView.Listener, IOnRequestCompleted {

    private Activity activity;
    private AdvancedWebView myWebView;
    private LinearLayout splashFrame;
    private View splash, promotion;
    private View whiteBg, imageBg, imageBg1;

    private boolean connectivityClearance = false;
    private boolean splashEnded = false;
    private boolean intentIsAlreadyHandled = false;

    int delayAfterAnimation = 1500;

    private Handler mHandler;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //verifyStoragePermissions(this);

        mHandler = new Handler();

        splashFrame = findViewById(R.id.splash_frame);
        TextView tagline = findViewById(R.id.tagline);
        splash = findViewById(R.id.splash);
        promotion = findViewById(R.id.promotion);
        whiteBg = findViewById(R.id.white_bg);
        View handcrafted = findViewById(R.id.handcrafted);
        imageBg = findViewById(R.id.image_bg);
        imageBg1 = findViewById(R.id.image_bg_2);

        //String gcmId = FirebaseInstanceId.getInstance().getToken();
        String gcmId = FirebaseMessaging.getInstance().getToken().toString();
        if (gcmId != null)
            Log.d("FCM", gcmId);

        // Check Version

        VersionClient versionClient = new VersionClient(this);
        versionClient.execute();

        // Set Up Animation

        StorageHelper helper = new StorageHelper(this);
        if (!helper.read("FIRST_TIME").equals("YES")) {
            // App opened for first time
            helper.write("FIRST_TIME", "YES");
            delayAfterAnimation = 2500;
        }

        ValueAnimator animation = ValueAnimator.ofInt(2000, 0);
        animation.setDuration(2000);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                splashFrame.setPadding(0,
                        Integer.parseInt(valueAnimator.getAnimatedValue().toString()),
                        0, 0);
            }
        });
        animation.start();

        AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(1000);
        animation1.setStartOffset(2000);
        animation1.setFillAfter(true);
        tagline.startAnimation(animation1);
        handcrafted.startAnimation(animation1);
        promotion.startAnimation(animation1);

        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                startRepeatingTask();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        splashEnded = true;
                        if (connectivityClearance) {
                            clearSplash();
                        }
                    }
                }, delayAfterAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Set Up WebView

        // Works on Google plus but users get email
        //final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";

        // Unauthorized app error
        final String USER_AGENT = "Mozilla/5.0 (Linux; Android 8.1.0; Redmi Note 6 Pro Build/OPM1.171019.011) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.91 Mobile Safari/537.36";

        myWebView = findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        //webSettings.setAppCacheEnabled(true);

        //webSettings.settings.cacheMode=WebSettings.LOAD_NO_CACHE;

        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setUserAgentString(USER_AGENT);

        //myWebView.clearCache(true);

        myWebView.addPermittedHostname("veertrip.com");
        myWebView.setListener(this, this);
        myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        myWebView.loadUrl("https://www.veertrip.com/?is_app=true");
        //myWebView.loadUrl("http://192.168.0.100:8000");

        FreshchatConfig freshchatConfig=new FreshchatConfig(
                "8541f58e-8746-49d5-9bbe-e718f44c82e5",
                "d1b139c4-854a-4e18-a38e-89067e9f46bb"
        );
        Freshchat.getInstance(getApplicationContext()).init(freshchatConfig);
    }

    private void clearSplash() {
        whiteBg.setVisibility(View.GONE);
        splash.setVisibility(View.GONE);
        imageBg.setVisibility(View.GONE);
        imageBg1.setVisibility(View.GONE);
        promotion.setVisibility(View.GONE);

        handleIntentData();
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        Uri appLinkData = intent.getData();
        String url = intent.getStringExtra("URL");

        if (!intentIsAlreadyHandled) {
            intentIsAlreadyHandled = true;

            if (url != null && !url.isEmpty()) {
                myWebView.loadUrl(url);

            } else if (appLinkData != null && !appLinkData.toString().isEmpty()) {
                myWebView.loadUrl(appLinkData.toString());
            }
        }
    }

    @Override
    public void onDestroy() {
        myWebView.onDestroy();
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                checkConnectivity();
            } finally {
                mHandler.postDelayed(mStatusChecker, 10000);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // For testing purpose
        // Freshchat.showConversations(MainActivity.this);

        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        } else {
            StorageHelper helper = new StorageHelper(this);
            if (!helper.read("FIRST_CLOSE").equals("YES")) {
                //showRatingDialog();
                helper.write("FIRST_CLOSE", "YES");
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
    }

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (connectivityManager == null ||
                connectivityManager.getActiveNetworkInfo() == null ||
                !connectivityManager.getActiveNetworkInfo().isConnected()) {
            showConnectivityError();

        } else {
            connectivityClearance = true;
            if (splashEnded) {
                clearSplash();
            }
        }
    }

    private void showConnectivityError() {
        Snackbar snackbar = Snackbar.make(this.getCurrentFocus(), "No Internet Connection",
                4000);
        View snackbarLayout = snackbar.getView();
        TextView textView = snackbarLayout.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextSize(17);
        snackbarLayout.setPadding(16, 16, 16, 16);
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wifi, 0, 0, 0);
        textView.setCompoundDrawablePadding(getResources().getDimensionPixelOffset(R.dimen.snackbar_icon_padding));
        snackbarLayout.setBackgroundColor(Color.parseColor("#ffa000"));
        snackbar.show();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        Log.d("URL", url);
    }

    @Override
    public void onPageFinished(String url) {
        this.myWebView.evaluateJavascript("window.isApp = true",null);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        showConnectivityError();
        myWebView.loadUrl("about:blank");
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        myWebView.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        myWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        myWebView.onActivityResult(requestCode, resultCode, intent);
    }

    protected void showRatingDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        builder.setTitle("Did you like the experience?")
                .setMessage("We hope that you liked the experience. Please rate us 5 stars on Play Store")
                .setPositiveButton("RATE US", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.onBackPressed();
                    }
                })
                .show();
    }

    protected void showUpdateDialog(boolean forced) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        builder.setTitle("New Version Available")
                .setCancelable(!forced)
                .setMessage("Please update now for a better experience")
                .setPositiveButton("UPDATE", null);

        if (!forced) {
            builder.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });
        }

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    protected void setFreshChatUser(String url) {
        Map<String,String> queryParams = QueryHelper.getMap(url);

        FreshchatUser freshUser=Freshchat.getInstance(getApplicationContext()).getUser();

        freshUser.setFirstName(queryParams.get("firstName"));
        freshUser.setLastName(queryParams.get("lastName"));
        freshUser.setEmail(queryParams.get("email"));
        freshUser.setPhone("+91", queryParams.get("mobileNumber"));

        try {
            Freshchat.getInstance(getApplicationContext()).setUser(freshUser);
        } catch (MethodNotAllowedException ignored) {}

        String message = queryParams.get("message");
        String channel = queryParams.get("channel");

        if (message != null && !message.isEmpty() && channel != null && !channel.isEmpty()) {
            FreshchatMessage freshchatMessage = new FreshchatMessage();
            freshchatMessage.setTag(channel);

            String urlDecodedMessage = Uri.decode(message);
            freshchatMessage.setMessage(urlDecodedMessage);

            Freshchat.sendMessage(MainActivity.this, freshchatMessage);

            List<String> tags = new ArrayList<>();
            tags.add(channel);

            ConversationOptions options = new ConversationOptions();
            options.filterByTags(tags, channel);

            Freshchat.showConversations(MainActivity.this, options);

        } else {
            Freshchat.showConversations(MainActivity.this);
        }
    }

    @Override
    public void onExternalPageRequest(String url) {
        // FreshChat

        if (url.contains("live-chat-android")) {
            this.setFreshChatUser(url);
            return;
        }

        // Special case

        if (url.contains("facebook.com/sharer/sharer.php")) {
            if (AdvancedWebView.Browsers.hasAlternative(this)) {
                AdvancedWebView.Browsers.openUrl(this, url);
                return;
            }
        }

        Log.d("URL", url);
        if (url.contains("mailto:")) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, url.replace("mailto:", ""));
            intent.putExtra(Intent.EXTRA_SUBJECT, "");
            intent.putExtra(Intent.EXTRA_TEXT, "");

            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (ActivityNotFoundException ignored) {}
            return;
        }

        if (url.contains("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {}
            return;
        }

        // Load specific urls outside
        if (url.contains("medium.com") ||
                url.contains("flyai.mobi") ||
                url.contains("jetairways.com") ||
                url.contains("airvistara.com") ||
                url.contains("spicejet.com") ||
                url.contains("airasia.com") ||
                url.contains("goindigo.in") ||
                url.contains("goair.in") ||
                url.contains("zoomair.co.in") ||

                url.contains("facebook.com/veertrip") ||
                url.contains("twitter.com/veertrip") ||
                url.contains("linkedin.com") ||
                url.contains("instagram.com/veertripofficial") ||

                url.contains(".pdf") ||

                url.contains("bharatkeveer.gov.in") ||

                url.contains("truejet.com") ||
                url.contains("google.com/maps/dir") ||

                url.contains("veertax.com") ||

                url.contains("dropbox.com") ||
                url.contains("maps.google.com") ||
                url.contains("pmcares.gov.in") ||
                url.contains("mohfw.gov.in") ||

                url.contains("goindigo.in/web-check-in") ||
                url.contains("icheck.sita.aero") ||

                url.startsWith("https://veer-static.s3.ap-south-1.amazonaws.com/covid19/") ||

                url.contains("whatsapp")) {
            AdvancedWebView.Browsers.openUrl(this, url);
            return;
        }

        // Check present earlier, do not disturb
        if (url.contains("facebook.com") || url.contains("cashfree.com") || url.contains("accounts.google.com")) {
            myWebView.loadUrl(url);

        } else {
            // Load all other urls inside due to a bug in payment
            myWebView.loadUrl(url);
        }
    }

    @Override
    public void onRequestComplete(int clientCode, Object response) {
        if (clientCode == VersionClient.ACTIVITY_CODE && response != null) {
            try {
                JSONObject jsonResponse = (JSONObject) response;
                JSONObject androidVersion = jsonResponse.getJSONObject("android");
                int majorVersion = androidVersion.getInt("majorVersion");
                int minorVersion = androidVersion.getInt("minorVersion");
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int currentVersion = packageInfo.versionCode;

                if (currentVersion < majorVersion) {
                    showUpdateDialog(true);
                } else if (currentVersion < minorVersion) {
                    showUpdateDialog(false);
                }
            } catch (Exception ignored) {}
        }
    }
}
