include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'secure-config',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Implementation-Title: Secure-Config plugin',
    'Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/secure-config',
  ],
  provided_deps = [
    '//lib/commons:codec',
  ],
  deps = [
  ],
)

java_test(
  name = 'secure-config_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['secure-config'],
  deps = GERRIT_PLUGIN_API + GERRIT_TESTS + [
    ':secure-config__plugin',
  ],
)
