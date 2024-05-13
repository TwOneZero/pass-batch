package com.bodytok.pass.job.notification;

import com.bodytok.pass.adaptor.KakaoTalkMessageAdapter;
import com.bodytok.pass.config.TestBatchConfig;
import com.bodytok.pass.repository.booking.BookingEntity;
import com.bodytok.pass.repository.booking.BookingRepository;
import com.bodytok.pass.repository.booking.BookingStatus;
import com.bodytok.pass.repository.notification.NotificationEntity;
import com.bodytok.pass.repository.notification.NotificationRepository;
import com.bodytok.pass.repository.pass.PassEntity;
import com.bodytok.pass.repository.pass.PassRepository;
import com.bodytok.pass.repository.pass.PassStatus;
import com.bodytok.pass.repository.user.UserEntity;
import com.bodytok.pass.repository.user.UserRepository;
import com.bodytok.pass.repository.user.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {SendNotificationBeforeClassJobConfig.class, TestBatchConfig.class})
public class SendNotificationBeforeClassJobConfigTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PassRepository passRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private SendNotificationItemWriter sendNotificationItemWriter;

    @MockBean
    private KakaoTalkMessageAdapter kakaoTalkMessageAdapter;


    @Test
    public void test_addNotificationStep_success() throws Exception {
        // given
        addBookingEntity();
        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("addNotificationStep");

        // then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

    }

    private void addBookingEntity() {
        final LocalDateTime now = LocalDateTime.now();
        var rand = (int)(Math.random() * 8999) + 1000;
        final String userId = "A100" + rand;

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId);
        userEntity.setUserName("김영희");
        userEntity.setStatus(UserStatus.ACTIVE);
        userEntity.setPhone("01033334444");
        userEntity.setMeta(Map.of("uuid", "abcd1234"));
        userRepository.save(userEntity);

        PassEntity passEntity = new PassEntity();
        passEntity.setPackageSeq(1);
        passEntity.setUserId(userId);
        passEntity.setStatus(PassStatus.PROGRESSED);
        passEntity.setRemainingCount(10);
        passEntity.setStartedAt(now.minusDays(60));
        passEntity.setEndedAt(now.minusDays(1));
        passRepository.save(passEntity);

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setPassSeq(passEntity.getPassSeq());
        bookingEntity.setUserId(userId);
        bookingEntity.setStatus(BookingStatus.READY);
        bookingEntity.setStartedAt(now.plusMinutes(10));
        bookingEntity.setEndedAt(bookingEntity.getStartedAt().plusMinutes(50));
        bookingRepository.save(bookingEntity);

    }
}