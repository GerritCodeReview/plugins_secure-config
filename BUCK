include_defs('//lib/maven.defs')

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

java_test(
  name = 'secure-config_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['secure-config'],
  deps = [
    ':secure-config__plugin',
    '//gerrit-acceptance-framework:lib',
  ],
)
