<모임+지도 합친 최종 버전>

Main에서 모임 지도 버튼 누르면 이동
* MapPlaceActivity.java
* activity_map_place.xml
* dialog_place_info.xml

코드가 섞여 헷갈릴거 같아서 주석으로 소연이 코드 부분 표시함!

// dev branch 코드 변경
* 모임 참가, 모임 탈퇴 버튼 동작 (코드 입력시 해당 코드 모임 참가 / 탈퇴)
* 모임방 클릭시 (grid item 클릭시) 모임 방 내부로 들어가기 동작
* UI 변경 (기존 GridView => Main으로 통합)


//해야 함
1. 프래그먼트로 액티비티 분할 (모임 내부 메뉴 만들기)
2. 사진첩 (게시판)
3. 채팅
4 ....


// 해야하지 않을까?
1. 모임 탈퇴시 유저가 하나도 없으면 해당 모임 삭제
2. 회원 탈퇴시 유저가 속한 모임 방에서 유저 id 삭제


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


