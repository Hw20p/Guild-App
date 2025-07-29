package com.hyeon.repository;

import com.hyeon.model.GuildMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuildMemberRepository {
    private static final String DB_URL = "jdbc:h2:./data/guild_members;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    public GuildMemberRepository() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = getConnection()) {
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS guild_members (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    sub_characters TEXT,
                    knight_characters TEXT
                )
                """;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }
        } catch (SQLException e) {
            throw new RuntimeException("데이터베이스 초기화 실패", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public List<GuildMember> findAll() {
        List<GuildMember> members = new ArrayList<>();
        String sql = "SELECT * FROM guild_members ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapResultSetToGuildMember(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("멤버 조회 실패", e);
        }

        return members;
    }

    public Optional<GuildMember> findById(Long id) {
        String sql = "SELECT * FROM guild_members WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuildMember(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("멤버 조회 실패", e);
        }

        return Optional.empty();
    }

    public GuildMember save(GuildMember member) {
        if (member.getId() == null) {
            return insert(member);
        } else {
            return update(member);
        }
    }

    private GuildMember insert(GuildMember member) {
        String sql = "INSERT INTO guild_members (name, sub_characters, knight_characters) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getSubCharactersString());
            pstmt.setString(3, member.getKnightCharactersString());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("멤버 생성 실패");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    member.setId(generatedKeys.getLong(1));
                }
            }

            return member;
        } catch (SQLException e) {
            throw new RuntimeException("멤버 생성 실패", e);
        }
    }

    private GuildMember update(GuildMember member) {
        String sql = "UPDATE guild_members SET name = ?, sub_characters = ?, knight_characters = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getSubCharactersString());
            pstmt.setString(3, member.getKnightCharactersString());
            pstmt.setLong(4, member.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("멤버 수정 실패");
            }

            return member;
        } catch (SQLException e) {
            throw new RuntimeException("멤버 수정 실패", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM guild_members WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("멤버 삭제 실패", e);
        }
    }

    public List<GuildMember> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        List<GuildMember> members = new ArrayList<>();
        String sql = """
            SELECT * FROM guild_members 
            WHERE LOWER(name) LIKE ? 
               OR LOWER(sub_characters) LIKE ? 
               OR LOWER(knight_characters) LIKE ?
            ORDER BY id
            """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword.toLowerCase().trim() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSetToGuildMember(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("검색 실패", e);
        }

        return members;
    }

    public void deleteAll() {
        String sql = "DELETE FROM guild_members";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("전체 데이터 삭제 실패", e);
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM guild_members";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("카운트 조회 실패", e);
        }

        return 0;
    }

    private GuildMember mapResultSetToGuildMember(ResultSet rs) throws SQLException {
        GuildMember member = new GuildMember();
        member.setId(rs.getLong("id"));
        member.setName(rs.getString("name"));
        member.setSubCharacters(rs.getString("sub_characters"));
        member.setKnightCharacters(rs.getString("knight_characters"));
        return member;
    }
}