package(default_visibility = ["//visibility:public"])

filegroup(
    name = "manifest",
    srcs = ["src/main/AndroidManifest.xml"],
)

android_library(
    name = "libausbc",
    srcs = glob([
        "src/main/java/com/jiangdg/ausbc/**/*.java",
    ]),
    manifest = ":manifest",
    deps = [
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:com_google_guava_guava",
    ],
)

cc_library(
    name = "libausbc_native",
    srcs = glob([
        "src/main/cpp/**/*.cpp",
    ]),
    hdrs = glob([
        "src/main/cpp/**/*.h",
    ]),
    includes = [
        "src/main/cpp",
    ],
    linkopts = [
        "-landroid",
        "-llog",
    ],
) 