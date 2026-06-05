package io.hamlook.aetheria.init;

import io.hamlook.aetheria.Aetheria;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClasspathScanner {

    public static List<String> findClassNames(
            Class<?> referenceClass, String basePackage, Predicate<String> filter
    ) {
        URL location = referenceClass.getProtectionDomain().getCodeSource().getLocation();
        try {
            Path root = resolveRoot(location);
            List<String> names = new ArrayList<>();
            String basePath = basePackage.replace(".", "/");

            Predicate<String> effectiveFilter = fqn ->
                    fqn.startsWith(basePackage + ".")
                    && !fqn.contains("$")
                    && (filter == null || filter.test(fqn));

            if (Files.isDirectory(root)) {
                scanDirectory(root, basePath, effectiveFilter, names);
            } else {
                scanJar(root, effectiveFilter, names);
            }
            return names;
        } catch (Exception e) {
            Aetheria.logger.severe("[ClasspathScanner] Failed to scan classes: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public static List<String> findClassNames(Class<?> referenceClass, String basePackage) {
        return findClassNames(referenceClass, basePackage, null);
    }

    public static List<Class<?>> loadClasses(List<String> classNames, ClassLoader loader) {
        List<Class<?>> classes = new ArrayList<>();
        for (String name : classNames) {
            try {
                classes.add(Class.forName(name, false, loader));
            } catch (Throwable ignored) {
            }
        }
        return classes;
    }

    private static Path resolveRoot(URL location) throws URISyntaxException, IOException {
        if ("jar".equals(location.getProtocol())) {
            String raw = location.toExternalForm();
            int separator = raw.indexOf("!/");
            if (separator != -1) {
                try {
                    location = new URL(raw.substring(4, separator));
                } catch (MalformedURLException e) {
                    throw new IOException("Failed to parse jar: URL: " + raw, e);
                }
            }
        }
        return Paths.get(location.toURI());
    }

    private static void scanDirectory(Path root, String basePath,
                                      Predicate<String> filter, List<String> out) throws IOException {
        Path target = root.resolve(basePath);
        if (!Files.exists(target)) return;
        try (Stream<Path> stream = Files.walk(target)) {
            stream.map(p -> root.relativize(p).toString())
                  .filter(p -> p.endsWith(".class"))
                  .map(ClasspathScanner::toClassName)
                  .filter(filter)
                  .forEach(out::add);
        }
    }

    private static void scanJar(Path jar,
                                Predicate<String> filter, List<String> out) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(jar))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String fqn = toClassName(name);
                    if (filter.test(fqn)) {
                        out.add(fqn);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static String toClassName(String path) {
        return path.substring(0, path.length() - 6)
                   .replace('\\', '/')
                   .replace('/', '.');
    }
}
