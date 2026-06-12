package quran.audio.subsection;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.OutputStream;

import com.getcapacitor.JSArray;

@CapacitorPlugin(name = "SafWriter")
public class SafWriterPlugin extends Plugin {

    @PluginMethod
    public void writeFile(PluginCall call) {
        String folderUri = call.getString("folderUri");
        String fileName = call.getString("fileName");
        String base64 = call.getString("data");
        String mimeType = call.getString("mimeType", "audio/wav");

        if (folderUri == null || fileName == null || base64 == null) {
            call.reject("Missing folderUri, fileName, or data");
            return;
        }

        try {
            Uri treeUri = Uri.parse(folderUri);
            DocumentFile dir = DocumentFile.fromTreeUri(getContext(), treeUri);

            if (dir == null || !dir.exists()) {
                call.reject("Folder not found or permission lost");
                return;
            }

            DocumentFile existing = dir.findFile(fileName);
            if (existing != null && existing.exists()) {
                existing.delete();
            }

            DocumentFile newFile = dir.createFile(mimeType, fileName);
            if (newFile == null) {
                call.reject("Could not create file");
                return;
            }

            byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            ContentResolver resolver = getContext().getContentResolver();
            OutputStream out = resolver.openOutputStream(newFile.getUri());
            out.write(bytes);
            out.flush();
            out.close();

            JSObject result = new JSObject();
            result.put("uri", newFile.getUri().toString());
            call.resolve(result);

        } catch (Exception e) {
            call.reject("Write failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void findAudioFiles(PluginCall call) {
        String folderUri = call.getString("folderUri");
        JSArray results = new JSArray();

        try {
            Uri treeUri = Uri.parse(folderUri);
            DocumentFile dir = DocumentFile.fromTreeUri(getContext(), treeUri);
            scanAudio(dir, results);

            JSObject ret = new JSObject();
            ret.put("files", results);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Scan failed: " + e.getMessage());
        }
    }

    private void scanAudio(DocumentFile dir, JSArray results) {
        if (dir == null || !dir.isDirectory()) return;

        for (DocumentFile file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanAudio(file, results); // recursive
            } else {
                String name = file.getName();
                if (name != null && isAudioFile(name)) {
                    JSObject f = new JSObject();
                    f.put("name", name);
                    f.put("uri", file.getUri().toString());
                    f.put("size", file.length());
                    results.put(f);
                }
            }
        }
    }

    private boolean isAudioFile(String name) {
        String lower = name.toLowerCase();
        // return lower.endsWith(".ayb");
        return lower.endsWith(".wav") || lower.endsWith(".mp3")
            || lower.endsWith(".aac") || lower.endsWith(".ogg")
            || lower.endsWith(".flac") || lower.endsWith(".m4a");
    }

    @PluginMethod
    public void deleteFile(PluginCall call) {
        String fileUri = call.getString("uri");
        try {
            Uri uri = Uri.parse(fileUri);
            DocumentFile file = DocumentFile.fromSingleUri(getContext(), uri);
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                JSObject ret = new JSObject();
                ret.put("deleted", deleted);
                call.resolve(ret);
            } else {
                call.reject("File not found");
            }
        } catch (Exception e) {
            call.reject("Delete failed: " + e.getMessage());
        }
    }
}