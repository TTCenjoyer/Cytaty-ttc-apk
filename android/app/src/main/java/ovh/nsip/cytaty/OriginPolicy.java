package ovh.nsip.cytaty;

/** Only the two explicitly allowed HTTPS origins may host the app. */
final class OriginPolicy {
 static final String PRIMARY="https://cytat.nsip.ovh";
 static final String BACKUP="https://cytaty.c0re.ovh";
 interface Probe { boolean healthy(String origin); }
 static String choose(Probe probe,boolean backupOnly){
  if(!backupOnly&&probe.healthy(PRIMARY))return PRIMARY;
  if(probe.healthy(BACKUP))return BACKUP;
  return null;
 }
 static boolean trusted(String scheme,String host,int port){
  return "https".equals(scheme)&&(port==-1||port==443)&&("cytat.nsip.ovh".equals(host)||"cytaty.c0re.ovh".equals(host));
 }
}
