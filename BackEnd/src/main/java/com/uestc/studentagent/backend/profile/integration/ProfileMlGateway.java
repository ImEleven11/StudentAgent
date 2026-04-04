package com.uestc.studentagent.backend.profile.integration;

import com.uestc.studentagent.backend.profile.entity.StudentProfileEntity;

public interface ProfileMlGateway {

    void notifyResumeUploaded(StudentProfileEntity profile);
}
