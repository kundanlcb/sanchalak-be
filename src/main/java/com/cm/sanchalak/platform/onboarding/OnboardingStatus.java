package com.cm.sanchalak.platform.onboarding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingStatus {
    private boolean profileComplete;
    private boolean academicYearCreated;
    private boolean adminUserInvited;
    private boolean subscriptionActive;

    public boolean isAllComplete() {
        return profileComplete && academicYearCreated && adminUserInvited && subscriptionActive;
    }
}
