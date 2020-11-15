// 중간지점 화면

package com.example.mmmmeeting.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


public class MiddlePlaceActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesListener {

    private ScheduleInfo postInfo;
    private String meetingname;
    private String scheduleId;

    //private ArrayList<LatLng> position;
    private double[] userTime;
    private double[] userDist;
    //##
    //private LatLng midP = new LatLng(37.6663555,127.0557141);
    private LatLng midP;

    private int countTry;
    private LinearLayout midpoint_select;


    private String duration;
    private double Dur;


    //임의로 중간지점 대충 지정
    private LatLng curPoint = new LatLng(37.56593052663891, 126.97680764976288);

    private String str_url;

    private double latVector, lonVector;
    private int avgTime;
    private double avgDist;
    private boolean flag;
    private int flagCount;

    private GoogleMap mMap;

    int i = 0;
    int j = 0;
    Point center = new Point(0, 0);

    List<Marker> previous_marker = null;
    int radius = 1000;

    Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg)
        {
            Bundle bd = msg.getData( ) ;            /// 전달 받은 메세지에서 번들을 받음
            if(bd.getString("flag")!=null){
                String flagStr = bd.getString("flag");
                // placessuccess가 2번 이상인 상황: flag 설정해서 동작하지 못하게 함
                if(flagStr == "NO"){
                    flag = true;
                }
            }
        } ;

    } ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_middle);

        midpoint_select=(LinearLayout)findViewById(R.id.midpoint_select);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //시작했을때 지도화면 서울로 보이게 지정
        LatLng SEOUL = new LatLng(37.56, 126.97);
        MarkerOptions marker = new MarkerOptions();
        marker.position(SEOUL);
        marker.visible(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 8));
        mMap.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        postInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        meetingname = postInfo.getMeetingID();
        scheduleId = postInfo.getId();


        // meetings collection에 모임 이름인 문서 읽기

        db.collection("meetings").whereEqualTo("name", meetingname).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // document에서 이름이 userID인 필드의 데이터 얻어옴
                                List users = (List) document.getData().get("userID");
                                String[] addr = new String[users.size()];
                                String[] name = new String[users.size()];
                                // userID가 동일한 user 문서에서 이름, 주소 읽어오기
                                for (int m = 0; m < users.size(); m++) {
                                    DocumentReference docRef = db.collection("users").document(users.get(m).toString());

                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    addr[i] = document.getData().get("address").toString();
                                                    name[i++] = document.getData().get("name").toString();
                                                } else {
                                                    // 존재하지 않는 문서
                                                    Log.d("Attend", "No Document");
                                                }
                                                if (i==users.size())
                                                    gStart(addr,name); // 중간지점 찾기 시작
                                            } else {
                                                Log.d("Attend", "Task Fail : " + task.getException());
                                            }
                                        }
                                    });
                                }
                            }
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    // 중간 지점 찾기 시작!
    private void gStart(String[] addr, String[] name){
        BitmapDrawable bitmapdraw1 = (BitmapDrawable) getResources().getDrawable(R.drawable.user);
        Bitmap b = bitmapdraw1.getBitmap();
        Bitmap UserMarker = Bitmap.createScaledBitmap(b, 100, 100, false);

        Point[] points = new Point[addr.length];
        for (String str : addr) {
            getPointFromGeoCoder(str, points);
        }
        for (int k = 0; k < points.length; k++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(points[k].getX(), points[k].getY())).title(name[k]).icon(BitmapDescriptorFactory.fromBitmap(UserMarker)));
        }
        Point[] hull = GrahamScan.convexHull(points);

        countTry = 0;
        PolygonCenter(hull);
        curPoint = new LatLng(center.x, center.y);
        //중간지점 찾기 (사용자 위치들과 무게중심 좌표 넘겨주기)
        findMidPoint(hull, curPoint);

        String midAdr = getCurrentAddress(midP);
        //##
        //String midAdr = "서울특별시 상계8동 동일로 1545";

        //LinearLayout 정의
        RelativeLayout.LayoutParams rl_params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        //LinearLayout 생성
        RelativeLayout ly = new RelativeLayout(this);
        ly.setLayoutParams(rl_params);
        //ly.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv_mid = new TextView(this);
        int id =1;
        tv_mid.setId(id);
        tv_mid.setText("중간지점 주소 : "+midAdr);
        tv_mid.setLayoutParams(rl_params);
        ly.addView(tv_mid);

        Button btn_mid = new Button(this);
        btn_mid.setText("선택");
        RelativeLayout.LayoutParams btn_params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        btn_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        btn_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
        btn_params.addRule(RelativeLayout.BELOW,tv_mid.getId());
        btn_params.setMargins(0,0,30,0);
        btn_mid.setLayoutParams(btn_params);
        ly.addView(btn_mid);


        midpoint_select.addView(ly);
        midpoint_select.setVisibility(View.VISIBLE);

        btn_mid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MiddlePlaceActivity.this);
                //제목
                alertDialogBuilder.setTitle("중간지점 선택");

                //AlertDialog 세팅
                SpannableString s = new SpannableString("이 곳을 중간지점으로 선택하시겠습니까?\n"+midAdr);
                s.setSpan(new RelativeSizeSpan(0.5f),22,22+midAdr.length(),0);
                s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")),22,22+midAdr.length(),0);
                alertDialogBuilder.setMessage(s)
                        .setCancelable(false)
                        .setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //프로그램 종료
                                MiddlePlaceActivity.this.finish();
                            }
                        }).setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //장소리스트 화면으로 넘어감
                        Intent intent = new Intent(MiddlePlaceActivity.this, PlaceListActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putParcelable("midpoint",midP);
                        bundle.putString("name", meetingname);
                        bundle.putString("scheduleId", scheduleId);
                        intent.putExtras(bundle);
                        //i.putExtra("midpoint",midP);
                        startActivity(intent);
                    }
                });

                //다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();

                //다이얼로그 보여주기
                alertDialog.show();

            }
        });

        //중간지점 지도 위에 표시
        //mMap.addMarker(new MarkerOptions().position(midP).title("중간지점 찾음!").icon(BitmapDescriptorFactory.fromBitmap(MiddleMarker)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midP, 10));
        findSub(midP);
    }

    // 지오코딩(주소->좌표)
    private void getPointFromGeoCoder(String addr, Point[] points) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(addr, 10);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (listAddress.isEmpty()) {
            System.out.println("주소가 없습니다.");
            return;
        }

        points[j++] = new Point(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
    }

    // 무게중심 구하기
    private void PolygonCenter(Point[] hull) {
        double area = 0;
        double factor = 0;

        for (int i = 0; i < hull.length - 1; i++) {
            factor = (hull[i].x * hull[i + 1].y) - (hull[i + 1].x * hull[i].y);

            area += factor * 0.5;
            center.x += (hull[i].x + hull[i + 1].x) * factor;
            center.y += (hull[i].y + hull[i + 1].y) * factor;
        }
        factor = (hull[hull.length - 1].x * hull[0].y) - (hull[0].x * hull[hull.length - 1].y);

        area += factor * 0.5;
        center.x += (hull[hull.length - 1].x + hull[0].x) * factor;
        center.y += (hull[hull.length - 1].y + hull[0].y) * factor;

        center.x = center.x / (area * 6);
        center.y = center.y / (area * 6);
    }

    //중간지점 찾기

    /*
     * 1. 받아온 무게중심까지의 이동시간 받아오기
     * 2. 이동시간 받은걸로 최적의 경로인지 아닌지 판단(어떻게 판단할 것인지 제대로 정해야할듯)
     * 3. 최적이다 => midpoint  /  최적이 아니다 => 계산된 값을 m(현재 중간지점)에 더해서 중간지점 갱신하기
     * 4. 중간지점이 다각형 내부에 있는지 확인하기
     * 5. 다각형 내부에 중간지점이
     *                      있다 => findMidPoint 재귀호출해서 현재 중간지점이 최적의 경로인지 다시 판단
     *                      없다 => 다각형 내부 임의의 점을 지정해서 다시 findMidPoint를 재귀호출하는 방식으로? (아직 잘 모르겠음)
     * 6. 마지막에 midP에 m을 넣어줌으로써 중간지점 저장
     *
     */
    public void findMidPoint(Point[] hull, LatLng m) {


        //이동시간 저장할 공간
        userTime = new double[hull.length];

        latVector = 0;
        lonVector = 0;
        avgTime = 0;

        //유저들 이동시간 받아오기
//        for (int i = 0; i < hull.length; i++) {
//            //이동시간 받기
//            double time = getPathTime(hull[i].getposition(), m);
//            //이동시간 저장
//            userTime[i] = time;
//
//            System.out.println("현재 position부터 중간점까지의 이동시간 : " + time);
//
//            //중간지점부터 사용자 위치까지의 단위벡터 구하기
//            Point unitVector = getUnitVector(m, hull[i].getposition());
//
//            //시간가중치와 단위벡터의 곱
//            latVector += unitVector.getX() * time;
//            lonVector += unitVector.getY() * time;
//
//            // 총 이동시간의 합
//            avgTime += time;
//        }


        avgDist=0.0;
        //이동시간 저장할 공간
        userDist = new double[hull.length];
        //유저들의 이동좌표
        for(int i=0;i<hull.length;i++){
            double dist = SphericalUtil.computeDistanceBetween(hull[i].getposition(), m);

            userDist[i]=dist;
            //중간지점부터 사용자 위치까지의 단위벡터 구하기
            Point unitVector = getUnitVector(m, hull[i].getposition());

            //시간가중치와 단위벡터의 곱
            latVector += unitVector.getX() * dist;
            lonVector += unitVector.getY() * dist;

            // 총 이동시간의 합
            avgDist += dist;

        }


        //이동시간의 평균
        //avgTime /= hull.length;
        avgDist/=hull.length;

        //시간가중치와 단위벡터의 곱의 합을 평균이동시간과 유저들의 수의 곱으로 나눈다.
//        latVector /= (avgTime * hull.length);
//        lonVector /= (avgTime * hull.length);
        latVector /= (avgDist * hull.length);
        lonVector /= (avgDist * hull.length);

        //최적의 경로인지 확인하기 위함
        boolean isOptimized = false;
        int optC = 0;

        //새로운 점이 최적인가?
//        for (int i = 0; i < userTime.length; i++) {
//            //임의로 최적의 경로 확인
//            //사용자 이동시간과 평균 이동시간의 차이가 30분이 넘지 않았을 때 최적이라고 임의로 지정해놓음 (바꿔야함)
//            if (Math.abs(userTime[i] - avgTime) < 30) {
//                System.out.println(i + "차이 : " + Math.abs(userTime[i] - avgTime));
//                optC++;
//            }
//            //마지막 점까지 이동시간과 평균시간의 차이가 30분을 넘지 않으면 최적
//            if (optC == userTime[userTime.length - 1])
//                isOptimized = true;
//        }

        //새로운 점이 최적인가?
        for (int i = 0; i < userDist.length; i++) {
            //임의로 최적의 경로 확인
            //사용자 이동시간과 평균 이동시간의 차이가 30분이 넘지 않았을 때 최적이라고 임의로 지정해놓음 (바꿔야함)
            if (Math.abs(userDist[i] - avgDist) < 30) {
                System.out.println(i + "차이 : " + Math.abs(userDist[i] - avgDist));
                optC++;
            }
            //마지막 점까지 이동시간과 평균시간의 차이가 30분을 넘지 않으면 최적
            if (optC == userDist[userDist.length - 1])
                isOptimized = true;
        }

//        최적이라면 => 중간지점 출력(midPoint)
//        if(isOptimized==true){
//            midP = m;
//        }


        boolean check = true;
        //최적이 아니라면, 새로운 위치로 바꾸기
        if (isOptimized == false) {
            m = new LatLng(m.latitude + latVector / hull.length,
                    m.longitude + lonVector / hull.length);

            //다각형안에 들어가는지 확인
            check = point_in_polygon(m, hull);

            countTry++;
            System.out.println("중간지점 count : " + countTry);
            System.out.println("중간지점은 여기! : " + m);
//            for (int i = 0; i < userTime.length; i++) {
//                System.out.println("사용자들로부터 중간지점까지의 이동시간은 : " + userTime[i]);
//            }
            for (int i = 0; i < userDist.length; i++) {
                System.out.println("사용자들로부터 중간지점까지의 이동거리는 : " + userDist[i]);
            }
            if (countTry < 6) {
                findMidPoint(hull, m);
            }
            //findMidPoint(hull,midP);
        }
        System.out.println("update Check : " + check);

        // 1. 중간지점이 범위 내에 있다면 다시 midpoint가 맞는지 탐색하고
        // 2. 범위 내에 없다면 범위 내의 임의의 지점을 설정해줘서 다시 탐색하는 방식으로? 수정해야할듯
//        if(check==true){
//            countTry++;
//            if(countTry<5) findMidPoint(hull,mid);
//        }
//        if(check==false){
//            //범위 내 임의의 지점 설정해주기
//            System.out.println("범위 밖으로 나갔습니다.");
//        }

        //countTry++;
        midP = m;
    }


    //단위벡터 구하기
    public Point getUnitVector(LatLng start, LatLng end) {
        double v_x = end.latitude - start.latitude;
        double v_y = end.longitude - start.longitude;

        double u = Math.sqrt(Math.pow(v_x, 2) + Math.pow(v_y, 2));
        v_x /= u;
        v_y /= u;

        Point p = new Point(v_x, v_y);

        return p;
    }

    //이동시간 구하기
    public double getPathTime(LatLng start, LatLng end) {
        System.out.println("들어왔습니다.");
        String getJS = getJSON(start, end);

        try {
            JSONObject jsonObject = new JSONObject(getJS);

            JSONObject route= jsonObject.getJSONObject("route");
            System.out.println("route 출력 : "+route);

            JSONObject traOb = (JSONObject) route.getJSONArray("traoptimal").get(0);
            JSONObject summary = traOb.getJSONObject("summary");

            //총 이동시간 => 이건 leg마다 다르니까 step에 같이 출력하기
            duration = summary.getString("duration");
            System.out.println("duration출력 : "+duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        double totalT = 0;
        if (duration == null) {
            System.out.println("이동시간 정보가 없습니다. " + "시작 : " + start);
            totalT = 0;
        } else {
            Dur = Double.parseDouble(duration);
            Dur = Dur/60000;
            System.out.println("이동시간은 다음과 같다 : "+Dur);
            totalT=Dur;
        }

        return totalT;
    }


    // 좌표 리스트를 다각형으로 지도에 표시하기 위해
    private PolygonOptions makePolygon(ArrayList<LatLng> polygon_list) {

        PolygonOptions opts = new PolygonOptions();
        for (LatLng location : polygon_list) {
            opts.add(location);
        }
        return opts;
    }

    // Point 타입 좌표 배열을 ArrayList<LatLng> 타입으로 변환
    private ArrayList<LatLng> changeToList(Point[] polygon_point) {

        ArrayList<LatLng> polygonList = new ArrayList<>();

        for (int i = 0; i < polygon_point.length; i++) {
            LatLng temp = new LatLng(polygon_point[i].getX(), polygon_point[i].getY());
            polygonList.add(temp);
        }

        Log.d("TEST CHECK", polygonList.toString());
        //mMap.addPolygon(makePolygon(polygonList).strokeColor(Color.RED));
        return polygonList;
    }

    // 확인하려는 좌표와 도형의 좌표가 들어있는 Point타입 배열을 인자로 받아 좌표가 도형에 속하는지 확인
    public boolean point_in_polygon(LatLng point, Point[] polygon) {
        ArrayList<LatLng> polygonList = changeToList(polygon);
        //LatLng point = new LatLng(point.getX(),point.getY()); // 만약 확인하려는 좌표도 Point 타입인 경우 사용

        // PolyUtil 함수 사용
        boolean inside = PolyUtil.containsLocation(point, polygonList, true);
        Log.d("TEST CHECK", "inside check : " + inside);
        return inside;
    }

    // 중간지점 근처 역 찾기
    private void findSub(LatLng midP){
        String apiKey = getString(R.string.api_key);
        previous_marker = new ArrayList<Marker>();

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(MiddlePlaceActivity.this)
                .key(apiKey)
                .latlng(midP.latitude, midP.longitude)//중간지점 위치
                .radius(radius) //500 미터 내에서 검색
                .type(PlaceType.SUBWAY_STATION) //지하철
                .language("ko", "KR")
                .build()
                .execute();
    }
    // 500 미터 내에 없을 경우 찾을 때 까지 500 미터 늘려서 다시 계산
    @Override
    public void onPlacesFailure(PlacesException e) {
        String apiKey = getString(R.string.api_key);
        if(radius < 3000) {
            radius += 1000;
            new NRPlaces.Builder()
                    .listener(MiddlePlaceActivity.this)
                    .key(apiKey)
                    .latlng(midP.latitude, midP.longitude)//현재 위치
                    .radius(radius) //500 미터 내에서 검색
                    .type(PlaceType.SUBWAY_STATION) //지하철
                    .language("ko", "KR")
                    .build()
                    .execute();
        }
    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
        flagCount++;
        if(flagCount>=2) {
            Bundle bd = new Bundle();      /// 번들 생성
            bd.putString("flag", "NO"); // 번들에 값 넣기
            Message msg = mHandler.obtainMessage();   /// 핸들에 전달할 메세지 구조체 받기
            msg.setData(bd);                     /// 메세지에 번들 넣기
            mHandler.handleMessage(msg);
        }
        if(flag) return;

        BitmapDrawable bitmapdraw2 = (BitmapDrawable) getResources().getDrawable(R.drawable.middleplace);
        Bitmap c = bitmapdraw2.getBitmap();
        Bitmap MiddleMarker = Bitmap.createScaledBitmap(c, 100, 100, false);
        // 중간지점과 가장 가까운 역 표시
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double distance = (double)radius;
                double temp = 0.0;
                LatLng latlng = new LatLng(0,0);
                int i=1;
                int size = places.size();
                String name = null;
                for (noman.googleplaces.Place place : places) {
                    LatLng latLng
                            = new LatLng(place.getLatitude(), place.getLongitude());
                    temp = distance;
                    distance = SphericalUtil.computeDistanceBetween(midP, latLng);
                    System.out.println(place.getName()+ " 거리 : "+ distance);
                    if(distance < temp) {
                        latlng = latLng;
                        name = place.getName();
                    }
                    if(i==size) {
                        String markerSnippet = getCurrentAddress(latlng);

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latlng);
                        markerOptions.title(name+"역");
                        markerOptions.snippet(markerSnippet).icon(BitmapDescriptorFactory.fromBitmap(MiddleMarker));
                        Marker item = mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
                        previous_marker.add(item);
                        midP= latlng;
                    }
                    i++;
                }
                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }
        });
    }

    @Override
    public void onPlacesFinished() {

    }

    public String getCurrentAddress(LatLng latlng) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
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
                conn.setRequestMethod("GET");
                //네이버 플랫폼에서 발급받은 키
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "p16r9d98f3");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "kCciijwt6AT7OGT6mx7IUDHXfV6NYrW41O03R3cj");
                conn.setDoInput(true);
                conn.connect();

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

    public String getJSON(LatLng depart, LatLng arrival) {

        String str_origin = depart.longitude + "," + depart.latitude;
        String str_dest = arrival.longitude + "," + arrival.latitude;

        str_url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?" +
                "start="+str_origin+"&goal="+str_dest;

        String resultText = "값이 없음";

        try {
            resultText = new Task().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return resultText;
    }
}