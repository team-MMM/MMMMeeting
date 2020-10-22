package com.example.mmmmeeting.activity;

/*
 * 검색된 주소의 경로를 출력하는 클래스
 * (중간지점 탐색에서도 쓰이려면 목적지 변수를 하나 정해서 다른 클래스에서 사용할 수 있도록 해야함)
 */

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.mmmmeeting.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static android.view.View.VISIBLE;

public class DirectionActivity extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private RelativeLayout rl_container; //모든 경로 정보들이 담길 공간 (추천경로, 다른 경로 등)
    private RelativeLayout rl_another_view; //다른 경로들이 들어갈 공간
    private RelativeLayout rl_route_view;  // 추천 경로 보여주는 공간
    private LinearLayout ll_detail_course_container; //(경로 눌렀을 때)자세한 경로를 보여줌
    private LinearLayout ll_traffic_detail_route_container; //디테일한 경로 동적 생성해서 추가
    private LinearLayout ll_flow_container; //다른 경로들의 내용
    private FlowLayout fl_route; //추천경로 내용
    private FlowLayout fl_another_route; //다른경로들 동적추가 (추천 경로는 하나지만 그 외 경로들은 몇개인지 미리 알 수 없기 때문에 동적생성)
    private ImageButton goback;

    private String getOverview = null;


    private Marker start_m; // 시작 마커
    private Marker end_m; // 도착 마커

    private int fl_count = 0;
    private int R_fl_count = 0;

    private GoogleMap map;

    private GpsTracker gpsTracker;

    private static final String TAG = "directions";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    //private ArrayList<ArrayList<SampleItem>> getshortInfo = new ArrayList<ArrayList<SampleItem>>(); //간단한 경로값 (ex. 도보 5분 > 버스 3분>...)
    //private ArrayList<ArrayList<SampleItem>> getlongInfo = new ArrayList<ArrayList<SampleItem>>(); //자세한 경로
    //private String[] getdurArray; //현재 경로의 이동시간


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)


    private String str_url = null;
    private String str_info = null;

    private LatLng depart; //출발지

    private String arriv;//SearchMapActivity에서 검색했던 목적지를 받아오는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_direction_map);

        initAllComponent();

        //위치상태 체크?
        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        } else {

            checkRunTimePermission();
        }


        Intent a = getIntent();
        arriv = a.getStringExtra("address");


        gpsTracker = new GpsTracker(DirectionActivity.this);

        double curr_latitude = gpsTracker.getLatitude();
        double curr_longtitude = gpsTracker.getLongitude();
        depart = new LatLng(curr_latitude, curr_longtitude);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //parse 시작!
        showInfo(curr_latitude, curr_longtitude, arriv);


        //반복문을 돌면서 경로를 출력할거임
        for (int i = 0; i < shortInfo.size(); i++) {
            ArrayList<SampleItem> inner = shortInfo.get(i);


            //다른 경로들 추가 생성 (추천 경로 외의 경로들 동적 생성해서 레이아웃에 추가한다.)
            fl_another_route = new FlowLayout(DirectionActivity.this);
            FlowLayout.LayoutParams param = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            param.bottomMargin = 100;
            fl_another_route.setLayoutParams(param);

            fl_another_route.setOrientation(FlowLayout.HORIZONTAL);
            fl_another_route.setBackgroundColor(Color.WHITE);
            ll_flow_container.addView(fl_another_route);


            TextView time = findViewById(R.id.during_time);
            time.setText(durArray[0]);

            if (i > 0) {

                ///////이동시간 텍스트뷰 동적 생성//////////
                TextView time_t = new TextView(this);
                time_t.setText(durArray[i]);

                time_t.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) time_t.getLayoutParams();
                params.gravity = Gravity.RIGHT;
                time_t.setLayoutParams(params);
                time_t.setTextSize(28);
                time_t.setPadding(5, 0, 5, 0);

                ll_flow_container.addView(time_t);
            }

            for (int j = 0; j < inner.size(); j++) {
                /*
                 * method_view의 인자로 다음을 넘겨준다.
                 * 1. 이미지 번호 ( 0:walk , 1:bus, 2:subway )
                 * 2. 경로 내용
                 * 3. 몇번째 경로인가
                 */
                method_view(inner.get(j).getImg_num(), inner.get(j).getText(), i);
            }

        }

        rl_container.setVisibility(VISIBLE);

    }

    private void initAllComponent() {
        rl_container = findViewById(R.id.rl_container);
        fl_route = findViewById(R.id.fl_route);
        rl_route_view = findViewById(R.id.rl_route_view);
        rl_another_view = findViewById(R.id.rl_another_view);
        ll_flow_container = findViewById(R.id.ll_flow_container);
        ll_detail_course_container = findViewById(R.id.ll_detail_course_container);
        ll_traffic_detail_route_container = findViewById(R.id.ll_traffic_detail_route_container);
        goback = findViewById(R.id.goback);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        checkRunTimePermission();
        MarkerOptions marker1 = new MarkerOptions();
        marker1.position(depart).title("출발")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        map.addMarker(marker1);


        Point p = getPointFromGeoCoder(arriv); //주소를 위치값으로 변환
        LatLng arrival = new LatLng(p.X_value(), p.Y_value());

        MarkerOptions marker2 = new MarkerOptions();
        marker2.position(arrival).title("도착")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        map.addMarker(marker2);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(arrival, 10));

    }

    void checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(DirectionActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }
    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                //startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                } else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(DirectionActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }


    ////////////////////////
    //URL연결, JSON 받아오기///
    ////////////////////////
    public class Task extends AsyncTask<String, Void, String> {
        private String str, receiveMsg;

        @Override
        protected String doInBackground(String... parms) {
            URL url = null;

            try {
                url = new URL(str_url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);

                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    reader.close();
                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }

    public void showInfo(double latitude, double longtitude, String arrival) {

        String apiKey = getString(R.string.api_key);
        Intent a = getIntent();

        String str_origin = latitude + "," + longtitude;
        //String str_origin = currentPosition.latitude+","+currentPosition.longitude;
        System.out.println("현재위치는 : " + str_origin);
        String str_dest = arrival;

        str_url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + str_origin + "&destination=" + str_dest + "&mode=transit" +
                "&alternatives=true&language=ko&key=" + apiKey;


        String resultText = "값이 없음";

        try {
            resultText = new Task().execute().get();

            parse(resultText); //파싱 시작

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("str_info : " + str_info);
    }


    //화면에 경로가 보여지도록 하는 메소드
    Drawable img;

    public void method_view(int img_num, String contents, int i) {
        Resources res = getResources();

        switch (img_num) {
            case 0:
                img = ResourcesCompat.getDrawable(res, R.drawable.walk, null);
                break;
            case 1:
                img = ResourcesCompat.getDrawable(res, R.drawable.bus, null);
                break;
            case 2:
                img = ResourcesCompat.getDrawable(res, R.drawable.subway, null);
                break;
        }


        //경로를 선택했을 때 나타나는 ClickListener
        final int no = i;
        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_traffic_detail_route_container.removeAllViews();
                ll_detail_course_container.setVisibility(VISIBLE);

                ArrayList<SampleItem> outer = new ArrayList<SampleItem>();
                ArrayList<SampleItem> inner = new ArrayList<SampleItem>();

                inner = shortInfo.get(no); //간단한 경로 정보
                outer = longInfo.get(no);  // 세부적인 경로 정보

                //반복문을 돌면서 경로정보를 받아서 layout에 추가하여 보여주도록 한다.
                for (int i = 0; i < outer.size(); i++) {

                    TextView tv_method_course = new TextView(DirectionActivity.this);
                    tv_method_course.setText(inner.get(i).getText());
                    tv_method_course.setTextSize(20);
                    tv_method_course.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) tv_method_course.getLayoutParams();
                    params1.gravity = Gravity.LEFT;
                    tv_method_course.setLayoutParams(params1);
                    tv_method_course.setPadding(25, 25, 25, 25);
                    ll_traffic_detail_route_container.addView(tv_method_course);

                    TextView tv_detail_course = new TextView(DirectionActivity.this);
                    tv_detail_course.setText(outer.get(i).getText());
                    tv_detail_course.setTextSize(18);
                    tv_detail_course.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) tv_detail_course.getLayoutParams();
                    params2.gravity = Gravity.LEFT;
                    params2.setMargins(0, 0, 0, 30);
                    tv_detail_course.setLayoutParams(params2);
                    tv_detail_course.setPadding(5, 5, 20, 15);
                    ll_traffic_detail_route_container.addView(tv_detail_course);

                    ArrayList<LatLng> path_points = decodePolyPoints(getPolyline[no][i]); // 폴리라인 포인트 디코드 후 ArrayList에 저장

                    Polyline line = null;

                    if (line == null) {
                        line = map.addPolyline(new PolylineOptions()
                                .color(Color.rgb(58, 122, 255))
                                .geodesic(true)
                                .addAll(path_points));
                    } else {
                        line.remove();
                        line = map.addPolyline(new PolylineOptions()
                                .color(Color.rgb(58, 122, 255))
                                .geodesic(true)
                                .addAll(path_points));
                    }

                    if (goingE_lat[no][i] != null) {

                        double gelatitude = Double.parseDouble(goingE_lat[no][i]);
                        double gelngtitude = Double.parseDouble(goingE_lng[no][i]);
                        String Transit_n = TransitName[no][i];
                        String next_Transit_n = null;

                        if (i + 1 < outer.size()) {
                            if (TransitName[no][i + 1] != null)
                                next_Transit_n = TransitName[no][i + 1];
                        }

                        LatLng GoingE = new LatLng(gelatitude, gelngtitude);

                        if (Transit_n == null) {
                            Transit_n = "도보";
                            if (next_Transit_n != null) {
                                marker_arr[1][i] = map.addMarker(new MarkerOptions().position(GoingE).title(Transit_n + " 후, " + next_Transit_n + " 승차"));
                            } else {
                                if (i == outer.size() - 1) {
                                    marker_arr[1][i] = map.addMarker(new MarkerOptions().position(GoingE).title(Transit_n + " 후, 도착"));
                                }
                                marker_arr[1][i] = map.addMarker(new MarkerOptions().position(GoingE).title(Transit_n));
                            }
                        } else {
                            if (i == outer.size() - 1) {
                                marker_arr[1][i] = map.addMarker(new MarkerOptions().position(GoingE).title(Transit_n + " 하차 후, 도착"));
                            }
                            marker_arr[1][i] = map.addMarker(new MarkerOptions().position(GoingE).title(Transit_n + " 하차"));
                        }

                        onMapReady(map);
                    }

                    if (goingS_lat[no][i] != null) {

                        double gslatitude = Double.parseDouble(goingS_lat[no][i]);
                        double gslngtitude = Double.parseDouble(goingS_lng[no][i]);
                        String Transit_n = TransitName[no][i];
                        String prev_Transit_n = null;
                        if (i != 0) {
                            prev_Transit_n = TransitName[no][i - 1];
                        }

                        LatLng GoingS = new LatLng(gslatitude, gslngtitude);

                        if (Transit_n == null) {
                            Transit_n = "도보";
                            // 도보 전 지하철 하차, 버스 하차 등을 표시할 수 있도록
                            if (prev_Transit_n != null) {
                                marker_arr[0][i] = map.addMarker(new MarkerOptions().position(GoingS).title(prev_Transit_n + "하차 후, " + Transit_n));
                            } else {
                                marker_arr[0][i] = map.addMarker(new MarkerOptions().position(GoingS).title(Transit_n));
                            }
                        } else {
                            marker_arr[0][i] = map.addMarker(new MarkerOptions().position(GoingS).title(Transit_n + " 승차"));
                        }

                        onMapReady(map);
                    }
                }

                double alatitude = Double.parseDouble(arrival_lat);
                double alngtitude = Double.parseDouble(arrival_lng);

                LatLng End = new LatLng(alatitude, alngtitude);

                end_m = map.addMarker(new MarkerOptions().position(End).title("도착"));

                onMapReady(map);

                map.moveCamera(CameraUpdateFactory.newLatLng(End));
                map.animateCamera(CameraUpdateFactory.zoomTo(15));


                //뒤로가기 버튼 (다시 간단한 경로 보여줌)
                goback.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ll_detail_course_container.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };

        TextView ith_route = null;
        if (i == 0) {//j가 0이라면 추천경로(최단시간)
            ith_route = new TextView(DirectionActivity.this);
            ith_route.setText(contents);
            ith_route.setTextSize(22);

            int h = 130;
            int w = 130;
            img.setBounds(0, 0, w, h);
            ith_route.setCompoundDrawables(img, null, null, null);


            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_VERTICAL;
            ith_route.setLayoutParams(lp);

            R_fl_count += 1;
            fl_route.addView(ith_route);
            fl_route.setOnClickListener(click);
            fl_route.setPadding(15, 20, 15, 20);
            rl_route_view.setVisibility(View.VISIBLE);

        } else if (i > 0) { //다른 경로

            ith_route = new TextView(DirectionActivity.this);
            ith_route.setText(contents);
            ith_route.setTextSize(22);

            int h = 130;
            int w = 130;
            img.setBounds(0, 0, w, h);
            ith_route.setCompoundDrawables(img, null, null, null);

            ith_route.setGravity(Gravity.CENTER_VERTICAL);

            fl_count += 1;
            fl_another_route.addView(ith_route);
            fl_another_route.setOnClickListener(click);
            rl_another_view.setPadding(15, 20, 15, 20);
            rl_another_view.setVisibility(View.VISIBLE);
        }

    }

    public static ArrayList<LatLng> decodePolyPoints(String encodedPath) {
        int len = encodedPath.length();

        final ArrayList<LatLng> path = new ArrayList<LatLng>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }

        return path;
    }
    //////////////////텍스트로 받은 위치 정보를 좌표로 변환하기 위해서 생성/////////////////////
    class Point {
        // 위도
        public double x;
        // 경도
        public double y;
        public String addr;
        // 포인트를 받았는지 여부
        public boolean havePoint;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("x : ");
            builder.append(x);
            builder.append(" y : ");
            builder.append(y);
            builder.append(" addr : ");
            builder.append(addr);

            return builder.toString();
        }

        public double X_value() {
            return x;
        }

        public double Y_value() {
            return y;
        }
    }

    private Point getPointFromGeoCoder(String addr) {
        Point point = new Point();
        point.addr = addr;

        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress;

        try {
            listAddress = geocoder.getFromLocationName(addr, 1);
        } catch (IOException e) {
            e.printStackTrace();
            point.havePoint = false;
            return point;
        }

        if (listAddress.isEmpty()) {
            point.havePoint = false;
            return point;
        }

        point.havePoint = true;
        point.y = listAddress.get(0).getLongitude();
        point.x = listAddress.get(0).getLatitude();
        return point;
    }


    //출발주소, 도착주소, 총 이동시간
    private String Dur, str_Start, str_End;

    private ArrayList<ArrayList<SampleItem>> longInfo;
    private ArrayList<SampleItem> long_inner;

    private ArrayList<ArrayList<SampleItem>> shortInfo;
    private ArrayList<SampleItem> short_inner;


    private String step = null;
    private String shortText = null;
    private String durArray[];

    private String departure_lat = null;
    private String departure_lng = null;
    private String[][] goingS_lat;
    private String[][] goingS_lng;
    private String[][] goingE_lat;
    private String[][] goingE_lng;
    private String arrival_lat = null;
    private String arrival_lng = null;
    private String[][] TransitName;
    private String[][] getPolyline;
    private String[][] getInstructions;

    private Marker[][] marker_arr; // 중간 마커 배열
    private LatLng End_location; // 도착 위치 표시

    private ArrayList<String[][]> Allget;


    public void parse(String JSON) {


        longInfo = new ArrayList<ArrayList<SampleItem>>();
        shortInfo = new ArrayList<ArrayList<SampleItem>>();


        int list_len;

        JSONArray routesArray;
        JSONArray legsArray;
        JSONArray stepsArray;

        String[] getInstructions; //이동정보 저장
        String[] arrival_name; //대중교통 도착지 저장
        String[] depart_name; //대중교통 출발지 저장
        String[] getHeadsign;
        String[] getBusNo;//노선 정보(~호선, 버스번호)
        String[] getCurrentDur, getCurrentDis;
        String[] getTransit;


        SampleItem sampleItem;

        try {

            JSONObject jsonObject = new JSONObject(JSON);
            //JSON 파일을 JSON객체로 바꿔준다.


            boolean routecheck = jsonObject.isNull("routes");
            if (routecheck == true) {
                Toast.makeText(getApplicationContext(), "경로가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                System.out.println("경로 x");
            } else {
                String routes = jsonObject.getString("routes");

                if (routes.isEmpty()) { // 경로가 존재하지 않는다면

                    Toast.makeText(getApplicationContext(), "경로가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                    System.out.println("경로 x");

                }

                routesArray = jsonObject.getJSONArray("routes");
                //Object에서 routes라는 이름의 키 값을 저장

                int i = 0;
                int routesSize = routesArray.length();

                goingS_lat = new String[routesSize][20]; // route의 개수만큼 그리고 그 안에 자잘한 route들을 최대 20으로 배열을 생성
                goingS_lng = new String[routesSize][20];
                goingE_lat = new String[routesSize][20];
                goingE_lng = new String[routesSize][20];
                getPolyline = new String[routesSize][20];
                TransitName = new String[routesSize][20];
                marker_arr = new Marker[2][20];


                JSONObject preferredObject = routesArray.getJSONObject(0);
                String singleRoute = preferredObject.getString("overview_polyline");
                JSONObject pointsObject = new JSONObject(singleRoute);
                String points = pointsObject.getString("points");
                getOverview = points;

                do {
                    //routes Array 배열의 길이만큼 반복을 돌리면서
                    System.out.println("i검색  : " + i);
                    System.out.println("routesArray 길이 :" + routesArray.length());
                    System.out.println("routesArray" + i + " : " + routesArray.get(i));

                    legsArray = ((JSONObject) routesArray.get(i)).getJSONArray("legs");
                    //JSONObject legJsonObject = legsArray.getJSONObject(i);
                    JSONObject legJsonObject = legsArray.getJSONObject(0);


                    //출발지, 도착지(나중에는 i=0일때만 들어와서 저장할 수 있도록 하기)
                    if (i == 0) {
                        str_Start = legJsonObject.getString("start_address");
                        str_End = legJsonObject.getString("end_address");
                    }

                    //총 이동시간 => 이건 leg마다 다르니까 step에 같이 출력하기
                    String duration = legJsonObject.getString("duration");
                    //Object에서 키 값이 duration인 변수를 찾아서 저장
                    JSONObject durJsonObject = new JSONObject(duration);
                    //duration에도 Object가 존재하므로 Object를 변수에 저장
                    //getDuration[j] = durJsonObject.getString("text");
                    Dur = durJsonObject.getString("text");
                    durArray = new String[routesArray.length()];
                    durArray[i] = Dur;
                    //step+="총 이동시간 : "+ Dur+"\n\n";

                    stepsArray = legJsonObject.getJSONArray("steps");
                    list_len = stepsArray.length();

                    getInstructions = new String[list_len]; //이동정보 저장
                    getCurrentDur = new String[list_len];
                    getCurrentDis = new String[list_len];
                    arrival_name = new String[list_len]; //대중교통 도착지 저장
                    depart_name = new String[list_len]; //대중교통 출발지 저장
                    getHeadsign = new String[list_len];
                    getBusNo = new String[list_len];//노선 정보(~호선, 버스번호)
                    getTransit = new String[list_len];

                    short_inner = new ArrayList<SampleItem>(); //간단한 경로 정보
                    long_inner = new ArrayList<SampleItem>(); //자세한 경로 정보

                    for (int k = 0; k < list_len; k++) {
                        //확인
                        System.out.println("세번째 반복문 ");
                        System.out.println("stepsArray 길이 :" + list_len);
                        System.out.println("stepsArray" + k + "번째 : " + stepsArray.get(k));


                        JSONObject stepsObject = stepsArray.getJSONObject(k);
                        //이동정보 저장
                        getInstructions[k] = stepsObject.getString("html_instructions");

                        String end_location = stepsObject.getString("end_location");
                        JSONObject endJsonObject = new JSONObject(end_location);
                        if (k >= list_len - 1) {
                            arrival_lat = endJsonObject.getString("lat");
                            arrival_lng = endJsonObject.getString("lng");

                            Double End_lat = Double.parseDouble(arrival_lat);
                            Double End_lng = Double.parseDouble(arrival_lng);
                            End_location = new LatLng(End_lat, End_lng);
                        } else {
                            goingE_lat[i][k] = endJsonObject.getString("lat");
                            goingE_lng[i][k] = endJsonObject.getString("lng");
                        }

                        String start_location = stepsObject.getString("start_location");
                        JSONObject startJsonObject = new JSONObject(start_location);
                        if (k == 0) {
                            departure_lat = startJsonObject.getString("lat");
                            departure_lng = startJsonObject.getString("lng");
                        } else {
                            goingS_lat[i][k] = startJsonObject.getString("lat");
                            goingS_lng[i][k] = startJsonObject.getString("lng");
                        }

                        String polyline = stepsObject.getString("polyline");
                        JSONObject polyJsonObject = new JSONObject(polyline);
                        getPolyline[i][k] = polyJsonObject.getString("points"); // 인코딩 된 포인트를 얻어옴


                        //각각의 이동시간(도보,대중교통)
                        String currnet_dur = stepsObject.getString("duration");
                        JSONObject CdurJsonObject = new JSONObject(currnet_dur);
                        getCurrentDur[k] = CdurJsonObject.getString("text");

                        //각각의 이동거리(도보,대중교통)
                        String currnet_dis = stepsObject.getString("distance");
                        JSONObject CdisJsonObject = new JSONObject(currnet_dis);
                        getCurrentDis[k] = CdisJsonObject.getString("text");


                        //도보인지 확인
                        String TRANSIT = stepsObject.getString("travel_mode");

                        //현재 단계에서 대중교통을 이용하는지 확인
                        String[] Check = getInstructions[k].split(" ");
                        //대중교통 저장
                        String TransitCheck = Check[0];


                        //도보일 경우
                        if (TRANSIT.equals("WALKING")) {

                            step = getInstructions[k] + "\n" + getCurrentDur[k] + "  |  " + getCurrentDis[k] + "이동\n";
                            shortText = "도보 " + getCurrentDur[k];
                            if (k != (list_len - 1)) {
                                shortText += ">";
                            }

                            short_inner.add(k, new SampleItem(shortText, 0));
                            long_inner.add(k, new SampleItem(step, 0));
                            continue;

                        }

                        if (TRANSIT.equals("TRANSIT")) {

                            String train_details = stepsObject.getString("transit_details");
                            JSONObject transitObject = new JSONObject(train_details);

                            String arrival_stop = transitObject.getString("arrival_stop");
                            JSONObject arrivalObject = new JSONObject(arrival_stop);
                            arrival_name[k] = arrivalObject.getString("name");

                            String depart_stop = transitObject.getString("departure_stop");
                            JSONObject departObject = new JSONObject(depart_stop);
                            depart_name[k] = departObject.getString("name");

                            getHeadsign[k] = transitObject.getString("headsign");

                            String line = transitObject.getString("line");
                            JSONObject lineObject = new JSONObject(line);
                            getBusNo[k] = lineObject.getString("short_name");


                            step = "\n" + depart_name[k] + "승차"
                                    + "\n 소요시간 : " + getCurrentDur[k] + " | " + "이동거리 : " + getCurrentDis[k]
                                    + "\n" + arrival_name[k] + "하차" +
                                    "\n" + getHeadsign[k] + "방향";

                            if (TransitCheck.equals("기차")) {
                                getTransit[k] = lineObject.getString("name");

                                shortText = getTransit[k] + "(" + getCurrentDur[k] + ")";
                                if (k != (list_len - 1)) {
                                    shortText += ">";
                                }
                                short_inner.add(k, new SampleItem(shortText, 2));//기차 이미지 추가

                                step += "\n " + getTransit[k] + "\n\n";
                                long_inner.add(k, new SampleItem(step, 1));

                                continue;
                            }
                            if (TransitCheck.equals("버스") || TransitCheck.equals("Bus")) {

                                if (!lineObject.isNull("short_name")) {
                                    getTransit[k] = lineObject.getString("short_name");
                                } else getTransit[k] = lineObject.getString("name");

                                shortText = getTransit[k] + "번(" + getCurrentDur[k] + ")";
                                if (k != (list_len - 1)) {
                                    shortText += ">";
                                }
                                short_inner.add(k, new SampleItem(shortText, 1));

                                step += "\n" + getTransit[k] + "번\n\n";
                                long_inner.add(k, new SampleItem(step, 1));

                                continue;
                            }
                            System.out.println("step : " + step);
                            if (TransitCheck.equals("지하철") || TransitCheck.equals("Subway")) {

                                getTransit[k] = lineObject.getString("short_name");
                                if (TransitCheck.equals("지하철") && getTransit[k].equals("1")) {
                                    getTransit[k] += "호선";
                                }

                                shortText = getTransit[k] + "(" + getCurrentDur[k] + ")";
                                if (k != (list_len - 1)) {
                                    shortText += ">";
                                }

                                short_inner.add(k, new SampleItem(shortText, 2));

                                long_inner.add(k, new SampleItem(step, 2));
                                continue;
                            }
                            TransitName[i][k] = getTransit[k];
                        }

                        shortText = null;
                        step = null;
                    }
                    shortInfo.add(short_inner);
                    longInfo.add(long_inner);

                    i++;
                } while (i < routesSize);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }
}
