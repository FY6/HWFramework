package com.android.server.rms.io;

import android.os.FileUtils;
import android.rms.utils.Utils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import libcore.io.IoUtils;
import libcore.io.Streams;

public class IOFileRotator {
    private static final int MAX_FILE_SIZE = 262144;
    private static long MAX_SIZE_BASE_PATH = 2097152;
    private static final String SUFFIX_BACKUP = ".backup";
    private static final String SUFFIX_NO_BACKUP = ".no_backup";
    private static final String TAG = "RMS.IO.FileRotator";
    private static long mMaxSizeForBasePath = MAX_SIZE_BASE_PATH;
    private final File mBasePath;
    private final long mDeleteAgeMillis;
    private long mMaxFileSize;
    private final String mPrefix;
    private final long mRotateAgeMillis;

    private static class FileInfo {
        public long endMillis;
        public final String prefix;
        public long startMillis;

        public FileInfo(String prefix) {
            this.prefix = (String) Preconditions.checkNotNull(prefix);
        }

        public boolean parse(String name) {
            this.endMillis = -1;
            this.startMillis = -1;
            int dotIndex = name.lastIndexOf(46);
            int dashIndex = name.lastIndexOf(45);
            if (dotIndex == -1 || dashIndex == -1) {
                if (Utils.DEBUG) {
                    Log.d(IOFileRotator.TAG, "FileInfo.parse,name:" + name + " missing time section");
                }
                return false;
            } else if (this.prefix.equals(name.substring(0, dotIndex))) {
                try {
                    this.startMillis = Long.parseLong(name.substring(dotIndex + 1, dashIndex));
                    if (name.length() - dashIndex == 1) {
                        this.endMillis = Long.MAX_VALUE;
                    } else {
                        this.endMillis = Long.parseLong(name.substring(dashIndex + 1));
                    }
                    return true;
                } catch (NumberFormatException e) {
                    Slog.e(IOFileRotator.TAG, "FileInfo.parse,name:" + name + " NumberFormatException");
                    return false;
                }
            } else {
                if (Utils.DEBUG) {
                    Log.d(IOFileRotator.TAG, "FileInfo.parse,name:" + name + " prefix doesn't match");
                }
                return false;
            }
        }

        public String build() {
            StringBuilder name = new StringBuilder();
            name.append(this.prefix).append('.').append(this.startMillis).append('-');
            if (this.endMillis != Long.MAX_VALUE) {
                name.append(this.endMillis);
            }
            return name.toString();
        }

        public boolean isActive() {
            return this.endMillis == Long.MAX_VALUE;
        }
    }

    public interface Reader {
        void read(InputStream inputStream) throws IOException;
    }

    public interface Writer {
        void write(OutputStream outputStream) throws IOException;
    }

    public interface Rewriter extends Reader, Writer {
        void reset();

        boolean shouldWrite();
    }

    private static class RewriterDef implements Rewriter {
        private Reader mReader = null;
        private Writer mWriter = null;

        public RewriterDef(Reader reader, Writer writer) {
            this.mReader = reader;
            this.mWriter = writer;
        }

        public void reset() {
        }

        public void read(InputStream in) throws IOException {
            if (this.mReader == null) {
                Log.e(IOFileRotator.TAG, "RewriterDef,the reader is null");
            } else {
                this.mReader.read(in);
            }
        }

        public boolean shouldWrite() {
            return true;
        }

        public void write(OutputStream out) throws IOException {
            if (this.mWriter == null) {
                Log.e(IOFileRotator.TAG, "RewriterDef,the Writer is null");
            } else {
                this.mWriter.write(out);
            }
        }
    }

    public IOFileRotator(File basePath, String prefix, long rotateAgeMillis, long deleteAgeMillis, long maxFileSize) {
        this(basePath, prefix, rotateAgeMillis, deleteAgeMillis);
        this.mMaxFileSize = maxFileSize;
    }

