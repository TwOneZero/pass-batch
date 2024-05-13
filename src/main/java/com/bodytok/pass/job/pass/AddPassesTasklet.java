package com.bodytok.pass.job.pass;

import com.bodytok.pass.repository.pass.*;
import com.bodytok.pass.repository.user.UserGroupMappingEntity;
import com.bodytok.pass.repository.user.UserGroupMappingRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Component
public class AddPassesTasklet implements Tasklet {

    private final PassRepository passRepository;
    private final BulkPassRepository bulkPassRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;


    @Override
    public RepeatStatus execute(
            @NonNull StepContribution contribution,
            @NonNull ChunkContext chunkContext) throws Exception {

        // 이용권 시작 일시 1일전 group 내 사용자에게 이용권 추가
        final LocalDateTime startedAt = LocalDateTime.now().minusDays(1);
        final List<BulkPassEntity> bulkPassEntities =
                bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY,startedAt);

        int count=0;

        for (BulkPassEntity bulkPassEntity : bulkPassEntities) {
            String userGroupId = bulkPassEntity.getUserGroupId();
            final List<String> userIds =
                    userGroupMappingRepository.findByUserGroupId(userGroupId)
                            .stream().map(UserGroupMappingEntity::getUserId)
                            .toList();
            count += addPasses(bulkPassEntity, userIds);

            bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
        }


        log.info("AddPassesTasklet : execute : {}건의 이용권 추가 완료, startedAt={}", count, startedAt);
        return RepeatStatus.FINISHED;
    }

    // bulkPass 정보로 pass 데이터 생성
    private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds) {
        List<PassEntity> passEntities = new ArrayList<>();

        for(String userId : userIds) {
            PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);
            passEntities.add(passEntity);
        }
        return passRepository.saveAll(passEntities).size();
    }
}
