package ovh.nsip.cytaty;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.*;
import android.widget.*;
public class MainActivity extends Activity {
 private WebView web; private LinearLayout error;
 private static final String HOME="https://cytat.nsip.ovh/";
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  root.setOnApplyWindowInsetsListener((v,insets)->{if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}else{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());}return insets;});
  FrameLayout content=new FrameLayout(this);web=new WebView(this);content.addView(web,new FrameLayout.LayoutParams(-1,-1));
  error=new LinearLayout(this);error.setOrientation(LinearLayout.VERTICAL);error.setPadding(32,64,32,32);error.setBackgroundColor(Color.rgb(246,245,239));TextView message=new TextView(this);message.setText("Nie można wczytać cytatów. Sprawdź połączenie z internetem.");Button retry=new Button(this);retry.setText("Spróbuj ponownie");retry.setOnClickListener(v->{error.setVisibility(View.GONE);web.reload();});error.addView(message);error.addView(retry);error.setVisibility(View.GONE);content.addView(error,new FrameLayout.LayoutParams(-1,-1));root.addView(content,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setSafeBrowsingEnabled(true);settings.setSupportMultipleWindows(false);
  CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if("https".equals(u.getScheme())&&"cytat.nsip.ovh".equals(u.getHost()))return false;if("https".equals(u.getScheme())||"http".equals(u.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(android.content.ActivityNotFoundException ignored){}}return true;}
   @Override public void onPageStarted(WebView v,String url,android.graphics.Bitmap icon){error.setVisibility(View.GONE);}
   @Override public void onPageFinished(WebView v,String url){CookieManager.getInstance().flush();}
   @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame())error.setVisibility(View.VISIBLE);}
  });
  if(state==null)web.loadUrl(HOME);else web.restoreState(state);
 }
 @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);web.saveState(state);}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}

