package jp.hmori.assettest.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class StorageManager {

	public final File dataDir;

	private final String path;
	private final AssetManager assetManager;
	private static final int BUFFER_SIZE = 1024;

	public StorageManager(Context context, String path) {
		this(context, path, false);
	}

	public StorageManager(Context context, String path, boolean useExternal) {
		super();
		this.path = path;
		this.assetManager = context.getResources().getAssets();
		this.dataDir = useExternal ?
				new File(new File(Environment.getExternalStorageDirectory(), context.getPackageName()), path) :
					context.getDir(path, Context.MODE_PRIVATE);
	}

	public void initData() throws IOException {
		copyFiles(null, path, dataDir);
	}

	public void deleteData() throws IOException {
		deleteAll(dataDir);
	}

	private void copyFiles(final String parentPath, final String filename, final File toDir) throws IOException {

		String assetpath = (parentPath != null ? parentPath + File.separator + filename : filename);
		if (isDirectory(assetpath)) {
			if (!toDir.exists()) {
				toDir.mkdirs();
			}
			for (String child : assetManager.list(assetpath)) {
				copyFiles(assetpath, child, new File(toDir, child));
			}
		} else {
			if (assetpath.toLowerCase().endsWith(".zip")) {
				unzip(assetManager.open(assetpath, AssetManager.ACCESS_STREAMING), toDir.getParentFile());
			} else {
				copyData(assetManager.open(assetpath), new FileOutputStream(new File(toDir.getParentFile(), filename)));
			}
		}
	}

	private boolean isDirectory(final String path) throws IOException {
		boolean isDirectory = false;
		try {
			if (assetManager.list(path).length > 0){
				isDirectory = true;
			} else {
				// check openable file
				assetManager.open(path);
			}
		} catch (FileNotFoundException fnfe) {
			isDirectory = true;
		}
		return isDirectory;
	}

	private void deleteAll(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteAll(f);
			}
		}
		file.delete();
	}

	private void unzip(InputStream is, File toDir) throws IOException {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(is);
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String entryFilePath = entry.getName().replace('\\', File.separatorChar);
				File outFile = new File(toDir, entryFilePath);
				if (entry.isDirectory()) {
					outFile.mkdirs();
				} else {
					writeData(zis, new FileOutputStream(outFile));
					zis.closeEntry();
				}
			}
		} finally {
			if (zis != null) { try { zis.close(); } catch (IOException ioe) {} }
		}
	}
	
	private void copyData(final InputStream in, final OutputStream out) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		try {
			writeData(bis, out);
		} finally {
			if (bis != null) { try { bis.close(); } catch (IOException ioe) {} }
		}
	}

	private void writeData(final InputStream is, final OutputStream os) throws IOException {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(os);
			byte[] buffer = new byte[BUFFER_SIZE];
			int len = 0;
			while ( (len = is.read(buffer, 0, buffer.length)) > 0) {
				bos.write(buffer, 0, len);
			}
			bos.flush();
		} finally {
			if (bos != null) { try { bos.close(); } catch (IOException ioe) {} }
		}
	}
	
}
