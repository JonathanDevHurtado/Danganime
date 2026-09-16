package com.danganime.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

public class MainActivity extends BridgeActivity {

    private static volatile Set<String> BLOCKED_DOMAINS = java.util.Collections.emptySet();
    private static volatile String COSMETIC_CSS = "";
    private static volatile String AD_BLOCK_JS = "";
    private static volatile boolean filtersLoaded = false;

    private static final String[] SAFE_BASES = {
        "cloudflare.com", "googleapis.com", "unpkg.com", "jsdelivr.net", "gstatic.com",
        "amazonaws.com", "amazon.com", "cloudfront.net", "akamai.net", "akamaized.net",
        "fastly.net", "googleusercontent.com", "youtube.com", "googlevideo.com",
        "github.io", "githubusercontent.com", "microsoft.com", "apple.com", "google.com",
        "facebook.com", "twitter.com", "x.com", "instagram.com", "whatsapp.com",
        "youtube-nocookie.com", "bootstrapcdn.com", "wp.com", "wixstatic.com",
        "squarespace.com", "vercel.app", "netlify.app", "web.app",
        "jwpcdn.com", "jwplayer.com", "jwplatform.com",
        "streamtape.com", "streamtape.net", "streamtape.to", "streamtape.xyz",
        "streamtape.cc", "strtape.cloud", "strcloud.in",
        "ok.ru", "vk.com", "vk.ru", "rutube.ru", "dailymotion.com", "vimeo.com",
        "yourupload.com", "netu.ac",
        "earnvids.com", "savefiles.com", "vidara.to", "saidochesto.top",
        "streamwish.to", "streamwish.com", "streamwish.xyz", "streamwish.online",
        "hexupload.net", "hexupload.com",
        "mixdrop.co", "mixdrop.to", "mixdrop.ag", "mixdrop.sx", "mixdrop.bz", "mixdrop.ch",
        "filemoon.sx", "filemoon.to", "filemoon.in",
        "mp4upload.com", "vidsonic.net", "vidsonic.com",
        "zoplayer.com", "zoplayer.to", "zplayer.live",
        "imgur.com", "ibb.co", "gyazo.com",
        "streamnova.to", "streamnova.net", "streamnova-zone.com"
    };

    private FrameLayout browserOverlay;
    private LinearLayout browserContent;
    private LinearLayout browserToolbar;
    private TextView browserTitle;
    private WebView browserView;
    private BrowserChromeClient browserChrome;
    private boolean browserVisible = false;

    private static final String SCHEME_OPEN = "danganime";

