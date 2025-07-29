package com.hyeon.service;

import com.hyeon.model.GuildMember;
import com.hyeon.repository.GuildMemberRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class GuildMemberService {
    private static GuildMemberService instance;
    private final GuildMemberRepository repository;
    private ObservableList<GuildMember> members;

    private GuildMemberService() {
        this.repository = new GuildMemberRepository();
        this.members = FXCollections.observableArrayList();
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
        return repository.findById(id).orElse(null);
    }

    // 멤버 추가
    public void addMember(GuildMember member) {
        GuildMember savedMember = repository.save(member);
        members.add(savedMember);
    }

    // 멤버 수정
    public void updateMember(GuildMember updatedMember) {
        repository.save(updatedMember);
        // ObservableList에서도 업데이트
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(updatedMember.getId())) {
                members.set(i, updatedMember);
                break;
            }
        }
    }

    // 멤버 삭제
    public void deleteMember(Long id) {
        repository.deleteById(id);
        members.removeIf(member -> member.getId().equals(id));
    }

    // 키워드로 검색
    public List<GuildMember> searchByKeyword(String keyword) {
        return repository.searchByKeyword(keyword);
    }

    // 통계 정보
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalMembers = repository.count();
        long membersWithSubs = members.stream()
                .mapToLong(member -> member.getSubCharacters().isEmpty() ? 0 : 1)
                .sum();
        long membersWithKnights = members.stream()
                .mapToLong(member -> member.getKnightCharacters().isEmpty() ? 0 : 1)
                .sum();
        int totalSubCharacters = members.stream()
                .mapToInt(member -> member.getSubCharacters().size())
                .sum();
        int totalKnightCharacters = members.stream()
                .mapToInt(member -> member.getKnightCharacters().size())
                .sum();

        stats.put("totalMembers", totalMembers);
        stats.put("membersWithSubs", membersWithSubs);
        stats.put("membersWithKnights", membersWithKnights);
        stats.put("totalSubCharacters", totalSubCharacters);
        stats.put("totalKnightCharacters", totalKnightCharacters);

        return stats;
    }

    // 데이터 로드
    private void loadData() {
        List<GuildMember> loadedMembers = repository.findAll();
        members.setAll(loadedMembers);
    }

    // 데이터 새로고침
    public void refreshData() {
        loadData();
    }

    // 데이터 초기화
    public void clearAllData() {
        repository.deleteAll();
        members.clear();
    }

    // 샘플 데이터 추가
    public void addSampleData() {
        if (!members.isEmpty()) {
            return; // 이미 데이터가 있으면 추가하지 않음
        }

        List<GuildMember> sampleMembers = Arrays.asList(
                new GuildMember("요마", new ArrayList<>(), Arrays.asList("검기사, 창기사, 탱기사, 쌍기사"))
        );

        for (GuildMember member : sampleMembers) {
            addMember(member);
        }
    }
}