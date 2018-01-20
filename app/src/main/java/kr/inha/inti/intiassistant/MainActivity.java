package kr.inha.inti.intiassistant;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    SpeechRecognizer mRecognizer;
    TextView textView;
    TextView textView2;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private long lastTimeBackPressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(recognitionListener);

        textView2 = (TextView) findViewById(R.id.result);
        textView = (TextView) findViewById(R.id.voiceText);
        //URL 설정
        //String url = "http://39.115.148.109:5000/";
        String url = "http://39.115.148.109:5000/request/google-assistant";

        //AsyncTask를 통해 HttpURLConnection 수행
        downloadTask processingThread = new downloadTask(url, null);//AsyncTask 객체 생성
        processingThread.execute(); //일반적, 여러 AsyncTask 객체를 만들어 다수의 작업 수행, execute()가 호출된 순서대로 처리
        //processingThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arr); //병렬처리를 위한 수행법, 여러 AsyncTask 객체를 만들어 다수의 작업 수행, 순서에 상관없이 동시처리

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO
                );
            }
        } // if 끝


        Button button = (Button) findViewById(R.id.voice);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecognizer.startListening(intent);
            }
        });

    } // onCreate 끝

    // 두 번 누르면 꺼짐
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
            finish();
            return;
        }
        Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        lastTimeBackPressed = System.currentTimeMillis();

    }

    // 음성인식하기
    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int i) {
            textView.setText("시간이 초과되었습니다");

        }

        @Override
        public void onResults(Bundle bundle) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = bundle.getStringArrayList(key);

            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);//rs에 텍스트 데이터 받아옴

            textView.setText(rs[0]);
            sendObject(rs[0]); //JSONObject로 보내기 위해 호출
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };

    private void sendObject(String s){
        JSONObject jsonObject = new JSONObject();
        //JSONArray jsonArray = new JSONArray(); //이런 형식도 존재함
        try{
            jsonObject.put("textVoice", s);//textVoice가 key, s가 value
            //textView2.setText(jsonObject.getString("textVoice"));//이건 그 내용 출력
        }catch (JSONException e){
            e.printStackTrace();
        }
       // receiveObject(jsonObject);
    }

    private void receiveObject(JSONObject data){ //JSONObject를 받아오는 것
       // recyclerView.setVisibility(View.GONE);
        //objectResultLo.setVisibility(View.VISIBLE);
        try{
            textView.setText("reponse : " + data.getString("reponse")); //받아와서 key는 response로 지정하고 getString으로 읽음
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private class downloadTask extends AsyncTask<Void, Void, String>{

        private String url;
        private ContentValues values;

        public downloadTask(String url, ContentValues values){
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values);

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textView2.setText(s);//이건 그 내용 출력
        }

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onCancelled(String result) {
//            super.onCancelled(result);
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... values) {
//            super.onProgressUpdate(values);
//        }
    }
}
