package ovh.nsip.cytaty;
final class UpdatePolicy {
 static boolean shouldCheck(boolean manual,boolean checkedThisLaunch,long last,long now){
  return manual||!checkedThisLaunch||now<last||now-last>=6*60*60*1000L;
 }
}
