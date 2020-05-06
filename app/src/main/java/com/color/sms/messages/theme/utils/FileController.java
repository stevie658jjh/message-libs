package com.color.sms.messages.theme.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.color.sms.messages.theme.model.ImageCategoryModel;
import com.color.sms.messages.theme.model.MediaAddingModel;

public class FileController {

    private static int RECORDER_SAMPLERATE;

    public static Uri getUri(File file) {
        Uri gpxContentUri;
        try {
            gpxContentUri = FileProvider.getUriForFile(MyApplication.getInstance(),
                    "com.color.sms.messages.theme.fileprovider",
                    file);
        } catch (Exception e) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            gpxContentUri = Uri.fromFile(file);
        }
        return gpxContentUri;
    }

    public static byte[] bitmapToByteArray(Bitmap image, String type) {
        byte[] output = new byte[0];
        if (image == null) {
            return output;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            if (type.contains("png")) {
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            } else
                image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            output = stream.toByteArray();
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
        return output;
    }

    public static byte[] getAudioByte(String filePath) {
        byte[] bytes = null;
        try {
//            InputStream is = MyApplication.getInstance().openFileInput(filePath);
            FileInputStream fis = new FileInputStream(getFile(filePath));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                bos.write(buf, 0, n);
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static byte[] convertVideoToBytes(Uri uri) {
        byte[] videoBytes = null;
        try {
            File file = new File(getUriRealPath(MyApplication.getInstance(), uri));
            videoBytes = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoBytes;
    }

    private static boolean isDocumentUri(Context ctx, Uri uri) {
        boolean ret = false;
        if (ctx != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(ctx, uri);
        }
        return ret;
    }

    private static boolean isMediaDoc(String uriAuthority) {
        boolean ret = false;

        if ("com.android.providers.media.documents".equals(uriAuthority)) {
            ret = true;
        }
        return ret;
    }

    private static String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause) {
        String ret = "";
        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if (cursor != null) {
            boolean moveToFirst = cursor.moveToFirst();
            if (moveToFirst) {

                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;

                if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Images.Media.DATA;
                } else if (uri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Audio.Media.DATA;
                } else if (uri == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Video.Media.DATA;
                }

                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }

        return ret;
    }

    public static String getUriRealPath(Context ctx, Uri uri) {
        String ret = "";

        if (ctx != null && uri != null) {

            if (isDocumentUri(ctx, uri)) {

                // Get uri related document id.
                String documentId = DocumentsContract.getDocumentId(uri);

                // Get uri authority.
                String uriAuthority = uri.getAuthority();

                if (isMediaDoc(uriAuthority)) {
                    String idArr[] = documentId.split(":");
                    if (idArr.length == 2) {
                        // First item is document type.
                        String docType = idArr[0];

                        // Second item is document real id.
                        String realDocId = idArr[1];

                        // Get content uri by document type.
                        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        if ("image".equals(docType)) {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(docType)) {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(docType)) {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        // Get where clause with real document id.
                        String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;

                        ret = getImageRealPath(MyApplication.getInstance().getContentResolver(), mediaContentUri, whereClause);
                    }

                } else if (isDownloadDoc(uriAuthority)) {
                    // Build download uri.
                    Uri downloadUri = Uri.parse("content://downloads/public_downloads");

                    // Append download document id at uri end.
                    Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));

                    ret = getImageRealPath(MyApplication.getInstance().getContentResolver(), downloadUriAppendId, null);

                } else if (isExternalStoreDoc(uriAuthority)) {
                    String idArr[] = documentId.split(":");
                    if (idArr.length == 2) {
                        String type = idArr[0];
                        String realDocId = idArr[1];

                        if ("primary".equalsIgnoreCase(type)) {
                            ret = Environment.getExternalStorageDirectory() + "/" + realDocId;
                        }
                    }
                }
            }
        }

        return ret;
    }

    /* Check whether this document is provided by ExternalStorageProvider. */
    private static boolean isExternalStoreDoc(String uriAuthority) {
        boolean ret = false;

        if ("com.android.externalstorage.documents".equals(uriAuthority)) {
            ret = true;
        }

        return ret;
    }

    private static boolean isDownloadDoc(String uriAuthority) {
        boolean ret = false;

        if ("com.android.providers.downloads.documents".equals(uriAuthority)) {
            ret = true;
        }

        return ret;
    }

    public static Uri getUri(String filePath) {
        if (filePath.contains("content://")) {
            return Uri.parse(filePath);
        } else return getUri(new File(filePath));
    }

    public static File getFile(String filePath) {
        if (filePath.contains("content://")) {
            return new File(Uri.parse(filePath).getPath());
        } else return new File(filePath);
    }

    public static List<ImageCategoryModel> getImageCategory() {
        List<ImageCategoryModel> list = new ArrayList<>();
        AssetManager assetManager = MyApplication.getInstance().getAssets();
        try {
            String[] fileListInSubfolder = assetManager.list("images");
            if (fileListInSubfolder != null) {
                for (String string :
                        fileListInSubfolder) {
                    String[] abc = assetManager.list("images/" + string);
                    if (abc != null && abc.length > 0 && abc[0] != null) {
                        String path = "file:///android_asset/images/" + string + "/" + abc[0];
                        list.add(new ImageCategoryModel(path, string));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<ImageCategoryModel> getGifCategory() {
        List<ImageCategoryModel> list = new ArrayList<>();
        AssetManager assetManager = MyApplication.getInstance().getAssets();
        try {
            String[] fileListInSubfolder = assetManager.list("gif");
            if (fileListInSubfolder != null) {
                for (String string :
                        fileListInSubfolder) {
                    String[] abc = assetManager.list("gif/" + string);
                    if (abc != null && abc.length > 0 && abc[0] != null) {
                        String path = "file:///android_asset/gif/" + string + "/" + abc[0];
                        list.add(new ImageCategoryModel(path, string));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> getListImageCategoryItems(String type, String category) {
        List<String> list = new ArrayList<>();
        AssetManager assetManager = MyApplication.getInstance().getAssets();
        try {
            String[] folderSubs = assetManager.list(type + "/" + category);
            if (folderSubs != null) {
                for (String folderSub : folderSubs) {
                    if (!TextUtils.isEmpty(folderSub)) {
                        list.add(folderSub);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + System.nanoTime() + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static File createTextFile(Context context) throws IOException {
        // Create an image file name
        String imageFileName = "TXT_" + System.nanoTime() + "_";
        File storageDir = context.getExternalCacheDir();
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".txt",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = MyApplication.getInstance().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static boolean isImageFile(Uri path) {
        String mimeType = getMimeType(path);
        return mimeType != null && mimeType.startsWith("image") && !mimeType.toLowerCase().contains("gif");
    }

    public static boolean isVideoFile(Uri path) {
        String mimeType = getMimeType(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean isAudio(Uri path) {
        String mimeType = getMimeType(path);
        return mimeType != null && mimeType.startsWith("audio");
    }

    public static boolean isGif(Uri path) {
        String mimeType = getMimeType(path);
        return mimeType != null && mimeType.toLowerCase().contains("gif");
    }

    public static String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = MyApplication.getInstance().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static String getMimeType(String url) {
        if (url.contains("content://")) {
            return getMimeType(getUri(url));
        }
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getFileNameWithoutExtension(String s) {
        if (!s.contains(".")) {
            return s;
        } else {
            int last = s.lastIndexOf(".");
            return s.substring(0, last);
        }
    }

    public void appendAudio(String path1, String path2, String output, FileListener fileListener) {
        new BackgroundAsyncTask(path1, path2, output, fileListener).execute();
    }

    public class BackgroundAsyncTask extends AsyncTask<Void, Integer, Void> {
        String path1, path2, output;
        private FileListener fileListener;

        public BackgroundAsyncTask(String path1, String path2, String output, FileListener listener) {
            this.path1 = path1;
            this.path2 = path2;
            this.output = output;
            this.fileListener = listener;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void result) {
            fileListener.OnSuccess(output);
        }

        public void onProgressUpdate(Integer... values) {
        }

        public void onPreExecute() {
//            try {
//                myProgress = 0;
//                File f1 = new File(path1);
//                File f2 = new File(path1);
//                processtime = GetFileSize(f1, f2) * 5;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        private long GetFileSize(File f1, File f2) {
            try {
                if (!f1.exists() || !f2.exists()) {
                    return 0;
                }
                double bytes = (double) (f1.length() + f2.length());
                return (long) (bytes / 1024.0d);
            } catch (Exception e) {
                fileListener.OnFailed(e);
                return 0;
            }
        }

        public Void doInBackground(Void... params) {
            try {
                InputStream in = new FileInputStream(path1);
                byte[] buffer = new byte[1048576];
                OutputStream os = new FileOutputStream(new File(output), true);
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    os.write(buffer, 0, count);
                    os.flush();
                }
                in.close();
                InputStream in2 = new FileInputStream(path2);
                while (true) {
                    int count2 = in2.read(buffer);
                    if (count2 == -1) {
                        break;
                    }
                    os.write(buffer, 0, count2);
                    os.flush();
                }
                in2.close();
                os.close();
            } catch (FileNotFoundException e2) {
                fileListener.OnFailed(e2);
                e2.printStackTrace();
            } catch (IOException e) {
                fileListener.OnFailed(e);
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles())
                    deleteRecursive(child);
            fileOrDirectory.delete();
        }
    }

    public static void deleteRecursive(String fileOrDirectory) {
        File file = new File(fileOrDirectory);
        deleteRecursive(file);
    }

    public static void makeSureFileExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static MediaAddingModel.Type getFileType(Uri uri) {
        if (isImageFile(uri)) return MediaAddingModel.Type.ATTACH_IMAGE;
        if (isVideoFile(uri)) return MediaAddingModel.Type.ATTACH_VIDEO;
        if (isAudio(uri)) return MediaAddingModel.Type.AUDIO;
        if (isGif(uri)) return MediaAddingModel.Type.GIF;
        return MediaAddingModel.Type.ATTACH_FILE;
    }

    public interface FileListener {
        void OnSuccess(String filePath);

        void OnFailed(Exception e);
    }
}
