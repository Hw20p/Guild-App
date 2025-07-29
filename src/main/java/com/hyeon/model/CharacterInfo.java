package com.hyeon.model;

public class CharacterInfo {
    private String name;
    private String ocid;
    private String level;
    private String job;
    private String worldName;

    public CharacterInfo(String name, String ocid) {
        this.name = name;
        this.ocid = ocid;
    }

    // 추가 생성자
    public CharacterInfo(String name, String ocid, String level, String job, String worldName) {
        this.name = name;
        this.ocid = ocid;
        this.level = level;
        this.job = job;
        this.worldName = worldName;
    }

    public String getName() { return name; }
    public String getOcid() { return ocid; }
    public String getLevel() { return level; }
    public String getJob() { return job; }
    public String getWorldName() { return worldName; }
}
