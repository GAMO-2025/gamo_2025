package gamo.web.member.service;

import gamo.web.member.repository.DeletedMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDeleteSchedulerService {
    private final DeletedMemberRepository deletedMemberRepository;

    //12시마다 스케줄러 작동
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduledDeleteMembers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        int deletedCount = deletedMemberRepository.deleteAllByDeletedAtBefore(threshold);

        log.info("[Scheduler] {}건의 탈퇴 회원 데이터 삭제 완료 (삭제 기준일: {})", deletedCount, threshold);
    }
}
