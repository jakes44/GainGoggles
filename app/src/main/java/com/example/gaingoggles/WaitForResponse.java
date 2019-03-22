package com.example.gaingoggles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.UUID;

public class WaitForResponse extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_response);
        Uri uri = Uri.parse(getIntent().getStringExtra(MainActivity.EXTRA_URI));
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File f = new File(getCacheDir().getPath() + UUID.randomUUID().toString() + ".mp4");
            copyInputStreamToFile(is,f);
            sendVideo(f);
        }catch (Exception e){
            System.out.println("ERROR OPENING URI STREAM");
            e.printStackTrace();
        }

    }

    private void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendVideo(File f) {

        new SendVideoClass(getApplicationContext()).execute(f);
    }

}

class SendVideoClass extends AsyncTask<File, Void, Response> {

    Context context;

    public SendVideoClass(Context c){
        this.context = c;
    }

    @Override
    protected Response doInBackground(File... vid) {
        System.out.println("FILE: " + vid[0].getPath());

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", vid[0].getPath(),
                        RequestBody.create(MediaType.parse("video/mp4"),vid[0]))
                .build();
        Request request = new Request.Builder().url("http://api.gaingoggles.com/upload").post(formBody).build();

        try {
            Response response = client.newCall(request).execute();
//            System.out.println(response.message());
//            System.out.println("Success: " + response.body().string());

            return response;

        }catch(Exception e){
            System.out.println("ERROR");
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(Response r){
        new DownloadVideoClass(context).execute(r);
    }


}

class DownloadVideoClass extends AsyncTask<Response, Void,File> {

        Context context;

        public DownloadVideoClass(Context c){
            this.context = c;
        }

        @Override
        protected File doInBackground(Response... r) {
            OkHttpClient client = new OkHttpClient();

            String task_id = null;
            try {
                task_id = r[0].body().string();
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
            String r_string = "PENDING";

            while(!r_string.equals("SUCCESS")){


                Request request = new Request.Builder().url("http://api.gaingoggles.com/status/"+task_id).get().build();

                try {
                    Response response = client.newCall(request).execute();
                    System.out.println(response.message());
                    r_string = response.body().string();
                    Thread.sleep(20000);
                }catch(Exception e){
                    System.out.println("ERROR");
                    e.printStackTrace();
                    break;
                }
            }

            System.out.println("WERE DONE");

            Request request = new Request.Builder().url("http://api.gaingoggles.com/download").get().build();

            try {
                Response response = client.newCall(request).execute();
                System.out.println(response.message());
                File f = new File(context.getCacheDir(), "newvid.mp4");
                copyInputStreamToFile(response.body().byteStream(), f);
                return f;
            }catch(Exception e){
                System.out.println("ERROR");
                e.printStackTrace();
                return null;
            }
    }

    @Override
    public void onPostExecute(File f){
            Intent view = new Intent(context, WorkoutActivity.class);
            view.putExtra(MainActivity.EXTRA_FILE, f.getAbsolutePath());
            context.startActivity(view);
    }

    private void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}