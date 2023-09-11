package com.github.vbauer.vfs2.provider.smb;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

/**
 * An SMB file system.
 *
 * @author Vladislav Bauer
 */
public class SmbFileSystem extends AbstractFileSystem implements FileSystem {

    protected SmbFileSystem(FileName rootName, FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);
    }

    @Override
    protected FileObject createFile(AbstractFileName name) throws Exception {
        return new SmbFileObject(name, this);
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(Collection<Capability> capabilities) {
        capabilities.addAll(SmbFileProvider.CAPABILITIES);
    }
}
