/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Vfs2LocalTest. Exercises the provider against an {@link EmbeddedSmbServer}, so it needs neither a
 * CIFS host on the network nor docker.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-09-02 nsano initial version <br>
 */
public class Vfs2LocalTest {

    static {
        // must happen before jcifs' SingletonContext is created
        EmbeddedSmbServer.configureClient();
    }

    static EmbeddedSmbServer server;

    static FileSystemManager manager;

    static FileSystemOptions options;

    @BeforeAll
    static void setUp() throws Exception {
        server = EmbeddedSmbServer.start();
Debug.println("server: " + server.getUrl() + ", root: " + server.getRoot());

        options = new FileSystemOptions();
        StaticUserAuthenticator auth = new StaticUserAuthenticator(
                EmbeddedSmbServer.DOMAIN, EmbeddedSmbServer.USER, EmbeddedSmbServer.PASSWORD);
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, auth);

        manager = VFS.getManager();
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (manager != null) {
            manager.close();
        }
        if (server != null) {
            server.close();
        }
    }

    static FileObject resolve(String path) throws Exception {
        return manager.resolveFile(server.getUrl() + path, options);
    }

    @Test
    void test_provider_is_registered() {
        assertTrue(manager.hasProvider("cifs"));
    }

    @Test
    void test_list_children() throws Exception {
        Files.writeString(server.getRoot().resolve("listed.txt"), "listed");

        FileObject dir = resolve("/");
        assertEquals(FileType.FOLDER, dir.getType());

        List<String> names = Arrays.stream(dir.getChildren()).map(f -> f.getName().getBaseName()).toList();
Debug.println("children: " + names);
        assertTrue(names.contains("listed.txt"));
    }

    @Test
    void test_write_read_rename_delete() throws Exception {
        String text = "commons-vfs2-cifs\n";

        // $ echo ... > $remote/test.txt
        FileObject target = resolve("/test.txt");
        target.createFile();
        try (OutputStream os = target.getContent().getOutputStream(0x8000)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(text.length(), target.getContent().getSize());

        // $ cat $remote/test.txt
        try (InputStream is = target.getContent().getInputStream()) {
            assertEquals(text, new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }

        // $ mv $remote/test.txt $remote/renamed.txt
        FileObject renamed = resolve("/renamed.txt");
        target.moveTo(renamed);
        assertTrue(renamed.exists());
        assertFalse(target.exists());

        // $ rm $remote/renamed.txt
        renamed.delete();
        assertFalse(renamed.exists());
    }

    @Test
    void test_folder() throws Exception {
        FileObject folder = resolve("/dir/sub");
        folder.createFolder();
        assertEquals(FileType.FOLDER, folder.getType());
        assertTrue(Files.isDirectory(server.getRoot().resolve("dir").resolve("sub")));

        assertTrue(resolve("/dir").delete(Selectors.SELECT_ALL) > 0);
        assertFalse(resolve("/dir").exists());
    }
}
