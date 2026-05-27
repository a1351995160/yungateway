package com.wps.yundoc.wpsclient.application;

import com.wps.yundoc.capability.userfile.api.UserFileResponse;
import com.wps.yundoc.credential.domain.WpsUserToken;

import java.util.List;

public interface WpsFileClient {

    List<UserFileResponse> listFiles(WpsUserToken token);
}
