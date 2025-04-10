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
    name = "libusb",
    srcs = [
        "libausbc/src/main/jni/libusb/libusb/core.c",
        "libausbc/src/main/jni/libusb/libusb/descriptor.c",
        "libausbc/src/main/jni/libusb/libusb/hotplug.c",
        "libausbc/src/main/jni/libusb/libusb/io.c",
        "libausbc/src/main/jni/libusb/libusb/sync.c",
        "libausbc/src/main/jni/libusb/libusb/strerror.c",
        "libausbc/src/main/jni/libusb/libusb/os/android_usbfs.c",
        "libausbc/src/main/jni/libusb/libusb/os/poll_posix.c",
        "libausbc/src/main/jni/libusb/libusb/os/threads_posix.c",
        "libausbc/src/main/jni/libusb/libusb/os/android_netlink.c",
    ],
    hdrs = glob([
        "libausbc/src/main/jni/libusb/libusb/*.h",
        "libausbc/src/main/jni/libusb/libusb/os/*.h",
        "libausbc/src/main/jni/common/*.h",
    ]),
    includes = [
        "libausbc/src/main/jni/common",
        "libausbc/src/main/jni",
        "libausbc/src/main/jni/libusb",
        "libausbc/src/main/jni/libusb/libusb",
        "libausbc/src/main/jni/libusb/libusb/os",
        "libausbc/src/main/jni/libusb/android",
    ],
    copts = [
        "-std=c99",
        "-DANDROID",
        "-DANDROID_NDK",
        "-DLOG_NDEBUG",
        "-DACCESS_RAW_DESCRIPTORS",
        "-O3",
        "-fstrict-aliasing",
        "-fprefetch-loop-arrays",
    ],
    linkopts = [
        "-landroid",
        "-llog",
    ],
)

cc_library(
    name = "libuvc",
    srcs = glob([
        "libuvc-git/src/ctrl.c",
        "libuvc-git/src/device.c",
        "libuvc-git/src/diag.c",
        "libuvc-git/src/frame.c",
        "libuvc-git/src/frame-mjpeg.c",
        "libuvc-git/src/init.c",
        "libuvc-git/src/stream.c",
    ]),
    hdrs = glob([
        "libuvc-git/include/libuvc/*.h",
        "libuvc-git/include/*.h",
        "libausbc/src/main/jni/libusb/libusb/*.h",
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0/*.h",
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0/include/*.h",
    ]),
    includes = [
        "libuvc-git/include",
        "libausbc/src/main/jni/libusb/libusb",
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0",
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0/include",
    ],
    copts = [
        "-DLIBUVC_HAS_JPEG",
        "-std=c99",
    ],
    linkopts = [
        "-landroid",
        "-llog",
    ],
    visibility = ["//visibility:public"],
)

cc_library(
    name = "libausbc_native_c",
    srcs = glob([
        "libausbc/src/main/jni/**/*.c",
    ], exclude=[
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0/**/*.c",
        "libausbc/src/main/jni/libjpeg-turbo-2.1.1/**/*.c",
        "libausbc/src/main/jni/libusb/**/*.c",
    ]),
    hdrs = glob([
        "libausbc/src/main/jni/**/*.h",
        "libuvc-git/include/*.h",
    ], exclude=[
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0/**/*.h",
        "libausbc/src/main/jni/libjpeg-turbo-2.1.1/**/*.h",
        "libausbc/src/main/jni/libusb/**/*.h",
    ]),
    includes = [
        "libausbc/src/main/jni",
        "libausbc/src/main/jni/common",
        "libausbc/src/main/jni/common/utils",
        "libausbc/src/main/jni/libusb",
        "libausbc/src/main/jni/libuvc",
        "libausbc/src/main/jni/libuvc/include",
        "libausbc/src/main/jni/libuvc/src",
        "libausbc/src/main/jni/UVCCamera",
        "libuvc-git/include",
        "libuvc-git/include/libuvc",
    ],
    copts = [
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
        ":libuvc",
        ":libusb",
    ],
)

cc_library(
    name = "libausbc_native_cpp",
    srcs = glob([
        "libausbc/src/main/jni/**/*.cpp",
    ], exclude=[
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0/**/*.cpp",
        "libausbc/src/main/jni/libjpeg-turbo-2.1.1/**/*.cpp",
        "libausbc/src/main/jni/UVCCamera/*.cpp",
        "libausbc/src/main/jni/hacks/*.cpp",
    ]),
    hdrs = glob([
        "libausbc/src/main/jni/**/*.hpp",
        "libuvc-git/include/*.h",
    ], exclude=[
        "libausbc/src/main/jni/libjpeg-turbo-1.5.0/**/*.hpp",
        "libausbc/src/main/jni/libjpeg-turbo-2.1.1/**/*.hpp",
    ]),
    includes = [
        "libausbc/src/main/jni",
        "libausbc/src/main/jni/common",
        "libausbc/src/main/jni/common/utils",
        "libausbc/src/main/jni/libusb",
        "libausbc/src/main/jni/libuvc",
        "libausbc/src/main/jni/libuvc/include",
        "libausbc/src/main/jni/libuvc/src",
        "libausbc/src/main/jni/UVCCamera",
        "libausbc/src/main/jni/libuvc/include/libuvc",
        "libuvc-git/include/libuvc",
    ],
    copts = [
        "-std=c++17",
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
        ":libuvc",
        ":libusb",
        ":libausbc_native_c",
    ],
)

cc_library(
    name = "libausbc_native",
    deps = [
        # ":libausbc_native_c",
        # ":libausbc_native_cpp",
        ":libuvc",
        ":libusb",  
    ],
) 