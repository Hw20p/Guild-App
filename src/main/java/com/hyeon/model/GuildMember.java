package com.hyeon.model;

import java.util.ArrayList;
import java.util.List;

public class GuildMember {
    private Long id;
    private String name;
    private List<String> subCharacters;
    private List<String> knightCharacters;

    // 생성자
    public GuildMember() {
        this.subCharacters = new ArrayList<>();
        this.knightCharacters = new ArrayList<>();
    }

    public GuildMember(String name, List<String> subCharacters, List<String> knightCharacters) {
        this.name = name;
        this.subCharacters = subCharacters != null ? new ArrayList<>(subCharacters) : new ArrayList<>();
        this.knightCharacters = knightCharacters != null ? new ArrayList<>(knightCharacters) : new ArrayList<>();
    }

    public GuildMember(Long id, String name, List<String> subCharacters, List<String> knightCharacters) {
        this.id = id;
        this.name = name;
        this.subCharacters = subCharacters != null ? new ArrayList<>(subCharacters) : new ArrayList<>();
        this.knightCharacters = knightCharacters != null ? new ArrayList<>(knightCharacters) : new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getSubCharacters() { return subCharacters; }
    public void setSubCharacters(List<String> subCharacters) {
        this.subCharacters = subCharacters != null ? new ArrayList<>(subCharacters) : new ArrayList<>();
    }

    public List<String> getKnightCharacters() { return knightCharacters; }
    public void setKnightCharacters(List<String> knightCharacters) {
        this.knightCharacters = knightCharacters != null ? new ArrayList<>(knightCharacters) : new ArrayList<>();
    }

    // 유틸리티 메서드
    public void addSubCharacter(String subCharacter) {
        if (subCharacter != null && !subCharacter.trim().isEmpty()) {
            this.subCharacters.add(subCharacter.trim());
        }
    }

    public void addKnightCharacter(String knightCharacter) {
        if (knightCharacter != null && !knightCharacter.trim().isEmpty()) {
            this.knightCharacters.add(knightCharacter.trim());
        }
    }

    public void removeSubCharacter(String subCharacter) {
        this.subCharacters.remove(subCharacter);
    }

    public void removeKnightCharacter(String knightCharacter) {
        this.knightCharacters.remove(knightCharacter);
    }

    @Override
    public String toString() {
        return String.format("이름: %s | 부캐: %s | 기사: %s",
                name,
                subCharacters.isEmpty() ? "없음" : String.join(", ", subCharacters),
                knightCharacters.isEmpty() ? "없음" : String.join(", ", knightCharacters));
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