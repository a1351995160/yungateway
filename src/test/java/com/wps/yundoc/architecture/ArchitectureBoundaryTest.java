package com.wps.yundoc.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.wps.yundoc", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureBoundaryTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule business_modules_should_not_call_wps_http_directly =
            noClasses()
                    .that().resideInAnyPackage(
                            "..accessauth..",
                            "..businesssystem..",
                            "..capabilitypolicy..",
                            "..credential..",
                            "..yundocfile..",
                            "..upload..",
                            "..share..",
                            "..permission..",
                            "..idempotency..",
                            "..audit..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.web.client..",
                            "org.apache.http..",
                            "okhttp3..")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule adapters_should_not_access_mappers_directly =
            noClasses()
                    .that().resideInAPackage("..adapter..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure.mapper..")
                    .allowEmptyShould(true);
}
