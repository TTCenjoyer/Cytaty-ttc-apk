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
 private WebView web; private LinearLayout error,root; private TextView message;
 private final java.util.concurrent.ExecutorService origins=java.util.concurrent.Executors.newSingleThreadExecutor();
 private String activeOrigin=OriginPolicy.PRIMARY;private boolean selecting=false,closed=false;private int selectionId=0;
 private AppUpdater updater;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  root.setOnApplyWindowInsetsListener((v,insets)->{if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}else{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());}return insets;});
  FrameLayout content=new FrameLayout(this);web=new WebView(this);content.addView(web,new FrameLayout.LayoutParams(-1,-1));
  error=new LinearLayout(this);error.setOrientation(LinearLayout.VERTICAL);error.setPadding(32,64,32,32);message=new TextView(this);message.setText("Nie można wczytać cytatów. Sprawdź połączenie z internetem.");Button retry=new Button(this);retry.setText("Spróbuj ponownie");retry.setOnClickListener(v->{error.setVisibility(View.GONE);openAvailable(false);});error.addView(message);error.addView(retry);error.setVisibility(View.GONE);content.addView(error,new FrameLayout.LayoutParams(-1,-1));root.addView(content,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  boolean systemDark=(getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)==android.content.res.Configuration.UI_MODE_NIGHT_YES;
  applyTheme(getPreferences(MODE_PRIVATE).getBoolean("dark",systemDark));
  web.setOnTouchListener((v,event)->{if(event.getActionMasked()==android.view.MotionEvent.ACTION_UP)web.postDelayed(this::syncTheme,100);return false;});
  web.setOnKeyListener((v,key,event)->{if(event.getAction()==android.view.KeyEvent.ACTION_UP)web.postDelayed(this::syncTheme,100);return false;});
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setSafeBrowsingEnabled(true);settings.setSupportMultipleWindows(false);
  CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if(OriginPolicy.trusted(u.getScheme(),u.getHost(),u.getPort()))return false;if("https".equals(u.getScheme())||"http".equals(u.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(android.content.ActivityNotFoundException ignored){}}return true;}
   @Override public void onPageStarted(WebView v,String url,android.graphics.Bitmap icon){error.setVisibility(View.GONE);}
   @Override public void onPageFinished(WebView v,String url){CookieManager.getInstance().flush();syncTheme();}
   @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame())connectionFailed(r.getUrl());}
   @Override public void onReceivedHttpError(WebView v,WebResourceRequest r,WebResourceResponse response){if(r.isForMainFrame()&&response.getStatusCode()>=500)connectionFailed(r.getUrl());}
   @Override public void onReceivedSslError(WebView v,SslErrorHandler handler,android.net.http.SslError sslError){handler.cancel();connectionFailed(Uri.parse(sslError.getUrl()));}
  });
  openAvailable(false);
  updater=new AppUpdater(this);updater.check(false);
 }
 private boolean originHealthy(String origin){
  java.net.HttpURLConnection connection=null;
  try{connection=(java.net.HttpURLConnection)new java.net.URL(origin+"/api/health").openConnection();connection.setConnectTimeout(2500);connection.setReadTimeout(2500);connection.setInstanceFollowRedirects(false);connection.setRequestProperty("Accept","application/json");int code=connection.getResponseCode();
   if(code==403&&"challenge".equals(connection.getHeaderField("cf-mitigated")))return true;
   if(code!=200)return false;try(java.io.InputStream input=connection.getInputStream()){byte[] bytes=new byte[1024];int n=input.read(bytes);if(n<0)return false;String body=new String(bytes,0,n,java.nio.charset.StandardCharsets.UTF_8);return body.contains("cytaty-ttc")&&body.contains("ok");}
  }catch(java.io.IOException ignored){return false;}finally{if(connection!=null)connection.disconnect();}
 }
 private void openAvailable(boolean backupOnly){
  if(closed||selecting)return;selecting=true;final int request=++selectionId;error.setVisibility(View.VISIBLE);message.setText("Łączenie z cytatami…");
  origins.execute(()->{String chosen=OriginPolicy.choose(this::originHealthy,backupOnly);runOnUiThread(()->{if(closed||request!=selectionId)return;selecting=false;if(chosen==null){message.setText("Nie można połączyć się z żadną domeną. Spróbuj ponownie.");error.setVisibility(View.VISIBLE);return;}activeOrigin=chosen;error.setVisibility(View.GONE);web.loadUrl(chosen+"/");});});
 }
 private void connectionFailed(Uri failed){
  if(closed||selecting||!OriginPolicy.trusted(failed.getScheme(),failed.getHost(),failed.getPort()))return;
  if("cytat.nsip.ovh".equals(failed.getHost())&&OriginPolicy.PRIMARY.equals(activeOrigin)){web.stopLoading();openAvailable(true);}else{message.setText("Nie można wczytać cytatów. Sprawdź połączenie z internetem.");error.setVisibility(View.VISIBLE);}
 }
 private void syncTheme(){
  if(web==null)return;
  Uri uri=Uri.parse(web.getUrl()==null?"":web.getUrl());
  if(!OriginPolicy.trusted(uri.getScheme(),uri.getHost(),uri.getPort()))return;
  web.evaluateJavascript("document.documentElement.dataset.theme",value->{if("\"dark\"".equals(value))applyTheme(true);else if("\"light\"".equals(value))applyTheme(false);});
 }
 @SuppressWarnings("deprecation")
 private void applyTheme(boolean dark){
  int background=Color.parseColor(dark?"#1c1c1e":"#f6f5ef");
  root.setBackgroundColor(background);web.setBackgroundColor(background);error.setBackgroundColor(background);message.setTextColor(Color.parseColor(dark?"#d9d8d4":"#22352d"));
  getWindow().setStatusBarColor(background);getWindow().setNavigationBarColor(background);if(android.os.Build.VERSION.SDK_INT>=28)getWindow().setNavigationBarDividerColor(background);getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(background));
  int flags=getWindow().getDecorView().getSystemUiVisibility();int light=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
  getWindow().getDecorView().setSystemUiVisibility(dark?flags & ~light:flags | light);
  if(android.os.Build.VERSION.SDK_INT>=29){getWindow().setStatusBarContrastEnforced(false);getWindow().setNavigationBarContrastEnforced(false);}
  getPreferences(MODE_PRIVATE).edit().putBoolean("dark",dark).apply();
 }
 @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus)syncTheme();}
 @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);web.saveState(state);}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onResume(){super.onResume();if(updater!=null){updater.resume();updater.check(false);}}
 @Override protected void onDestroy(){closed=true;selectionId++;origins.shutdownNow();if(updater!=null)updater.close();web.destroy();super.onDestroy();}
}

