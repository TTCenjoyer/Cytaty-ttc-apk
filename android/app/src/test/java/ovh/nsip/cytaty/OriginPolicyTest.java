package ovh.nsip.cytaty;
import org.junit.Test;
import static org.junit.Assert.*;
public class OriginPolicyTest {
 @Test public void primaryWins(){assertEquals(OriginPolicy.PRIMARY,OriginPolicy.choose(o->true,false));}
 @Test public void backupWhenPrimaryFails(){assertEquals(OriginPolicy.BACKUP,OriginPolicy.choose(o->o.equals(OriginPolicy.BACKUP),false));}
 @Test public void backupOnlyNeverChecksPrimary(){assertEquals(OriginPolicy.BACKUP,OriginPolicy.choose(o->{assertEquals(OriginPolicy.BACKUP,o);return true;},true));}
 @Test public void bothFail(){assertNull(OriginPolicy.choose(o->false,false));}
 @Test public void strictOriginList(){assertTrue(OriginPolicy.trusted("https","cytaty.c0re.ovh",443));assertTrue(OriginPolicy.trusted("https","cytat.nsip.ovh",-1));assertFalse(OriginPolicy.trusted("http","cytat.nsip.ovh",-1));assertFalse(OriginPolicy.trusted("https","cytat.nsip.ovh.evil.test",-1));assertFalse(OriginPolicy.trusted("https","cytat.nsip.ovh",8443));}
}
