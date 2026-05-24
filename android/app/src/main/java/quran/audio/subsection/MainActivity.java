package quran.audio.subsection;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(SafWriterPlugin.class);  // ← this line BEFORE super
        super.onCreate(savedInstanceState);
    }
}