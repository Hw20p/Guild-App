package com.hyeon.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuildMember implements Serializable {
    private Long id;
    private String name;
    private String subCharacters; // List를 문자열로 변경 (콤마 구분)
    private String knightCharacters; // List를 문자열로 변경 (콤마 구분)

    // 기본 생성자
    public GuildMember() {
        this.subCharacters = "";
        this.knightCharacters = "";
    }

    public GuildMember(String name, List<String> subCharacters, List<String> knightCharacters) {
        this.name = name;
        this.subCharacters = listToString(subCharacters);
        this.knightCharacters = listToString(knightCharacters);
    }

    public GuildMember(Long id, String name, List<String> subCharacters, List<String> knightCharacters) {
        this.id = id;
        this.name = name;
        this.subCharacters = listToString(subCharacters);
        this.knightCharacters = listToString(knightCharacters);
    }

    // 기본 Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // DB용 문자열 필드 접근자
    public String getSubCharactersString() {
        return subCharacters;
    }

    public void setSubCharacters(String subCharacters) {
        this.subCharacters = subCharacters != null ? subCharacters : "";
    }

    public String getKnightCharactersString() {
        return knightCharacters;
    }

    public void setKnightCharacters(String knightCharacters) {
        this.knightCharacters = knightCharacters != null ? knightCharacters : "";
    }

    // List로 반환하는 메서드들 (기존 호환성 유지)
    public List<String> getSubCharacters() {
        return stringToList(subCharacters);
    }

    public void setSubCharacters(List<String> subCharacters) {
        this.subCharacters = listToString(subCharacters);
    }

    public List<String> getKnightCharacters() {
        return stringToList(knightCharacters);
    }

    public void setKnightCharacters(List<String> knightCharacters) {
        this.knightCharacters = listToString(knightCharacters);
    }

    // 유틸리티 메서드들
    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }

    private List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(str.split(",")));
    }

    public void addSubCharacter(String subCharacter) {
        if (subCharacter != null && !subCharacter.trim().isEmpty()) {
            List<String> currentSubs = getSubCharacters();
            currentSubs.add(subCharacter.trim());
            setSubCharacters(currentSubs);
        }
    }

    public void addKnightCharacter(String knightCharacter) {
        if (knightCharacter != null && !knightCharacter.trim().isEmpty()) {
            List<String> currentKnights = getKnightCharacters();
            currentKnights.add(knightCharacter.trim());
            setKnightCharacters(currentKnights);
        }
    }

    public void removeSubCharacter(String subCharacter) {
        List<String> currentSubs = getSubCharacters();
        currentSubs.remove(subCharacter);
        setSubCharacters(currentSubs);
    }

    public void removeKnightCharacter(String knightCharacter) {
        List<String> currentKnights = getKnightCharacters();
        currentKnights.remove(knightCharacter);
        setKnightCharacters(currentKnights);
    }

    @Override
    public String toString() {
        List<String> subs = getSubCharacters();
        List<String> knights = getKnightCharacters();

        return String.format("이름: %s | 부캐: %s | 기사: %s",
                name,
                subs.isEmpty() ? "없음" : String.join(", ", subs),
                knights.isEmpty() ? "없음" : String.join(", ", knights));
    }

    // equals와 hashCode (ID 기준)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GuildMember that = (GuildMember) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}