package com.hyeon.service;

import com.hyeon.model.GuildMember;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GuildMemberService {
    private static GuildMemberService instance;
    private ObservableList<GuildMember> members;
    private Long nextId = 1L;
    private final String DATA_FILE = "guild_members.dat";

    private GuildMemberService() {
        members = FXCollections.observableArrayList();
        loadData();
    }

    public static GuildMemberService getInstance() {
        if (instance == null) {
            instance = new GuildMemberService();
        }
        return instance;
    }

    // 전체 멤버 조회
    public ObservableList<GuildMember> getAllMembers() {
        return members;
    }

    // ID로 멤버 조회
    public GuildMember getMemberById(Long id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // 멤버 추가
    public void addMember(GuildMember member) {
        if (member.getId() == null) {
            member.setId(nextId++);
        }
        members.add(member);
        saveData();
    }

    // 멤버 수정
    public void updateMember(GuildMember updatedMember) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(updatedMember.getId())) {
                members.set(i, updatedMember);
                saveData();
                return;
            }
        }
    }

    // 멤버 삭제
    public void deleteMember(Long id) {
        members.removeIf(member -> member.getId().equals(id));
        saveData();
    }

    // 키워드로 검색
    public List<GuildMember> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(members);
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        return members.stream()
                .filter(member ->
                        (member.getName() != null && member.getName().toLowerCase().contains(lowerKeyword)) ||
                                member.getSubCharacters().stream().anyMatch(sub -> sub.toLowerCase().contains(lowerKeyword)) ||
                                member.getKnightCharacters().stream().anyMatch(knight -> knight.toLowerCase().contains(lowerKeyword))
                )
                .collect(Collectors.toList());
    }

    // 통계 정보
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMembers", members.size());
        stats.put("membersWithSubs", members.stream()
                .mapToLong(member -> member.getSubCharacters().isEmpty() ? 0 : 1)
                .sum());
        stats.put("membersWithKnights", members.stream()
                .mapToLong(member -> member.getKnightCharacters().isEmpty() ? 0 : 1)
                .sum());
        stats.put("totalSubCharacters", members.stream()
                .mapToInt(member -> member.getSubCharacters().size())
                .sum());
        stats.put("totalKnightCharacters", members.stream()
                .mapToInt(member -> member.getKnightCharacters().size())
                .sum());
        return stats;
    }

    // 데이터 저장
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            List<GuildMember> memberList = new ArrayList<>(members);
            oos.writeObject(memberList);
            oos.writeLong(nextId);
        } catch (IOException e) {
            System.err.println("데이터 저장 실패: " + e.getMessage());
        }
    }

    // 데이터 로드
    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            List<GuildMember> loadedMembers = (List<GuildMember>) ois.readObject();
            nextId = ois.readLong();
            members.addAll(loadedMembers);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("데이터 로드 실패: " + e.getMessage());
        }
    }

    // 데이터 초기화 (테스트용)
    public void clearAllData() {
        members.clear();
        nextId = 1L;
        saveData();
    }

    // 샘플 데이터 추가
    public void addSampleData() {
        if (!members.isEmpty()) {
            return; // 이미 데이터가 있으면 추가하지 않음
        }

        List<GuildMember> sampleMembers = Arrays.asList(
                new GuildMember("홍길동", Arrays.asList("길동부캐1", "길동부캐2"), Arrays.asList("길동기사1")),
                new GuildMember("김철수", Arrays.asList("철수부캐"), Arrays.asList("철수기사1", "철수기사2")),
                new GuildMember("이영희", new ArrayList<>(), Arrays.asList("영희기사")),
                new GuildMember("박민수", Arrays.asList("민수부캐1", "민수부캐2", "민수부캐3"), new ArrayList<>())
        );

        for (GuildMember member : sampleMembers) {
            addMember(member);
        }
    }
}