package com.example.mmmmeeting.activity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataParser {

    //private String step;
    private String entire_step=null;
    //private String[] getDuration;

    //출발주소, 도착주소, 총 이동시간
    private String Dur,str_Start,str_End;

    public String parse(String JSON){
//        String entire_step=null;
//        String step = null;

        int list_len;
        String step;

        JSONArray routesArray;
        JSONArray legsArray;
        JSONArray stepsArray;

        String[] getInstructions; //이동정보 저장
        String[] arrival_name; //대중교통 도착지 저장
        String[] depart_name; //대중교통 출발지 저장
        String[] getHeadsign;
        String[] getBusNo;//노선 정보(~호선, 버스번호)
        String[] getCurrentDur,getCurrentDis;

        try{
            JSONObject jsonObject = new JSONObject(JSON);
            //JSON 파일을 JSON객체로 바꿔준다.
            //String routes = jsonObject.getString("routes");
            //routesArray = new JSONArray(routes);
            routesArray = jsonObject.getJSONArray("routes");
            //Object에서 routes라는 이름의 키 값을 저장

            int i=0;

            do{
                //routes Array 배열의 길이만큼 반복을 돌리면서
                System.out.println("i검색  : "+ i);
                step="<<<<<<<<"+(i+1)+"번째 경로" +">>>>>>>>>>"+"\n\n";

                System.out.println("routesArray 길이 :"+routesArray.length());
                System.out.println("routesArray"+i+" : "+routesArray.get(i));

                legsArray = ((JSONObject)routesArray.get(i)).getJSONArray("legs");
                //JSONObject legJsonObject = legsArray.getJSONObject(i);
                JSONObject legJsonObject = legsArray.getJSONObject(0);


                //출발지, 도착지(나중에는 i=0일때만 들어와서 저장할 수 있도록 하기)
                if(i==0) {
                    str_Start = legJsonObject.getString("start_address");
                    str_End = legJsonObject.getString("end_address");
                    entire_step="출발지 : "+str_Start+"\n"
                            +"도착지 : "+str_End+"\n"
                            +"-------------------------------------\n";

                }

                //총 이동시간 => 이건 leg마다 다르니까 step에 같이 출력하기
                String duration = legJsonObject.getString("duration");
                //Object에서 키 값이 duration인 변수를 찾아서 저장
                JSONObject durJsonObject = new JSONObject(duration);
                //duration에도 Object가 존재하므로 Object를 변수에 저장
                //getDuration[j] = durJsonObject.getString("text");
                Dur= durJsonObject.getString("text");
                step+="총 이동시간 : "+ Dur+"\n\n";

                stepsArray = legJsonObject.getJSONArray("steps");
                list_len = stepsArray.length();

                getInstructions = new String[list_len]; //이동정보 저장
                getCurrentDur = new String[list_len];
                getCurrentDis = new String[list_len];
                arrival_name=new String[list_len]; //대중교통 도착지 저장
                depart_name=new String[list_len]; //대중교통 출발지 저장
                getHeadsign = new String[list_len];
                getBusNo = new String[list_len];//노선 정보(~호선, 버스번호)

                for(int k=0;k<list_len;k++) {
                    //확인
                    System.out.println("세번째 반복문 ");
                    System.out.println("stepsArray 길이 :" + list_len);
                    System.out.println("stepsArray" + k + "번째 : " + stepsArray.get(k));


                    JSONObject stepsObject = stepsArray.getJSONObject(k);
                    //이동정보 저장
                    getInstructions[k] = stepsObject.getString("html_instructions");

                    //각각의 이동시간(도보,대중교통)
                    String currnet_dur = stepsObject.getString("duration");
                    JSONObject CdurJsonObject = new JSONObject(currnet_dur);
                    getCurrentDur[k] = CdurJsonObject.getString("text");

                    //각각의 이동거리(도보,대중교통)
                    String currnet_dis = stepsObject.getString("distance");
                    JSONObject CdisJsonObject = new JSONObject(currnet_dis);
                    getCurrentDis[k] = CdisJsonObject.getString("text");


                    //현재 단계에서 대중교통을 이용하는지 확인
                    String[] Check = getInstructions[k].split(" ");
                    //대중교통 저장
                    String TransitCheck = Check[0];

                    //도보일 경우
                    if (Check[0].equals("Walk") || Check[1].equals("도보")) {
                        step += getInstructions[k] + "\n" + getCurrentDur[k] + "  |  " + getCurrentDis[k] + "이동\n";
                    }

                    if (TransitCheck.equals("Bus") || TransitCheck.equals("Subway")
                            || TransitCheck.equals("train") || TransitCheck.equals("rail")
                            || TransitCheck.equals("버스") || TransitCheck.equals("지하철")) {

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

                        step += "\n" + depart_name[k] + "승차"
                                + "\n 소요시간 : " + getCurrentDur[k] + " | " + "이동거리 : " + getCurrentDis[k]
                                + "\n" + arrival_name[k] + "하차" +
                                "\n" + getHeadsign[k] + "방향" +
                                "\n 번호: " + getBusNo[k] + "\n\n";


                    }

                    //확인
                    System.out.println("step 출력 : " + step);

                }

                entire_step+=step+"\n\n";

//                if (entire_step == null) {
//                    entire_step = step+"\n\n";
//                } else {
//                    entire_step += step+"\n\n";
//                }
                i++;
            }while(i<routesArray.length());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("entire_step : "+entire_step);
        return entire_step;
    }

    public String getDepartAddress(){
        return str_Start;
    }
    public String getArrivalAddress(){
        return str_End;
    }
    public String getDuration(){
        return Dur;
    }
}
