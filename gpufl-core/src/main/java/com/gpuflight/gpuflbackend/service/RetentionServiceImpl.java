package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.config.RetentionProperties;
import com.gpuflight.gpuflbackend.dao.RetentionDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetentionServiceImpl implements RetentionService {

    private final RetentionDao retentionDao;
    private final RetentionProperties props;

    @Scheduled(cron = "${gpufl.retention.cleanup-cron}")
    public void scheduledCleanup() {
        if (!props.isEnabled()) return;
        int deleted = runCleanup();
        log.info("Retention cleanup: deleted {} expired session(s)", deleted);
    }

    @Override
    @Transactional
    public int runCleanup() {
        List<String> expired = retentionDao.findExpiredSessionIds(props.getDefaultDays());
        if (expired.isEmpty()) return 0;
        retentionDao.deleteBySessionIds(expired);
        return expired.size();
    }
}
