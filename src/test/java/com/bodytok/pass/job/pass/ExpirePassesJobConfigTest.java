package com.bodytok.pass.job.pass;

import com.bodytok.pass.config.TestBatchConfig;
import com.bodytok.pass.repository.pass.PassEntity;
import com.bodytok.pass.repository.pass.PassRepository;
import com.bodytok.pass.repository.pass.PassStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;



@Slf4j
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@SpringJUnitConfig(classes = {ExpirePassesJobConfig.class, TestBatchConfig.class})
class ExpirePassesJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PassRepository passRepository;

    @Test
    public void test_expiredPassesStep() throws Exception {
        //Given

        addPassEntities(10);

        //When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance jobInstance = jobExecution.getJobInstance();

        //Then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals("expirePassesJob", jobInstance.getJobName());
    }


    private void addPassEntities(int size) {
        final LocalDateTime now = LocalDateTime.now();
        final Random random = new Random();

        List<PassEntity> passEntities = new ArrayList<>();
        for (int i = 0; i < size; ++i) {

            PassEntity passEntity = PassEntity.builder()
                    .packageSeq(1).userId("A" + 1000000 + i)
                    .status(PassStatus.PROGRESSED)
                    .remainingCount(random.nextInt(11))
                    .startedAt(now.minusDays(60))
                    .endedAt(now.minusDays(1))
                    .build();

            passEntities.add(passEntity);

        }
        passRepository.saveAll(passEntities);

    }

}
