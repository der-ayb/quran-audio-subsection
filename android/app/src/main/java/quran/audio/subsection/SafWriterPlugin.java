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

@CapacitorPlugin(name = "SafWriter")
public class SafWriterPlugin extends Plugin {

    @PluginMethod
    public void writeFile(PluginCall call) {
        String folderUri = call.getString("folderUri");
        String fileName  = call.getString("fileName");
        String base64    = call.getString("data");
        String mimeType  = call.getString("mimeType", "audio/wav");

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
}