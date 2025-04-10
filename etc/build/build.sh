#!/bin/bash

#######################################
# Hello world
#######################################
# Galaxy S10
# bazel build -c opt --config=android_arm64 mediapipe/examples/android/src/java/com/google/mediapipe/apps/basic:helloworld

#######################################
# simple preview test
#######################################
# Galaxy S10
bazel build --sandbox_debug -c opt --config=android_arm64 mediapipe/examples/android/src/java/com/google/mediapipe/apps/custom/simple:simple_test
