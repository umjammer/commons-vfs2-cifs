/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.filesys.app.SMBOnlyXMLServerConfiguration;
import org.filesys.smb.server.SMBServer;

/**
 * An in-process SMB server for tests, so no external CIFS host is needed.
 *
 * <p>It is <a href="https://github.com/FileSysOrg/jfileserver">JFileServer</a> listening on an
 * ephemeral port with a single share backed by a temporary directory. The open source build of
 * JFileServer speaks SMB1 only (SMB2/SMB3 are licensed), and its {@code LocalAuthenticator} keeps
 * plain text passwords, so the jcifs-ng client has to be told to negotiate SMB1 and to send an
 * NTLMv1 response. That is what {@link #configureClient()} does, and it must be called before the
 * jcifs {@code SingletonContext} is created, i.e. before any {@code cifs://} URL is resolved.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-09-02 nsano initial version <br>
 * @see "https://www.filesys.org/"
 */
public final class EmbeddedSmbServer implements Closeable {

    /** the domain the test user belongs to */
    public static final String DOMAIN = "WORKGROUP";

    /** the test user */
    public static final String USER = "test";

    /** the test user's password */
    public static final String PASSWORD = "test";

    /** the name of the exported share */
    public static final String SHARE = "TEST";

    /** jfileserver session debug flags, e.g. "Negotiate,Socket,Tree,Error,State" */
    private static final String DEBUG_FLAGS = System.getProperty("smb.server.debug", "Error");

    private final SMBServer server;

    private final Path root;

    private final int port;

    private EmbeddedSmbServer(SMBServer server, Path root, int port) {
        this.server = server;
        this.root = root;
        this.port = port;
    }

    /**
     * Tells jcifs-ng how to talk to {@link EmbeddedSmbServer}. Has no effect once the jcifs
     * {@code SingletonContext} has been created, so call it first thing in a test.
     */
    public static void configureClient() {
        // the open source jfileserver only implements SMB1
        System.setProperty("jcifs.smb.client.minVersion", "SMB1");
        // jfileserver's LocalAuthenticator validates NTLMv1, not NTLMv2
        System.setProperty("jcifs.smb.lmCompatibility", "0");
        // skip the NetBIOS broadcast lookup, it costs 6 seconds before the first connection
        System.setProperty("jcifs.resolveOrder", "DNS");
    }

    /**
     * Starts a server on a free port, sharing a freshly created temporary directory.
     *
     * @return a running server, close it to shut down and delete the temporary directory
     * @throws Exception when the server cannot be started
     */
    public static EmbeddedSmbServer start() throws Exception {
        Path root = Files.createTempDirectory("commons-vfs2-cifs");
        int port = findFreePort();

        SMBOnlyXMLServerConfiguration configuration = new SMBOnlyXMLServerConfiguration();
        configuration.loadConfiguration(new StringReader(configurationXml(root, port)));

        SMBServer server = new SMBServer(configuration);
        server.startServer();
        waitUntilListening(port);

        return new EmbeddedSmbServer(server, root, port);
    }

    /**
     * @return the port the SMB service is listening on
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the local directory {@link #SHARE} is backed by
     */
    public Path getRoot() {
        return root;
    }

    /**
     * @return the base url of {@link #SHARE}, without a trailing slash
     */
    public String getUrl() {
        return "cifs://127.0.0.1:" + port + "/" + SHARE;
    }

    @Override
    public void close() throws IOException {
        server.shutdownServer(true);
        deleteRecursively(root);
    }

    /**
     * Note that {@code netBIOSSMB} and {@code Win32NetBIOS} are left out on purpose, they would
     * need the privileged ports 137-139.
     */
    private static String configurationXml(Path root, int port) {
        return """
                <?xml version="1.0" standalone="no"?>
                <fileserver>
                  <servers>
                    <SMB enable="true"/>
                  </servers>
                  <SMB>
                    <host name="JFILESRV" domain="%s">
                      <smbdialects>SMB1</smbdialects>
                      <comment>commons-vfs2-cifs test server</comment>
                      <tcpipSMB port="%d" platforms="linux,macosx,solaris,windows"/>
                    </host>
                    <sessionDebug flags="%s"/>
                    <authenticator>
                      <class>org.filesys.server.auth.LocalAuthenticator</class>
                      <mode>USER</mode>
                    </authenticator>
                  </SMB>
                  <debug>
                    <output type="console">
                      <class>org.filesys.debug.ConsoleDebug</class>
                    </output>
                  </debug>
                  <shares>
                    <diskshare name="%s" comment="commons-vfs2-cifs test share">
                      <driver>
                        <class>org.filesys.smb.server.disk.JavaNIODiskDriver</class>
                        <LocalPath>%s</LocalPath>
                      </driver>
                    </diskshare>
                  </shares>
                  <security>
                    <JCEProvider>org.bouncycastle.jce.provider.BouncyCastleProvider</JCEProvider>
                    <users>
                      <user name="%s">
                        <password>%s</password>
                      </user>
                    </users>
                  </security>
                </fileserver>
                """.formatted(DOMAIN, port, DEBUG_FLAGS, SHARE, root, USER, PASSWORD);
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void waitUntilListening(int port) throws IOException, InterruptedException {
        IOException last = null;
        for (int i = 0; i < 50; i++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 200);
                return;
            } catch (IOException e) {
                last = e;
                Thread.sleep(100);
            }
        }
        throw new IOException("smb server did not start on port " + port, last);
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var paths = Files.walk(path)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        }
    }
}
