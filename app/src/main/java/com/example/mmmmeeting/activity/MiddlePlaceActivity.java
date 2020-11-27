// 중간지점 화면

package com.example.mmmmeeting.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


public class MiddlePlaceActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesListener {

    private ScheduleInfo scheduleInfo;
    private String meetingname;
    private String scheduleId;
    String code;

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
    private boolean findSub_flg=true;
    private int flagCount;

    private Point[] users;
    private ArrayList<Integer> member_num;
    private ArrayList<Point> centers;
    private double latitude;
    private double longtitude;
    private LatLng mid;// 현재 중간지점
    private LatLng tem; // 이전 중간지점
    private int temp = 1000;
    private String[] address;
    private String[] m_name;
    private String sub_name=null;

    private GoogleMap mMap;

    AppCompatDialog progressDialog;

    int i = 0;
    int j = 0;
    Point center = new Point(0, 0);

    List<Marker> previous_marker = null;
    int radius = 1000;

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Bundle bd = msg.getData();            /// 전달 받은 메세지에서 번들을 받음
            if (bd.getString("flag") != null) {
                String flagStr = bd.getString("flag");
                // placessuccess가 2번 이상인 상황: flag 설정해서 동작하지 못하게 함
                if (flagStr == "NO") {
                    flag = true;
                }
            }
        }

        ;

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_middle);


        progressDialog = new AppCompatDialog(MiddlePlaceActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progress_loading);

        ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
        Glide.with(getApplicationContext()).load(R.drawable.wemeetnow).into(img_loading_frame);


        midpoint_select = (LinearLayout) findViewById(R.id.midpoint_select);
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
        scheduleInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        code = scheduleInfo.getMeetingID();
        scheduleId = scheduleInfo.getId();

        db.collection("meetings").document(code)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 문서가 존재하는 경우
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
                                        if (i == users.size()){
                                            //clustering(addr, name); // 중간지점 찾기 시작
                                            address=addr;
                                            m_name=name;
                                            BackgroundTask Btask =new BackgroundTask();
                                            Btask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        }
                                    } else {
                                        Log.d("Attend", "Task Fail : " + task.getException());
                                    }
                                }
                            });
                        }
                    } else {
                        // 존재하지 않는 문서
                        Log.d("Attend", "No Document");
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
                }

            }
        });

    }

    class BackgroundTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {

            progressDialog.show();

            super.onPreExecute();

        }


        @Override
        protected String doInBackground(String... strings) {

            String abc = "clustering OK!!!";
            clustering(address, m_name); // 중간지점 찾기 시작
            runOnUiThread(new Runnable(){
                public void run(){
                    BitmapDrawable bitmapdraw2 = (BitmapDrawable) getResources().getDrawable(R.drawable.mid);
                    Bitmap b = bitmapdraw2.getBitmap();
                    Bitmap MidMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                    if(findSub_flg==false) {
                        mMap.addMarker(new MarkerOptions().position(midP).icon(BitmapDescriptorFactory.fromBitmap(MidMarker)));
                    }
                    BitmapDrawable bitmapdraw1 = (BitmapDrawable) getResources().getDrawable(R.drawable.user);
                    b = bitmapdraw1.getBitmap();
                    Bitmap UserMarker = Bitmap.createScaledBitmap(b, 100, 100, false);

                    for (int k = 0; k < address.length; k++) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(users[k].getX(), users[k].getY())).icon(BitmapDescriptorFactory.fromBitmap(UserMarker)));
                    }

                    String midAdr = getCurrentAddress(midP);
                    //##
                    //String midAdr = "서울특별시 상계8동 동일로 1545";
                    findSub(midP);

                    //LinearLayout 정의
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    //LinearLayout 정의
                    RelativeLayout.LayoutParams rl_params = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    rl_params.setMargins(10,0,10,0);

                    //LinearLayout 생성
                    RelativeLayout ly = new RelativeLayout(MiddlePlaceActivity.this);
                    ly.setLayoutParams(rl_params);
                    //ly.setOrientation(LinearLayout.HORIZONTAL);

                    TextView tv_mid = new TextView(MiddlePlaceActivity.this);
                    int id =1;
                    tv_mid.setId(id);
                    tv_mid.setText("중간지점 주소 : "+midAdr);
                    tv_mid.setTypeface(null, Typeface.BOLD);
                    tv_mid.setTextSize(16);
                    tv_mid.setLayoutParams(rl_params);
                    ly.addView(tv_mid);

                    final int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                    final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

                    Button btn_mid = new Button(MiddlePlaceActivity.this);
                    btn_mid.setText("선택");
                    RelativeLayout.LayoutParams btn_params = new RelativeLayout.LayoutParams(width,height);
                    btn_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
                    //btn_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                    btn_params.addRule(RelativeLayout.BELOW,tv_mid.getId());
                    btn_params.setMargins(0,0,30,0);
                    btn_mid.setLayoutParams(btn_params);
                    btn_mid.setBackground(getDrawable(R.drawable.button_shape));
                    btn_mid.setTextColor(Color.WHITE);
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
                            if(sub_name==null){
                                sub_name="없습니다.";
                                SpannableString s = new SpannableString("가장 가까운 역이 "+sub_name +"\n이 곳을 중간지점으로 선택하시겠습니까?\n"+ midAdr);
                                int i = 33 + sub_name.length();
                                s.setSpan(new RelativeSizeSpan(0.7f), i, i + midAdr.length(), 0);
                                s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")), i, i + midAdr.length(), 0);
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
                                                bundle.putString("code", code);
                                                bundle.putString("scheduleId", scheduleId);
                                                intent.putExtras(bundle);
                                                //i.putExtra("midpoint",midP);
                                                Log.d("Send","meetingname 전달 : "+code);
                                                startActivity(intent);
                                            }
                                        });

                            }
                            else{
                                sub_name+="역 입니다.";
                                SpannableString s = new SpannableString("가장 가까운 역은 "+sub_name +"\n이 곳을 중간지점으로 선택하시겠습니까?\n");
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
                                        bundle.putString("code", code);
                                        bundle.putString("scheduleId", scheduleId);
                                        intent.putExtras(bundle);
                                        //i.putExtra("midpoint",midP);
                                        Log.d("Send","meetingname 전달 : "+code);
                                        startActivity(intent);
                                    }
                                });
                            }
                            //다이얼로그 생성
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            //다이얼로그 보여주기
                            alertDialog.show();

                        }
                    });

                    //중간지점 지도 위에 표시
                    //mMap.addMarker(new MarkerOptions().position(midP).title("중간지점 찾음!").icon(BitmapDescriptorFactory.fromBitmap(MiddleMarker)));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midP, 10));
                }
            });

            //mMap.addMarker(new MarkerOptions().position(midP).icon(BitmapDescriptorFactory.fromBitmap(MidMarker)));


            return abc;

        }

        @Override
        protected void onPostExecute(String s) {

            progressDialog.dismiss();
            super.onPostExecute(s);
            Toast.makeText(MiddlePlaceActivity.this, "로딩 완료", Toast.LENGTH_SHORT).show();
        }

    }

    // 중간 지점 찾기 시작!
    private void clustering(String[] addr, String[] name) {

        HashMap<String, ArrayList<String>> user_map = new HashMap<>(); //{"도","주소"}

        int q = 0;
        for (String str : addr) {

            //getPointFromGeoCoder(str);
            Log.d("Clustering", "addr : " + addr[q]);
            String[] area = addr[q].split(" ");
            String do_name = area[0];

            switch (do_name) {
                case "서울특별시":
                case "인천광역시":
                    do_name = "경기도";
                    break;
                case "대전광역시":
                case "세종특별자치시":
                    do_name = "충청남도";
                    break;
                case "대구광역시":
                    do_name = "전라북도";
                    break;
                case "울산광역시":
                case "부산광역시":
                    do_name = "경상남도";
                    break;
                case "광주광역시":
                    do_name = "전라남도";
                    break;
                default:
                    do_name = area[0];
            }

            Log.d("Clustering", "구역 : " + addr[q]);
            if (user_map.containsKey(do_name)) {
                /*
                 * 이미 키가 존재할 경우
                 * ArrayList를 기존의 Points로 초기화하고 새 Points 추가함
                 */
                ArrayList<String> user_point = user_map.get(do_name);
                user_point.add(addr[q]);
                user_map.put(do_name, user_point);

            } else {
                /*
                 * 키가 존재하지 않을 경우
                 * ArrayList를 초기화하고 Points추가
                 */
                ArrayList<String> user_point = new ArrayList<>();
                user_point.add(addr[q]);
                user_map.put(do_name, user_point);
            }
            Log.d("Clustering", " 클러스터링중 : " + user_map);
            q++;
        }
        users = new Point[addr.length];
        for (int i = 0; i < addr.length; i++) {
            users[i] = getPointFromGeoCoder(addr[i]);
        }


        //클러스터링된 맵을 반복문을 돌면서 centroid와 size를 저장한다.
        centers = new ArrayList<>();
        member_num = new ArrayList<>();

        Point[] points;   //주소좌표 담을 공간


        /*클러스터링 결과 맵의 size가
         *  1. 1인 경우 : 모든 유저가 같은 구역에 있음(Ex)모두 경기도, 서울특별시, 충청도 등)
         *  2. 모임원들 수와 같은 경우 : 모든 유저가 다른 구역에 흩어져있음
         *
         * => 이럴 경우엔 바로 graham을 사용해서 무게중심 구하고 중간지점 구하기
         */


        if (user_map.size() == 1 || user_map.size() == addr.length) {
            //int k=0;
            points = new Point[addr.length];
            for (int i = 0; i < points.length; i++) {
                points[i] = getPointFromGeoCoder(addr[i]);
                Log.d("Clustering", "points : " + points[i]);
            }
            ArrayList<LatLng> k_result = kmeans(2, points);
            Log.d("Clustering", "k_result 크기 : " + k_result.size());

            for (int j = 0; j < k_result.size(); j++) {
                centers.add(new Point(k_result.get(j).latitude, k_result.get(j).longitude));
            }
            for (Map.Entry<Integer, ArrayList<Point>> elem : cmap.entrySet()) {
                //System.out.println( String.format("키 -> %s, 값 -> %s", elem.getKey(), elem.getValue()) );
                ArrayList<Point> p = elem.getValue();
                member_num.add(p.size());
            }
            Log.d("Clustering", "센터 크기  : " + centers.size());
            Log.d("Clustering", "membernum 크기  : " + member_num.size());


            for (int i = 0; i < centers.size(); i++) {
                //mMap.addMarker(new MarkerOptions().position(new LatLng(centers.get(i).x, centers.get(i).y)).title("centroid " + i).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                Log.d("Clustering", "center출력  : " + centers.get(i));
                // 인원수에 비례하여 평균점 계산
                latitude += centers.get(i).x * member_num.get(i);
                longtitude += centers.get(i).y * member_num.get(i);
            }
            latitude /= addr.length;
            longtitude /= addr.length;

            mid = new LatLng(latitude, longtitude);// 현재 중간지점
            Log.d("Clustering", "현재 중간지점 : " + mid);
            tem = mid; // 이전 중간지점

            FindMid();

//            Point[] hull = GrahamScan.convexHull(points);
//            Point mid_per = PolygonCenter(hull);
//            mid =new LatLng(mid_per.x,mid_per.y);
//            for(int j=0;j<points.length;j++){
//                member_num.add(1);
//            }
            //curPoint = new LatLng(center.x, center.y);
//            FindMid();
        }//군집이 존재할 경우
        else {
            Log.d("Clustering", " 군집이 존재합니다.");


            for (Map.Entry<String, ArrayList<String>> elem : user_map.entrySet()) {
                //System.out.println( String.format("키 -> %s, 값 -> %s", elem.getKey(), elem.getValue()) );
                ArrayList<String> p = elem.getValue();
                points = new Point[p.size()];
                //Point[] point = p.toArray(new Point[p.size()]);
                Log.d("Clustering", " map의 key,value : " + elem.getKey() + "," + elem.getValue());

                for (int i = 0; i < p.size(); i++) {
                    points[i] = getPointFromGeoCoder(p.get(i));
                    Log.d("Clustering", "포인트가 존재한다 : " + points[i]);
                }

                Point[] hull = new Point[points.length];
                Log.d("Clustering", "포인트의 길이는 : " + points.length);

                if (points.length > 2) {
                    hull = GrahamScan.convexHull(points);
                    centers.add(PolygonCenter(hull));
                } else {
                    centers.add(PolygonCenter(points));
                }

                member_num.add(p.size());
                //curPoint = new LatLng(center.x, center.y);
            }
            latitude = 0.0;
            longtitude = 0.0;

            Log.d("Clustering", "센터 크기  : " + centers.size());
            Log.d("Clustering", "membernum 크기  : " + member_num.size());


            for (int i = 0; i < centers.size(); i++) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(centers.get(i).x, centers.get(i).y)).title("centroid " + i).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                Log.d("Clustering", "center출력  : " + centers.get(i));
                // 인원수에 비례하여 평균점 계산
                latitude += centers.get(i).x * member_num.get(i);
                longtitude += centers.get(i).y * member_num.get(i);
            }
            latitude /= addr.length;
            longtitude /= addr.length;

            mid = new LatLng(latitude, longtitude);// 현재 중간지점
            Log.d("Clustering", "현재 중간지점 : " + mid);
            tem = mid; // 이전 중간지점
            FindMid();

            Log.d("Clustering", "center_p(클러스터의 중심들) : " + centers);