    @SuppressLint("SetJavaScriptEnabled")
    private static void loadFilters(android.content.Context context) {
        if (filtersLoaded) return;
        long t0 = System.currentTimeMillis();
        android.util.Log.d("Danganime", "Cargando filtros en hilo '" + Thread.currentThread().getName() + "'");

        try {
            Set<String> blocked = new HashSet<>(100000);
            InputStream is = context.getAssets().open("filters/blocked_domains.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII), 65536);
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    blocked.add(line);
                }
            }
            reader.close();
            is.close();

            is = context.getAssets().open("filters/cosmetic_selectors.txt");
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), 8192);
            StringBuilder css = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                css.append(line);
            }
            reader.close();
            is.close();

            Set<String> jsDoms = new HashSet<>(JS_TOP);
            jsDoms.retainAll(blocked);

            BLOCKED_DOMAINS = blocked;
            COSMETIC_CSS = css.toString().trim();
            AD_BLOCK_JS = buildAdBlockJs(jsDoms);
            filtersLoaded = true;
            android.util.Log.d("Danganime", "Bloqueador listo: " + blocked.size() + " dominios red, " + jsDoms.size() + " en JS, CSS " + COSMETIC_CSS.length() + " bytes (" + (System.currentTimeMillis() - t0) + " ms, hilo '" + Thread.currentThread().getName() + "')");
        } catch (IOException e) {
            android.util.Log.e("Danganime", "No se pudieron cargar filtros: " + e.getMessage());
            loadFallbackFilters();
            filtersLoaded = true;
        }
    }

    private static final Set<String> JS_TOP = new HashSet<>(java.util.Arrays.asList(
        "adangle.online", "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "google-analytics.com", "adservice.google.com", "adnxs.com", "adsrvr.org",
        "moatads.com", "quantserve.com", "scorecardresearch.com", "taboola.com",
        "outbrain.com", "criteo.com", "criteo.net", "amazon-adsystem.com",
        "media.net", "propellerads.com", "propellerads.tech", "propellerclick.com",
        "popads.net", "popads.media", "mypopads.com", "popunder.bid", "popunderjs.com",
        "popunderz.com", "exoclick.com", "adsterra.com", "adsterraserver.com",
        "monetag.com", "galaksion.com", "staticgalaksion.com", "adskeeper.com",
        "adk2x.com", "juicyads.com", "juicyads.me", "hilltopads.com", "hilltopads.net",
        "trafficjunky.com", "trafficjunky.net", "getpopunder.com", "softpopads.com",
        "pubmatic.com", "openx.com", "rubiconproject.com", "casalemedia.com",
        "smartadserver.com", "teads.tv", "sharethrough.com", "revcontent.com",
        "zergnet.com", "hotjar.com", "clarity.ms", "mc.yandex.ru", "googletagmanager.com"
    ));

    private static String buildAdBlockJs(Set<String> doms) {
        JSONArray arr = new JSONArray();
        for (String d : doms) arr.put(d);
        String json = arr.toString();
        String tokens = "['adangle','adstera','propeller','exoclick','popunder','popads','popcash'," +
            "'juicyads','hilltopads','clickunder','adskeeper','monetag','galaksion','galax','revcontent','admaven','adrecover']";
        return "(function(){var W=window;if(W.__dgAd)return;W.__dgAd=1;" +
            "var D=" + json + ",TK=" + tokens + ";" +
            "function host(u){try{return new URL(u).hostname;}catch(e){" +
            "var m=(u||'').match(/^[a-z][a-z0-9+.-]*:\\/\\/([^\\/?#]+)/);" +
            "return m?m[1].split(':')[0].toLowerCase():'';}}" +
            "function inD(h){for(var i=0;i<D.length;i++){var d=D[i];" +
            "if(h===d||h.slice(-d.length-1)==='.'+d)return true;}return false;}" +
            "function isAdUrl(u){var h=host(u);return h?inD(h):false;}" +
            "function hasTok(s){s=(s||'').toLowerCase();for(var i=0;i<TK.length;i++){if(s.indexOf(TK[i])!==-1)return true;}return false;}" +
            "var f=W.fetch;if(f){W.fetch=function(u,o){var s=(typeof u==='string')?u:((u&&u.url)||'');" +
            "if(isAdUrl(s))return Promise.reject(new Error('blocked'));return f.apply(this,arguments);};}" +
            "var xo=XMLHttpRequest.prototype.open;XMLHttpRequest.prototype.open=function(m,u){" +
            "if(isAdUrl(String(u))){return;}return xo.apply(this,arguments);};" +
            "var _open=W.open;W.open=function(u){try{if(u&&isAdUrl(String(u))){return null;}}catch(e){}" +
            "return _open? _open.apply(W,arguments):null;};" +
            "function kill(n){if(!n||n.nodeType!==1)return;" +
            "if(n.tagName==='BODY'||n.tagName==='HTML')return;" +
            "if(hasTok(n.id+'.'+n.className)){try{n.remove();}catch(e){}return;}" +
            "if(n.tagName==='SCRIPT'||n.tagName==='IFRAME'||n.tagName==='IMG'){" +
            "if(isAdUrl(n.src||'')){try{n.remove();}catch(e){}}}}" +
            "var mo=new MutationObserver(function(ms){for(var i=0;i<ms.length;i++){" +
            "var as=ms[i].addedNodes;for(var j=0;j<as.length;j++){kill(as[j]);}}});" +
            "if(document.documentElement){" +
            "mo.observe(document.documentElement,{childList:true,subtree:true});}" +
            "document.addEventListener('click',function(e){var a=e.target;" +
            "while(a&&a!==document){if(a.tagName==='A'&&a.href){if(isAdUrl(a.href)){e.preventDefault();e.stopPropagation();return false;}}" +
            "a=a.parentElement;}},true);" +
            "var H=location.hostname;" +
            "if(H.indexOf('pluto.tv')!==-1){" +
            "var pd=function(){" +
            "var isHome=(location.pathname.indexOf('/home')!==-1)||(location.pathname==='/');" +
            "var docks=document.querySelectorAll('[class*=\"PersistentAlwaysOnPlayer\"]');" +
            "for(var i=0;i<docks.length;i++){if(isHome){docks[i].style.setProperty('display','none','important');}else{docks[i].style.removeProperty('display');}}" +
            "var vs=document.querySelectorAll('video');" +
            "for(var j=0;j<vs.length;j++){var v=vs[j];" +
            "var inDock=false;try{inDock=!!(v.closest&&v.closest('[class*=\"PersistentAlwaysOnPlayer\"]'));}catch(x){}" +
            "if(isHome&&inDock){try{v.muted=true;if(!v.__dgNoPlay){v.__dgNoPlay=1;v.play=function(){return Promise.resolve();};}v.pause();}catch(x){}}" +
            "else if(v.__dgNoPlay){try{delete v.play;v.__dgNoPlay=0;}catch(x){}}}};" +
            "pd();setInterval(pd,800);" +
            "document.addEventListener('play',function(e){if(e.target&&e.target.tagName==='VIDEO'&&location.pathname.indexOf('/home')!==-1&&e.target.closest&&e.target.closest('[class*=\"PersistentAlwaysOnPlayer\"]')){try{e.target.pause();e.target.muted=true;}catch(x){}}},true);" +
            "window.__dgT={lastY:0,on:false};" +
            "document.addEventListener('touchstart',function(e){if(location.pathname.indexOf('/home')===-1)return;if(e.touches&&e.touches.length){window.__dgT.lastY=e.touches[0].clientY;window.__dgT.on=true;}},{passive:true});" +
            "document.addEventListener('touchmove',function(e){if(!window.__dgT.on)return;if(location.pathname.indexOf('/home')===-1)return;if(!e.touches||!e.touches.length)return;var y=e.touches[0].clientY;var dy=window.__dgT.lastY-y;window.__dgT.lastY=y;window.scrollBy(0,dy);},{passive:true});" +
            "document.addEventListener('touchend',function(){window.__dgT.on=false;},{passive:true});" +
            "var dgT=null;" +
            "function dgBar(){return document.querySelector('avia-control-bar');}" +
            "function dgHide(){var b=dgBar();if(b){b.style.setProperty('display','none','important');}}" +
            "function dgShow(){var b=dgBar();if(b){b.style.setProperty('display','flex','important');b.style.setProperty('flex-direction','column','important');b.style.setProperty('opacity','1','important');b.style.setProperty('visibility','visible','important');}if(dgT)clearTimeout(dgT);dgT=setTimeout(dgHide,8000);}" +
            "document.addEventListener('touchstart',dgShow,true);" +
            "document.addEventListener('click',dgShow,true);" +
            "var dgInit=setInterval(function(){if(dgBar()){dgShow();clearInterval(dgInit);}},500);}" +
            "if(H.indexOf('runtime.tv')!==-1){" +
            "var fv=function(){var vs=document.querySelectorAll('video');for(var i=0;i<vs.length;i++){try{vs[i].muted=false;vs[i].volume=1;}catch(e){}}};" +
            "fv();setInterval(fv,1000);" +
            "document.addEventListener('play',function(e){if(e.target&&e.target.tagName==='VIDEO'){e.target.muted=false;e.target.volume=1;}},true);" +
            "document.addEventListener('volumechange',function(e){if(e.target&&e.target.tagName==='VIDEO'&&(e.target.muted||e.target.volume<1)){e.target.muted=false;e.target.volume=1;}},true);}" +
            "})();";
    }

    private static void loadFallbackFilters() {
        String[] fallback = {
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "google-analytics.com", "adservice.google.com", "adnxs.com",
            "adsrvr.org", "moatads.com", "taboola.com", "outbrain.com",
            "criteo.com", "amazon-adsystem.com", "propellerads.com",
            "popads.net", "exoclick.com", "adsterra.com", "monetag.com",
            "revcontent.com", "smartadserver.com", "pubmatic.com",
            "openx.com", "rubiconproject.com", "casalemedia.com",
            "quantserve.com", "scorecardresearch.com", "hotjar.com",
            "clarity.ms", "segment.com", "googletagmanager.com"
        };
        Set<String> blocked = new HashSet<>(java.util.Arrays.asList(fallback));
        BLOCKED_DOMAINS = blocked;
        Set<String> jsDoms = new HashSet<>(JS_TOP);
        jsDoms.retainAll(blocked);
        AD_BLOCK_JS = buildAdBlockJs(jsDoms);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> loadFilters(getApplicationContext()), "filters-loader").start();

        CookieManager.getInstance().setAcceptCookie(true);

        WebView appView = getBridge().getWebView();
        appView.setWebViewClient(new AppSchemeClient(getBridge()));

        WebSettings s = appView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        hideSystemUI();
    }

    private void ensureBrowser() {
        if (browserOverlay != null) return;

        browserOverlay = new FrameLayout(this);
        browserOverlay.setBackgroundColor(Color.BLACK);

        browserContent = new LinearLayout(this);
        browserContent.setOrientation(LinearLayout.VERTICAL);
        browserContent.setBackgroundColor(Color.BLACK);

        browserToolbar = new LinearLayout(this);
        browserToolbar.setOrientation(LinearLayout.HORIZONTAL);
        browserToolbar.setGravity(Gravity.CENTER_VERTICAL);
        browserToolbar.setBackgroundColor(Color.rgb(16, 16, 24));
        browserToolbar.setPadding(dp(4), dp(4), dp(4), dp(4));

        browserTitle = new TextView(this);
        browserTitle.setTextColor(Color.WHITE);
        browserTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        browserTitle.setMaxLines(1);
        browserTitle.setPadding(dp(10), 0, dp(10), 0);
        browserTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        browserToolbar.addView(browserTitle);

        browserToolbar.addView(toolbarButton("🏠", v -> {
            closeBrowser();
            WebView av = getBridge().getWebView();
            if (av != null) {
                av.evaluateJavascript("if(typeof appHome==='function')appHome();", null);
            }
        }, "Inicio"));
        browserToolbar.addView(toolbarButton("↻", v -> {
            if (browserView != null) browserView.reload();
        }, "Recargar"));
        browserToolbar.addView(toolbarButton("⛶", v -> setChromeVisible(false), "Pantalla completa"));
        browserToolbar.addView(toolbarButton("↗", v -> {
            if (browserView != null) {
                String u = browserView.getUrl();
                if (u != null && u.startsWith("http")) openExternal(u);
            }
        }, "Abrir en navegador"));
        browserToolbar.addView(toolbarButton("×", v -> closeBrowser(), "Cerrar"));

        browserView = new WebView(this);
        browserView.setBackgroundColor(Color.BLACK);
        browserView.setWebViewClient(new AdBlockWebViewClient());
        browserChrome = new BrowserChromeClient();
        browserView.setWebChromeClient(browserChrome);
        configureBrowserSettings(browserView.getSettings());
        CookieManager.getInstance().setAcceptThirdPartyCookies(browserView, true);

        browserContent.addView(browserToolbar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        browserContent.addView(browserView, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        browserOverlay.addView(browserContent, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ViewGroup content = getWindow().getDecorView().findViewById(android.R.id.content);
        content.addView(browserOverlay, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        browserOverlay.setVisibility(View.GONE);
    }

    private void setChromeVisible(boolean showToolbar) {
        if (browserToolbar != null) {
            browserToolbar.setVisibility(showToolbar ? View.VISIBLE : View.GONE);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureBrowserSettings(WebSettings st) {
        st.setJavaScriptEnabled(true);
        st.setDomStorageEnabled(true);
        st.setDatabaseEnabled(true);
        st.setMediaPlaybackRequiresUserGesture(false);
        st.setLoadWithOverviewMode(true);
        st.setUseWideViewPort(true);
        st.setSupportZoom(false);
        st.setSupportMultipleWindows(false);
        st.setJavaScriptCanOpenWindowsAutomatically(false);
        st.setCacheMode(WebSettings.LOAD_DEFAULT);
        st.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        st.setAllowContentAccess(true);
        st.setAllowFileAccess(false);
        st.setAllowFileAccessFromFileURLs(false);
        st.setAllowUniversalAccessFromFileURLs(false);
        st.setGeolocationEnabled(false);
    }

    private Button toolbarButton(String text, View.OnClickListener l, String desc) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setMinHeight(0);
        b.setMinimumWidth(0);
        b.setMinimumHeight(0);
        b.setPadding(dp(10), dp(8), dp(10), dp(8));
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setContentDescription(desc);
        b.setOnClickListener(l);
        return b;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    private void openBrowser(String url, String title) {
        ensureBrowser();
        setChromeVisible(true);
        if (browserOverlay.getVisibility() != View.VISIBLE) {
            browserOverlay.setVisibility(View.VISIBLE);
            browserOverlay.bringToFront();
        }
        if (title != null && !title.isEmpty()) {
            browserTitle.setText(title);
        }
        if (browserView.getUrl() == null || !browserView.getUrl().equals(url)) {
            browserView.loadUrl(url);
        }
        browserVisible = true;
    }

    private void closeBrowser() {
        if (browserChrome != null) {
            browserChrome.exitFullscreen();
        }
        setChromeVisible(true);
        if (browserOverlay != null) {
            browserOverlay.setVisibility(View.GONE);
        }
        if (browserView != null) {
            browserView.stopLoading();
        }
        browserVisible = false;
    }

    private void openExternal(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(Intent.createChooser(i, "Abrir con"));
        } catch (Exception e) {
            android.util.Log.e("Danganime", "No se pudo abrir externo: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (browserVisible) {
            if (browserView != null && browserView.canGoBack()) {
                browserView.goBack();
            } else {
                closeBrowser();
            }
            return;
        }
        WebView webView = getBridge().getWebView();
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            webView.evaluateJavascript(
                "(function(){ if(typeof handleBackPress === 'function') return handleBackPress(); return 'no_handler'; })()",
                value -> {
                    boolean exit = value == null
                        || value.equals("null")
                        || value.equals("\"no_handler\"")
                        || value.equals("\"exit\"");
                    if (exit) {
                        MainActivity.super.onBackPressed();
                    }
                }
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController c = window.getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private boolean isAdUrl(String url) {
        if (url == null) return false;
        String host = extractHost(url);
        if (host == null || host.isEmpty()) return false;
        while (true) {
            if (BLOCKED_DOMAINS.contains(host)) return true;
            int dot = host.indexOf('.');
            if (dot < 0 || dot == host.length() - 1) return false;
            host = host.substring(dot + 1);
        }
    }

    private String extractHost(String url) {
        try {
            String lower = url.toLowerCase();
            int scheme = lower.indexOf("://");
            int start = scheme >= 0 ? scheme + 3 : 0;
            int end = start;
            int len = lower.length();
            while (end < len) {
                char c = lower.charAt(end);
                if (c == '/' || c == '?' || c == '#') break;
                end++;
            }
            String hostPort = lower.substring(start, end);
            int port = hostPort.indexOf(':');
            String host = port >= 0 ? hostPort.substring(0, port) : hostPort;
            return host.isEmpty() ? null : host;
        } catch (Exception e) {
            return null;
        }
    }

    private WebResourceResponse blockRequest() {
        return new WebResourceResponse("text/plain", "utf-8",
            new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
    }

    private void injectCosmeticCss(WebView view) {
        if (COSMETIC_CSS.isEmpty()) return;
        String b64 = Base64.encodeToString(COSMETIC_CSS.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        String js = "(function(){if(!document.getElementById('danganime-adblock')){" +
            "var s=document.createElement('style');s.id='danganime-adblock';s.type='text/css';" +
            "s.appendChild(document.createTextNode(atob('" + b64 + "')));" +
            "var h=document.head||document.documentElement;" +
            "if(h)h.appendChild(s);}})();";
        view.evaluateJavascript(js, null);
    }

    private void injectAdBlockJs(WebView view) {
        if (AD_BLOCK_JS.isEmpty()) return;
        view.evaluateJavascript(AD_BLOCK_JS, null);
    }

    private class AppSchemeClient extends BridgeWebViewClient {
        AppSchemeClient(com.getcapacitor.Bridge bridge) {
            super(bridge);
        }

        private boolean handleScheme(Uri u) {
            if (u != null && SCHEME_OPEN.equalsIgnoreCase(u.getScheme())) {
                String url = u.getQueryParameter("url");
                String title = u.getQueryParameter("title");
                if (url != null) {
                    runOnUiThread(() -> openBrowser(url, title));
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (handleScheme(request.getUrl())) return true;
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (handleScheme(Uri.parse(url))) return true;
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private class AdBlockWebViewClient extends WebViewClient {
        private int blockedThisPage = 0;

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (isAdUrl(url)) {
                blockedThisPage++;
                if (blockedThisPage % 50 == 1) {
                    android.util.Log.d("Danganime", "[red] bloqueado(" + blockedThisPage + "): " + extractHost(url));
                }
                return blockRequest();
            }
            return null;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            blockedThisPage = 0;
            injectCosmeticCss(view);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            injectCosmeticCss(view);
            injectAdBlockJs(view);
            if (blockedThisPage > 0) {
                android.util.Log.d("Danganime", "Página '" + (view.getTitle() != null ? view.getTitle() : url)
                    + "' -> " + blockedThisPage + " peticiones de ads bloqueadas");
            }
            String t = view.getTitle();
            if (t != null && !t.isEmpty()) {
                browserTitle.setText(t);
            }
        }

        private String rewriteHost(String url) {
            try {
                Uri u = Uri.parse(url);
                String host = u.getHost();
                if (host != null && host.matches("www\\d+\\.animeonline\\.ninja")) {
                    String nu = u.buildUpon().authority("ww3.animeonline.ninja").build().toString();
                    android.util.Log.d("Danganime", "[nav] reescrito a ww3: " + nu);
                    return nu;
                }
            } catch (Exception ignored) {}
            return null;
        }

        private boolean isSafeHost(String host) {
            if (host == null) return false;
            for (String b : SAFE_BASES) {
                if (host.equals(b) || host.endsWith("." + b)) return true;
            }
            return false;
        }

        private static final String[] PLAYER_HINTS = {
            "stream", "vid", "player", "embed", "play", "watch", "cdn", "video",
            "file", "moon", "drop", "share", "tape", "nova", "lulu", "dood",
            "mix", "mp4", "voe", "upn", "byse", "hex", "save", "zilla", "jkplayer",
            "saidochesto", "mycloud", "hgcloud", "fembed", "netu", "ok.ru", "vk.",
            "dailymotion", "vimeo", "rutube", "yourupload", "streamtape"
        };

        private boolean isLikelyPlayerHost(String host) {
            if (host == null) return false;
            String h = host.toLowerCase();
            for (String k : PLAYER_HINTS) {
                if (h.contains(k)) return true;
            }
            return false;
        }

        private String baseDomain(String host) {
            if (host == null) return null;
            String[] p = host.split("\\.");
            if (p.length <= 2) return host;
            String last2 = p[p.length - 2] + "." + p[p.length - 1];
            String[] twoLevel = {"co.jp", "co.uk", "com.br", "com.ar", "com.mx", "co.kr",
                "com.au", "com.co", "or.jp", "ne.jp", "ac.jp", "go.jp", "com.pe", "com.ve"};
            for (String s : twoLevel) {
                if (last2.equals(s) && p.length >= 3) {
                    return p[p.length - 3] + "." + last2;
                }
            }
            return last2;
        }

        private boolean cancelPopunder(WebView view, String target) {
            String host = extractHost(target);
            String cur = extractHost(view.getUrl());
            if (host == null || cur == null) return false;
            if (isSafeHost(host) || isLikelyPlayerHost(host)) return false;
            String bh = baseDomain(host);
            String bc = baseDomain(cur);
            if (bh != null && bc != null && !bh.equals(bc)) {
                android.util.Log.d("Danganime", "[nav] popunder cancelado: " + host);
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String u = request.getUrl().toString();
            if (isAdUrl(u)) {
                android.util.Log.d("Danganime", "[nav] cancelada (ad): " + extractHost(u));
                return true;
            }
            String rw = rewriteHost(u);
            if (rw != null) {
                view.loadUrl(rw);
                return true;
            }
            if (cancelPopunder(view, u)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (isAdUrl(url)) {
                android.util.Log.d("Danganime", "[nav] cancelada (ad): " + extractHost(url));
                return true;
            }
            String rw = rewriteHost(url);
            if (rw != null) {
                view.loadUrl(rw);
                return true;
            }
            if (cancelPopunder(view, url)) {
                return true;
            }
            return false;
        }
    }

    private class BrowserChromeClient extends WebChromeClient {
        private View customView;
        private CustomViewCallback customViewCallback;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (customView != null) {
                callback.onCustomViewHidden();
                return;
            }
            customView = view;
            customViewCallback = callback;
            if (browserToolbar != null) browserToolbar.setVisibility(View.GONE);
            browserOverlay.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            browserOverlay.setBackgroundColor(Color.BLACK);
            hideSystemUI();
        }

        void exitFullscreen() {
            if (customView == null) return;
            browserOverlay.removeView(customView);
            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
            }
            customView = null;
            customViewCallback = null;
            setChromeVisible(true);
        }

        @Override
        public void onHideCustomView() {
            if (customView == null) return;
            browserOverlay.removeView(customView);
            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
            }
            customView = null;
            customViewCallback = null;
            setChromeVisible(true);
        }
    }
}