    public IOFileRotator(File basePath, String prefix, long rotateAgeMillis, long deleteAgeMillis) {
        this.mMaxFileSize = 262144;
        this.mBasePath = (File) Preconditions.checkNotNull(basePath);
        this.mPrefix = (String) Preconditions.checkNotNull(prefix);
        this.mRotateAgeMillis = rotateAgeMillis;
        this.mDeleteAgeMillis = deleteAgeMillis;
        if (!(this.mBasePath.exists() || (this.mBasePath.mkdirs() ^ 1) == 0)) {
            Log.e(TAG, "IOFileRotator,fail to create the directory:" + this.mBasePath);
        }
        for (String name : getBasePathFileList()) {
            if (name.startsWith(this.mPrefix)) {
                if (name.endsWith(SUFFIX_BACKUP)) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "recovering " + name);
                    }
                    File backupFile = new File(this.mBasePath, name);
                    if (!backupFile.renameTo(new File(this.mBasePath, name.substring(0, name.length() - SUFFIX_BACKUP.length())))) {
                        Log.e(TAG, "IOFileRotator,fail to renameTo,file:" + backupFile.getName());
                    }
                } else if (name.endsWith(SUFFIX_NO_BACKUP)) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "recovering " + name);
                    }
                    File noBackupFile = new File(this.mBasePath, name);
                    File file = new File(this.mBasePath, name.substring(0, name.length() - SUFFIX_NO_BACKUP.length()));
                    if (!noBackupFile.delete()) {
                        Log.e(TAG, "IOFileRotator,fail to delete,file:" + noBackupFile.getName());
                    }
                    if (!file.delete()) {
                        Log.e(TAG, "IOFileRotator,fail to delete,file:" + file.getName());
                    }
                }
            }
        }
    }

    public void deleteAll() {
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : getBasePathFileList()) {
            if (info.parse(name) && !new File(this.mBasePath, name).delete()) {
                Log.e(TAG, "deleteAll,fail to delete the file:" + name);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0014 A:{Catch:{ all -> 0x003c, all -> 0x0041 }} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0014 A:{Catch:{ all -> 0x003c, all -> 0x0041 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dumpAll(OutputStream os) throws IOException {
        FileInputStream zos = new ZipOutputStream(os);
        int i;
        FileInputStream is;
        try {
            FileInfo info = new FileInfo(this.mPrefix);
            String[] baseFileList = getBasePathFileList();
            i = 0;
            int length = baseFileList.length;
            if (i < length) {
                String name = baseFileList[i];
                if (info.parse(name)) {
                    zos.putNextEntry(new ZipEntry(name));
                    is = new FileInputStream(new File(this.mBasePath, name));
                    Streams.copy(is, zos);
                    zos.closeEntry();
                    i++;
                    if (i < length) {
                    }
                }
                i++;
                if (i < length) {
                }
            }
        } catch (Throwable th) {
            i = th;
        } finally {
            IoUtils.closeQuietly(
/*
Method generation error in method: com.android.server.rms.io.IOFileRotator.dumpAll(java.io.OutputStream):void, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x003d: INVOKE  (wrap: java.io.FileInputStream
  ?: MERGE  (r4_2 'is' java.io.FileInputStream) = (r4_0 'is' java.io.FileInputStream), (r6_0 'zos' java.io.FileInputStream)) libcore.io.IoUtils.closeQuietly(java.lang.AutoCloseable):void type: STATIC in method: com.android.server.rms.io.IOFileRotator.dumpAll(java.io.OutputStream):void, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:205)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:298)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:322)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:260)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:222)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: MERGE  (r4_2 'is' java.io.FileInputStream) = (r4_0 'is' java.io.FileInputStream), (r6_0 'zos' java.io.FileInputStream) in method: com.android.server.rms.io.IOFileRotator.dumpAll(java.io.OutputStream):void, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:688)
	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:658)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:340)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 21 more
Caused by: jadx.core.utils.exceptions.CodegenException: MERGE can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:213)
	... 26 more

*/

    public long getAvailableBytesInActiveFile(long currentTimeMillis) {
        RuntimeException e;
        File file;
        Throwable th;
        long availableBytes = 0;
        try {
            File activeFile = new File(this.mBasePath, getActiveName(currentTimeMillis));
            try {
                availableBytes = this.mMaxFileSize - activeFile.length();
                if (availableBytes <= 0) {
                    availableBytes = 0;
                }
            } catch (RuntimeException e2) {
                e = e2;
                file = activeFile;
                Log.e(TAG, "checkIfActiveFileFull,RuntimeException:" + e.getMessage());
                return availableBytes;
            } catch (Exception e3) {
                file = activeFile;
                try {
                    Log.e(TAG, "checkIfActiveFileFull,fail to read the file's size");
                    return availableBytes;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        } catch (RuntimeException e4) {
            e = e4;
            Log.e(TAG, "checkIfActiveFileFull,RuntimeException:" + e.getMessage());
            return availableBytes;
        } catch (Exception e5) {
            Log.e(TAG, "checkIfActiveFileFull,fail to read the file's size");
            return availableBytes;
        }
        return availableBytes;
    }

    public boolean removeFilesWhenOverFlow() {
        boolean isHandle = false;
        try {
            long directorySize = Utils.getSizeOfDirectory(this.mBasePath);
            if (directorySize < mMaxSizeForBasePath) {
                Log.i(TAG, "removeFilesWhenOverFlow,the total size is ok,current size:" + directorySize);
                return false;
            }
            int index;
            String[] baseFiles = getBasePathFileList();
            int totalSize = 0;
            int deleteEndIndex = 0;
            for (index = baseFiles.length - 1; index >= 0; index--) {
                totalSize = (int) (((long) totalSize) + new File(this.mBasePath, baseFiles[index]).length());
                if (((long) totalSize) >= mMaxSizeForBasePath) {
                    deleteEndIndex = index;
                    break;
                }
            }
            for (index = 0; index <= deleteEndIndex; index++) {
                if (!new File(this.mBasePath, baseFiles[index]).delete()) {
                    Log.e(TAG, "removeFilesWhenOverFlow,fail to delete the " + baseFiles[index]);
                }
            }
            isHandle = true;
            return isHandle;
        } catch (RuntimeException e) {
            Log.e(TAG, "removeFilesWhenOverFlow,RuntimeException:" + e.getMessage());
        } catch (Exception e2) {
            Log.e(TAG, "removeFilesWhenOverFlow,fail to read the file's size");
        }
    }

    public void forceFile(long currentTimeMillis, long endTimeMills) {
        String activeFileName = getActiveName(currentTimeMillis);
        File currentFile = new File(this.mBasePath, activeFileName);
        if (currentFile.exists()) {
            FileInfo info = new FileInfo(this.mPrefix);
            if (info.parse(activeFileName) && (info.isActive() ^ 1) == 0) {
                info.endMillis = endTimeMills;
                File destFile = new File(this.mBasePath, info.build());
                if (!currentFile.renameTo(destFile)) {
                    Log.e(TAG, "forceFile,fail to renameTo:destFile" + destFile.getName());
                }
            }
        }
    }

    public void rewriteActive(Rewriter rewriter, long currentTimeMillis) throws IOException {
        if (rewriter == null) {
            Log.e(TAG, "rewriteActive,the rewriter is null");
        } else {
            rewriteSingle(rewriter, getActiveName(currentTimeMillis));
        }
    }

    @Deprecated
    public void combineActive(Reader reader, Writer writer, long currentTimeMillis) throws IOException {
        rewriteActive(new RewriterDef(reader, writer), currentTimeMillis);
    }

    private void rewriteSingle(Rewriter rewriter, String name) throws IOException {
        IOException rethrowAsIoException;
        if (Utils.DEBUG) {
            Log.d(TAG, "rewriting " + name);
        }
        File file = new File(this.mBasePath, name);
        rewriter.reset();
        File backupFile;
        if (file.exists()) {
            readFile(file, rewriter);
            if (rewriter.shouldWrite()) {
                backupFile = new File(this.mBasePath, name + SUFFIX_BACKUP);
                if (!file.renameTo(backupFile)) {
                    Log.e(TAG, "rewriteSingle,fail to renameTo:" + backupFile.getName());
                }
                try {
                    writeFile(file, rewriter);
                    if (!backupFile.delete()) {
                        Log.e(TAG, "rewriteSingle,fail to delete the file:" + backupFile.getName());
                    }
                } catch (Throwable t) {
                    if (!file.delete()) {
                        Log.e(TAG, "rewriteSingle,fail to delete the file:" + file.getName());
                    }
                    if (!backupFile.renameTo(file)) {
                        Log.e(TAG, "rewriteSingle,fail to renameTo:" + backupFile.getName());
                    }
                    rethrowAsIoException = rethrowAsIoException(t);
                }
            } else {
                return;
            }
        }
        backupFile = new File(this.mBasePath, name + SUFFIX_NO_BACKUP);
        if (!backupFile.createNewFile()) {
            Log.e(TAG, "rewriteSingle,fail to createNewFile," + backupFile.getName());
        }
        try {
            writeFile(file, rewriter);
            if (!backupFile.delete()) {
                Log.e(TAG, "rewriteSingle,fail to delete the file:" + backupFile.getName());
            }
        } catch (Throwable t2) {
            if (!file.delete()) {
                Log.e(TAG, "rewriteSingle,fail to delete the file:" + file.getName());
            }
            if (!backupFile.delete()) {
                Log.e(TAG, "rewriteSingle,fail to delete the file:" + backupFile.getName());
            }
            rethrowAsIoException = rethrowAsIoException(t2);
        }
    }

    public void readMatching(Reader reader, long matchStartMillis, long matchEndMillis) throws IOException {
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : getBasePathFileList()) {
            if (info.parse(name) && info.startMillis <= matchEndMillis && matchStartMillis <= info.endMillis) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "reading matching " + name);
                }
                readFile(new File(this.mBasePath, name), reader);
            }
        }
    }

    private String getActiveName(long currentTimeMillis) {
        String oldestActiveName = null;
        long oldestActiveStart = Long.MAX_VALUE;
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : getBasePathFileList()) {
            if (info.parse(name) && info.isActive() && info.startMillis < currentTimeMillis && info.startMillis < oldestActiveStart) {
                oldestActiveName = name;
                oldestActiveStart = info.startMillis;
            }
        }
        if (oldestActiveName != null) {
            return oldestActiveName;
        }
        info.startMillis = currentTimeMillis;
        info.endMillis = Long.MAX_VALUE;
        return info.build();
    }

    private String[] getBasePathFileList() {
        String[] baseFiles = this.mBasePath.list();
        if (baseFiles != null && baseFiles.length != 0) {
            return baseFiles;
        }
        Log.e(TAG, "getBasePathFileList,the baseFiles is empty");
        return new String[0];
    }

    public void maybeRotate(long currentTimeMillis) {
        long rotateBefore = currentTimeMillis - this.mRotateAgeMillis;
        long deleteBefore = currentTimeMillis - this.mDeleteAgeMillis;
        FileInfo info = new FileInfo(this.mPrefix);
        for (String name : getBasePathFileList()) {
            if (info.parse(name)) {
                File file;
                if (info.isActive()) {
                    if (info.startMillis <= rotateBefore) {
                        if (Utils.DEBUG) {
                            Log.d(TAG, "rotating " + name);
                        }
                        info.endMillis = currentTimeMillis;
                        file = new File(this.mBasePath, name);
                        File destFile = new File(this.mBasePath, info.build());
                        if (!file.renameTo(destFile)) {
                            Log.e(TAG, "maybeRotate,fail to renameTo:" + destFile.getName());
                        }
                    }
                } else if (info.endMillis <= deleteBefore) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "deleting " + name);
                    }
                    file = new File(this.mBasePath, name);
                    if (!file.delete()) {
                        Log.e(TAG, "maybeRotate,fail to delete the file:" + file.getName());
                    }
                }
            }
        }
    }

    private static void readFile(File file, Reader reader) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        try {
            reader.read(bis);
        } finally {
            IoUtils.closeQuietly(bis);
        }
    }

    private static void writeFile(File file, Writer writer) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        try {
            writer.write(bos);
            bos.flush();
        } finally {
            FileUtils.sync(fos);
            IoUtils.closeQuietly(bos);
        }
    }

    private static IOException rethrowAsIoException(Throwable t) throws IOException {
        if (t instanceof IOException) {
            throw ((IOException) t);
        }
        throw new IOException(t.getMessage(), t);
    }
}
