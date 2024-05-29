package net.nyx.printerclient;


import static net.nyx.printerclient.Result.msg;
import static net.nyx.printerclient.WebviewMain.Config.ACTIVATE_PROGRESS_BAR;
import static net.nyx.printerclient.WebviewMain.Config.ENABLE_PULL_REFRESH;
import static net.nyx.printerclient.WebviewMain.Config.ENABLE_SWIPE_NAVIGATE;
import static net.nyx.printerclient.WebviewMain.Config.ENABLE_ZOOM;
import static net.nyx.printerclient.WebviewMain.Config.EXIT_APP_DIALOG;
import static net.nyx.printerclient.WebviewMain.Config.HIDE_HORIZONTAL_SCROLLBAR;
import static net.nyx.printerclient.WebviewMain.Config.HIDE_VERTICAL_SCROLLBAR;
import static net.nyx.printerclient.WebviewMain.Config.INCREMENT_WITH_REDIRECTS;
import static net.nyx.printerclient.WebviewMain.Config.MAX_TEXT_ZOOM;
import static net.nyx.printerclient.WebviewMain.Config.SPECIAL_LINK_HANDLING_OPTIONS;
import static net.nyx.printerclient.WebviewMain.Config.downloadableExtension;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.hd.viewcapture.ViewCapture;

import net.nyx.printerclient.WebviewMain.AlertManager;
import net.nyx.printerclient.WebviewMain.Config;
import net.nyx.printerclient.WebviewMain.CustomWebView;
import net.nyx.printerclient.WebviewMain.NotificationHelper;
import net.nyx.printerclient.aop.SingleClick;
import net.nyx.printerservice.print.IPrinterService;
import net.nyx.printerservice.print.PrintTextFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    protected Button btnVer;
    protected Button btnPaper;
    protected Button btn1;
    protected Button btn2;
    protected Button btn3;
    protected Button btnScan;
    protected TextView tvLog;

    private static final int RC_SCAN = 0x99;
    public static String PRN_TEXT;
    protected Button btn4;
    protected Button btnLbl;
    protected Button btnLblLearning;

    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler();
    String[] version = new String[1];







    // from webview



    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";

    private static final String INDEX_FILE = "file:///android_asset/local-html/index.html";
    private static final int CODE_AUDIO_CHOOSER = 5678;
    private WebView webView;
    private WebView mWebviewPop;
    private SharedPreferences preferences;
    private RelativeLayout mContainer;
    private RelativeLayout windowContainer;

    private View offlineLayout;


    public static final int REQUEST_CODE_QR_SCAN = 1234;

    SwipeRefreshLayout mySwipeRefreshLayout;
    public ProgressBar progressBar;
    private String deepLinkingURL;
    int mCount = -1;

    private String mCM, mVM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR = 1;
    public String hostpart;
    private boolean offlineFileLoaded = false;
    private boolean isNotificationURL = false;
    private boolean extendediap = true;
    public String uuid = "";
    public static Context mContext;
    private boolean isRedirected = false;


    static long TimeStamp = 0;
    private static boolean connectedNow = false;


    private Tag myTag;
    private boolean NFCenabled = false;
    private boolean readModeNFC = false;
    private boolean writeModeNFC = false;
    private String textToWriteNFC = "";
    public static final String USER_AGENT_GOOGLE = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.101 Mobile Safari/537.36";
    public static final String USER_AGENT_FB = "Mozilla/5.0 (Linux; U; Android 2.2) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";

    // Manual Cookie Sync
    private final Handler cookieSyncHandler = new Handler();
    private Runnable cookieSyncRunnable;

    // Scanning Mode
    private boolean scanningModeOn = false;
    private boolean persistentScanningMode = false;
    private float previousScreenBrightness;

    private PowerManager.WakeLock wakeLock;


    @SuppressLint("SetJavaScriptEnabled")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView.enableSlowWholeDocumentDraw();
        mContext = this;
        uuid = Settings.System.getString(super.getContentResolver(), Settings.Secure.ANDROID_ID);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        setContentView(R.layout.activity_main);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");

        // Acquire the wake lock to keep the CPU running
        wakeLock.acquire();
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initView();
        bindService();
        registerQscScanReceiver();
        Timber.plant(new Timber.DebugTree());
        PRN_TEXT = getString(R.string.print_text);




        // from webview

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null &&
                (intent.getData().getScheme().equals("http") || intent.getData().getScheme().equals("https"))) {
            Uri data = intent.getData();
            List<String> pathSegments = data.getPathSegments();
            if (pathSegments.size() > 0) {
                deepLinkingURL = pathSegments.get(0).substring(5);
                String fullDeepLinkingURL = data.getScheme() + "://" + data.getHost() + "" + data.getPath();
//                fullDeepLinkingURL = fullDeepLinkingURL.replace("/link=", "");
                deepLinkingURL = fullDeepLinkingURL;
            }
        }/* else if (intent != null && intent.getData() != null && (intent.getData().getScheme().equals("https"))) {
            Uri data = intent.getData();
            List<String> pathSegments = data.getPathSegments();
            if (pathSegments.size() > 0) {
                deepLinkingURL = pathSegments.get(0).substring(5);
                String fulldeeplinkingurl = data.getPath().toString();
                fulldeeplinkingurl = fulldeeplinkingurl.replace("/link=", "");
                deepLinkingURL = fulldeeplinkingurl;
            }
        }*/

        if (intent != null) {
            Bundle extras = getIntent().getExtras();
            String URL = null;
            if (extras != null) {
                URL = extras.getString("ONESIGNAL_URL");
            }
            if (URL != null && !URL.equalsIgnoreCase("")) {
                isNotificationURL = true;
                deepLinkingURL = URL;
            } else isNotificationURL = false;
        }





        if (savedInstanceState == null) {
            AlertManager.appLaunched(this);
        }







        webView = findViewById(R.id.webView);
        mContainer = findViewById(R.id.web_container);
        windowContainer = findViewById(R.id.window_container);
        webView.setLayerType(View.LAYER_TYPE_NONE, null);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