//            for (int k = 0; k < points.length; k++) {
//                mMap.addMarker(new MarkerOptions().position(new LatLng(points[k].getX(), points[k].getY())).title(name[k]).icon(BitmapDescriptorFactory.fromBitmap(UserMarker)));
//            }
        }
    }

    // 지오코딩(주소->좌표)
    private Point getPointFromGeoCoder(String addr) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(addr, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (listAddress.isEmpty()) {
            System.out.println("주소가 없습니다.");
        }

        return new Point(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
    }

    private Point pt;

    // 무게중심 구하기
    private Point PolygonCenter(Point[] hull) {
        double area = 0;
        double factor = 0;

        double clat = 0.0, clng = 0.0;

        if (hull.length == 1) {
            pt = hull[0];
        } else if (hull.length == 2) {
            clat = (hull[0].x + hull[1].x) / 2.0;
            clng = (hull[0].y + hull[1].y) / 2.0;
            pt = new Point(clat, clng);
        } else {

            int j = 0;
            for (int i = 0; i < hull.length; i++) {
                j = (i + 1) % hull.length;
                factor = (hull[i].x * hull[j].y) - (hull[j].x * hull[i].y);

                area += factor;
                clat += (hull[i].x + hull[j].x) * factor;
                clng += (hull[i].y + hull[j].y) * factor;
            }

            area *= 0.5;
            clat = clat / (area * 6);
            clng = clng / (area * 6);

            pt = new Point(clat, clng);
        }
        return pt;
    }


    //중간지점 찾기
    public void FindMid() {

        Log.d("Clustering", " 처음 좌표: " + mid);
        //이동시간 저장할 공간
        userTime = new double[centers.size()];

        latVector = 0;
        lonVector = 0;
        avgTime = 0; // 현재 평균 이동시간

        //유저들 이동시간 받아오기
        for (int i = 0; i < centers.size(); i++) {
            //이동시간 받기
            double time = getPathTime(mid, new LatLng(centers.get(i).x, centers.get(i).y));
            //이동시간 저장
            userTime[i] = time;

            Log.d("Clustering", "현재 position부터 중간점까지의 이동시간 : " + time);

            // 총 이동시간의 합
            avgTime += (time * member_num.get(i));
        }

        //이동시간의 평균
        avgTime /= centers.size();
        Log.d("Clustering", " 평균 시간 : " + avgTime);

        for (int i = 0; i < centers.size(); i++) {
            //중간지점부터 사용자 위치까지의 단위벡터 구하기
            Point unitVector = getUnitVector(mid, new LatLng(centers.get(i).x, centers.get(i).y));

            double t = (userTime[i] * member_num.get(i)) - avgTime;

            Log.d("Clustering", i + " 가중치 시간 : " + t);

            //시간가중치와 단위벡터의 곱
            latVector += unitVector.getX() * t;
            lonVector += unitVector.getY() * t;

        }

        //가중치와 단위벡터의 곱의 합을 클러스터 수로 나눈다.
        latVector /= (avgTime * centers.size());
        lonVector /= (avgTime * centers.size());

        Log.d("Clustering", " 시간벡터 : " + latVector + "," + lonVector);

        //최적의 경로인지 확인하기 위함
        boolean isOptimized = false;

        //새로운 점이 최적인가?
        for (int i = 0; i < userTime.length; i++) {
            //임의로 최적의 경로 확인
            Log.d("Clustering", i + "차이 : " + Math.abs(userTime[i] * member_num.get(i) - avgTime));
        }
        // 현재 평균시간이 이전 평균시간보다 크면 그 전이 최적!
        if (temp < avgTime) {
            isOptimized = true;
        }
        //최적이라면 => 중간지점 출력(midPoint)
        if (isOptimized == true) {
            midP = tem;
        }


        boolean check = true;
        //최적이 아니라면, 새로운 위치로 바꾸기
        if (isOptimized == false) {
            tem = mid;
            temp = avgTime;
            mid = new LatLng(mid.latitude + latVector,
                    mid.longitude + lonVector);

            countTry++;
            Log.d("Clustering", "중간지점 count : " + countTry);
            Log.d("Clustering", "중간지점은 여기! : " + tem);
            for (int i = 0; i < userTime.length; i++) {
                Log.d("Clustering", "사용자들로부터 중간지점까지의 이동시간은 : " + userTime[i]);
            }
            // 최대 3번 FindMid 실행
            if (countTry < 4) {
                FindMid();
            } else {
                midP = tem;
            }
        }
        System.out.println("update Check : " + check);
    }


    //kmeans클러스터링

    private ArrayList<LatLng> cp;
    private ArrayList<LatLng> prevcenter;
    private Map<Integer, ArrayList<Point>> cmap;

    public ArrayList<LatLng> kmeans(int k, Point[] pos) {
        Log.d("Clustering", " 클러스터개수는 " + k + "개입니다.");
        //클러스터만큼의 센트로이드 랜덤생성
        cp = new ArrayList<>();
        setCentroids(k, pos);

        int iter_i = 0;
        do {
            Log.d("Clustering", "--------------" + iter_i + "번째" + "--------------");
            Log.d("Clustering", "----<<assignment>>----");
            //assignment
            assignment(pos);
            //Log.d("Clustering","----<<updateCenter>>----");
            //update
            //updateCenter();
            iter_i++;
        } while (Stop());
        return prevcenter;
    }

    public void setCentroids(int k_num, Point[] pos) {

        Log.d("Clustering", " 센트로이드 초기화");

        //center.clear();
        cp = new ArrayList<>();

        int[] result = new int[k_num]; //k개만큼의 난수를 담을 배열
        LatLng[] result_point = new LatLng[k_num];
        for (int i = 0; i < k_num; i++) {
            int rValue = (int) (Math.random() * pos.length);

            result[i] = rValue;
            for (int j = 0; j < i; j++) {
                if (result[i] == result[j]) {
                    i--;
                    break;
                } // i번째 난수가 지금까지 도출된 난수와 비교하여 중복이라면 i번째 난수를 다시 출력하도록 i--
            }
            Log.d("Clustering", "랜덤 centroid: " + users[rValue].x + "," + users[rValue].y);
            result_point[i] = new LatLng(users[rValue].x, users[rValue].y);
        }
        for (int j = 0; j < result_point.length; j++) {
            cp.add(result_point[j]);
        }
        Log.d("Clustering", "랜덤 centroid 개수 : " + k_num + "/" + cp.size());
        prevcenter = cp;

    }


    public void assignment(Point[] pos) {

        cmap = new HashMap<>();

        for (int i = 0; i < pos.length; i++) {
            double nearest = 0;
            int nearest_num = 0;
            for (int j = 0; j < cp.size(); j++) {
                Log.d("Clustering", "cp출력 :" + cp.get(j));

                ArrayList<LatLng> pos_list = changeToList(pos);
                double dist = SphericalUtil.computeDistanceBetween(pos_list.get(i), cp.get(j));
                if (j == 0) {
                    nearest = dist;
                    nearest_num = 0;
                } else {
                    if (nearest - dist > 0) {
                        nearest = dist;
                        nearest_num = j;
                    }
                }
                Log.d("Clustering", i + "번째 사람의 nearest :" + nearest + ", nearest num : " + nearest_num);
            }
            if (cmap.containsKey(nearest_num)) {
                /*
                 * 이미 키가 존재할 경우
                 * ArrayList를 기존의 Points로 초기화하고 새 Points 추가함
                 */
                ArrayList<Point> user_point = cmap.get(nearest_num);
                user_point.add(pos[i]);
                cmap.put(nearest_num, user_point);

            } else {
                /*
                 * 키가 존재하지 않을 경우
                 * ArrayList를 초기화하고 Points추가
                 */
                ArrayList<Point> user_point = new ArrayList<>();
                user_point.add(pos[i]);
                cmap.put(nearest_num, user_point);
            }
        }
        Log.d("Clustering", "cmap 출력 : " + cmap);
        Log.d("Clustering", "----<<updateCenter>>----");
        //update
        updateCenter();
    }

    public void updateCenter() {
        //center.clear();
        cp = new ArrayList<>();
        // 방법2
        for (Map.Entry<Integer, ArrayList<Point>> elem : cmap.entrySet()) {
            Log.d("Clustering", "클러스터 출력(key) : " + elem.getKey());
            Log.d("Clustering", "클러스터 출력(value) : " + elem.getValue());
            Point[] cluster_points = elem.getValue().toArray(new Point[elem.getValue().size()]);
            Point p;
            p = PolygonCenter(cluster_points);

            cp.add(new LatLng(p.x, p.y));
        }
        for (int i = 0; i < cp.size(); i++) {
            Log.d("Clustering", "무게중심 출력 : " + cp.get(i));
        }
    }

    public boolean Stop() {
        boolean check = true;
        int count_ch = 0;
        for (int i = 0; i < cp.size(); i++) {
            double diff_lat = cp.get(i).latitude - prevcenter.get(i).latitude;
            double diff_lng = cp.get(i).longitude - prevcenter.get(i).longitude;
            Log.d("Clustering", "prevcenter : " + prevcenter.get(i).latitude + "," + prevcenter.get(i).longitude);
            if (diff_lat == 0 && diff_lng == 0) {
                count_ch++;
            }
        }
        if (count_ch == cp.size()) {
            Log.d("Clustering", "이전과 차이없음");
            Log.d("Clustering", "클러스터링 완료! ");
            check = false;
        }
        prevcenter = new ArrayList<>();
        prevcenter = cp;
        for (int i = 0; i < cp.size(); i++) {
            Log.d("Clustering", "centroid : " + cp.get(i));
            Log.d("Clustering", "prevcenter 재지정 : " + prevcenter.get(i));
        }

        return check;
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

            JSONObject route = jsonObject.getJSONObject("route");
            System.out.println("route 출력 : " + route);

            JSONObject traOb = (JSONObject) route.getJSONArray("traoptimal").get(0);
            JSONObject summary = traOb.getJSONObject("summary");

            //총 이동시간 => 이건 leg마다 다르니까 step에 같이 출력하기
            duration = summary.getString("duration");
            System.out.println("duration출력 : " + duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        double totalT = 0;
        if (duration == null) {
            System.out.println("이동시간 정보가 없습니다. " + "시작 : " + start);
            totalT = 0;
        } else {
            Dur = Double.parseDouble(duration);
            Dur = Dur / 60000;
            System.out.println("이동시간은 다음과 같다 : " + Dur);
            totalT = Dur;
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
    private void findSub(LatLng midP) {
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
        if (radius < 3000) {
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
        else{
            findSub_flg=false;
        }
    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
        flagCount++;
        if (flagCount >= 2) {
            Bundle bd = new Bundle();      /// 번들 생성
            bd.putString("flag", "NO"); // 번들에 값 넣기
            Message msg = mHandler.obtainMessage();   /// 핸들에 전달할 메세지 구조체 받기
            msg.setData(bd);                     /// 메세지에 번들 넣기
            mHandler.handleMessage(msg);
        }
        if (flag) return;

        BitmapDrawable bitmapdraw2 = (BitmapDrawable) getResources().getDrawable(R.drawable.middleplace);
        Bitmap c = bitmapdraw2.getBitmap();
        Bitmap MiddleMarker = Bitmap.createScaledBitmap(c, 100, 100, false);
        // 중간지점과 가장 가까운 역 표시
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double distance = (double) radius;
                double temp = 0.0;
                LatLng latlng = new LatLng(0, 0);
                int i = 1;
                int size = places.size();
                String name = null;
                for (noman.googleplaces.Place place : places) {
                    LatLng latLng
                            = new LatLng(place.getLatitude(), place.getLongitude());
                    temp = distance;
                    distance = SphericalUtil.computeDistanceBetween(midP, latLng);
                    System.out.println(place.getName() + " 거리 : " + distance);
                    if (distance < temp) {
                        latlng = latLng;
                        name = place.getName();
                    }
                    if (i == size) {
                        sub_name=name;
                        String markerSnippet = getCurrentAddress(latlng);

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latlng);
                        markerOptions.title(name + "역");
                        markerOptions.snippet(markerSnippet).icon(BitmapDescriptorFactory.fromBitmap(MiddleMarker));
                        Marker item = mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
                        previous_marker.add(item);
                        midP = latlng;
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
                "start=" + str_origin + "&goal=" + str_dest;

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


