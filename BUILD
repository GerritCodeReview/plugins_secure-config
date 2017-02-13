load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "secure-config",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Implementation-Title: Secure-Config plugin",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/secure-config",
    ],
    javacopts = ["-Xep:InsecureCryptoUsage:WARN"],
)

junit_tests(
    name = "secure_config_tests",
    srcs = glob(["src/test/java/**/*Test.java"]),
    tags = ["secure-config"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":secure-config__plugin",
    ],
)
