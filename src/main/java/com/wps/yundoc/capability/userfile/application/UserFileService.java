package com.wps.yundoc.capability.userfile.application;

import com.wps.yundoc.capability.userfile.api.UserFileListResponse;
import com.wps.yundoc.capability.userfile.api.UserFileResponse;
import com.wps.yundoc.credential.application.WpsUserAuthorizationService;
import com.wps.yundoc.credential.domain.WpsUserToken;
import com.wps.yundoc.wpsclient.application.WpsFileClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFileService {

    private final WpsUserAuthorizationService authorizationService;
    private final WpsFileClient fileClient;

    public UserFileService(WpsUserAuthorizationService authorizationService, WpsFileClient fileClient) {
        this.authorizationService = authorizationService;
        this.fileClient = fileClient;
    }

    public UserFileListResponse list(String userId) {
        WpsUserToken token = authorizationService.requireToken(userId);
        List<UserFileResponse> files = fileClient.listFiles(token);
        return new UserFileListResponse(userId, files);
    }
}
