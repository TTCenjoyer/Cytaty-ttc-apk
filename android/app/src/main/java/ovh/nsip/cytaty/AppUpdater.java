package ovh.nsip.cytaty;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Only downloads an explicit release APK; it never executes web page instructions. */
final class AppUpdater {
 private static final String BASE="https://github.com/TTCenjoyer/Cytaty-ttc-apk/releases/download/mobile-latest/";
 private static final long MAX_APK=50L*1024*1024;
 private final Activity activity;
 private final ExecutorService executor=Executors.newSingleThreadExecutor();
 private final Handler main=new Handler(Looper.getMainLooper());
 private boolean busy,closed;
 private File pending;
 AppUpdater(Activity activity){this.activity=activity;}
 void check(boolean manual){
  if(busy||closed)return;
  long last=activity.getPreferences(0).getLong("updateCheck",0);
  if(!manual&&System.currentTimeMillis()-last<6*60*60*1000L)return;
  busy=true;
  executor.execute(()->{try{
   JSONObject release=new JSONObject(readText(BASE+"update.json"));
   long version=release.getLong("versionCode");String name=release.getString("versionName"),sha=release.getString("sha256");
   if(!sha.matches("[a-fA-F0-9]{64}"))throw new IOException("Invalid manifest");
   long current=version(activity.getPackageManager().getPackageInfo(activity.getPackageName(),0));
   main.post(()->{if(closed)return;busy=false;activity.getPreferences(0).edit().putLong("updateCheck",System.currentTimeMillis()).apply();
    if(version<=current){if(manual)toast("Masz najnowszą wersję aplikacji.");return;}
    new AlertDialog.Builder(activity).setTitle("Nowa wersja Cytatów TTC")
     .setMessage("Dostępna jest wersja "+name+". Pobrać aktualizację? Android poprosi Cię o potwierdzenie instalacji.")
     .setNegativeButton("Później",null).setPositiveButton("Pobierz",(d,w)->download(version,sha)).show();
   });
  }catch(Exception error){main.post(()->{busy=false;if(!closed&&manual)toast("Nie udało się sprawdzić aktualizacji. Spróbuj ponownie później.");});}});
 }
 private void download(long expectedVersion,String sha){
  if(busy||closed)return;busy=true;toast("Pobieranie aktualizacji…");
  executor.execute(()->{File part=new File(activity.getCacheDir(),"updates/update.part"),apk=new File(part.getParentFile(),"Cytaty-TTC.apk");
   try{
    if(!part.getParentFile().isDirectory()&&!part.getParentFile().mkdirs())throw new IOException("Cache unavailable");
    HttpURLConnection connection=open(BASE+"Cytaty-TTC.apk");
    try(InputStream in=connection.getInputStream();OutputStream out=new FileOutputStream(part)){
     MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] buffer=new byte[32768];long total=0;int n;
     while((n=in.read(buffer))!=-1){total+=n;if(total>MAX_APK)throw new IOException("APK too large");digest.update(buffer,0,n);out.write(buffer,0,n);}
     if(!hex(digest.digest()).equalsIgnoreCase(sha))throw new IOException("Checksum mismatch");
    }finally{connection.disconnect();}
    verifyPackage(part,expectedVersion);
    if(apk.exists()&&!apk.delete())throw new IOException("Old download unavailable");
    if(!part.renameTo(apk))throw new IOException("Cannot save APK");
    main.post(()->{busy=false;if(!closed){pending=apk;install();}});
   }catch(Exception error){part.delete();main.post(()->{busy=false;if(!closed)toast("Aktualizacja nie została zainstalowana. Nie udało się pobrać lub zweryfikować pliku.");});}
  });
 }
 private void install(){
  if(pending==null||!pending.isFile()||closed)return;
  if(!activity.getPackageManager().canRequestPackageInstalls()){
   new AlertDialog.Builder(activity).setTitle("Zezwól na aktualizacje")
    .setMessage("W ustawieniach Androida zezwól tej aplikacji na instalowanie aktualizacji. Po powrocie potwierdzisz instalację.")
    .setNegativeButton("Anuluj",(d,w)->pending=null).setPositiveButton("Otwórz ustawienia",(d,w)->{
     try{activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+activity.getPackageName())));}
     catch(Exception error){pending=null;toast("Otwórz ustawienia instalowania nieznanych aplikacji w Androidzie.");}
    }).show();return;
  }
  try{
   Uri uri=FileProvider.getUriForFile(activity,activity.getPackageName()+".updates",pending);
   Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive")
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);activity.startActivity(intent);pending=null;
  }catch(Exception error){toast("Nie udało się otworzyć instalatora Androida.");pending=null;}
 }
 void resume(){if(pending!=null&&activity.getPackageManager().canRequestPackageInstalls())install();}
 void close(){closed=true;main.removeCallbacksAndMessages(null);executor.shutdownNow();}
 private void toast(String message){Toast.makeText(activity,message,Toast.LENGTH_LONG).show();}
 @SuppressWarnings("deprecation")
 private void verifyPackage(File apk,long expectedVersion)throws Exception{
  PackageManager pm=activity.getPackageManager();int flags=Build.VERSION.SDK_INT>=28?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES;
  PackageInfo candidate=pm.getPackageArchiveInfo(apk.getAbsolutePath(),flags),installed=pm.getPackageInfo(activity.getPackageName(),flags);
  if(candidate==null||!activity.getPackageName().equals(candidate.packageName)||version(candidate)!=expectedVersion||version(candidate)<=version(installed))throw new IOException("Unexpected package");
  Signature[] a=signers(candidate),b=signers(installed);
  if(a==null||b==null||a.length!=b.length||a.length==0)throw new IOException("Missing signer");
  for(Signature signer:a)if(!Arrays.asList(b).contains(signer))throw new IOException("Signer mismatch");
 }
 @SuppressWarnings("deprecation")
 private static Signature[] signers(PackageInfo info){return Build.VERSION.SDK_INT>=28&&info.signingInfo!=null?info.signingInfo.getApkContentsSigners():info.signatures;}
 @SuppressWarnings("deprecation")
 private static long version(PackageInfo info){return Build.VERSION.SDK_INT>=28?info.getLongVersionCode():info.versionCode;}
 private static String hex(byte[] bytes){StringBuilder value=new StringBuilder();for(byte b:bytes)value.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return value.toString();}
 private static HttpURLConnection open(String address)throws IOException{
  URL url=new URL(address);
  for(int i=0;i<6;i++){
   if(!"https".equals(url.getProtocol()))throw new IOException("HTTPS required");
   HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","Cytaty-TTC-Updater");
   int status=c.getResponseCode();
   if(status>=300&&status<400){String location=c.getHeaderField("Location");c.disconnect();if(location==null)throw new IOException("No redirect");url=new URL(url,location);continue;}
   if(status!=200){c.disconnect();throw new IOException("HTTP "+status);}return c;
  }throw new IOException("Too many redirects");
 }
 private static String readText(String address)throws IOException{
  HttpURLConnection c=open(address);try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
   byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>32768)throw new IOException("Manifest too large");out.write(buffer,0,n);}return out.toString("UTF-8");
  }finally{c.disconnect();}
 }
}
