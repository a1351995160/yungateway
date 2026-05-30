package com.wps.yundoc.businesssystem.application;

import com.wps.yundoc.auth.application.ClientSecretDigest;
import com.wps.yundoc.auth.application.ClientSecretDigestService;
import com.wps.yundoc.auth.application.SecretGenerator;
import com.wps.yundoc.businesssystem.api.BusinessSystemApiPermissionUpdateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemCreateResponse;
import com.wps.yundoc.businesssystem.api.BusinessSystemListRequest;
import com.wps.yundoc.businesssystem.api.BusinessSystemListResponse;
import com.wps.yundoc.businesssystem.api.BusinessSystemResponse;
import com.wps.yundoc.businesssystem.api.BusinessSystemSecretResponse;
import com.wps.yundoc.businesssystem.api.BusinessSystemUpdateRequest;
import com.wps.yundoc.businesssystem.domain.ApiPermissionDefinition;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemApiPermissionMapper;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemApiPermissionPO;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemMapper;
import com.wps.yundoc.businesssystem.infrastructure.BizSystemPO;
import com.wps.yundoc.common.api.PageWindow;
import com.wps.yundoc.common.error.YundocErrorCode;
import com.wps.yundoc.common.error.YundocException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessSystemAdminService {

    private static final String ENABLED = "ENABLED";

    private final BizSystemMapper bizSystemMapper;
    private final BizSystemApiPermissionMapper permissionMapper;
    private final SecretGenerator secretGenerator;
    private final ClientSecretDigestService digestService;
    private final BusinessSystemAssembler assembler;

    public BusinessSystemAdminService(
            BizSystemMapper bizSystemMapper,
            BizSystemApiPermissionMapper permissionMapper,
            SecretGenerator secretGenerator,
            ClientSecretDigestService digestService,
            BusinessSystemAssembler assembler) {
        this.bizSystemMapper = bizSystemMapper;
        this.permissionMapper = permissionMapper;
        this.secretGenerator = secretGenerator;
        this.digestService = digestService;
        this.assembler = assembler;
    }

    @Transactional
    public BusinessSystemCreateResponse create(BusinessSystemCreateRequest request) {
        String clientSecret = secretGenerator.generateClientSecret();
        BizSystemPO bizSystem = newBizSystem(request, clientSecret);
        insertBizSystem(bizSystem);
        BusinessSystemResponse response = assembler.toResponse(bizSystem, Collections.emptyList());
        return new BusinessSystemCreateResponse(response, clientSecret);
    }

    public BusinessSystemResponse get(String businessSystemId) {
        return get(businessSystemId, true);
    }

    public BusinessSystemResponse get(String businessSystemId, boolean includePermissions) {
        BizSystemPO bizSystem = requireBizSystem(businessSystemId);
        List<BizSystemApiPermissionPO> permissions = permissionsFor(businessSystemId, includePermissions);
        return assembler.toResponse(bizSystem, permissions);
    }

    public BusinessSystemListResponse list(BusinessSystemListRequest request) {
        return list(request, true);
    }

    public BusinessSystemListResponse list(BusinessSystemListRequest request, boolean includePermissions) {
        List<BizSystemPO> fetched = bizSystemMapper.selectPage(
                normalize(request.getKeyword()),
                normalize(request.getStatus()),
                request.getPageSize() + 1,
                request.offset());
        PageWindow<BizSystemPO> page = PageWindow.fromFetched(fetched, request.getPageSize());
        return new BusinessSystemListResponse(toResponses(page.getItems(), includePermissions), page.hasMore());
    }

    public BusinessSystemResponse getPermissions(String businessSystemId) {
        return get(businessSystemId);
    }

    @Transactional
    public BusinessSystemResponse update(String businessSystemId, BusinessSystemUpdateRequest request) {
        requireBizSystem(businessSystemId);
        bizSystemMapper.updateProfile(
                businessSystemId,
                request.getBusinessSystemName(),
                request.getStatus(),
                request.getJwtTtlSeconds(),
                request.getDescription(),
                LocalDateTime.now());
        return get(businessSystemId);
    }

    @Transactional
    public BusinessSystemResponse savePermissions(
            String businessSystemId,
            BusinessSystemApiPermissionUpdateRequest request) {
        requireBizSystem(businessSystemId);
        List<String> apiPermissions = normalizedApiCodes(request.getApiPermissions());
        validateApiCodes(apiPermissions);
        replacePermissions(businessSystemId, apiPermissions);
        bizSystemMapper.increasePermissionVersion(businessSystemId, LocalDateTime.now());
        return get(businessSystemId);
    }

    @Transactional
    public BusinessSystemSecretResponse resetClientSecret(String businessSystemId) {
        BizSystemPO bizSystem = requireBizSystem(businessSystemId);
        String clientSecret = secretGenerator.generateClientSecret();
        ClientSecretDigest digest = digestService.digestNew(clientSecret);
        updateSecret(businessSystemId, digest);
        BizSystemPO updated = requireBizSystem(businessSystemId);
        return new BusinessSystemSecretResponse(
                businessSystemId,
                bizSystem.getClientId(),
                clientSecret,
                updated.getTokenVersion());
    }

    private BizSystemPO newBizSystem(BusinessSystemCreateRequest request, String clientSecret) {
        ClientSecretDigest digest = digestService.digestNew(clientSecret);
        BizSystemPO bizSystem = new BizSystemPO();
        fillIdentity(bizSystem, request);
        fillSecret(bizSystem, digest);
        fillDefaults(bizSystem, request);
        return bizSystem;
    }

    private void fillIdentity(BizSystemPO bizSystem, BusinessSystemCreateRequest request) {
        bizSystem.setBusinessSystemId(resolveBusinessSystemId(request));
        bizSystem.setBusinessSystemName(request.getBusinessSystemName());
        bizSystem.setClientId(secretGenerator.generateClientId());
    }

    private void fillSecret(BizSystemPO bizSystem, ClientSecretDigest digest) {
        bizSystem.setClientSecretDigest(digest.getDigest());
        bizSystem.setClientSecretSalt(digest.getSalt());
        bizSystem.setClientSecretAlg(digest.getAlgorithm());
    }

    private void fillDefaults(BizSystemPO bizSystem, BusinessSystemCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        bizSystem.setStatus(ENABLED);
        bizSystem.setTokenVersion(1);
        bizSystem.setPermissionVersion(1);
        bizSystem.setJwtTtlSeconds(request.getJwtTtlSeconds());
        bizSystem.setDescription(request.getDescription());
        bizSystem.setCreatedAt(now);
        bizSystem.setUpdatedAt(now);
    }

    private String resolveBusinessSystemId(BusinessSystemCreateRequest request) {
        if (hasNoBusinessSystemId(request)) {
            return secretGenerator.generateBusinessSystemId();
        }
        return request.getBusinessSystemId();
    }

    private boolean hasNoBusinessSystemId(BusinessSystemCreateRequest request) {
        String businessSystemId = request.getBusinessSystemId();
        return businessSystemId == null || businessSystemId.isEmpty();
    }

    private void insertBizSystem(BizSystemPO bizSystem) {
        try {
            bizSystemMapper.insert(bizSystem);
        } catch (DuplicateKeyException ex) {
            throw new YundocException(YundocErrorCode.VALIDATION_FAILED, "Business system already exists", ex);
        }
    }

    private BizSystemPO requireBizSystem(String businessSystemId) {
        BizSystemPO bizSystem = bizSystemMapper.selectByBusinessSystemId(businessSystemId);
        if (bizSystem == null) {
            throw new YundocException(YundocErrorCode.VALIDATION_FAILED, "Business system does not exist");
        }
        return bizSystem;
    }

    private void validateApiCodes(List<String> apiCodes) {
        for (String apiCode : apiCodes) {
            validateApiCode(apiCode);
        }
    }

    private List<String> normalizedApiCodes(List<String> apiCodes) {
        return new ArrayList<>(new LinkedHashSet<>(apiCodes));
    }

    private void validateApiCode(String apiCode) {
        if (!ApiPermissionDefinition.exists(apiCode)) {
            throw new YundocException(YundocErrorCode.VALIDATION_FAILED, "Unknown api permission");
        }
    }

    private void replacePermissions(String businessSystemId, List<String> apiCodes) {
        permissionMapper.deleteByBusinessSystemId(businessSystemId);
        if (apiCodes.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<BizSystemApiPermissionPO> permissions = apiCodes.stream()
                .map(apiCode -> newPermission(businessSystemId, apiCode, now))
                .collect(Collectors.toList());
        permissionMapper.insertAll(permissions);
    }

    private BizSystemApiPermissionPO newPermission(String businessSystemId, String apiCode, LocalDateTime now) {
        BizSystemApiPermissionPO permission = new BizSystemApiPermissionPO();
        permission.setBusinessSystemId(businessSystemId);
        permission.setApiCode(apiCode);
        permission.setStatus(ENABLED);
        permission.setCreatedAt(now);
        permission.setUpdatedAt(now);
        return permission;
    }

    private void updateSecret(String businessSystemId, ClientSecretDigest digest) {
        bizSystemMapper.updateSecret(
                businessSystemId,
                digest.getDigest(),
                digest.getSalt(),
                digest.getAlgorithm(),
                LocalDateTime.now());
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private List<BizSystemApiPermissionPO> permissionsFor(String businessSystemId, boolean includePermissions) {
        if (!includePermissions) {
            return Collections.emptyList();
        }
        return permissionMapper.selectByBusinessSystemId(businessSystemId);
    }

    private List<BusinessSystemResponse> toResponses(List<BizSystemPO> bizSystems, boolean includePermissions) {
        if (bizSystems.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<BizSystemApiPermissionPO>> permissionsByBusinessSystemId =
                permissionsByBusinessSystemId(bizSystems, includePermissions);
        return bizSystems.stream()
                .map(bizSystem -> assembler.toResponse(
                        bizSystem,
                        permissionsByBusinessSystemId.getOrDefault(
                                bizSystem.getBusinessSystemId(),
                                Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private List<String> businessSystemIds(List<BizSystemPO> bizSystems) {
        return bizSystems.stream()
                .map(BizSystemPO::getBusinessSystemId)
                .collect(Collectors.toList());
    }

    private Map<String, List<BizSystemApiPermissionPO>> permissionsByBusinessSystemId(
            List<BizSystemPO> bizSystems,
            boolean includePermissions) {
        if (!includePermissions) {
            return Collections.emptyMap();
        }
        return permissionMapper.selectByBusinessSystemIds(businessSystemIds(bizSystems)).stream()
                .collect(Collectors.groupingBy(BizSystemApiPermissionPO::getBusinessSystemId));
    }
}
