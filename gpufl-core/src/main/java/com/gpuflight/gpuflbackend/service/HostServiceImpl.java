package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.CudaDeviceDao;
import com.gpuflight.gpuflbackend.dao.SessionDao;
import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;
import com.gpuflight.gpuflbackend.entity.SessionEntity;
import com.gpuflight.gpuflbackend.mapper.CudaStaticDeviceMapper;
import com.gpuflight.gpuflbackend.model.presentation.CudaStaticDeviceDto;
import com.gpuflight.gpuflbackend.model.presentation.HostSummaryDto;
import com.gpuflight.gpuflbackend.model.presentation.SessionSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HostServiceImpl implements HostService {

    private final SessionDao sessionDao;
    private final CudaDeviceDao cudaDeviceDao;

    @Override
    public List<HostSummaryDto> getHostSummaries() {
        List<SessionEntity> allSessions = sessionDao.findAll();
        if (allSessions.isEmpty()) {
            return List.of();
        }

        Set<String> sessionIds = allSessions.stream()
                .map(SessionEntity::getSessionId)
                .collect(Collectors.toSet());

        List<CudaStaticDeviceEntity> allDevices = cudaDeviceDao.findBySessionIds(sessionIds);
        Map<String, List<CudaStaticDeviceDto>> devicesBySession = allDevices.stream()
                .collect(Collectors.groupingBy(
                        CudaStaticDeviceEntity::getSessionId,
                        Collectors.mapping(CudaStaticDeviceMapper::mapToCudaStaticDeviceDto, Collectors.toList())
                ));

        // Group sessions by hostname, preserving insertion order (sessions are already newest-first)
        Map<String, List<SessionEntity>> sessionsByHost = new LinkedHashMap<>();
        for (SessionEntity s : allSessions) {
            String host = s.getHostname() != null ? s.getHostname() : "unknown";
            sessionsByHost.computeIfAbsent(host, k -> new ArrayList<>()).add(s);
        }

        List<HostSummaryDto> result = new ArrayList<>();
        for (Map.Entry<String, List<SessionEntity>> entry : sessionsByHost.entrySet()) {
            String hostname = entry.getKey();
            List<SessionEntity> hostSessions = entry.getValue();
            String ipAddr = hostSessions.stream()
                    .filter(s -> s.getIpAddr() != null)
                    .map(SessionEntity::getIpAddr)
                    .findFirst()
                    .orElse(null);

            List<SessionSummaryDto> sessionDtos = hostSessions.stream()
                    .map(s -> new SessionSummaryDto(
                            s.getSessionId(),
                            s.getAppName(),
                            s.getStartTime(),
                            s.getEndTime(),
                            devicesBySession.getOrDefault(s.getSessionId(), List.of())
                    ))
                    .toList();

            result.add(new HostSummaryDto(hostname, ipAddr, sessionDtos));
        }

        return result;
    }
}
