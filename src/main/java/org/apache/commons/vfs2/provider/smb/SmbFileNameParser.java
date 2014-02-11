package org.apache.commons.vfs2.provider.smb;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.URLFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

/**
 * Implementation for sftp. set default port to 139
 */

public class SmbFileNameParser extends URLFileNameParser {

    private static final SmbFileNameParser INSTANCE = new SmbFileNameParser();


    public SmbFileNameParser() {
        super(139);
    }


    public static FileNameParser getInstance() {
        return INSTANCE;
    }

    public FileName parseUri(
        final VfsComponentContext context, final FileName base, final String filename
    ) throws FileSystemException {
        final StringBuilder name = new StringBuilder();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath(filename, name);

        // extract domain
        String username = auth.getUserName();
        final String domain = extractDomain(username);
        if (domain != null) {
            username = username.substring(domain.length() + 1);
        }

        // Decode and adjust separators
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);

        // Extract the share
        final String share = UriParser.extractFirstElement(name);
        if (share == null || share.length() == 0) {
            throw new FileSystemException("vfs.provider.smb/missing-share-name.error", filename);
        }

        // Normalise the path.  Do this after extracting the share name,
        // to deal with things like smb://hostname/share/..
        final FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        return new SmbFileName(
            auth.getScheme(),
            auth.getHostName(),
            auth.getPort(),
            username,
            auth.getPassword(),
            domain,
            share,
            path,
            fileType);
    }

    private String extractDomain(final String username) {
        if (username == null) {
            return null;
        }

        for (int i = 0; i < username.length(); i++) {
            if (username.charAt(i) == '\\') {
                return username.substring(0, i);
            }
        }

        return null;
    }

}
