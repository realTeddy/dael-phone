#!/usr/bin/env bash
# Restart the headless 'dael' AVD and wait for boot.
ADB=~/Android/Sdk/platform-tools/adb
for p in $(pgrep -f "qemu-system|avd dael" | grep -v $$); do kill $p 2>/dev/null; done
sleep 3
for p in $(pgrep -f "qemu-system|avd dael" | grep -v $$); do kill -9 $p 2>/dev/null; done
export ANDROID_HOME=$HOME/Android/Sdk
nohup ~/Android/Sdk/emulator/emulator -avd dael -no-window -gpu swiftshader_indirect -no-audio -no-boot-anim -camera-front emulated > /tmp/emulator.log 2>&1 &
sleep 3
until $ADB -s emulator-5554 shell getprop sys.boot_completed 2>/dev/null | grep -q 1; do sleep 3; done
echo booted
