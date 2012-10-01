package jp.hmori.assettest;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import jp.hmori.assettest.util.StorageManager;
import android.os.Bundle;
import android.os.Process;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String DATA_DIR_NAME = "data";
	private static final boolean USE_EXTERNAL_STORAGE = true; //use inner storage.
	private StorageManager storageManager;
	
	private Button expandButton;
	private Button deleteButton;
	private TextView storageDataTextView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    	storageManager = new StorageManager(this, DATA_DIR_NAME, USE_EXTERNAL_STORAGE);

        expandButton = (Button)findViewById(R.id.expandButton);
        expandButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					expandDataAction();
				}
				return false;
			}
		});

        
        deleteButton = (Button)findViewById(R.id.deleteButton);
        deleteButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					deleteDataAction();
				}
				return false;
			}
		});
        
        storageDataTextView = (TextView)findViewById(R.id.storageDataTextView);
        displayDatas();
    }
    
	@Override
	protected void onStop() {
		super.onStop();
		deleteDataAction();
		android.os.Process.killProcess(Process.myPid());
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void expandDataAction() {
        try {
        	storageManager.initData();
        } catch (Throwable th) {
        	th.printStackTrace();
        }
        displayDatas();
		Toast.makeText(this, "finish expand", Toast.LENGTH_SHORT).show();
    }
    
    private void deleteDataAction() {
		try {
			storageManager.deleteData();
		} catch (Throwable th) {
        	th.printStackTrace();
		}
        displayDatas();
		Toast.makeText(this, "finish delete", Toast.LENGTH_SHORT).show();
    }


    private void displayDatas() {
    	TreeSet<String> set = new TreeSet<String>();
    	findFiles(storageManager.dataDir, set);

    	StringBuilder sb = new StringBuilder();
    	for (String path : set) {
        	sb.append(path);
        	sb.append("\n");
    	}
        storageDataTextView.setText(sb.toString());
    }
    
    private void findFiles(File dir, Set<String> set) {
    	File[] files = dir.listFiles();
    	if (files != null) {
        	for (File file : files) {
        		set.add(file.getPath());
        		if (file.isDirectory()) {
        			findFiles(file, set);
        		}
        	}
    	}
    }
}
