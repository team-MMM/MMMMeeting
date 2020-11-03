# MMMMeeting

<br>
//2020.11.02 변경 <br>
* 중간지점 - 모임원들의 주소를 받아 중간지점 찾음(모임원,중간지점 marker 변경) <br>             
* 예시 <br>
<img src="https://user-images.githubusercontent.com/72245168/97830813-99fbd600-1d11-11eb-8e40-b5815022a6de.PNG" width="30%"></img>
<img src="https://user-images.githubusercontent.com/72245168/97830854-b566e100-1d11-11eb-9b6c-58456480613a.PNG" width="30%"></img>


//2020.11.02 변경 <주영 - 캘린더><br> 
-> firebase에 전부 연결<br> 
-> 메모(일정), 되는 날짜 표시(동그라미), 확정 날짜 전부 됨<br> 
-> 확정한 날짜는 네모 표시함<br> 


<br>//2020.11.02 변경 <카테고리 추천>
 * categorySelect 클래스에 작성됨
 * 다른 클래스로 category 값 전달 불가 -> 추천 카테고리 선택에 시간이 걸려서, 선택 전의 값이 전달됨<br>
  -> 카테고리 추천이 필요한 곳에 categorySelect 클래스에 있는 모든 함수 이동 필요..<br>
+) 회원정보 입력시 주소 입력 필수로 변경 => 주소 입력 안 된 모임원 있는 경우 중간지점 찾기 에러 발생<br>


<h1>* 모임 분리 방법!!</h1>
특정 액티비티 or 프래그먼트에서 미팅 이름 전달 -> 전달 받음 -> DB에서 미팅 이름을 포함하고 있는 데이터 가져옴 <br><br>

* <intent사용!!> <참고: FragHome- myStartActivity 함수> <br>
액티비티->액티비티 or 프래그먼트->액티비티 <br>
Intent intent = new Intent(getActivity(), 클래스이름.class); <br>
intent.putExtra("Name",meetingName); <br><br>

* <bundle사용!!> <참고: MeetingActivity- case R.id.menu_home 부분> <br>
액티비티->프래그먼트 or 프래그먼트->프래그먼트 <br>
bundle.putString("Name", getIntent().getExtras().getString("Name")); <br>
프래그먼트이름.setArguments(bundle); <br><br>

* DB에서 데이터 가져오기 <참고: FragHome- postsUpdate 함수> <br>
// 스케쥴 테이블 접근 <br>
CollectionReference collectionReference = firebaseFirestore.collection("schedule"); <br>
// 스케쥴 테이블의 문서 접근 <br>
for (QueryDocumentSnapshot document : task.getResult()) <br>
// 문서에 미팅ID가 미팅 이름과 같으면 동작! <br>
if(document.getData().get("meetingID").toString().equals(meetingName)){ <br>
    //예시- 문서의 스케쥴 제목 가져오고 싶을 때 <br>
    String title = document.getData.get("title").toString() -> 문서의 타이틀 이름을 string으로 가져옴 <br>
} <br>


//2020.10.29 변경 사항
<br>
 * 중간지점 찾기
 -> 모든 user의 주소를 이용해 중간지점 찾음 <br>
 * 추가된 Activity >> GrahamScan, Point, Stack <br>
 * 수정된 Activity >> MiddlePlaceActivity <br>

 * 회원정보 입력창에 별점 입력, DB 저장 (Map 형식)
 * 모임나가기, 회원 탈퇴시 모임방 인원이 0명이 되면 그 모임 DB에서 삭제<br>
  -> 모임 나가기에서 동작 확인, 회원 탈퇴 모임방 나가기 코드 복붙 (테스트 X)


// dev branch 코드 변경
* 모임 참가, 모임 탈퇴 버튼 동작 (코드 입력시 해당 코드 모임 참가 / 탈퇴)
* 모임방 클릭시 (grid item 클릭시) 모임 방 내부로 들어가기 동작
* UI 변경 (기존 GridView => Main으로 통합)

// 흐름도 <br>
SignAct -> MainAct(초기 화면) -> GridAdapter(모임 목록 관리) -> MeetingAct(한 모임 내부) <br>
-> FragHome(약속 목록 홈 화면) -> MakeScheduleAct(약속 만들기) -> ScheduleAdapter(약속 목록 관리)-> <br>
ContentScheduleAct(약속 내용 보기/날짜, 장소 정하기) -> <br>
<h1>★주영 코드 부분★</h1>
1. CalendarAct(공유 달력) - activity_calendar.xml 
-> db에서 모임이름 받아오기
-> 약속장소 저장하기
<h1>★소연 코드 부분★</h1>
2. PlaceChoiceAct(장소 선택)<br>
*1.MiddlePlaceAct(중간지점) - activity_place_middle.xml<br>
*2.SearchPlaceAct(장소찾기) - activity_place_search.xml <br>
<br><br>
//2020.10.23 변경 사항
<br>
*추가된 Activity >> DirectionActivity, GpsTracker, SampleItem<br>
*추가된 xml >> activity_direction_map<br>
<br>
수정사항<br>
* UI 정리 ( 추천경로와 그 외의 경로를 보여줌 , 경로를 선택하면 상세정보를 볼 수 있음)<br>
* Polyline 합치기 (선택된 경로의 Polyline을 볼 수 있도록 수정함)<br>
* 아직 코드 정리가 안돼서 다시 정리하고 주석 달아서 올릴 예정임
<br><br>

<br><br>
// 2020.10.21 변경
* 약속 화면 추가
* 캘린더/장소 선택 화면 추가
* 게시판 이동
* 메뉴 탭 변경 (홈/채팅/게시판/지각자/정산)
* schedule db에 올라가는거 확인 했습니다 (모임 ID는 임의로 넣었는데 곧 추가해야 할듯)

<br><br>
// 2020 .10 .20 변경
* 주소 검색
* 주석 추가
* 그리드뷰 디자인 변경
* 회원정보 -> 이름 입력값 유지
* 회원 탈퇴시 모임원에서도 삭제

<br><br>
// 2020 .11 .1 변경
* 정산부분 (나중에 시간 남으면 정산결과 게시글로 바로 올릴 수 있는 버튼 구현하면 좋을 것 같음)
<br>

//해야 함
<h1>★ 1. 중간지점 ★</h1>

1. meeting마다 속하는 모임원들의 주소만을 이용하도록 변경
2. 최적의 중간지점을 찾는데 시간 단축 필요

// 해야하지 않을까?
1. 모임 탈퇴시 유저가 하나도 없으면 해당 모임 삭제 => 함 <br>
  -> 삭제되는 모임의 게시된 약속, 게시판 관련 정보도 삭제될 수 있도록 하면 좋을 것 같다..
2. 회원 탈퇴시 유저가 속한 모임 방에서 유저 id 삭제 => 함
3. 모임 생성시 중복 이름 불가능하게 (현재 모임 초대시 모임 코드 선택해서 전송되는데 이름으로 모임코드 확인함) => 함



<br><br>
***************************************************************************
com.example.mmmmeeting

***Firebase 팀 아이디 만들었어요!!

아이디: mmmcapstone@gmail.com <br>
비밀번호: (영어로)컴종설!


