package com.bodytok.pass.job.notification;


import com.bodytok.pass.repository.booking.BookingEntity;
import com.bodytok.pass.repository.booking.BookingStatus;
import com.bodytok.pass.repository.notification.NotificationEntity;
import com.bodytok.pass.repository.notification.NotificationEvent;
import com.bodytok.pass.repository.notification.NotificationModelMapper;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class SendNotificationBeforeClassJobConfig {

    private final int CHUNK_SIZE = 10;

    private final SendNotificationItemWriter sendNotificationItemWriter;
    private final EntityManagerFactory entityManagerFactory;


    @Bean
    public Job sendNotificationBeforeClassJob(JobRepository jobRepository, Step addNotificationStep) {
        return new JobBuilder("sendNotificationBeforeClassJob", jobRepository)
                .start(addNotificationStep)
                .build();
    }

    @Bean
    public Step addNotificationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("addNotificationStep", jobRepository)
                .<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(addNotificationItemReader())
                .processor(addNotificationItemProcessor())
                .writer(addNotificationItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<BookingEntity> addNotificationItemReader() {
        // 상태(status)가 준비 중 , 시작일시(startedAt)가 10분 후 시작하는 예약이 알람 대상이 됨.
        return new JpaPagingItemReaderBuilder<BookingEntity>()
                .name("addNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("select b from BookingEntity b join fetch b.userEntity where b.status = :status and b.startedAt <= :startedAt order by b.bookingSeq")
                .parameterValues(Map.of("status", BookingStatus.READY, "startedAt", LocalDateTime.now().plusMinutes(10)))
                .build();
    }

    @Bean
    public ItemProcessor<BookingEntity, NotificationEntity> addNotificationItemProcessor() {
        return bookingEntity -> NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity, NotificationEvent.BEFORE_CLASS);
    }

    @Bean
    public JpaItemWriter<NotificationEntity> addNotificationItemWriter() {
        return new JpaItemWriterBuilder<NotificationEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    /// 두번 째 Step
    @Bean
    public Step sendNotificationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendNotificationStep", jobRepository)
                .<NotificationEntity, NotificationEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(sendNotificationItemReader())
                .writer(sendNotificationItemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    //Thread-safe 위해 한 번 감싸줌
    @Bean
    public SynchronizedItemStreamReader<NotificationEntity> sendNotificationItemReader() {
        //이벤트가 `수업 전` 이며, 발송 여부 (sent) 가 `미발송(0)` 인 알람이 조회 대상
        JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
                .name("sendNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select n from NotificationEntity n where n.event = :event and n.sent = :sent")
                .parameterValues(Map.of("event",NotificationEvent.BEFORE_CLASS, "sent",false))
                .build();

        return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
                .delegate(itemReader)
                .build();
    }


}
