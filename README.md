<모임+지도 합친 최종 버전>

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
<h1>★소연 코드 부분★</h1>
2. PlaceChoiceAct(장소 선택)<br>
*1.MiddlePlaceAct(중간지점) - activity_place_middle.xml<br>
*2.SearchPlaceAct(장소찾기) - activity_place_search.xml 


<br><br>
// 2020.10.21 변경
* 약속 화면 추가
* 캘린더/장소 선택 화면 추가
* 게시판 이동
* 메뉴 탭 변경 (홈/채팅/게시판/지각자/정산)
* schedule db에 올라가는거 확인 했습니다 (모임 ID는 임의로 넣었는데 곧 추가해야 할듯)
<br>
// 2020 .10 .20 변경

*주소 검색

*주석 추가

*그리드뷰 디자인 변경

*회원정보 -> 이름 입력값 유지

*회원 탈퇴시 모임원에서도 삭제


//해야 함
<h1>★ 1. 중간지점 ★</h1>


// 해야하지 않을까?
1. 모임 탈퇴시 유저가 하나도 없으면 해당 모임 삭제
2. 회원 탈퇴시 유저가 속한 모임 방에서 유저 id 삭제 => 함
3. 모임 생성시 중복 이름 불가능하게 (현재 모임 초대시 모임 코드 선택해서 전송되는데 이름으로 모임코드 확인함)



<br><br>
***************************************************************************
com.example.mmmmeeting

***Firebase 팀 아이디 만들었어요!!

아이디: mmmcapstone@gmail.com <br>
비밀번호: (영어로)컴종설!

*******FIREBASE 연동 안될 때 *********

https://console.firebase.google.com/

안드로이드 스튜디오 맨 오른쪽 창 X버튼 밑 Gradle 버튼 클릭

MMMMmeeting->APP->TASK->ANDROID->signingreport 더블클릭 SHA1: 로 시작하는 코드 복사!!

firebase에 "SHA 인증서 지문"에 추가하기 -> 이거 때문에 구글 로그인이 안되는거 같음 -> 그래도 안되면 json 다운 받아보기..

<br><br>


