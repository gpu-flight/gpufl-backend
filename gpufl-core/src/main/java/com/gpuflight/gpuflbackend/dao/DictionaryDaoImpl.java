package com.gpuflight.gpuflbackend.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;

@Repository
public class DictionaryDaoImpl implements DictionaryDao {
    private final JdbcTemplate jdbcTemplate;

    public DictionaryDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void upsert(String sessionId, String dictType, int dictId, String name) {
        jdbcTemplate.update("""
            INSERT INTO session_dictionaries (session_id, dict_type, dict_id, name)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (session_id, dict_type, dict_id) DO UPDATE SET name = EXCLUDED.name
            """, sessionId, dictType, dictId, name);
    }

    @Override
    public Map<Integer, String> loadDict(String sessionId, String dictType) {
        Map<Integer, String> result = new HashMap<>();
        jdbcTemplate.query(
            "SELECT dict_id, name FROM session_dictionaries WHERE session_id = ? AND dict_type = ?",
            rs -> { result.put(rs.getInt("dict_id"), rs.getString("name")); },
            sessionId, dictType
        );
        return result;
    }
}