//        webView.setGestureDetector(new GestureDetector(new CustomeGestureDetector()));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        if (!ENABLE_PULL_REFRESH) {
            mySwipeRefreshLayout.setEnabled(false);

        }

        if(HIDE_VERTICAL_SCROLLBAR){
            webView.setVerticalScrollBarEnabled(false);
        }
        if(HIDE_HORIZONTAL_SCROLLBAR){
            webView.setHorizontalScrollBarEnabled(false);
        }

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (ENABLE_PULL_REFRESH) {
                            webView.reload();

                        }
                        mySwipeRefreshLayout.setRefreshing(false);

                    }
                }
        );

        offlineLayout = findViewById(R.id.offline_layout);

        this.findViewById(android.R.id.content).setBackgroundColor(getResources().getColor(R.color.launchLoadingSignBackground));
        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

        final Button tryAgainButton = findViewById(R.id.try_again_button);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Try again!");
                webView.setVisibility(View.GONE);
                loadMainUrl();
            }
        });

        webView.setWebViewClient(new AdvanceWebViewClient());
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        Context appContext = this;

        // Collect the App Name to use as the title for Javascript Dialogs
        final String appName;
        String appName1;
        try {
            appName1 = appContext.getApplicationInfo().loadLabel(appContext.getPackageManager()).toString();
        } catch (Exception e) {
            // If unsuccessful in collecting the app name, set the name to the page title.
            appName1 = webView.getTitle();
        }
        appName = appName1;

        webView.setWebChromeClient(new AdvanceWebChromeClient() {

            // Functions to support alert(), confirm() and prompt() Javascript Dialogs

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog dialog = new AlertDialog.Builder(view.getContext()).
                        setTitle(appName).
                        setMessage(message).
                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        }).create();
                dialog.show();
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(view.getContext())
                        .setTitle(appName)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        });
                b.show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                final EditText input = new EditText(appContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(defaultValue);
                new AlertDialog.Builder(appContext)
                        .setTitle(appName)
                        .setView(input)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm(input.getText().toString());
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        });


        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        registerForContextMenu(webView);

        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (ENABLE_ZOOM) {
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
        } else {
            webSettings.setBuiltInZoomControls(false);
        }
        if (Config.CLEAR_CACHE_ON_STARTUP) {
            //webSettings.setAppCacheEnabled(false);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            //webSettings.setAppCacheEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowFileAccess(true);
        //webSettings.setLoadWithOverviewMode(true);
        //webSettings.setUseWideViewPort(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Custom Text Zoom
        if (MAX_TEXT_ZOOM > 0) {
            float systemTextZoom = getResources().getConfiguration().fontScale * 100;
            if (systemTextZoom > MAX_TEXT_ZOOM) {
                webView.getSettings().setTextZoom(MAX_TEXT_ZOOM);
            }
        }

        // Phone orientation setting for Android 8 (Oreo)
        if (webSettings.getUserAgentString().contains("Mobile") && android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            if (Config.PHONE_ORIENTATION == "auto") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else if (Config.PHONE_ORIENTATION == "portrait") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else if (Config.PHONE_ORIENTATION == "landscape") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            // Phone orientation setting for all other Android versions
        } else if (webSettings.getUserAgentString().contains("Mobile")) {
            if (Config.PHONE_ORIENTATION == "auto") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else if (Config.PHONE_ORIENTATION == "portrait") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (Config.PHONE_ORIENTATION == "landscape") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            // Tablet/Other orientation setting
        } else {
            if (Config.TABLET_ORIENTATION == "auto") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else if (Config.TABLET_ORIENTATION == "portrait") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (Config.TABLET_ORIENTATION == "landscape") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        if (!Config.USER_AGENT.isEmpty()) {
            webSettings.setUserAgentString(Config.USER_AGENT);
        }

        if (Config.CLEAR_CACHE_ON_STARTUP) {
            webView.clearCache(true);
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        }

        if (Config.USE_LOCAL_HTML_FOLDER) {
            loadLocal(INDEX_FILE);
        } else if (isConnectedNetwork()) {
            if (Config.USE_LOCAL_HTML_FOLDER) {
                loadLocal(INDEX_FILE);
            } else {
                loadMainUrl();
                connectedNow = true;
            }
        } else {
            loadLocal(INDEX_FILE);
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                askForPermission();
            }
        }, 1000);

        if (!connectedNow) {
            checkInternetConnection();
        }


        if (getIntent().getExtras() != null) {
            String openurl = getIntent().getExtras().getString("openURL");
            if (openurl != null) {
                openInExternalBrowser(openurl);
            }

        }





    }

    private class WebAppInterface {
        @JavascriptInterface
        public void stopLoading() {
            runOnUiThread(() -> webView.stopLoading());
            runOnUiThread(() -> {
                // Optional: Inform the user
                Toast.makeText(MainActivity.this, "Loading stopped for this URL.", Toast.LENGTH_SHORT).show();
            });
        }
    }
    private IPrinterService printerService;
    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            showLog("printer service disconnected, try reconnect");
            printerService = null;
            // 尝试重新bind
            handler.postDelayed(() -> bindService(), 5000);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Timber.d("onServiceConnected: %s", name);
            printerService = IPrinterService.Stub.asInterface(service);
            getVersion();
        }
    };


    private void bindService() {
        Intent intent = new Intent();
        intent.setPackage("net.nyx.printerservice");
        intent.setAction("net.nyx.printerservice.IPrinterService");
        bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        unbindService(connService);
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_ver) {
//            getVersion();
        } else if (view.getId() == R.id.btn_paper) {
            paperOut();
        } else if (view.getId() == R.id.btn1) {
//            printText();
        } else if (view.getId() == R.id.btn2) {
//            printBarcode();
        } else if (view.getId() == R.id.btn3) {
//            printQrCode();
        } else if (view.getId() == R.id.btn4) {
            printBitmap();
        } else if (view.getId() == R.id.btn_scan) {
//            scan();
        } else if (view.getId() == R.id.btn_lbl) {
//            printLabel();
        } else if (view.getId() == R.id.btn_lbl_learning) {
//            printLabelLearning();
        }
    }

    private final BroadcastReceiver qscReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.android.NYX_QSC_DATA".equals(intent.getAction())) {
                String qsc = intent.getStringExtra("qsc");
                showLog("qsc scan result: %s", qsc);
                printText("qsc-quick-scan-code\n" + qsc);
            }
        }
    };

    private void registerQscScanReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.NYX_QSC_DATA");
        registerReceiver(qscReceiver, filter);
    }

    private void unregisterQscReceiver() {
        unregisterReceiver(qscReceiver);
    }

    private void getVersion() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = printerService.getPrinterVersion(version);
                    showLog("Version: " + msg(ret) + "  " + version[0]);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void paperOut() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.paperOut(80);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void printText(String text) {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintTextFormat textFormat = new PrintTextFormat();
                    // textFormat.setTextSize(32);
                    // textFormat.setUnderline(true);
                    int ret = printerService.printText(text, textFormat);
                    showLog("Print text: " + msg(ret));
                    if (ret == 0) {
                        paperOut();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void printBitmap() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = printerService.printBitmap(BitmapFactory.decodeStream(getAssets().open("bmp.png")), 1, 1);
                    showLog("Print bitmap: " + msg(ret));
                    if (ret == 0) {
                        paperOut();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }









    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        unregisterQscReceiver();

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.menu_item_clear) {
//            clearLog();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void initView() {
        btnVer = (Button) findViewById(R.id.btn_ver);
        btnVer.setOnClickListener(MainActivity.this);
        btnPaper = (Button) findViewById(R.id.btn_paper);
        btnPaper.setOnClickListener(MainActivity.this);
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(MainActivity.this);
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(MainActivity.this);
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(MainActivity.this);
        btnScan = (Button) findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(MainActivity.this);
        tvLog = (TextView) findViewById(R.id.tv_log);
        btn4 = (Button) findViewById(R.id.btn4);
        btn4.setOnClickListener(MainActivity.this);
        btnLbl = (Button) findViewById(R.id.btn_lbl);
        btnLbl.setOnClickListener(MainActivity.this);
        btnLblLearning = (Button) findViewById(R.id.btn_lbl_learning);
        btnLblLearning.setOnClickListener(MainActivity.this);
    }

    void showLog(String log, Object... args) {
        if (args != null && args.length > 0) {
            log = String.format(log, args);
        }
        String res = log;
        Log.e(TAG, res);
        DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tvLog.getLineCount() > 100) {
                    tvLog.setText("");
                }
                tvLog.append((dateFormat.format(new Date()) + ":" + res + "\n"));
                tvLog.post(new Runnable() {
                    @Override
                    public void run() {
                        ((ScrollView) tvLog.getParent()).fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }





    // from webview







    private void openInExternalBrowser(String launchUrl) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(launchUrl));
        startActivity(browserIntent);
    }

    private void handleURl(String urlString) {

        if (URLUtil.isValidUrl(urlString) && !Config.OPEN_NOTIFICATION_URLS_IN_SYSTEM_BROWSER) {
            String urlToLoad = sanitizeURL(urlString);
            webView.loadUrl(urlToLoad);
        }
        if (URLUtil.isValidUrl(urlString) && Config.OPEN_NOTIFICATION_URLS_IN_SYSTEM_BROWSER) {
            Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            startActivity(external);
        }
    }



    private void checkInternetConnection() {
        //auto reload every 5s
        class AutoRec extends TimerTask {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {

                        if (!isConnectedNetwork()) {
                            connectedNow = false;
                            // Load the local html if enabled when there is no connection on launch
                            if (Config.FALLBACK_USE_LOCAL_HTML_FOLDER_IF_OFFLINE || Config.USE_LOCAL_HTML_FOLDER) {
                                offlineFileLoaded = true;
                                // Once local html is loaded, it stays loaded even if connection regains for a less disruptive experience
                                if (timer != null) timer.cancel();
                            } else {
                                connectedNow = false;
                                offlineLayout.setVisibility(View.VISIBLE);
                                playBeep();
                                System.out.println("attempting reconnect");
                                webView.setVisibility(View.GONE);

                                loadMainUrl();

                                Log.d("", "reconnect");
                            }
                        } else {
                            if (!connectedNow) {
                                Log.d("", "connected");
                                System.out.println("Try again!");
                                webView.setVisibility(View.GONE);
                                loadMainUrl();
                                connectedNow = true;
                                if (timer != null) timer.cancel();
                            }
                        }
                    }
                });
            }
        }
        timer.schedule(new AutoRec(), 0, 5000);
        //timer.cancel();
    }
    public void playBeep() {
        MediaPlayer m = new MediaPlayer();
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }

            AssetFileDescriptor descriptor = getAssets().openFd("beepbeep.mp3");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(false);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void loadLocal(String path) {
        webView.loadUrl(path);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);


    }

    public ValueCallback<Uri[]> uploadMessage;
    private ValueCallback<Uri> mUploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }

        Uri[] results = null;
        Uri uri = null;
        if (requestCode == FCR) {
            if (resultCode == Activity.RESULT_OK) {
                if (mUMA == null) {
                    return;
                }
                if (intent == null || intent.getData() == null) {

                    if (intent != null && intent.getClipData() != null) {

                        int count = intent.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                        results = new Uri[intent.getClipData().getItemCount()];
                        for (int i = 0; i < count; i++) {
                            uri = intent.getClipData().getItemAt(i).getUri();
                            // results = new Uri[]{Uri.parse(mCM)};
                            results[i] = uri;

                        }
                        //do something with the image (save it to some directory or whatever you need to do with it here)
                    }

                    if (mCM != null) {
                        File file = new File(Uri.parse(mCM).getPath());
                        if (file.length() > 0)
                            results = new Uri[]{Uri.parse(mCM)};
                        else
                            file.delete();
                    }
                    if (mVM != null) {
                        File file = new File(Uri.parse(mVM).getPath());
                        if (file.length() > 0)
                            results = new Uri[]{Uri.parse(mVM)};
                        else
                            file.delete();
                    }

                } else {
                    String dataString = intent.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    } else {
                        if (intent.getClipData() != null) {
                            final int numSelectedFiles = intent.getClipData().getItemCount();
                            results = new Uri[numSelectedFiles];
                            for (int i = 0; i < numSelectedFiles; i++) {
                                results[i] = intent.getClipData().getItemAt(i).getUri();
                            }
                        }

                    }
                }
            } else {
                if (mCM != null) {
                    File file = new File(Uri.parse(mCM).getPath());
                    if (file != null) file.delete();
                }
                if (mVM != null) {
                    File file = new File(Uri.parse(mVM).getPath());
                    if (file != null) file.delete();
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else if (requestCode == CODE_AUDIO_CHOOSER) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null && intent.getData() != null) {
                    results = new Uri[]{intent.getData()};
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    String result = intent.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                    if (result != null && URLUtil.isValidUrl(result)) {
                        loadQRCodeURL(result);
                    }
                }
            }
        }
        /* else {
            super.handleActivityResult(requestCode, resultCode, intent);
        }*/
    }

    private boolean URLisExternal(String url) {
        // have to be careful here with NFC. if you are writing a URL to a card
        // then url.contains(Config.HOST) == True, so I changed it to hostpart.contains
        hostpart = Uri.parse(url).getHost();
        if (hostpart.contains(Config.HOST) || url.startsWith(Config.HOST)) {
            return false;
        } else {
            return true;
        }
    }

    private void loadQRCodeURL(String url) {
        switch(Config.QR_CODE_URL_OPTIONS) {

            // Option 1: load in an in-app tab
            case 1:
                openInInappTab(url);
                break;
            // Option 2: load in a new browser
            case 2:
                openInNewBrowser(url);
                break;
            // Option 3: load in an in-app tab if external
            case 3:
                if (URLisExternal(url)) {
                    openInInappTab(url);
                } else {
                    webView.loadUrl(url);
                }
                break;
            // Option 4: load in a new browser if external
            case 4:
                if (URLisExternal(url)) {
                    openInNewBrowser(url);
                } else {
                    webView.loadUrl(url);
                }
                break;
            // Default (Option 0): load in the app
            default:
                webView.loadUrl(url);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "";
        File mediaStorageDir = getCacheDir();
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + "WebView" + " directory");
                return null;
            }
        }
        return File.createTempFile(
                imageFileName,
                ".jpg",
                mediaStorageDir
        );
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "VID_" + timeStamp + "";
        File mediaStorageDir = getCacheDir();

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + "WebView" + " directory");
                return null;
            }
        }
        return File.createTempFile(
                imageFileName,
                ".mp4",
                mediaStorageDir
        );
    }

    @Override
    public void onBackPressed() {
        if (windowContainer.getVisibility() == View.VISIBLE) {
            ClosePopupWindow(mWebviewPop);
        } else if (Config.EXIT_APP_BY_BACK_BUTTON_ALWAYS) {
            if (EXIT_APP_DIALOG) {
//                ExitDialog();
            } else {
                super.onBackPressed();
            }
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else if (Config.EXIT_APP_BY_BACK_BUTTON_HOMEPAGE) {
            if (EXIT_APP_DIALOG) {
//                ExitDialog();
            } else {
                super.onBackPressed();
            }
        }
    }


    private void customCSS() {
        try {
            InputStream inputStream = getAssets().open("custom.css");
            byte[] cssbuffer = new byte[inputStream.available()];
            inputStream.read(cssbuffer);
            inputStream.close();

            String encodedcss = Base64.encodeToString(cssbuffer, Base64.NO_WRAP);
            if (!TextUtils.isEmpty(encodedcss)) {
                Log.d("css", "Custom CSS loaded");
                webView.loadUrl("javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "style.innerHTML = window.atob('" + encodedcss + "');" +
                        "parent.appendChild(style)" +
                        "})()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void customJavaScript() {
        try {
            InputStream inputStream = getAssets().open("custom.js");
            byte[] jsBuffer = new byte[inputStream.available()];
            inputStream.read(jsBuffer);
            inputStream.close();

            String encodedJs = Base64.encodeToString(jsBuffer, Base64.NO_WRAP);
            if (!TextUtils.isEmpty(encodedJs)) {
                Log.i(TAG, "Custom Javascript loaded");
                webView.loadUrl("javascript:(function() {" +
                        "var customJsCode = window.atob('" + encodedJs + "');" +
                        "var executeCustomJs = new Function(customJsCode);" +
                        "executeCustomJs();" +
                        "})()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openDownloadedAttachment(Context context, Uri parse, String downloadMimeType) {
    }


    protected static File screenshot(View view, String filename) {

        Date date = new Date();

        // Here we are initialising the format of our image name
        CharSequence format = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);
        try {
            // Initialising the directory of storage
            String dirpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "";
            File file = new File(dirpath);
            if (!file.exists()) {
                boolean mkdir = file.mkdir();
            }

            // File name
            String path = dirpath + "/DCIM/" + filename + "-" + format + ".jpeg";
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            File imageurl = new File(path);

            saveImage(bitmap, format.toString());

//            Process sh = Runtime.getRuntime().exec("su", null,null);
//            OutputStream os = sh.getOutputStream();
//            os.write(("/system/bin/screencap -p " + dirpath + "/DCIM/" + filename + ".png").getBytes("ASCII"));
//            os.flush();
//            os.close();
//            sh.waitFor();

//            if(imageurl.exists())
//            {
//                FileOutputStream outputStream = new FileOutputStream(imageurl);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
//                outputStream.flush();
//                outputStream.close();
//                System.out.println("!!!!1!");
//            }
//            else
//            {
//                FileOutputStream outputStream = new FileOutputStream(imageurl);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
//                outputStream.flush();
//                outputStream.close();
////                System.out.println("!!!!1!");
//                System.out.println("!!!! not exist !");
//            }

            return imageurl;

        } catch (IOException e) {
            System.out.println("!!!");
            e.printStackTrace();
        }
        return null;
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static String[] PERMISSION_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };



    private static final int REQUEST_NOTIFICATION = 11;

    public static void saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = mContext.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "img");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + "img";

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);
        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
    }

    private static String[] permissionstorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    // verifying if storage permission is given or not
    public static void verifystoragepermissions(Activity activity) {

        int permissions = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        System.out.println("?!" + permissions);
        System.out.println("?!!" + PackageManager.PERMISSION_GRANTED);

        // If storage permission is not given then request for External Storage Permission

        ActivityCompat.requestPermissions(activity, permissionstorage, 1);

    }


    private void loadMainUrl() {

        if (!isConnectedNetwork()) {
            System.out.println("loadMainUrl no connection");
        } else {
            offlineLayout.setVisibility(View.GONE);

            if (Config.IS_DEEP_LINKING_ENABLED && deepLinkingURL != null && !deepLinkingURL.isEmpty()) {
                Log.i(TAG, " deepLinkingURL " + deepLinkingURL);
                if (isNotificationURL && Config.OPEN_NOTIFICATION_URLS_IN_SYSTEM_BROWSER && URLUtil.isValidUrl(deepLinkingURL)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkingURL)));
                    deepLinkingURL = null;
                } else if (URLUtil.isValidUrl(deepLinkingURL)) {
                    webView.loadUrl(deepLinkingURL);
                    return;
                } else {
                    Toast.makeText(this, "URL is not valid", Toast.LENGTH_SHORT).show();
                }
            }
            String urlExt = "";
            String urlExt2 = "";
            String urlExtUUID = "";
            String language = "";
            if (Config.APPEND_LANG_CODE) {
                language = Locale.getDefault().getLanguage().toUpperCase();
                language = "?webview_language=" + language;
            } else {
                language = "";
            }
            String urlToLoad = Config.HOME_URL + language;

            urlToLoad += urlExt;

            urlToLoad += urlExt2;
            if (Config.UUID_ENHANCE_WEBVIEW_URL) {
                if (urlToLoad.contains("?") || urlExt.contains("?")) {
                    urlExtUUID = String.format("%suuid=%s", "&", uuid);
                } else {
                    urlExtUUID = String.format("%suuid=%s", "?", uuid);
                }
            }
            urlToLoad += urlExtUUID;

            if (Config.USE_LOCAL_HTML_FOLDER) {
                loadLocal(INDEX_FILE);
            } else {
                Log.i(TAG, " HOME_URL " + urlToLoad);
                webView.loadUrl(urlToLoad);
            }
        }
    }

    public boolean isConnectedNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }

    }

    @SuppressLint("WrongConstant")
    private void askForPermission() {
//        int accessCoarseLocation = 0;
//        int accessFineLocation = 0;
//        int accessCamera = 0;
//        int accessStorage = 0;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            accessCoarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
//            accessFineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
//            accessCamera = checkSelfPermission(Manifest.permission.CAMERA);
//            accessStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//            Log.d("per", ">=M");
//
//        } else {
//            Log.d("per", "<M");
//        }
//
//
//        List<String> listRequestPermission = new ArrayList<String>();
//
//        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
//            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
//        }
//        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
//            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
//        }
//        if (accessCamera != PackageManager.PERMISSION_GRANTED) {
//            listRequestPermission.add(Manifest.permission.CAMERA);
//        }
//        if (accessStorage != PackageManager.PERMISSION_GRANTED) {
//            listRequestPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//            listRequestPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//        if (!listRequestPermission.isEmpty()) {
//            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(strRequestPermission, 1);
//            }
//        }


    }







    @Override
    public void onStop() {

//        if (cookieSyncOn) {
//            Log.i(TAG, "Cookies sync cancelled");
//            cookieSyncHandler.removeCallbacks(cookieSyncRunnable);
//        }
//        if (Config.CLEAR_CACHE_ON_EXIT) {
//            webView.clearCache(true);
//            CookieManager.getInstance().removeAllCookies(null);
//            CookieManager.getInstance().flush();
//        }


        super.onStop();
    }

    @Override
    public void onResume() {

//        if (Config.AUTO_REFRESH_ENABLED) {
//            webView.reload();
//        }
        // Manual Cookie Sync Tool
//        if (Config.MANUAL_COOKIE_SYNC) {
//
//            // Check if the page requires manual cookie syncing
//            boolean syncCookies = false;
//            String url = webView.getUrl();
//            int nbTriggers = Config.MANUAL_COOKIE_SYNC_TRIGGERS.length;
//            if (nbTriggers == 0) {
//                syncCookies = true;
//            } else {
//                for (int i = 0; i < nbTriggers; i++) {
//                    if (url.startsWith(Config.MANUAL_COOKIE_SYNC_TRIGGERS[i])) {
//                        syncCookies = true;
//                        break;
//                    }
//                }
//            }
//
//            // Manually sync cookies so that there is no 30 second delay
//            if (syncCookies) {
//                Log.i(TAG, "Cookies sync on");
//                cookieSyncHandler.postDelayed(cookieSyncRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//                            CookieManager.getInstance().flush();
//                            Log.i(TAG, "Cookies flushed");
//                            cookieSyncHandler.postDelayed(cookieSyncRunnable, Config.COOKIE_SYNC_TIME);
//                        }
//                    }
//                }, Config.COOKIE_SYNC_TIME);
//            }
//
//            // Ensures consistent timing
//        }
        inAppUpdateTranslator();
        super.onResume();


    }
    private void inAppUpdateTranslator() {
        AppUpdateManager appUpdateManagerForTrans = AppUpdateManagerFactory.create(MainActivity.this);
        Task<AppUpdateInfo> appUpdateInfoTaskForTrans = appUpdateManagerForTrans.getAppUpdateInfo();
        appUpdateInfoTaskForTrans.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                try {
                    appUpdateManagerForTrans.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE
                            , MainActivity.this, 101);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();

//                    Log.d(TAG,"callInAppUpdate:"+e.getMessage());
                }
            }
        });
    }







    private Handler notificationHandler;
    private Handler CartRemindernotificationHandler;
    private Handler CategoryRecommNotificationHandler;
    private Handler ProductRecommNotificationHandler;

    Timer timer = new Timer();

    private class AdvanceWebViewClient extends MyWebViewClient {

        private Handler notificationHandler;

        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            if (Config.FALLBACK_USE_LOCAL_HTML_FOLDER_IF_OFFLINE) {
                loadLocal(INDEX_FILE);
            } else {
                webView.setVisibility(View.GONE);
                offlineLayout.setVisibility(View.VISIBLE);
                playBeep();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (Config.BLOCK_SELF_SIGNED_AND_FAULTY_SSL_CERTS){
                handler.cancel();
            }
            else{
                handler.proceed();
            }
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            Context context = view.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View dialogView = layoutInflater.inflate(R.layout.activity_dialog_credentials, new LinearLayout(context));
            EditText username = dialogView.findViewById(R.id.username);
            EditText password = dialogView.findViewById(R.id.password);

            builder.setView(dialogView)
                    .setTitle(R.string.auth_dialogtitle)
                    .setPositiveButton(R.string.submit, null)
                    .setNegativeButton(android.R.string.cancel,
                            (dialog, whichButton) -> handler.cancel())
                    .setOnDismissListener(dialog -> handler.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (TextUtils.isEmpty(username.getText())) {
                    username.setError(getResources().getString(R.string.user_name_required));
                } else if (TextUtils.isEmpty(password.getText())) {
                    password.setError(getResources().getString(R.string.password_name_required));
                } else {
                    handler.proceed(username.getText().toString(), password.getText().toString());
                    dialog.dismiss();
                }
            });
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            WebSettings webSettings = view.getSettings();

            // Google login helper tool
            if (Config.GOOGLE_LOGIN_HELPER_TRIGGERS.length != 0) {
                for (int i = 0; i < Config.GOOGLE_LOGIN_HELPER_TRIGGERS.length; i++) {
                    if (url.startsWith(Config.GOOGLE_LOGIN_HELPER_TRIGGERS[i])) {
                        webSettings.setUserAgentString(USER_AGENT_GOOGLE);
                        if (windowContainer.getVisibility() == View.VISIBLE) {
                            mWebviewPop.loadUrl(url);
                        } else {
                            view.loadUrl(url);
                        }
                        return true;
                    }
                }
            }

            // Facebook login helper tool
            if (Config.FACEBOOK_LOGIN_HELPER_TRIGGERS.length != 0) {
                for (int i = 0; i < Config.FACEBOOK_LOGIN_HELPER_TRIGGERS.length; i++) {
                    if (url.startsWith(Config.FACEBOOK_LOGIN_HELPER_TRIGGERS[i])) {
                        webSettings.setUserAgentString(USER_AGENT_FB);
                        if (windowContainer.getVisibility() == View.VISIBLE) {
                            mWebviewPop.loadUrl(url);
                        } else {
                            view.loadUrl(url);
                        }
                        return true;
                    }
                }
            }

            // Logout tool
            if (url.startsWith(Config.HOME_URL_LOGOUT) && (Config.HOME_URL_LOGOUT.length() != 0)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                } else if (mContext != null) {
                    CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(mContext);
                    cookieSyncManager.startSync();
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.removeAllCookie();
                    cookieManager.removeSessionCookie();
                    cookieSyncManager.stopSync();
                    cookieSyncManager.sync();
                }
            }

            // Scanning mode
            if (scanningModeOn && !persistentScanningMode) {
                turnOffScanningMode();
            }

            // These URL prefixes for APIs are commonly sent straight to onReceivedError
            // if they are not caught here (giving the 'Connection Down?' screen).

            if (url.contains("push.send.cancel")) {
                if (Config.USER_AGENT.contains("VRGl")) {
                    if (url.contains("cartreminderpush.send.cancel")) {
                        stopCartReminderNotification();
                    }
                    if (url.contains("categoryrecommpush.cancel")) {
                        stopCategoryRecommNotification();
                    }
                    if (url.contains("productrecommpush.cancel")) {
                        stopProductRecommNotification();
                    }
                    return true;
                }
                else{
                    stopNotification();
                    return true;
                }
            }
            if (url.contains("push.send")) {
                if (Config.USER_AGENT.contains("VRGl")) {

                    if (url.contains("cartreminderpush.send")) {
                        sendCartReminderNotification(url);
                    }
                    if (url.contains("categoryrecommpush.send")) {
                        sendCategoryRecommNotification(url);
                    }
                    if (url.contains("productrecommpush.send")) {
                        sendProductRecommNotification(url);
                    }
                    return true;
                }
                else {

                    return true;
                }
            }


            if (url.startsWith("getappversion://")) {
                webView.loadUrl("javascript: var versionNumber = '" + BuildConfig.VERSION_NAME + "';" +
                        "var bundleNumber  = '" + BuildConfig.VERSION_CODE + "';");
                return true;
            }
            if (url.startsWith("get-uuid://")) {
                webView.loadUrl("javascript: var uuid = '" + uuid + "';");
                return true;
            }

            if (!isRedirected) {
                //Basic Overriding part here (1/2)
                Log.i(TAG, "should override (1/2): " + url);

                if (url.startsWith("wc:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "WalletConnect app not found on device; 'wc:' scheme failed");
                    }
                    return true;
                }
                if (url.startsWith("mailto:")) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                    return true;
                }
                if (url.startsWith("share:") || url.contains("api.whatsapp.com")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("whatsapp:")) {
                    Intent i = new Intent();
                    i.setPackage("com.whatsapp");
                    i.setAction(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("geo:") || url.contains("maps:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("market:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("maps.app.goo.gl")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.contains("maps.google.com")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("intent:")) {
                    handleIntentUrl(url);
                    return true;
                }
                if (url.startsWith("tel:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("sms:")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("play.google.com")) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                if (url.startsWith("blob:")) {
                    //Prevent crash
                    return true;
                }


                // Check if the URL should always open in an in-app tab
                if ((url != null) && shouldAlwaysOpenInInappTab(url)) {
                    openInInappTab(url);
                    return true;
                }

                if (SPECIAL_LINK_HANDLING_OPTIONS != 0) {
                    WebView.HitTestResult result = view.getHitTestResult();
                    String data = result.getExtra();
                    Log.i(TAG, " data :" + data);

                    if ((data != null && data.endsWith("#")) || url.startsWith("newtab:")) {

                        String finalUrl = url;
                        if (url.startsWith("newtab:")) {
                            finalUrl = url.substring(7);
                        }

                        // Open special link in an in-app tab
                        if ((SPECIAL_LINK_HANDLING_OPTIONS == 1) || shouldAlwaysOpenInInappTab(finalUrl)) {
                            openInInappTab(finalUrl);
                            return true;

                            // Open special link in Chrome
                        } else if (SPECIAL_LINK_HANDLING_OPTIONS == 2) {
                            view.getContext().startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)));
                            return true;
                        }
                        return false;
                    }
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
            return false;
        }

    }

    private void handleIntentUrl(String url) {

        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "No app to handle intent URL");

            // Fallback URL
            String fallbackParameter = "browser_fallback_url=";
            String separatorChar = ";";
            int startingIndex = 0;
            if (url.contains(fallbackParameter)) {
                try {
                    String fallbackURL = url.substring(url.indexOf(fallbackParameter) + fallbackParameter.length());
                    fallbackURL = fallbackURL.substring(startingIndex, fallbackURL.indexOf(separatorChar));
                    if (URLUtil.isValidUrl(fallbackURL)) {
                        Log.e(TAG, "Fallback URL found, loading in external browser");
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackURL));
                        startActivity(i);
                    }
                } catch (Exception f) {
                    Log.e(TAG, "Fallback URL failed");
                }
            }
        }
    }
    private void wakeUpDevice(AppClass context) {

        PowerManager.WakeLock wakeLock = context.getWakeLock(); // get WakeLock reference via AppContext
        if (wakeLock.isHeld()) {
            wakeLock.release(); // release old wake lock
        }

        // create a new wake lock...
        wakeLock.acquire();

        // ... and release again
        wakeLock.release();
    }
    @SuppressWarnings("SpellCheckingInspection")
    private class MyWebViewClient extends WebViewClient {

        MyWebViewClient() {
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!isRedirected || INCREMENT_WITH_REDIRECTS) {
                super.onPageStarted(view, url, favicon);

            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!isRedirected) {
                setTitle(view.getTitle());
                customCSS();
                customJavaScript();
                // Disable link drag and drop
                if (!Config.LINK_DRAG_AND_DROP) {
                    String disableLinkDragScript = "javascript: var links = document.getElementsByTagName('a');" +
                            "for (var i = 0; i < links.length; i++) {" +
                            "   links[i].draggable = false;" +
                            "}";
                    view.loadUrl(disableLinkDragScript);
                }

                if (url.contains("print-receipt")){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap =  ViewCapture.with(mWebviewPop).getBitmap();
                            Log.e("34343434", "run: create bitmap" );
                            singleThreadExecutor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        int ret = printerService.printBitmap(bitmap, 1, 1);
                                        showLog("Print bitmap: " + msg(ret));
                                        if (ret == 0) {
                                            paperOut();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }, 500);

                    final Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("34343434", "backpress " );
                            onBackPressed();
                        }
                    }, 1000);

                    AppClass ctx = (AppClass) getApplicationContext();
                    wakeUpDevice(ctx);
                    NotificationHelper.showNotification(MainActivity.this, "New Order", "Click To View New Order.");

                }
                if (url.contains("json/new-order-received")){
                    onBackPressed();
                    AppClass ctx = (AppClass) getApplicationContext();
                    wakeUpDevice(ctx);
                    NotificationHelper.showNotification(MainActivity.this, "New Order", "Click To View New Order.");
                }


