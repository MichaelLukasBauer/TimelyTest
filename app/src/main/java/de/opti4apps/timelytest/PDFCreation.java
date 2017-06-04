package de.opti4apps.timelytest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.util.List;

import de.opti4apps.timelytest.data.PDFGenerator;

public class PDFCreation extends AppCompatActivity {
    private Button b;
    ListView list;
    private String path;
    private File dir;

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfcreation);
        b = (Button) findViewById(R.id.button1);
        list = (ListView) findViewById(R.id.list);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                        if (isExternalStorageWritable()) {
                            if(checkPermissions()) {
                                generatePdfReport();
                            }
                        }
                }
            });
        }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.compareToIgnoreCase(state) == 0) {
            return true;
        }
        return false;
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    private void generatePdfReport() {
        try {
            String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/UniTyLab/PDF Files";
            dir = new File(storagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            new PDFGenerator(PDFCreation.this).createPDF(storagePath);
            loadListView();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                generatePdfReport();

            }
            return;
        }
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
