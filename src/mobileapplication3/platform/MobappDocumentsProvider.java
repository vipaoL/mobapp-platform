// SPDX-License-Identifier: GPL-3.0-or-later

package mobileapplication3.platform;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static mobileapplication3.platform.Platform.SDK_INT;

@TargetApi(Build.VERSION_CODES.KITKAT)
public abstract class MobappDocumentsProvider extends DocumentsProvider {
    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_AVAILABLE_BYTES
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };

    private static final String ROOT_NAME = "root";
    private static File ROOT;
    private static String sAuthority;

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        sAuthority = info.authority;
    }

    @Override
    public boolean onCreate() {
        Platform.init(getContext());
        ROOT = getRootDir();
        return true;
    }

    @Override
    public DocumentsContract.Path findDocumentPath(String parentDocumentId, String childDocumentId) throws FileNotFoundException {
        File target = getFileForDocId(childDocumentId);
        File targetParent = parentDocumentId != null ? getFileForDocId(parentDocumentId) : ROOT;

        List<String> pathList = new ArrayList<>();
        File current = target;

        while (current != null) {
            pathList.add(getDocIdForFile(current));
            if (current.equals(targetParent)) {
                break;
            }
            current = current.getParentFile();
        }

        if (current == null && targetParent != null) {
            throw new FileNotFoundException("Failed to find path from " + parentDocumentId + " to " + childDocumentId);
        }

        Collections.reverse(pathList);

        if (SDK_INT >= Build.VERSION_CODES.O) {
            return new DocumentsContract.Path(ROOT_NAME, pathList);
        } else {
            return null;
        }
    }

    public static boolean openFolder(Context context, File targetFile) {
        if (ROOT == null || targetFile == null || sAuthority == null) {
            return false;
        }

        try {
            File canonicalTarget = targetFile.getCanonicalFile();
            File canonicalRoot = ROOT.getCanonicalFile();

            File temp = canonicalTarget;
            boolean isUnderRoot = false;
            while (temp != null) {
                if (temp.equals(canonicalRoot)) {
                    isUnderRoot = true;
                    break;
                }
                temp = temp.getParentFile();
            }

            if (!isUnderRoot) {
                return false;
            }

            String docId = getDocIdForFile(canonicalTarget);
            Uri documentUri = DocumentsContract.buildDocumentUri(sAuthority, docId);

            Intent intent = null;
            if (SDK_INT >= Build.VERSION_CODES.O) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(documentUri, DocumentsContract.Document.MIME_TYPE_DIR);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            } else if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            }

            if (intent != null) {
                context.startActivity(intent);
                return true;
            }
        } catch (Exception ex) {
            Logger.log(ex);
        }
        return false;
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        MatrixCursor roots = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
        if (ROOT == null) {
            return roots;
        }

        MatrixCursor.RowBuilder row = roots.newRow();
        row.add(Root.COLUMN_ROOT_ID, ROOT_NAME);
        row.add(Root.COLUMN_SUMMARY, getSummary());
        int flags = Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_SEARCH | Root.FLAG_LOCAL_ONLY;
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flags |= Root.FLAG_SUPPORTS_IS_CHILD;
        }
        row.add(Root.COLUMN_FLAGS, flags);
        row.add(Root.COLUMN_TITLE, getRootTitle());
        row.add(Root.COLUMN_DOCUMENT_ID, getDocIdForFile(ROOT));
        row.add(Root.COLUMN_AVAILABLE_BYTES, ROOT.getFreeSpace());
        row.add(Root.COLUMN_ICON, getRootIconRes());

        return roots;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        if (projection == null) {
            projection = DEFAULT_DOCUMENT_PROJECTION;
        }
        final MatrixCursor result = new MatrixCursor(projection);
        includeFile(result, documentId, null);
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        final File parent = getFileForDocId(parentDocumentId);
        for (File file : parent.listFiles()) {
            includeFile(result, null, file);
        }
        return result;
    }

    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        try {
            File parent = getFileForDocId(parentDocumentId).getCanonicalFile();
            File child = getFileForDocId(documentId).getCanonicalFile();

            File temp = child.getParentFile();
            while (temp != null) {
                if (temp.equals(parent)) {
                    return true;
                }
                temp = temp.getParentFile();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        return ParcelFileDescriptor.open(getFileForDocId(documentId), ParcelFileDescriptor.parseMode(mode));
    }

    @Override
    public String createDocument(String documentId, String mimeType, String displayName) throws FileNotFoundException {
        File parent = getFileForDocId(documentId);
        File file = new File(parent.getPath(), displayName);
        boolean success;
        try {
            if (Document.MIME_TYPE_DIR.equals(mimeType)) {
                success = file.mkdir();
            } else {
                success = file.createNewFile();
            }
            if (success) {
                success = file.setReadable(true) && file.setWritable(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        if (!success) {
            throw new FileNotFoundException("Failed to create document: \"" + displayName + "\" (id: \"" + documentId + "\")");
        }
        return getDocIdForFile(file);
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        if (!file.delete()) {
            throw new FileNotFoundException("Failed to delete document (id: \"" + documentId + "\")");
        }
    }

    @Override
    public void removeDocument(String documentId, String parentDocumentId) throws FileNotFoundException {
        deleteDocument(documentId);
    }

    @Override
    public String moveDocument(String sourceDocumentId, String sourceParentDocumentId, String targetParentDocumentId) throws FileNotFoundException {
        File sourceFile = getFileForDocId(sourceDocumentId);
        File targetParent = getFileForDocId(targetParentDocumentId);
        File destFile = new File(targetParent, sourceFile.getName());

        try {
            boolean renameSucceeded = sourceFile.renameTo(destFile);
            if (!renameSucceeded) {
                throw new FileNotFoundException("Failed to move document. Rename failed.");
            }
        } catch (Exception e) {
            throw new FileNotFoundException("Failed to move document. Error: " + e.getMessage());
        }

        return getDocIdForFile(destFile);
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        if (displayName == null) {
            throw new IllegalArgumentException("displayName is null");
        }

        // Create the destination file in the same directory as the source file
        File sourceFile = getFileForDocId(documentId);
        File sourceParentFile = sourceFile.getParentFile();
        if (sourceParentFile == null) {
            throw new FileNotFoundException("Failed to rename document. File has no parent.");
        }
        File destFile = new File(sourceParentFile.getPath(), displayName);

        // Try to do the rename
        try {
            boolean renameSucceeded = sourceFile.renameTo(destFile);
            if (!renameSucceeded) {
                throw new FileNotFoundException("Failed to rename document. Rename failed.");
            }
        } catch (Exception e) {
            throw new FileNotFoundException("Failed to rename document. Error: " + e.getMessage());
        }

        return getDocIdForFile(destFile);
    }

    private void includeFile(MatrixCursor result, String docId, File file) throws FileNotFoundException {
        if (file != null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }

        int flags = 0;

        if (file.isDirectory()) {
            flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
        } else {
            flags |= Document.FLAG_SUPPORTS_WRITE;
        }

        flags |= Document.FLAG_SUPPORTS_DELETE;
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flags |= Document.FLAG_SUPPORTS_RENAME;
        }
        if (SDK_INT >= Build.VERSION_CODES.N) {
            flags |= Document.FLAG_SUPPORTS_REMOVE;
            flags |= Document.FLAG_SUPPORTS_MOVE;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, file.getName());
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_MIME_TYPE, getTypeForFile(file));
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(Document.COLUMN_FLAGS, flags);
    }

    private static String getDocIdForFile(File file) {
        try {
            File root = ROOT.getCanonicalFile();
            file = file.getCanonicalFile();

            if (root.equals(file)) {
                return ROOT_NAME;
            }

            String relativePath = root.toURI().relativize(file.toURI()).getPath();
            if (relativePath.endsWith("/")) {
                relativePath = relativePath.substring(0, relativePath.length() - 1);
            }

            return ROOT_NAME + ':' + relativePath;
        } catch (IOException ex) {
            Logger.log(ex);
            return null;
        }
    }

    private File getFileForDocId(String docId) throws FileNotFoundException {
        File target = ROOT;
        if (docId.equals(ROOT_NAME) || docId.equals(ROOT_NAME + ":")) {
            return target;
        }
        final int splitIndex = docId.indexOf(':', 1);
        if (splitIndex < 0) {
            throw new FileNotFoundException("Missing root: " + docId);
        } else {
            final String path = docId.substring(splitIndex + 1);
            if (path.isEmpty()) {
                return target;
            }
            target = new File(target, path);
            if (!target.exists()) {
                throw new FileNotFoundException("File \"" + target + "\" not found (id: \"" + docId + "\")");
            }
            return target;
        }
    }

    private static String getTypeForFile(File file) {
        if (file.isDirectory()) {
            return Document.MIME_TYPE_DIR;
        } else {
            return getTypeForName(file.getName());
        }
    }

    private static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    protected abstract File getRootDir();
    protected abstract String getRootTitle();
    protected abstract int getRootIconRes();
    protected abstract String getSummary();
}