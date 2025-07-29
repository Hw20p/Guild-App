package com.hyeon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeon.model.CharacterInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class NexonApiService {
    private static final String API_KEY = "test_f0308276b6ff3e284f0e73114427db9f38c6cd223598a036c9b413e9c368b38eefe8d04e6d233bd35cf2fabdeb93fb0d";
    private static final String BASE_URL = "https://open.api.nexon.com/maplestory/v1";

    // 캐릭터 ocid 조회
    public String fetchCharacterId(String characterName) throws Exception {
        if (characterName == null || characterName.trim().isEmpty()) {
            throw new IllegalArgumentException("캐릭터 이름이 필요합니다.");
        }

        String encodedName = URLEncoder.encode(characterName, StandardCharsets.UTF_8);
        String apiUrl = BASE_URL + "/id?character_name=" + encodedName;

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("x-nxopen-api-key", API_KEY);
            conn.setConnectTimeout(5000); // 5초 연결 타임아웃
            conn.setReadTimeout(10000);   // 10초 읽기 타임아웃

            int responseCode = conn.getResponseCode();
            if (responseCode == 404) {
                throw new Exception("존재하지 않는 캐릭터입니다: " + characterName);
            } else if (responseCode != 200) {
                throw new Exception("API 호출 실패: HTTP " + responseCode);
            }

            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(in);

                if (jsonNode.has("ocid")) {
                    return jsonNode.get("ocid").asText();
                } else {
                    throw new Exception("응답에서 캐릭터 ID를 찾을 수 없습니다.");
                }
            }
        } catch (IOException e) {
            throw new Exception("네트워크 오류가 발생했습니다: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // 캐릭터 기본 정보 조회
    public CharacterInfo fetchCharacterInfo(String name, String ocid) throws Exception {
        if (ocid == null || ocid.trim().isEmpty()) {
            throw new IllegalArgumentException("캐릭터 ID가 필요합니다.");
        }

        // 어제 날짜로 설정 (넥슨 API는 하루 전 데이터만 제공)
        String yesterday = LocalDate.now().minusDays(1).toString();
        String apiUrl = BASE_URL + "/character/basic?ocid=" + ocid + "&date=" + yesterday;

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("x-nxopen-api-key", API_KEY);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 404) {
                throw new Exception("캐릭터 정보를 찾을 수 없습니다.");
            } else if (responseCode != 200) {
                throw new Exception("API 호출 실패: HTTP " + responseCode);
            }

            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(in);

                // 필수 필드 확인 및 추출
                String level = jsonNode.has("character_level") ?
                        jsonNode.get("character_level").asText() : "0";
                String job = jsonNode.has("character_class") ?
                        jsonNode.get("character_class").asText() : "알 수 없음";
                String world = jsonNode.has("world_name") ?
                        jsonNode.get("world_name").asText() : "알 수 없음";

                return new CharacterInfo(name, ocid, level, job, world);
            }
        } catch (IOException e) {
            throw new Exception("네트워크 오류가 발생했습니다: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // 캐릭터 능력치 정보 조회 (추가 기능)
    public JsonNode fetchCharacterStats(String ocid) throws Exception {
        String yesterday = LocalDate.now().minusDays(1).toString();
        String apiUrl = BASE_URL + "/character/stat?ocid=" + ocid + "&date=" + yesterday;

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("x-nxopen-api-key", API_KEY);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                throw new Exception("스탯 정보를 가져올 수 없습니다.");
            }

            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readTree(in);
            }
        } catch (IOException e) {
            throw new Exception("네트워크 오류: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // 캐릭터 장비 정보 조회 (추가 기능)
    public JsonNode fetchCharacterEquipment(String ocid) throws Exception {
        String yesterday = LocalDate.now().minusDays(1).toString();
        String apiUrl = BASE_URL + "/character/item-equipment?ocid=" + ocid + "&date=" + yesterday;

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("x-nxopen-api-key", API_KEY);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                throw new Exception("장비 정보를 가져올 수 없습니다.");
            }

            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readTree(in);
            }
        } catch (IOException e) {
            throw new Exception("네트워크 오류: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}