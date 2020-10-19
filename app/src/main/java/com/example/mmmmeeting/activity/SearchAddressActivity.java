package com.example.mmmmeeting.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mmmmeeting.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class SearchAddressActivity extends AppCompatActivity {

    Button search;
    TextView result;
    EditText address;

    XmlPullParser xpp;

    String data;
    String key="9sQrn%2BLJVkLWA9IjevFFxgzbIzondfA7i7DDYdaOioStlNxjDZkdHQ9KCDEQ%2FxSUjav04A7zzo60de%2Bp4FV%2FSA%3D%3D";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);

        search = findViewById(R.id.getAddress);
        result = findViewById(R.id.Addressresult);
        address = findViewById(R.id.editAddress);

        search.setOnClickListener(this::mOnClick);
    }

    //Button을 클릭했을 때 자동으로 호출되는 callback method....
    public void mOnClick(View v){
        switch( v.getId() ){
            case R.id.getAddress:

                //Android 4.0 이상 부터는 네트워크를 이용할 때 반드시 Thread 사용해야 함
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            data= getXmlData();//아래 메소드를 호출하여 XML data를 파싱해서 String 객체로 얻어오기
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        //UI Thread(Main Thread)를 제외한 어떤 Thread도 화면을 변경할 수 없기때문에
                        //runOnUiThread()를 이용하여 UI Thread가 TextView 글씨 변경하도록 함
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                result.setText(data); //TextView에 문자열  data 출력
                            }
                        });
                    }
                }).start();
                break;
        }
    }//mOnClick method..


    //XmlPullParser를 이용하여 Naver 에서 제공하는 OpenAPI XML 파일 파싱하기(parsing)
    String getXmlData() throws UnsupportedEncodingException {

        Log.d("address test","getXml");

        StringBuffer buffer=new StringBuffer();

        String str= address.getText().toString();//EditText에 작성된 Text얻어오기
        String location = URLEncoder.encode(str,"UTF-8");//한글의 경우 인식이 안되기에 utf-8 방식으로 encoding..

        //String queryUrl="http://openapi.epost.go.kr/postal/retrieveNewAdressAreaCdService/retrieveNewAdressAreaCdService/getNewAddressListAreaCd?ServiceKey=cQUBpAa38YT1HC3BX0G9HeM0GhdmzVb3xEW9vTTRytKWEeMJXbGZCdUsQElWj8IxaOOYSEAVWTwbh%2B%2BUoxL%2FZg%3D%3D&searchSe=road&srchwrd=%EB%8C%80%ED%8F%AC%EA%B8%B874";
        String queryUrl="http://openapi.epost.go.kr/postal/retrieveNewAdressAreaCdService/retrieveNewAdressAreaCdService/getNewAddressListAreaCd"  //요청 URL
                + "?searchSe=road"
                + "&srchwrd=" + location
                + "&ServiceKey=" + key;

        Log.d("address test", queryUrl);

        try {
            Log.d("address test", "try");

            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();

            while( eventType != XmlPullParser.END_DOCUMENT ){
                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();//테그 이름 얻어오기

                        if(tag.equals("item")) ;// 첫번째 검색결과
                        else if(tag.equals("zipNo")){
                            buffer.append("우편번호 : ");
                            xpp.next();
                            buffer.append(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n"); //줄바꿈 문자 추가
                        }
                        else if(tag.equals("rnAdres")){
                            buffer.append("지번주소 : ");
                            xpp.next();
                            buffer.append(xpp.getText());//category 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");//줄바꿈 문자 추가
                        }
                        else if(tag.equals("lnmAdres")){
                            buffer.append("도로명주소 :");
                            xpp.next();
                            buffer.append(xpp.getText());//description 요소의 TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");//줄바꿈 문자 추가
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag= xpp.getName(); //테그 이름 얻어오기

                        if(tag.equals("item")) buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                        break;
                }

                eventType= xpp.next();
            }

        } catch (Exception e) {
            Log.d("address test", "error : " + e);
        }

        buffer.append("파싱 끝\n");
        return buffer.toString();//StringBuffer 문자열 객체 반환

    }//getXmlData method....

}//MainActivity class..
