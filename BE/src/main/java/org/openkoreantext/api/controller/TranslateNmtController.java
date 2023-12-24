package org.openkoreantext.api.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openkoreantext.api.OpenKoreanTextHelper;
import org.openkoreantext.processor.phrase_extractor.KoreanPhraseExtractor;

import spark.Request;
import spark.Response;
import spark.Route;

// 네이버 기계번역 (Papago SMT) API 예제
public class TranslateNmtController {
        static OpenKoreanTextHelper tokenizerResource = new OpenKoreanTextHelper();

    final static String PARAM_TEXT = "text";

    public static Route translateNmt = (Request req, Response res) -> {
        String text = req.queryParams(PARAM_TEXT);
        String out = randomExtract(text);
        System.out.println(out);
        return out;
    };

    private static String transelateString(String text) throws ParseException{

        String clientId = "XAYIfez5B_vqjgiYXY8I";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "ArJ9gafB8x";//애플리케이션 클라이언트 시크릿값";
        String apiURL = "https://openapi.naver.com/v1/papago/n2mt";


        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        HttpURLConnection con = connect(apiURL);
        String postParams = "source=ko&target=en&text=" + text; //원본언어: 한국어 (ko) -> 목적언어: 영어 (en)
        try {
            con.setRequestMethod("POST");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postParams.getBytes());
                wr.flush();
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
                JSONParser jsonParser = new JSONParser();
                Object obj = jsonParser.parse(readBody(con.getInputStream()));
                JSONObject result = (JSONObject) ((JSONObject) obj).get("message");
                result = (JSONObject) result.get("result");
                
                return (String) result.get("translatedText");
            } else {  // 에러 응답
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static String randomExtract(String text) throws ParseException{
        List<KoreanPhraseExtractor.KoreanPhrase> extractPhrase = tokenizerResource.extractPhrase(text);
        List<String> phrases = new ArrayList<>();

        System.out.println(extractPhrase);
        for(KoreanPhraseExtractor.KoreanPhrase phrase : extractPhrase){
            phrases.add(phrase.toString());
        }

        if(phrases.size() == 0){
            return transelateString(text);
        } else{
            System.out.println(phrases);
            int random = (int)(Math.random() * phrases.size());
            System.out.println(random);
            String phrase = phrases.get(random).split("\\(")[0];
            System.out.println(phrase);
            String index = phrases.get(random).split("\\(")[1].split("\\)")[0];
            System.out.println(index);
            return transelateString(phrase)+" ("+index+")";
        }
    }

    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
}