//                if (SPLASH_SCREEN_ACTIVATED && SPLASH_SCREEN_ACTIVE && (SplashScreen.getInstance() != null) && REMAIN_SPLASH_OPTION) {
//                    SplashScreen.getInstance().finish();
//                    SPLASH_SCREEN_ACTIVE = false;
//                }
                super.onPageFinished(view, url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (!isRedirected) {
                hostpart = Uri.parse(url).getHost();
                Log.i(TAG, "should override : " + url);

                // logic for loading given URL
                if (isConnectedNetwork()) {

                    // Check for a file download URL (can be internal or external)
                    if (url.contains(".") &&
                            downloadableExtension.contains(url.substring(url.lastIndexOf(".")))) {

                        webView.stopLoading();

                        openInInappTab(url);


                        return true;
                    }

                    if (!URLisExternal(url)) {
                        return false;

                    } else if (url.startsWith("inapppurchase://")
                            || url.startsWith("inappsubscription://")) {

                        if (extendediap) {
                            Log.i(TAG, "InApp URL: " + url);

                            Log.i(TAG, " toast ");
                            String iaptext1 = "U2VlIGh0dHBzOi8vdGlueXVybC5jb20vaWFwLWZpeCB8IEluLUFwcCBQdXJjaGFzZSBmYWlsZWQu";
                            byte[] iapdata1 = Base64.decode(iaptext1, Base64.DEFAULT);
                            String iapdata1final = new String(iapdata1, StandardCharsets.UTF_8);
                            Toast.makeText(MainActivity.this, iapdata1final, Toast.LENGTH_SHORT).show();

                            return true;
                        } else {
                            String iaptext2 = "U2VlIGh0dHBzOi8vdGlueXVybC5jb20vaWFwLWZpeCB8IEluLUFwcCBQdXJjaGFzZSBmYWlsZWQu";
                            byte[] iapdata2 = Base64.decode(iaptext2, Base64.DEFAULT);
                            String iapdata2final = new String(iapdata2, StandardCharsets.UTF_8);
                            Toast.makeText(MainActivity.this, iapdata2final, Toast.LENGTH_LONG).show();
                            return true;
                        }
                    } else if (url.startsWith("qrcode://")) {
                        Log.e(TAG, url);

                        return true;
                    }
                    if (url.startsWith("savethisimage://?url=")) {
                        webView.stopLoading();
                        if (webView.canGoBack()) {
                            webView.goBack();
                        }

                        return true;
                    } else if (url.contains("push.send.cancel")) {
                        if (Config.USER_AGENT.contains("VRGl")) {
                            if (url.contains("cartreminderpush.send.cancel")) {
                                stopCartReminderNotification();
                            }
                            if (url.contains("categoryrecommpush.cancel")) {
                                stopCategoryRecommNotification();
                            }
                            if (url.contains("productrecommpush.cancel")) {
                                stopProductRecommNotification();
                            }
                            return true;
                        }
                        else{
                            stopNotification();
                            return true;
                        }
                    }
                    else if (url.contains("push.send")) {
                        if (Config.USER_AGENT.contains("VRGl")) {
                            if (url.contains("cartreminderpush.send")) {
                                sendCartReminderNotification(url);
                            }
                            if (url.contains("categoryrecommpush.send")) {
                                sendCategoryRecommNotification(url);
                            }
                            if (url.contains("productrecommpush.send")) {
                                sendProductRecommNotification(url);
                            }
                            return true;
                        }
                        else {
                            sendNotification(url);
                            return true;
                        }
                    }  else if (url.startsWith("get-uuid://")) {
                        webView.loadUrl("javascript: var uuid = '" + uuid + "';");
                        return true;
                    } else if (url.startsWith("reset://")) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            CookieManager.getInstance().removeAllCookies(null);
                            CookieManager.getInstance().flush();
                        } else if (mContext != null) {
                            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(mContext);
                            cookieSyncManager.startSync();
                            CookieManager cookieManager = CookieManager.getInstance();
                            cookieManager.removeAllCookie();
                            cookieManager.removeSessionCookie();
                            cookieSyncManager.stopSync();
                            cookieSyncManager.sync();
                        }


                        WebSettings webSettings = webView.getSettings();
                        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                        webView.clearCache(true);
                        android.webkit.WebStorage.getInstance().deleteAllData();
                        Toast.makeText(MainActivity.this, "App reset was successful.", Toast.LENGTH_LONG).show();
                        loadMainUrl();
                        return true;
                    } else if (url.startsWith("readnfc://")) {
                        readModeNFC = true;
                        writeModeNFC = false;
                        return true;
                    } else if (url.startsWith("writenfc://")) {
                        writeModeNFC = true;
                        readModeNFC = false;
                        textToWriteNFC = url.substring(url.indexOf("=") + 1, url.length());
                        return true;
                    } else if (url.startsWith("spinneron://")) {
                        progressBar.setVisibility(View.VISIBLE);
                        return true;
                    } else if (url.startsWith("spinneroff://")) {
                        progressBar.setVisibility(View.GONE);
                        return true;
                    } else if (url.startsWith("takescreenshot://")) {
                        verifystoragepermissions(MainActivity.this);

                        Toast.makeText(MainActivity.this, "Screenshot Saved", Toast.LENGTH_LONG).show();
                        screenshot(getWindow().getDecorView().getRootView(), "result");

                        return true;

                    } else if (url.startsWith("getonesignalplayerid://")) {

                        return true;

                    }  else if (url.startsWith("getfirebaseplayerid://")) {

                        String firebaseUserId = AlertManager.getFirebaseToken(MainActivity.this, "");
                        webView.loadUrl("javascript: var firebaseplayerid = '" + firebaseUserId + "';");

                        return true;

                    } else if (url.startsWith("getappversion://")) {
                        webView.loadUrl("javascript: var versionNumber = '" + BuildConfig.VERSION_NAME + "';" +
                                "var bundleNumber  = '" + BuildConfig.VERSION_CODE + "';");
                        return true;

                    } else if (url.startsWith("shareapp://")) {

                        String sharetext = url.toString();
                        String newmeg = sharetext.substring(20);
                        Log.d("newmeg", newmeg);

                        String inputString = newmeg;
                        String delimiter = "&url=";
                        String[] components = inputString.split(delimiter);
                        String message2 = "";
                        String url2 = "";

                        if (components.length > 1) {
                            message2 = components[0];
                            url2 = components[1];
                        } else {
                            message2 = newmeg;
                        }
                        String message1 = message2.replace("%20", " ");
                        String url1 = url2.replace("%20", " ");

                        String totalMessage = "";
                        if (message1.length() == 0) {
                            totalMessage = url1;
                        } else if (url1.length() == 0) {
                            totalMessage = message1;
                        } else {
                            totalMessage = message1 + "\n" + url1;
                        }

                        List<String> objectsToShare = new ArrayList<>();
                        objectsToShare.add(totalMessage);

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, totalMessage);

                        Intent chooser = Intent.createChooser(intent, "Share via");
                        chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        List<LabeledIntent> intents = new ArrayList<>();
                        for (ResolveInfo info : getPackageManager().queryIntentActivities(intent, 0)) {
                            Intent target = new Intent(Intent.ACTION_SEND);
                            target.setType("text/plain");
                            target.putExtra(Intent.EXTRA_TEXT, totalMessage);
                            target.setPackage(info.activityInfo.packageName);
                            intents.add(new LabeledIntent(target, info.activityInfo.packageName, info.loadLabel(getPackageManager()), info.icon));
                        }

                        Parcelable[] extraIntents = intents.toArray(new Parcelable[intents.size()]);
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                        startActivity(chooser);

                        return true;
                    }
                    else if (url.startsWith("statusbarcolor://") && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {

                        String input = url.substring(url.indexOf('/') + 2);
                        String[] values = input.split(",");
                        int nbValues = values.length;

                        if (nbValues == 3 || nbValues == 4) {
                            int colorValues[] = new int[nbValues];
                            for (int i = 0; i < nbValues; i++) {
                                colorValues[i] = Integer.parseInt(values[i].trim());
                            }
                            int color;
                            Double luminance = 0.0;
                            Double rgbFactor = 255.0;
                            if (nbValues == 3) {
                                // Index 0 = red, 1 = green, 2 = blue
                                color = Color.rgb(colorValues[0], colorValues[1], colorValues[2]);
                                luminance = 0.2126 * (colorValues[0] / rgbFactor) + 0.7152 * (colorValues[1] / rgbFactor) + 0.0722 * (colorValues[2] / rgbFactor);
                            } else {
                                // Inlcudes transparency (alpha); This feature is not fully supported yet as the webview dimensions need to be changed as well.
                                // Index 0 = alpga, 1 = red, 2 = green, 3 = blue
                                color = Color.argb(colorValues[0], colorValues[1], colorValues[2], colorValues[3]);
                            }
                            Window window = getWindow();
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(color);

                            // Automatically decide the color of the status bar text
                            Double darkThreshold = 0.5;
                            if (luminance < darkThreshold) {
                                // Color is dark; use white text
                                getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            } else {
                                // Color is light; use black text
                                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            }
                        }
                        return true;

                    } else if (url.startsWith("statusbartextcolor://") && ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M))) {

                        String input = url.substring(url.indexOf('/') + 2);

                        if (input.equals("white")) {
                            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        } else if (input.equals("black")) {
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        }
                        return true;

                    } else if (url.startsWith("scanningmode://")) {

                        String input = url.substring(url.indexOf('/') + 2);

                        if (input.equals("auto")) {
                            turnOnScanningMode();
                        } else if (input.equals("on")) {
                            persistentScanningMode = true;
                            turnOnScanningMode();
                        } else if (input.equals("off")) {
                            persistentScanningMode = false;
                            turnOffScanningMode();
                        }
                        return true;
                    }

                } else if (!isConnectedNetwork()) {
                    if (Config.FALLBACK_USE_LOCAL_HTML_FOLDER_IF_OFFLINE) {
                        if (!offlineFileLoaded) {
                            loadLocal(INDEX_FILE);
                            offlineFileLoaded = true;
                        } else {
                            loadLocal(url);
                        }
                    } else {
                        offlineLayout.setVisibility(View.VISIBLE);
                        playBeep();
                    }
                    return true;
                }

                if (hostpart.contains("whatsapp.com")) {
                    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    final int newDocumentFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | newDocumentFlag | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    startActivity(intent);
                }

                if (((Config.EXTERNAL_LINK_HANDLING_OPTIONS != 0) && !(url).startsWith("file://") && (!Config.USE_LOCAL_HTML_FOLDER || !(url).startsWith("file://"))) && URLUtil.isValidUrl(url)) {
                    if (Config.EXTERNAL_LINK_HANDLING_OPTIONS == 1) {
                        // open in a new tab (additional in-app browser)
                        openInInappTab(url);
                        return true;
                    } else if (Config.EXTERNAL_LINK_HANDLING_OPTIONS == 2) {
                        // open in a new browser
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return false;
        }
    }

    private void turnOnScanningMode() {
        if (!scanningModeOn) {
            WindowManager.LayoutParams layout = getWindow().getAttributes();

            // Record previous screen brightness
            previousScreenBrightness = layout.screenBrightness;

            // Turn on scanning mode
            scanningModeOn = true;
            layout.screenBrightness = 1F;
            getWindow().setAttributes(layout);

        }
    }

    private void turnOffScanningMode() {
        if (scanningModeOn) {
            WindowManager.LayoutParams layout = getWindow().getAttributes();

            // Turn off scanning mode
            scanningModeOn = false;
            layout.screenBrightness = previousScreenBrightness;
            getWindow().setAttributes(layout);

        }
    }


    private void sendNotification(String url) {
        final int secondsDelayed = Integer.parseInt(url.split("=")[1]);

        final String[] contentDetails = (url.substring((url.indexOf("msg!") + 4), url.length())).split("&!#");
        String message = contentDetails[0].replaceAll("%20", " ");
        String title = contentDetails[1].replaceAll("%20", " ");

        try {
            message = URLDecoder.decode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            title = URLDecoder.decode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String urlToOpen = null;
        // if data has length greater then 2 then there should be URL at index 2
        if(contentDetails.length > 2) {
            urlToOpen = contentDetails[2].replaceAll("%20", " ");
        }

        final Notification.Builder builder = getNotificationBuilder(title, message, urlToOpen);

        final Notification notification = builder.build();
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationHandler = new Handler();
        notificationHandler.postDelayed(() -> {
            notificationManager.notify(0, notification);
            notificationHandler = null;
        }, secondsDelayed * 1000);
    }

    private void sendCartReminderNotification(String url) {



        final int secondsDelayed = Integer.parseInt(url.split("=")[1]);

        final String[] contentDetails = (url.substring((url.indexOf("msg!") + 4), url.length())).split("&!#");

        String message = contentDetails[0].replaceAll("%20", " ");
        String title = contentDetails[1].replaceAll("%20", " ");


        try {
            message = URLDecoder.decode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            title = URLDecoder.decode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        String urlToOpen = null;
        // if data has length greater than 2 then there should be URL at index 2
        if (contentDetails.length > 2) {
            urlToOpen = contentDetails[2].replaceAll("%20", " ");
        }

        final Notification.Builder builder = getNotificationBuilder(title, message, urlToOpen);

        final Notification notification = builder.build();
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        CartRemindernotificationHandler = new Handler();
        CartRemindernotificationHandler.postDelayed(() -> {
            notificationManager.notify(0, notification);
            CartRemindernotificationHandler = null;
        }, secondsDelayed * 1000);
    }
    private void sendCategoryRecommNotification(String url) {


        final int secondsDelayed = Integer.parseInt(url.split("=")[1]);

        final String[] contentDetails = (url.substring((url.indexOf("msg!") + 4), url.length())).split("&!#");


        String message = contentDetails[0].replaceAll("%20", " ");
        String title = contentDetails[1].replaceAll("%20", " ");


        try {
            message = URLDecoder.decode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            title = URLDecoder.decode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        String urlToOpen = null;
        // if data has length greater than 2 then there should be URL at index 2
        if (contentDetails.length > 2) {
            urlToOpen = contentDetails[2].replaceAll("%20", " ");
        }

        final Notification.Builder builder = getNotificationBuilder(title, message, urlToOpen);

        final Notification notification = builder.build();
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        CategoryRecommNotificationHandler = new Handler();
        CategoryRecommNotificationHandler.postDelayed(() -> {
            notificationManager.notify(0, notification);
            CategoryRecommNotificationHandler = null;
        }, secondsDelayed * 1000);
    }
    private void sendProductRecommNotification(String url) {
        final int secondsDelayed = Integer.parseInt(url.split("=")[1]);

        final String[] contentDetails = (url.substring((url.indexOf("msg!") + 4), url.length())).split("&!#");
        String message = contentDetails[0].replaceAll("%20", " ");
        String title = contentDetails[1].replaceAll("%20", " ");


        try {
            message = URLDecoder.decode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            title = URLDecoder.decode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String urlToOpen = null;
        // if data has length greater than 2 then there should be URL at index 2
        if (contentDetails.length > 2) {
            urlToOpen = contentDetails[2].replaceAll("%20", " ");
        }

        final Notification.Builder builder = getNotificationBuilder(title, message, urlToOpen);

        final Notification notification = builder.build();
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        ProductRecommNotificationHandler = new Handler();
        ProductRecommNotificationHandler.postDelayed(() -> {
            notificationManager.notify(0, notification);
            ProductRecommNotificationHandler = null;
        }, secondsDelayed * 1000);
    }

    private void stopNotification() {
        if (notificationHandler != null) {
            notificationHandler.removeCallbacksAndMessages(null);
            notificationHandler = null;
        }
    }
    private void stopCartReminderNotification() {
        if (CartRemindernotificationHandler != null) {
            CartRemindernotificationHandler.removeCallbacksAndMessages(null);
            CartRemindernotificationHandler = null;
        }
    }
    private void stopCategoryRecommNotification() {
        if (CategoryRecommNotificationHandler != null) {
            CategoryRecommNotificationHandler.removeCallbacksAndMessages(null);
            CategoryRecommNotificationHandler = null;
        }
    }
    private void stopProductRecommNotification() {
        if (ProductRecommNotificationHandler != null) {
            ProductRecommNotificationHandler.removeCallbacksAndMessages(null);
            ProductRecommNotificationHandler = null;
        }
    }

    private Notification.Builder getNotificationBuilder(String title, String message, String urlToOpen) {

        createNotificationChannel();
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(MainActivity.this, getString(R.string.local_notification_channel_id));
        } else {
            builder = new Notification.Builder(MainActivity.this);
        }

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("ONESIGNAL_URL", urlToOpen);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(MainActivity.this, 1, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        }

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        return builder;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.local_notification_channel_name);
            String description = getString(R.string.local_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.local_notification_channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    private class CustomeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (ENABLE_SWIPE_NAVIGATE) {
                if (e1 == null || e2 == null) return false;
                if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) return false;
                else {

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;
                    int edgeSwipeTolerance = 30;

                    try {
                        // Detect a left swipe
                        if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 800) {
//                            Log.i(TAG, "LEFT swipe: e1.X = " + e1.getX() + ", e2.X = " + e2.getX());
                            // Detect a "forwards" gesture (left swipe from the right edge)
                            if (e1.getX() > (screenWidth - edgeSwipeTolerance)) {
                                Log.i(TAG, "FORWARDS swipe detected");
                                if (webView.canGoForward()) {
                                    webView.goForward();
                                }
                                return true;
                            }
                        }
                        // Detect a right swipe
                        else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 800) {
//                            Log.i(TAG, "RIGHT swipe: e1.X = " + e1.getX() + ", e2.X = " + e2.getX());
                            // Detect a "backwards" gesture (right swipe from the left edge)
                            if (e1.getX() < edgeSwipeTolerance) {
                                Log.i(TAG, "BACKWARDS swipe detected");
                                if (webView.canGoBack()) {
                                    webView.goBack();
                                }
                                return true;
                            }
                        }
                    } catch (Exception e) { // nothing
                    }
                    return false;
                }
            }
            return false;
        }
    }

    private class AdvanceWebChromeClient extends MyWebChromeClient {

        private Handler notificationHandler;
        private Handler CartRemindernotificationHandler;
        private Handler CategoryRecommNotificationHandler;
        private Handler ProductRecommNotificationHandler;

        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
            ClosePopupWindow(mWebviewPop);
            Log.i(TAG, "onCloseWindow url " + window.getUrl());
            Log.i(TAG, "onCloseWindow url " + window.getOriginalUrl());
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {

            Bundle extras = getIntent().getExtras();
            String URL = null;
            if (extras != null) {
                URL = extras.getString("ONESIGNAL_URL");
            }
            if (URL != null && !URL.equalsIgnoreCase("")) {
                isNotificationURL = true;
                deepLinkingURL = URL;
            } else isNotificationURL = false;
            preferences.edit().putString("proshow", "show").apply();

            Log.i(TAG, " LOG24 " + deepLinkingURL);

            WebView.HitTestResult result = view.getHitTestResult();
            String data = result.getExtra();

            // Link with an Image
            if (result.getType() == result.SRC_IMAGE_ANCHOR_TYPE) {
                // Get the source link, not the image link
                Message href = view.getHandler().obtainMessage();
                view.requestFocusNodeHref(href);
                String imageLinkSource = href.getData().getString("url");
                data = imageLinkSource;
            }

            // Check if the URL should always open in an in-app tab
            if ((data != null) && shouldAlwaysOpenInInappTab(data)) {
                openInInappTab(data);
                return true;
            }

//            if (resultMsg.what == 100){
//                SPECIAL_LINK_HANDLING_OPTIONS = 1;
//            }else {
//                SPECIAL_LINK_HANDLING_OPTIONS = 0;
//            }

            // Open special link in-app
            if (SPECIAL_LINK_HANDLING_OPTIONS == 0) {

                Log.i(TAG, "if ");

//                if ((data == null) || (data != null && data.endsWith("#"))) {
                    Log.i(TAG, "else true ");

                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    if (audioManager != null) {


                        int streamType = AudioManager.STREAM_MUSIC;



                        if (audioManager.getStreamVolume(streamType) == 0) {
                            int volumeLevel = 5; // Desired volume level

                            // Set the volume for music stream
                            audioManager.setStreamVolume(streamType, volumeLevel, AudioManager.FLAG_SHOW_UI);
                        }
                    }
                    windowContainer.setVisibility(View.VISIBLE);
                    mWebviewPop = new WebView(view.getContext());
                    webViewSetting(mWebviewPop);

                    mWebviewPop.setWebChromeClient(new AdvanceWebChromeClient());
                    mWebviewPop.setWebViewClient(new AdvanceWebViewClient());
//                mWebviewPop.setWebViewClient(new WebViewClient() {
//                    @Override
//                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                        Log.d("353535", "New shouldOverrideUrlLoading: " + request.getUrl());
//                        return super.shouldOverrideUrlLoading(view, request);
//                    }
//
//                    @Override
//                    public void onPageFinished(WebView view, String url) {
//                        super.onPageFinished(view, url);
//                        // Now you can safely get the URL
//                        Log.d("353535", "New onPageFinished: " + url);
//                    }
//
//                    @Override
//                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                        Log.d("353535", "New onPageStarted: " + url);
//                        super.onPageStarted(view, url, favicon);
//                    }
//                });

                    mWebviewPop.getSettings().setUserAgentString(mWebviewPop.getSettings().getUserAgentString().replace("wv", ""));
                    mContainer.addView(mWebviewPop);

                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(mWebviewPop);
                    resultMsg.sendToTarget();
                    return true;
//                } else {
//
//                    WebSettings webSettings = webView.getSettings();
//                    webSettings.setJavaScriptEnabled(true);
//                    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//                    webSettings.setSupportMultipleWindows(true);
//
//                    if (URLUtil.isValidUrl(data)) {
//                        if (!data.contains("/order/list"))
//                            webView.loadUrl(data);
//                    }
//                }

                // Open special link in a new in-app tab
            } else if (SPECIAL_LINK_HANDLING_OPTIONS == 1) {

                if (data == null) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
                    CustomTabsIntent customTabsIntent = builder.build();
                    WebView newWebView = new WebView(view.getContext());
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(newWebView);
                    resultMsg.sendToTarget();
                    newWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            // Retrieve cookies from WebView & set cookies in the customTabsIntent WebView
                            CookieManager cookieManager = CookieManager.getInstance();
                            String allCookies = cookieManager.getCookie(Uri.parse(url).toString());
                            if (allCookies != null) {
                                String[] cookieList = allCookies.split(";");
                                for (String cookie : cookieList) {
                                    customTabsIntent.intent.putExtra("android.webkit.CookieManager.COOKIE", cookie.trim());
                                }
                            }
                            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
                            webView.stopLoading();
                            return false;
                        }
                    });
                } else {
                    openInInappTab(data);
                }

                // Open special link in Chrome
            } else if (SPECIAL_LINK_HANDLING_OPTIONS == 2) {

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
                Log.i("TAG", " data " + data);
                WebView newWebView = new WebView(view.getContext());
                newWebView.setWebChromeClient(new WebChromeClient());
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
            }

            Log.i("TAG", " running this main activity ");
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.i(TAG, " onJsalert");
            return super.onJsAlert(view, url, message, result);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUM = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/pdf", "image/*"};
            i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(Intent.createChooser(i, "Upload"), FCR);
        }

        @SuppressLint("InlinedApi")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {

                if (mUMA != null) {
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;

                if (Arrays.asList(fileChooserParams.getAcceptTypes()).contains("audio/*")) {
                    Intent chooserIntent = fileChooserParams.createIntent();
                    startActivityForResult(chooserIntent, CODE_AUDIO_CHOOSER);
                    return true;
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCM);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    File videoFile = null;
                    try {
                        videoFile = createVideoFile();
                        takeVideoIntent.putExtra("PhotoPath", mVM);
                    } catch (IOException ex) {
                        Log.e(TAG, "Video file creation failed", ex);
                    }
                    if (videoFile != null) {
                        mVM = "file:" + videoFile.getAbsolutePath();
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", videoFile));
                    } else {
                        takeVideoIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                contentSelectionIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/* video/*");

                String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/pdf", "image/*", "video/*", "*/*"};
                contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

                Intent[] intentArray;
                if (takePictureIntent != null && takeVideoIntent != null) {
                    intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
                } else if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else if (takeVideoIntent != null) {
                    intentArray = new Intent[]{takeVideoIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Upload");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);

                return true;

            } else {
                Log.i(TAG, "File Chooser permissions not granted - requesting permissions");
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                }
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                }
                return false;
            }
        }


        protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/pdf", "image/*"};
            i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }

    }

    private class MyWebChromeClient extends WebChromeClient {

        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyWebChromeClient() {
        }


        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
            webView.clearFocus();
        }

        public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;

            if (Config.LANDSCAPE_FULLSCREEN_VIDEO) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846);
        }

        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        boolean progressBarActive = false;

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.i(TAG, "progress " + newProgress);

            //Activate progress bar if this is a new redirect
            if (ACTIVATE_PROGRESS_BAR && !progressBarActive) {
                progressBar.setVisibility(View.VISIBLE);
                progressBarActive = true;
            }

            isRedirected = true;
            String name = preferences.getString("proshow", "");

            if (ACTIVATE_PROGRESS_BAR && name.equals("show")) {
                progressBar.setVisibility(View.VISIBLE);
            }

            if (newProgress >= 80 && ACTIVATE_PROGRESS_BAR && progressBarActive) {
                /* remove progress bar when page has been loaded 80%,
                 since the frame will likely have already changed to new page
                 otherwise, the spinner will still be visible
                 while non-critical resources load in background*/
                progressBar.setVisibility(View.GONE);
                progressBarActive = false;
            }

            if (newProgress == 100) {
                isRedirected = false;
                webView.setVisibility(View.VISIBLE);
            }

            if (!ACTIVATE_PROGRESS_BAR) {
                progressBar.setVisibility(View.GONE);
                progressBarActive = false;
            }
        }

        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            request.grant(request.getResources());
        }

    }

    private void webViewSetting(WebView intWebView) {

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(intWebView, true);
        }

        WebSettings webSettings = intWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Config.CLEAR_CACHE_ON_STARTUP) {
            //webSettings.setAppCacheEnabled(false);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            //webSettings.setAppCacheEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(intWebView, true);
        }
        intWebView.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webSettings.setSupportMultipleWindows(true);
        webSettings.setUseWideViewPort(true);

        if (!Config.USER_AGENT.isEmpty()) {
            webSettings.setUserAgentString(webSettings.getUserAgentString().replace("wv", ""));
        }


    }

    // nfc



    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];

                }
            }
            read(msgs);
        }
    }

    private void read(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

            webView.loadUrl("javascript: readNFCResult('" + text + "');");


        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        TextView textView = new TextView(this);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextColor(Color.BLUE);
        textView.setText("read : " + text);

    }

    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = {createRecord(text)};
        NdefMessage message = new NdefMessage(records);
        writeData(tag, message);

    }

    public void writeData(Tag tag, NdefMessage message) {
        if (tag != null) {
            try {
                Ndef ndefTag = Ndef.get(tag);
                if (ndefTag == null) {
                    // Let's try to format the Tag in NDEF
                    NdefFormatable nForm = NdefFormatable.get(tag);
                    if (nForm != null) {
                        nForm.connect();
                        nForm.format(message);
                        nForm.close();
                        toast(WRITE_SUCCESS);
                    }
                } else {
                    ndefTag.connect();
                    ndefTag.writeNdefMessage(message);
                    ndefTag.close();
                    toast(WRITE_SUCCESS);
                }
            } catch (Exception e) {
                e.printStackTrace();
                toast("write error : " + e.getMessage());
            }
        }
    }

