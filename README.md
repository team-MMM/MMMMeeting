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
