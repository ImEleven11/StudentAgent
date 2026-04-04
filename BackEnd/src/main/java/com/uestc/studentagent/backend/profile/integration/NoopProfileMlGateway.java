package com.uestc.studentagent.backend.profile.integration;

import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopProfileMlGateway implements ProfileMlGateway {

    private static final Logger log = LoggerFactory.getLogger(NoopProfileMlGateway.class);

    @Override
    public void notifyResumeUploaded(StudentProfileEntity profile) {
        log.info("Profile {} uploaded a resume and is ready for ML parsing orchestration", profile.getId());
    }
}
