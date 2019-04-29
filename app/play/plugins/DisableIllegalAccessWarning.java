package play.plugins;

import play.PlayPlugin;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author jtremeaux
 */
public class DisableIllegalAccessWarning extends PlayPlugin {
    @Override
    public void onLoad() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }
}
