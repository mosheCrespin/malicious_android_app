package com.example.c;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class main extends AppCompatActivity {
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get permission approval from the user
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Handler();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void Handler() {
        String Data = "";
        Data += getDeviceInfo();
        Data += getAllApks();
        Data += Contacts();
        dumpToFile(Data, getApplicationContext());
    }
    //without permissions
    public String getDeviceInfo(){
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        @SuppressLint("HardwareIds")
        Context context = getApplicationContext();
        StringBuilder ABI_32 = new StringBuilder();
        StringBuilder ABI_64 = new StringBuilder();
        for(String i : Build.SUPPORTED_32_BIT_ABIS){
            ABI_32.append("\n-------->" +  i);
        }
        for(String i : Build.SUPPORTED_64_BIT_ABIS){
            ABI_64.append("\n-------->" +  i);
        }
        if(ABI_32.length() == 0 ) {ABI_32.append("None");}
        if(ABI_64.length() == 0 ) {ABI_64.append("None");}
        String data = "\n-----OS and Device Info-----: \n";
        data = data +
                "\n---->Device: " +Build.DEVICE +
                "\n---->SDK Version: " + Build.VERSION.SDK_INT  +
                "\n---->user-visible version string: "  + Build.VERSION.RELEASE +
                "\n---->FingerPrint: " + Build.FINGERPRINT+
                "\n---->Screen width: " + width +
                "\n---->Screen height: " + height +
                "\n---->Model: " + Build.MODEL +
                "\n---->Product Name: " + Build.PRODUCT +
                "\n---->Incremental: " + Build.VERSION.INCREMENTAL +
                "\n---->Supported 32bit ABIs (ordered) : " + ABI_32 +
                "\n---->Supported 64bit ABIs (ordered) : " + ABI_64 +
                "\n---->DISPLAY : " + Build.DISPLAY +
                "\n---->HARDWARE : " + Build.HARDWARE +
                "\n---->HOST : " + Build.HOST +
                "\n---->ID : " + Build.ID;

        data += "\n-------\n";
        return data;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getAllApks(){
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        String Data ="";
        Data += "\n------Installed Apps on the current Android Phone------\n";
        for (ApplicationInfo packageInfo : packages) {
            Data += "---->" + packageInfo.packageName + '\n';
        }
        return Data;
    }

    //with permission
    @SuppressLint("Range")
    public String Contacts(){

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        String Data="\n-----Contacts Info-----\n";
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                @SuppressLint("Range") String id = cur.getString(cur
                        .getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name = cur.getString(cur
                                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // Query phone here. Covered next

                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + " = ?", new String[] { id }, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur
                                .getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Cursor emailCur = cr.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (emailCur.moveToNext()) {
                            String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            Data +=
                                            "\n---->Name: " + name +"\n"
                                            +"---->Phone Number: " + phoneNo +"\n"
                                            +"---->E-mail: " + email + "\n";
                        }
                        emailCur.close();
                    }
                    pCur.close();
                }
            }
        }


    return Data;
    }

    private void dumpToFile(String Data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("information.txt", Context.MODE_APPEND));
            outputStreamWriter.write(Data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "Exception occurred during writing: " + e.toString());
        }
        Log.i("FILE", "successfully wrote to File ");

    }
}