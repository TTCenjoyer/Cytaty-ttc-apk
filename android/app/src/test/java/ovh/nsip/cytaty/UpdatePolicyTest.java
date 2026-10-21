package ovh.nsip.cytaty;
import org.junit.Test;
import static org.junit.Assert.*;
public class UpdatePolicyTest {
 @Test public void firstLaunchIgnoresRecentCheck(){assertTrue(UpdatePolicy.shouldCheck(false,false,1000,2000));}
 @Test public void resumeDoesNotRepeatPromptImmediately(){assertFalse(UpdatePolicy.shouldCheck(false,true,1000,2000));}
 @Test public void manualAlwaysChecks(){assertTrue(UpdatePolicy.shouldCheck(true,true,1000,2000));}
 @Test public void checksAfterSixHours(){assertTrue(UpdatePolicy.shouldCheck(false,true,1000,1000+6*60*60*1000L));}
 @Test public void clockChangeDoesNotBlockCheck(){assertTrue(UpdatePolicy.shouldCheck(false,true,2000,1000));}
}
