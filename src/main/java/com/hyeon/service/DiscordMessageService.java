package com.hyeon.service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordMessageService {

    // 다양한 디스코드 메시지 형식을 지원하는 패턴들
    private static final List<Pattern> MESSAGE_PATTERNS = Arrays.asList(
            // 한국어 형식: "사용자명 - 오늘/어제 오전/오후 시:분"
            Pattern.compile("^(.+?)\\s*-\\s*(오늘|어제)\\s+(오전|오후)\\s+\\d{1,2}:\\d{2}$"),
            // 한국어 날짜 형식: "사용자명 - YYYY/MM/DD 오전/오후 시:분"
            Pattern.compile("^(.+?)\\s*-\\s*\\d{4}/\\d{1,2}/\\d{1,2}\\s+(오전|오후)\\s+\\d{1,2}:\\d{2}$"),
            // 영어 형식: "번호. 닉네임 — 날짜시간" (em dash 사용)
            Pattern.compile("^(?:\\d+\\.\\s+)?([^—]+?)\\s+—\\s+(.+)$"),
            // 하이픈 형식: "닉네임 - 날짜시간"
            Pattern.compile("^([^-]+?)\\s+-\\s+(.+)$")
    );

    // 날짜 구분선 패턴
    private static final Pattern DATE_SEPARATOR_PATTERN = Pattern.compile("^\\d{4}년.*|^-{3,}.*|^={3,}.*");

    /**
     * 디스코드 메시지 텍스트를 파싱하여 사용자별로 분류
     * @param discordText 디스코드에서 복사한 메시지 텍스트
     * @return 사용자명을 키로 하고, 해당 사용자의 메시지 목록을 값으로 하는 Map
     */
    public static Map<String, List<String>> parseDiscordMessagesByUser(String discordText) {
        Map<String, List<String>> userMessages = new LinkedHashMap<>();

        if (discordText == null || discordText.trim().isEmpty()) {
            return userMessages;
        }

        String[] lines = discordText.split("\n");
        String currentUser = null;
        StringBuilder currentMessage = new StringBuilder();

        for (String line : lines) {
            line = line.trim();

            // 빈 줄이나 날짜 구분선 스킵
            if (line.isEmpty() || DATE_SEPARATOR_PATTERN.matcher(line).matches()) {
                continue;
            }

            // 새로운 메시지 시작인지 확인
            String detectedUser = detectMessageStart(line);

            if (detectedUser != null) {
                // 이전 메시지가 있으면 저장
                saveCurrentMessage(userMessages, currentUser, currentMessage);

                // 새 사용자와 메시지 시작
                currentUser = detectedUser;
                currentMessage = new StringBuilder();
            } else {
                // 메시지 내용 추가
                appendMessageContent(currentMessage, line);
            }
        }

        // 마지막 메시지 저장
        saveCurrentMessage(userMessages, currentUser, currentMessage);

        return userMessages;
    }

    /**
     * 라인이 새로운 메시지 시작인지 확인하고 사용자명 반환
     */
    private static String detectMessageStart(String line) {
        for (Pattern pattern : MESSAGE_PATTERNS) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String username = matcher.group(1);
                return cleanUsername(username);
            }
        }
        return null;
    }

    /**
     * 현재 메시지를 사용자 메시지 맵에 저장
     */
    private static void saveCurrentMessage(Map<String, List<String>> userMessages,
                                           String currentUser,
                                           StringBuilder currentMessage) {
        if (currentUser != null && currentMessage.length() > 0) {
            String messageContent = currentMessage.toString().trim();
            if (!messageContent.isEmpty()) {
                userMessages.computeIfAbsent(currentUser, k -> new ArrayList<>())
                        .add(messageContent);
            }
        }
    }

    /**
     * 메시지 내용을 StringBuilder에 추가
     */
    private static void appendMessageContent(StringBuilder currentMessage, String line) {
        if (currentMessage.length() > 0) {
            currentMessage.append("\n");
        }
        currentMessage.append(line);
    }

    /**
     * 사용자명에서 불필요한 문자들 제거 및 정리
     */
    private static String cleanUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Unknown User";
        }

        // 앞뒤 공백 제거
        username = username.trim();

        // 특수문자, 이모지, 역할 표시 등 제거
        username = username.replaceAll("[•ᴗ*\\(\\)\\[\\]【】「」]", "");
        username = username.replaceAll("연습생|봇|BOT|bot", "");

        // 연속된 공백을 하나로 변경
        username = username.replaceAll("\\s+", " ");

        // 최종 정리
        username = username.trim();

        return username.isEmpty() ? "Unknown User" : username;
    }

    /**
     * 메시지 통계 정보 생성
     */
    public static Map<String, Object> getMessageStatistics(Map<String, List<String>> userMessages) {
        Map<String, Object> stats = new HashMap<>();

        int totalUsers = userMessages.size();
        int totalMessages = userMessages.values().stream()
                .mapToInt(List::size)
                .sum();

        // 가장 많이 메시지를 보낸 사용자
        Optional<Map.Entry<String, List<String>>> mostActiveEntry = userMessages.entrySet().stream()
                .max(Map.Entry.comparingByValue(Comparator.comparing(List::size)));

        String mostActiveUser = mostActiveEntry.map(Map.Entry::getKey).orElse("없음");
        int maxMessages = mostActiveEntry.map(entry -> entry.getValue().size()).orElse(0);

        // 평균 메시지 길이 계산
        double averageMessageLength = userMessages.values().stream()
                .flatMap(List::stream)
                .mapToInt(String::length)
                .average()
                .orElse(0.0);

        stats.put("totalUsers", totalUsers);
        stats.put("totalMessages", totalMessages);
        stats.put("mostActiveUser", mostActiveUser);
        stats.put("mostActiveUserMessages", maxMessages);
        stats.put("averageMessagesPerUser", totalUsers > 0 ? (double) totalMessages / totalUsers : 0.0);
        stats.put("averageMessageLength", Math.round(averageMessageLength * 100.0) / 100.0);

        return stats;
    }

    /**
     * 사용자별 메시지를 텍스트 형태로 포맷팅
     */
    public static String formatMessagesAsText(Map<String, List<String>> userMessages) {
        if (userMessages.isEmpty()) {
            return "분석할 메시지가 없습니다.";
        }

        StringBuilder formatted = new StringBuilder();

        // 통계 정보 추가
        Map<String, Object> stats = getMessageStatistics(userMessages);
        formatted.append("디스코드 메시지 분석 결과\n");
        formatted.append("=".repeat(50)).append("\n");
        formatted.append(String.format("총 사용자: %d명\n", stats.get("totalUsers")));
        formatted.append(String.format("총 메시지: %d개\n", stats.get("totalMessages")));
        formatted.append(String.format("가장 활발한 사용자: %s (%d개 메시지)\n",
                stats.get("mostActiveUser"), stats.get("mostActiveUserMessages")));
        formatted.append(String.format("평균 메시지 길이: %.1f자\n", stats.get("averageMessageLength")));
        formatted.append("=".repeat(50)).append("\n\n");

        // 메시지 수 기준으로 정렬 (내림차순)
        userMessages.entrySet().stream()
                .sorted(Map.Entry.<String, List<String>>comparingByValue(
                        (list1, list2) -> Integer.compare(list2.size(), list1.size())))
                .forEach(entry -> {
                    formatted.append("👤 ").append(entry.getKey())
                            .append(" (").append(entry.getValue().size()).append("개 메시지)")
                            .append("\n").append("-".repeat(30)).append("\n");

                    for (int i = 0; i < entry.getValue().size(); i++) {
                        String singleLineMessage = entry.getValue().get(i).replace("\n", " ");
                        formatted.append(String.format("[%d] %s\n", i + 1, singleLineMessage));
                    }
                    formatted.append("\n");
                });

        return formatted.toString();
    }

    /**
     * 키워드로 메시지 검색
     */
    public static Map<String, List<String>> searchMessages(Map<String, List<String>> userMessages, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userMessages;
        }

        Map<String, List<String>> filteredMessages = new LinkedHashMap<>();
        String lowerKeyword = keyword.toLowerCase().trim();

        for (Map.Entry<String, List<String>> entry : userMessages.entrySet()) {
            List<String> matchingMessages = entry.getValue().stream()
                    .filter(message -> message.toLowerCase().contains(lowerKeyword))
                    .toList();

            if (!matchingMessages.isEmpty()) {
                filteredMessages.put(entry.getKey(), matchingMessages);
            }
        }

        return filteredMessages;
    }

    /**
     * 사용자명으로 메시지 검색
     */
    public static Map<String, List<String>> searchByUser(Map<String, List<String>> userMessages, String username) {
        if (username == null || username.trim().isEmpty()) {
            return userMessages;
        }

        Map<String, List<String>> filteredMessages = new LinkedHashMap<>();
        String lowerUsername = username.toLowerCase().trim();

        for (Map.Entry<String, List<String>> entry : userMessages.entrySet()) {
            if (entry.getKey().toLowerCase().contains(lowerUsername)) {
                filteredMessages.put(entry.getKey(), entry.getValue());
            }
        }

        return filteredMessages;
    }

    /**
     * 메시지 길이 기준으로 필터링
     */
    public static Map<String, List<String>> filterByMessageLength(Map<String, List<String>> userMessages,
                                                                  int minLength, int maxLength) {
        Map<String, List<String>> filteredMessages = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : userMessages.entrySet()) {
            List<String> filteredList = entry.getValue().stream()
                    .filter(message -> message.length() >= minLength && message.length() <= maxLength)
                    .toList();

            if (!filteredList.isEmpty()) {
                filteredMessages.put(entry.getKey(), filteredList);
            }
        }

        return filteredMessages;
    }
}