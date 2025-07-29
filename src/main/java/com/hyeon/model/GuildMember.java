package com.hyeon.model;

import java.util.List;

public class GuildMember {
    private String name;
    private List<String> subCharacters;
    private List<String> knightCharacters;

    public GuildMember(String name, List<String> subCharacters, List<String> knightCharacters) {
        this.name = name;
        this.subCharacters = subCharacters;
        this.knightCharacters = knightCharacters;
    }

    public String getName() { return name; }
    public List<String> getSubCharacters() { return subCharacters; }
    public List<String> getKnightCharacters() { return knightCharacters; }
}