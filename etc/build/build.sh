#!/bin/bash

#######################################
# Hello World
#######################################
# Galaxy S10
bazel build -c opt --config=android_arm64 mediapipe/examples/android/src/java/com/google/mediapipe/apps/basic:helloworld
