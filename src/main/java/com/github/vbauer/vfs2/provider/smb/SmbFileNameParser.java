package com.github.vbauer.vfs2.provider.smb;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.GenericURLFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;


/**
 * Implementation for sftp. set default port to 139.
 *
 * @author Vladislav Bauer
 */
public class SmbFileNameParser extends GenericURLFileNameParser {

    private static final SmbFileNameParser INSTANCE = new SmbFileNameParser();

    public SmbFileNameParser() {
        super(SmbFileName.DEFAULT_PORT);
    }

    public static FileNameParser getInstance() {
        return INSTANCE;
    }

    @Override
    public FileName parseUri(
        VfsComponentContext context, FileName base, String filename
    ) throws FileSystemException {
        StringBuilder name = new StringBuilder();

        // Extract the scheme and authority parts
        Authority auth = extractToPath(context, filename, name);

        // extract domain
        String username = auth.getUserName();
        String domain = extractDomain(username);
        if (domain != null) {
            username = username.substring(domain.length() + 1);
        }

        // Decode and adjust separators
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);

        // Extract the share
        String share = UriParser.extractFirstElement(name);
        if (share == null || share.isEmpty()) {
            throw new FileSystemException("vfs.provider.smb/missing-share-name.error", filename);
        }

        // Normalise the path.  Do this after extracting the share name,
        // to deal with things like smb://hostname/share/..
        FileType fileType = UriParser.normalisePath(name);
        String path = name.toString();

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

    private static String extractDomain(String username) {
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
