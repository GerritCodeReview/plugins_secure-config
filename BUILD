load('//tools/bzl:junit.bzl', 'junit_tests')
load('//tools/bzl:plugin.bzl', 'gerrit_plugin')

gerrit_plugin(
  name = 'secure-config',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Implementation-Title: Secure-Config plugin',
    'Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/secure-config',
  ],
  deps = [
  ],
)

junit_tests(
  name = 'secure-config_tests',
  srcs = glob(['src/test/java/**/*Test.java']),
  tags = ['secure-config'],
  deps = [
    ':secure-config__plugin',
    '//gerrit-acceptance-framework:lib',
  ],
  visibility = ['//visibility:public'],
)

