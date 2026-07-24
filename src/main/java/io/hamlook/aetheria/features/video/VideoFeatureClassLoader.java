package io.hamlook.aetheria.features.video;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;

/**
 * Minecraft 1.8.9 (via LWJGL's {@code jinput} dependency, used for joystick support)
 * ships an ancient JNA (~3.4.x) as one of Forge's own libraries. That legacy JNA
 * predates APIs the vlcj/JNA versions bundled with this mod need (e.g.
 * {@code Native.load(String, Class)}, added in JNA 4.2.1). Because Forge's shared
 * classloader resolves {@code com.sun.jna.*} from that legacy copy before it would
 * ever reach the classes bundled inside this mod's own jar, the wrong version wins
 * every time regardless of what version Aetheria ships — producing a
 * {@code NoSuchMethodError} out of {@code MediaPlayerFactory}'s constructor.
 * <p>
 * The usual shading fix for a classpath version conflict — relocating the
 * conflicting package to a unique name — is <b>not</b> viable for JNA specifically:
 * JNA's native calls are bound via JNI's default symbol mapping, which is derived
 * from the class's original package/class name (e.g.
 * {@code com.sun.jna.Native.sizeof} maps to the native symbol
 * {@code Java_com_sun_jna_Native_sizeof}). Renaming the package breaks that symbol
 * lookup and JNA stops working entirely, relocated or not.
 * <p>
 * Instead, this loader loads {@code com.sun.jna.*} and {@code uk.co.caprica.vlcj.*}
 * <i>child-first</i>, straight out of this mod's own jar, bypassing the parent
 * (shared/Forge) classpath entirely for just those two package trees. Every other
 * class (including this mod's own {@code io.hamlook.aetheria.*} classes) still
 * delegates to the parent classloader as normal, so the {@link VideoEngine}
 * interface — which crosses this boundary — remains identical on both sides.
 */
final class VideoFeatureClassLoader extends URLClassLoader {

    private static final String[] CHILD_FIRST_PREFIXES = {
            "com.sun.jna.",
            "uk.co.caprica.vlcj.",
    };

    // VlcVideoEngine itself must ALSO be defined by this isolated loader — not just
    // matched by the prefixes above — because a class's *own* internal references
    // (e.g. VlcVideoEngine's "MediaPlayerFactory factory" field) resolve through
    // whichever loader *defined that class*, not through whichever loader was asked
    // to look it up. If VlcVideoEngine were left to the normal prefix check, its name
    // ("io.hamlook.aetheria.features.video.VlcVideoEngine") wouldn't match either
    // prefix, so it would fall through to the parent classloader — which already
    // has an ordinary copy of it sitting in the same mod jar and would load it
    // immediately. At that point VlcVideoEngine's own vlcj/JNA field types would
    // resolve via the *parent*, completely bypassing this isolation and landing
    // right back on Forge's legacy JNA. Listing it here explicitly is what actually
    // routes it (and therefore everything it touches) through the isolated loader.
    private static final String[] CHILD_FIRST_EXACT = {
            "io.hamlook.aetheria.features.video.VlcVideoEngine",
    };

    private static volatile VideoFeatureClassLoader instance;

    static VideoFeatureClassLoader get() {
        VideoFeatureClassLoader result = instance;
        if (result == null) {
            synchronized (VideoFeatureClassLoader.class) {
                result = instance;
                if (result == null) {
                    instance = result = new VideoFeatureClassLoader();
                }
            }
        }
        return result;
    }

    /**
     * Discards the current isolated classloader (and every class it defined,
     * including {@code LibC}/{@code Native}) so the next {@link #get()} starts
     * completely fresh. Necessary because once a class's static initializer
     * throws once, the JVM permanently marks that class as broken for the rest
     * of that classloader's life — every later reference just reports a generic
     * {@code NoClassDefFoundError: Could not initialize class X} instead of the
     * real cause, even on an otherwise-clean retry. Without this, one failed
     * playback attempt would permanently mask the real error for the rest of
     * the game session.
     */
    static synchronized void reset() {
        if (instance != null) {
            try {
                instance.close();
            } catch (Exception ignored) {
            }
            instance = null;
        }
    }

    private VideoFeatureClassLoader() {
        super(new URL[]{ownJarUrl()}, VideoFeatureClassLoader.class.getClassLoader());
    }

    private static URL ownJarUrl() {
        CodeSource src = VideoFeatureClassLoader.class.getProtectionDomain().getCodeSource();
        if (src == null || src.getLocation() == null) {
            throw new IllegalStateException(
                    "Could not resolve Aetheria's own jar location for isolated vlcj/JNA loading");
        }
        return src.getLocation();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> found = findLoadedClass(name);
            if (found == null && isChildFirst(name)) {
                try {
                    found = findClass(name);
                } catch (ClassNotFoundException ignored) {
                    // Not present in our own jar for some reason; fall back to normal delegation.
                }
            }
            if (found == null) {
                found = super.loadClass(name, false);
            }
            if (resolve) {
                resolveClass(found);
            }
            return found;
        }
    }

    private static boolean isChildFirst(String name) {
        for (String exact : CHILD_FIRST_EXACT) {
            if (name.equals(exact)) return true;
        }
        for (String prefix : CHILD_FIRST_PREFIXES) {
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }
}