//    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {

//    }

    private NdefRecord createRecord(String text)
            throws UnsupportedEncodingException {

        if (text.startsWith("VCARD")) {

            String nameVcard = "BEGIN:" +
                    text.replace('_', '\n').replace("%20", " ")
                    + '\n' + "END:VCARD";

            byte[] uriField = nameVcard.getBytes(StandardCharsets.US_ASCII);
            byte[] payload = new byte[uriField.length + 1];              //add 1 for the URI Prefix
            //payload[0] = 0x01;                                      //prefixes http://www. to the URI
            System.arraycopy(uriField, 0, payload, 1, uriField.length);  //appends URI to payload

            NdefRecord nfcRecord = new NdefRecord(
                    NdefRecord.TNF_MIME_MEDIA, "text/vcard".getBytes(), new byte[0], payload);

//        byte[] vCardDataBytes = nameVcard.getBytes(Charset.forName("UTF-8"));
//        byte[] vCardPayload = new byte[vCardDataBytes.length+1];
//        System.arraycopy(vCardDataBytes, 0, vCardPayload, 1, vCardDataBytes.length);
//// vCardDataBytes[0] = (byte)0x00;
//        NdefRecord nfcRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,"text/x-vcard".getBytes(),new byte[] {}, vCardPayload);

            return nfcRecord;
        }

        //Intent intent = getIntent();
        //EditText editTextWeb = (EditText)

        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);

        return recordNFC;
    }


    // Sanitize URL to prevent XSS
    private String sanitizeURL(String url) {
        // Apply a whitelist approach to allow only known safe characters and patterns
        // Remove any characters that are not alphanumeric, forward slash, or colon.
        String localURL = url.replaceAll("[^-.a-zA-Z\\d/:]", "");

        // Apply Content Security Policy (CSP) if applicable
        return applyCSP(localURL);
    }

    // Apply Content Security Policy (CSP) to the URL if necessary
    private String applyCSP(String url) {
        // This method can add CSP headers or modify the URL as per your app's requirements

        // Add a CSP header to restrict inline scripts and unsafe sources
        String cspHeaderValue = "default-src 'self'; script-src 'self' 'unsafe-inline'; object-src 'none'; style-src 'self' 'unsafe-inline'; img-src 'self'; media-src 'self'; frame-src 'none'; font-src 'self'; connect-src 'self';";

        // Append the CSP header to the URL as a query parameter
        return url + "?CSP_HEADER=" + Uri.encode(cspHeaderValue);
    }


    @Override
    protected void onNewIntent(Intent intent) {

        if (intent != null) {
            Bundle extras = intent.getExtras();
            String URL = null;
            if (extras != null) {
                URL = extras.getString("ONESIGNAL_URL");
            } else if (intent.getDataString() != null) {
                URL = intent.getDataString();
            }
            handleURl(URL);
        }
        if (!readModeNFC && !writeModeNFC) {
            return;
        }
        super.onNewIntent(intent);
        setIntent(intent);
        if (readModeNFC) {
            readFromIntent(intent);
        }
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            toast("tag detected : " + myTag.toString());


            try {
                if (writeModeNFC) {
                    write(textToWriteNFC, myTag);
                }
            } catch (IOException | FormatException e) {
                e.printStackTrace();
                Toast.makeText(this, WRITE_ERROR, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void ClosePopupWindow(View view) {

        progressBar.setVisibility(View.GONE);
        preferences = getPreferences(MODE_PRIVATE);
        preferences.edit().putString("proshow", "noshow").apply();
        mContainer.removeAllViews();
        windowContainer.setVisibility(View.GONE);
        mWebviewPop.destroy();

    }


    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }




    boolean shouldAlwaysOpenInInappTab (String URL) {
        for (int i = 0; i < Config.ALWAYS_OPEN_IN_INAPP_TAB.length; i++) {
            if ((Config.ALWAYS_OPEN_IN_INAPP_TAB[i] != "") && (URL.startsWith(Config.ALWAYS_OPEN_IN_INAPP_TAB[i]))) {
                return true;
            }
        }
        return false;
    }

    void openInInappTab(String URL) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
        CustomTabsIntent customTabsIntent = builder.build();
        // Retrieve cookies from WebView & set cookies in the customTabsIntent WebView
        CookieManager cookieManager = CookieManager.getInstance();
        String allCookies = cookieManager.getCookie(URL);
        if (allCookies != null) {
            String[] cookieList = allCookies.split(";");
            for (String cookie : cookieList) {
                customTabsIntent.intent.putExtra("android.webkit.CookieManager.COOKIE", cookie.trim());
            }
        }
        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(URL));
        webView.stopLoading();
    }

    void openInNewBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(i);
    }











}
