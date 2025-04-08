package(default_visibility = ["//visibility:public"])

filegroup(
    name = "manifest",
    srcs = ["libausbc/src/main/AndroidManifest.xml"],
)

android_library(
    name = "libausbc",
    srcs = glob([
        "libausbc/src/main/java/com/jiangdg/ausbc/**/*.java",
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
        "libausbc/src/main/jni/**/*.c",
        "libausbc/src/main/jni/**/*.cpp",
    ]),
    hdrs = glob([
        "libausbc/src/main/jni/**/*.h",
        "libausbc/src/main/jni/**/*.hpp",
    ]),
    includes = [
        "libausbc/src/main/jni",
        "libausbc/src/main/jni/common",
        "libausbc/src/main/jni/libusb",
        "libausbc/src/main/jni/libuvc",
        "libausbc/src/main/jni/UVCCamera",
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0",
    ],
    copts = [
        "-std=c++17",
        "-std=c99",
        "-DANDROID_ARM_NEON",
        "-DREQUIRE_SIMD",
        "-DENABLE_STATIC",
        "-DLIBUVC_HAS_JPEG",
    ],
    linkopts = [
        "-landroid",
        "-llog",
    ],
    deps = [
        "//third_party/AndroidUSBCamera:libuvc",
        "//third_party/AndroidUSBCamera:libusb",
    ],
) 