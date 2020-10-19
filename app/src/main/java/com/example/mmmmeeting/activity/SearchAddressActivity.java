package com.example.mmmmeeting.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.view.subAddress;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchAddressActivity extends AppCompatActivity {

    Button search;
    EditText address;

    // 공공데이터 포털, 도로명 주소 자료 API 키
    String key="9sQrn%2BLJVkLWA9IjevFFxgzbIzondfA7i7DDYdaOioStlNxjDZkdHQ9KCDEQ%2FxSUjav04A7zzo60de%2Bp4FV%2FSA%3D%3D";

    // 검색 결과로 나오는 모든 주소 (도로명, 지번, 우편)들의 리스트
    private ArrayList<ArrayList<String>> addressGroup = null;

    // 결과로 나오는 각각의 (도로명, 지번, 우편) 리스트
    private ArrayList<String> addresslset = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);

        addressGroup = new ArrayList<ArrayList<String>>();

        search = findViewById(R.id.getAddress);
        address = findViewById(R.id.editAddress);

        search.setOnClickListener(this::mOnClick);
    }

    //Button을 클릭했을 때 자동으로 호출되는 callback method....
    private void mOnClick(View v){
        switch( v.getId() ){
            case R.id.getAddress:

                //Android 4.0 이상 부터는 네트워크를 이용할 때 반드시 Thread 사용해야 함
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            getXmlData();//아래 메소드를 호출하여 XML data를 파싱해서 String 객체로 얻어오기
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        //UI Thread(Main Thread)를 제외한 어떤 Thread도 화면을 변경할 수 없기때문에
                        //runOnUiThread()를 이용하여 UI Thread가 TextView 글씨 변경하도록 함
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 서브 레이아웃(결과 나올 레이아웃) 초기화
                                LinearLayout sublayer = (LinearLayout) findViewById(R.id.sublayer);
                                sublayer.removeAllViews();

                                // 주소 묶음 사이즈 만큼 (도로명, 우편, 지번 묶음 개수만큼 반복)
                                for(int i =0; i<addressGroup.size(); i++) {
                                    Log.d("address test", " " + i);
                                    // 주소 아이템 레이아웃 설정
                                    subAddress sublayout = new subAddress(getApplicationContext());
                                    final TextView tvPost = (TextView) sublayout.findViewById(R.id.postno);
                                    final TextView tvRoad = (TextView) sublayout.findViewById(R.id.addr_road);
                                    final TextView tvJibun = (TextView) sublayout.findViewById(R.id.addr_jibun);

                                    // 도로명, 우편, 지번 각각에 대한 결과 텍스트로 반환
                                    for (int j = 0; j < addresslset.size(); j++) {
                                        Log.d("address test", " " + addressGroup.get(i).get(j));

                                        switch (j) {
                                            case 0:
                                                tvPost.setText(addressGroup.get(i).get(j));
                                                break;
                                            case 1:
                                                tvRoad.setText(addressGroup.get(i).get(j));
                                                break;
                                            case 2:
                                                tvJibun.setText(addressGroup.get(i).get(j));
                                                break;
                                        }
                                    }

                                    // 각 주소 아이템 클릭시 동작 설정
                                    sublayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Log.d("address test", "sub Click");

                                            //Toast.makeText(SearchAddressActivity.this, "Click Listener", Toast.LENGTH_SHORT).show();
                                            // MemberInitActivity 주소 정보 전달
                                            Intent intent = new Intent(SearchAddressActivity.this, MemberInitActivity.class);

                                            intent.putExtra("road", tvRoad.getText());
                                            intent.putExtra("post", tvPost.getText());
                                            intent.putExtra("jibun", tvJibun.getText());

                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                                    // 하나의 아이템 (도로명, 우편, 지번) 텍스트 뷰 설정후
                                    // 해당 레이아웃 서브 레이아웃에 추가가
                                   sublayer.addView(sublayout);
                                }
                            }
                        });
                    }
                }).start();
                break;
        }

    }//mOnClick method..


    //XmlPullParser를 이용하여 Naver 에서 제공하는 OpenAPI XML 파일 파싱하기(parsing)
    private void getXmlData() throws UnsupportedEncodingException {

        Log.d("address test","getXml");

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
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기, 한글 사용 위해 utf-8 설정

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();

            // 오픈 API로 받은 xml 파일 파싱 (구분, 분리)
            while( eventType != XmlPullParser.END_DOCUMENT ){
                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();//테그 이름 얻어오기

                        if(tag.equals("item")) ;// 첫번째 검색결과
                        else if(tag.equals("zipNo")){
                            xpp.next();
                            addresslset = new ArrayList<String>();
                            addresslset.add(xpp.getText());
                        }
                        else if(tag.equals("rnAdres")){
                            xpp.next();
                            addresslset.add(xpp.getText());
                        }
                        else if(tag.equals("lnmAdres")){
                            xpp.next();
                            addresslset.add(xpp.getText());
                            addressGroup.add(addresslset);
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag= xpp.getName(); //테그 이름 얻어오기
                        if(tag.equals("item"));// 첫번째 검색결과종료
                        break;
                }

                eventType= xpp.next();
            }

        } catch (Exception e) {
            Log.d("address test", "error : " + e);
        }

        return;

    }//getXmlData method....

}//MainActivity class..
