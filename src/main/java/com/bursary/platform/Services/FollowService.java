package com.bursary.platform.Services;

import com.bursary.platform.DTOs.FollowLearnerRequest;
import com.bursary.platform.DTOs.FollowResponse;
import com.bursary.platform.DTOs.LearnerSearchResultResponse;
import com.bursary.platform.Entities.Learner;
import com.bursary.platform.Entities.Provider;
import com.bursary.platform.Entities.ProviderLearnerFollow;
import com.bursary.platform.Exceptions.DuplicateResourceException;
import com.bursary.platform.Exceptions.ResourceNotFoundException;
import com.bursary.platform.Repositories.LearnerRepository;
import com.bursary.platform.Repositories.ProviderLearnerFollowRepository;
import com.bursary.platform.Repositories.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final ProviderLearnerFollowRepository followRepository;
    private final ProviderRepository providerRepository;
    private final LearnerRepository learnerRepository;
    private final NotificationService notificationService;

    /**
     * Provider follows a learner
     */
    @Transactional
    public FollowResponse followLearner(Long providerId, Long learnerId, FollowLearnerRequest request) {
        log.info("Provider {} attempting to follow learner {}", providerId, learnerId);

        // Verify provider exists
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        // Verify learner exists
        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with ID: " + learnerId));

        // Check if already following
        if (followRepository.existsByProviderIdAndLearnerId(providerId, learnerId)) {
            throw new DuplicateResourceException("You are already following this learner");
        }

        // Create follow relationship
        ProviderLearnerFollow follow = ProviderLearnerFollow.builder()
                .providerId(providerId)
                .learnerId(learnerId)
                .followedAt(LocalDateTime.now())
                .notes(request != null ? request.getNotes() : null)
                .build();

        follow = followRepository.save(follow);
        log.info("Provider {} now following learner {}", providerId, learnerId);

        // Create notification for learner
        notificationService.createFollowerNotification(learnerId, provider.getOrganizationName(), follow.getId());

        return mapToFollowResponse(follow, learner);
    }

    /**
     * Provider unfollows a learner
     */
    @Transactional
    public void unfollowLearner(Long providerId, Long learnerId) {
        log.info("Provider {} unfollowing learner {}", providerId, learnerId);

        ProviderLearnerFollow follow = followRepository.findByProviderIdAndLearnerId(providerId, learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow relationship not found"));

        followRepository.delete(follow);
        log.info("Provider {} unfollowed learner {}", providerId, learnerId);
    }

    /**
     * Get all learners followed by provider
     */
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowedLearners(Long providerId) {
        log.info("Fetching followed learners for provider {}", providerId);

        List<ProviderLearnerFollow> follows = followRepository.findByProviderIdOrderByFollowedAtDesc(providerId);

        return follows.stream()
                .map(follow -> {
                    Learner learner = learnerRepository.findById(follow.getLearnerId())
                            .orElse(null);
                    return learner != null ? mapToFollowResponse(follow, learner) : null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    /**
     * Get all providers following a learner
     */
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowers(Long learnerId) {
        log.info("Fetching followers for learner {}", learnerId);

        List<ProviderLearnerFollow> follows = followRepository.findByLearnerIdOrderByFollowedAtDesc(learnerId);

        return follows.stream()
                .map(follow -> {
                    Learner learner = learnerRepository.findById(follow.getLearnerId())
                            .orElse(null);
                    return learner != null ? mapToFollowResponse(follow, learner) : null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    /**
     * Check if provider is following learner
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(Long providerId, Long learnerId) {
        return followRepository.existsByProviderIdAndLearnerId(providerId, learnerId);
    }

    /**
     * Get follow statistics
     */
    @Transactional(readOnly = true)
    public long getFollowerCount(Long learnerId) {
        return followRepository.countByLearnerId(learnerId);
    }

    // ========== Helper Methods ==========

    private FollowResponse mapToFollowResponse(ProviderLearnerFollow follow, Learner learner) {
        return FollowResponse.builder()
                .followId(follow.getId())
                .providerId(follow.getProviderId())
                .learnerId(follow.getLearnerId())
                .learnerName(learner.getFirstName() + " " + learner.getLastName())
                .notes(follow.getNotes())
                .followedAt(follow.getFollowedAt())
                .build();
    }
}