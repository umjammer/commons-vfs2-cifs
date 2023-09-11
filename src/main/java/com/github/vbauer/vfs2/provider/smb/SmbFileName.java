package com.github.vbauer.vfs2.provider.smb;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericFileName;

/**
 * An SMB URI.  Adds a share name to the generic URI.
 *
 * @author Vladislav Bauer
 */

public class SmbFileName extends GenericFileName {

    public static final int DEFAULT_PORT = 139;

    private final String share;
    private final String domain;
    private String uriWithoutAuth;

    protected SmbFileName(
        String scheme,
        String hostName, int port,
        String userName, String password,
        String domain, String share, String path,
        FileType type
    ) {
        super(scheme, hostName, port, DEFAULT_PORT, userName, password, path, type);
        this.share = share;
        this.domain = domain;
    }

    /**
     * Returns the share name.
     * @return share name
     */
    public String getShare() {
        return share;
    }

    /**
     * Builds the root URI for this file name.
     */
    @Override
    protected void appendRootUri(StringBuilder buffer, boolean addPassword) {
        super.appendRootUri(buffer, addPassword);
        buffer.append('/');
        buffer.append(share);
    }

    /**
     * put domain before username if both are set
     */
    @Override
    protected void appendCredentials(StringBuilder buffer, boolean addPassword) {
        String domain = getDomain();
        String userName = getUserName();

        if (domain != null && !domain.isEmpty() && userName != null && !userName.isEmpty()) {
            buffer.append(domain);
            buffer.append("\\");
        }
        super.appendCredentials(buffer, addPassword);
    }

    /**
     * Factory method for creating name instances.
     */
    @Override
    public FileName createName(String path, FileType type) {
        return new SmbFileName(
            getScheme(),
            getHostName(),
            getPort(),
            getUserName(),
            getPassword(),
            domain,
            share,
            path,
            type
        );
    }

    /**
     * Construct the path suitable for SmbFile when used with NtlmPasswordAuthentication
     * @return uri without auth
     * @throws org.apache.commons.vfs2.FileSystemException on error
     */
    public String getUriWithoutAuth() throws FileSystemException {
        if (uriWithoutAuth != null) {
            return uriWithoutAuth;
        }

        StringBuilder sb = new StringBuilder(120);
        sb.append(getScheme());
        sb.append("://");
        sb.append(getHostName());
        if (getPort() != DEFAULT_PORT) {
            sb.append(":");
            sb.append(getPort());
        }
        sb.append("/");
        sb.append(getShare());
        sb.append(getPathDecoded());
        uriWithoutAuth = sb.toString();
        return uriWithoutAuth;
    }

    /**
     * returns the domain name
     * @return domain name
     */
    public String getDomain() {
        return domain;
    }
}
