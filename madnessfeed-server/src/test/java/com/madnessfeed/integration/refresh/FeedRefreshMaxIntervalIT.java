package com.madnessfeed.integration.refresh;

import com.madnessfeed.TestConstants;
import com.madnessfeed.backend.dao.FeedDAO;
import com.madnessfeed.backend.dao.FeedSubscriptionDAO;
import com.madnessfeed.backend.dao.UnitOfWork;
import com.madnessfeed.backend.model.Feed;
import com.madnessfeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@QuarkusTest
class FeedRefreshMaxIntervalIT extends BaseIT {

    @Inject UnitOfWork unitOfWork;
    @Inject FeedDAO feedDAO;
    @Inject FeedSubscriptionDAO feedSubscriptionDAO;

    @BeforeEach
    void setup() {
        initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
        RestAssured.authentication =
                RestAssured.preemptive()
                        .basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
    }

    @AfterEach
    void cleanup() {
        RestAssured.reset();
    }

    @Test
    void feedsScheduledAfterMaxAreBroughtBack() {
        Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
        Long feedId =
                unitOfWork.call(
                        () -> feedSubscriptionDAO.findById(subscriptionId).getFeed().getId());

        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant max = now.plus(Duration.ofHours(1));

        // scheduled with a previous, higher max interval
        unitOfWork.run(
                () -> feedDAO.setDisabledUntil(List.of(feedId), now.plus(Duration.ofHours(4))));
        Assertions.assertEquals(1, unitOfWork.call(() -> feedDAO.capDisabledUntil(max)));
        Assertions.assertEquals(max, disabledUntil(feedId));

        // already within the max interval: left untouched
        Instant soon = now.plus(Duration.ofMinutes(10));
        unitOfWork.run(() -> feedDAO.setDisabledUntil(List.of(feedId), soon));
        Assertions.assertEquals(0, unitOfWork.call(() -> feedDAO.capDisabledUntil(max)));
        Assertions.assertEquals(soon, disabledUntil(feedId));
    }

    private Instant disabledUntil(Long feedId) {
        return unitOfWork.call(() -> {
            Feed feed = feedDAO.findById(feedId);
            return feed.getDisabledUntil().truncatedTo(ChronoUnit.SECONDS);
        });
    }
}
