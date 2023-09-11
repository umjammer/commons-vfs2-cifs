package com.github.vbauer.vfs2.provider.smb;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import static org.apache.commons.vfs2.UserAuthenticationData.Type;

/**
 * A file in an SMB file system.
 *
 * @author Vladislav Bauer
 */
public class SmbFileObject extends AbstractFileObject<SmbFileSystem> implements FileObject {

    private SmbFile file;

    protected SmbFileObject(AbstractFileName name, SmbFileSystem fileSystem) {
        super(name, fileSystem);
    }

    /**
     * Attaches this file object to its file resource.
     */
    @Override
    protected void doAttach() throws Exception {
        // Defer creation of the SmbFile to here
        if (file == null) {
            file = createSmbFile(getName());
        }
    }

    @Override
    protected void doDetach() throws Exception {
        // File closed through content-streams
        file = null;
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    @Override
    protected FileType doGetType() throws Exception {
        if (!file.exists()) {
            return FileType.IMAGINARY;
        } else if (file.isDirectory()) {
            return FileType.FOLDER;
        } else if (file.isFile()) {
            return FileType.FILE;
        }

        throw new FileSystemException("vfs.provider.smb/get-type.error", getName());
    }

    /**
     * Lists the children of the file.  Is only called if {@link #doGetType}
     * returns {@link FileType#FOLDER}.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        return UriParser.encode(file.list());
    }

    /**
     * Determines if this file is hidden.
     */
    @Override
    protected boolean doIsHidden() throws Exception {
        return file.isHidden();
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        file.delete();
    }

    @Override
    protected void doRename(FileObject newFile) throws Exception {
        file.renameTo(createSmbFile(newFile.getName()));
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        file.mkdir();
        file = createSmbFile(getName());
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return file.length();
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return file.getLastModified();
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        return new SmbFileInputStream(file);
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {
        return new SmbFileOutputStream(file, bAppend);
    }

    /**
     * Random access.
     */
    @Override
    protected RandomAccessContent doGetRandomAccessContent(RandomAccessMode mode) throws Exception {
        return new SmbFileRandomAccessContent(file, mode);
    }

    private SmbFile createSmbFile(
        FileName fileName
    ) throws MalformedURLException, SmbException, FileSystemException {
        SmbFileName smbFileName = (SmbFileName) fileName;
        String path = smbFileName.getUriWithoutAuth();

        UserAuthenticationData authData = null;
        SmbFile file;
        NtlmPasswordAuthenticator auth;
        CIFSContext context;
        try {
            FileSystemOptions fileSystemOptions = getFileSystem().getFileSystemOptions();
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, SmbFileProvider.AUTHENTICATOR_TYPES);
            auth = createNtlmPasswordAuthentication(smbFileName, authData);
            context = SingletonContext.getInstance().withCredentials(auth);
            file = new SmbFile(path, context);
        } finally {
            UserAuthenticatorUtils.cleanup(authData);
        }

        if (file.isDirectory() && !file.toString().endsWith("/")) {
            file = new SmbFile(path + "/", context);
        }

        return file;
    }

    private NtlmPasswordAuthenticator createNtlmPasswordAuthentication(
        SmbFileName smbFileName, UserAuthenticationData authData
    ) {
        String domain = getAuthValue(authData, UserAuthenticationData.DOMAIN, smbFileName.getDomain());
        String username = getAuthValue(authData, UserAuthenticationData.USERNAME, smbFileName.getUserName());
        String password = getAuthValue(authData, UserAuthenticationData.PASSWORD, smbFileName.getPassword());

        return new NtlmPasswordAuthenticator(domain, username, password);
    }

    private String getAuthValue(UserAuthenticationData authData, Type type, String value) {
        return UserAuthenticatorUtils.toString(
            UserAuthenticatorUtils.getData(authData, type, UserAuthenticatorUtils.toChar(value))
        );
    }
}
