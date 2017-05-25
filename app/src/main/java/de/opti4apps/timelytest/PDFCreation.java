package de.opti4apps.timelytest;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.io.File;
import java.io.FileNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import com.itextpdf.text.DocumentException;
import java.util.ArrayList;

import de.opti4apps.timelytest.data.PDFGenerator;

public class PDFCreation extends AppCompatActivity {
    private Button b;
    ListView list;
    private String path;
    private File dir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfcreation);
        b = (Button) findViewById(R.id.button1);
        list = (ListView) findViewById(R.id.list);

        //creating new file path
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UniTyLab/PDF Files";
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    new PDFGenerator(getApplicationContext()).createPDF(path);
                    loadListView();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        loadListView();
//getting filesList from directory and display in listview
    }
    public void loadListView() {
        try {

            GetFiles("/sdcard/UniTyLab/PDF Files");
            if (filenames.size() != 0)
                list.setAdapter(new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, filenames));

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // Clicking on items
                    File file = filesList.get(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<String> filenames;
    public ArrayList<File> filesList;
    public void GetFiles(String DirectoryPath) {
        filenames = new ArrayList<>();
        filesList = new ArrayList<>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            filenames.add(files[i].getName());
            filesList.add(files[i]);
        }
    }
}
