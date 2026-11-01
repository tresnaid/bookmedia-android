#!/usr/bin/env bash
set -e

echo "=== Installing OpenJDK 17 & Android Command Line Tools ==="
apt-get update -qq
apt-get install -y -qq openjdk-21-jdk wget unzip

export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

export ANDROID_HOME="/opt/android-sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools"

if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    echo "Downloading Android Commandline Tools..."
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip
    unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools
    mv /tmp/cmdline-tools/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
    rm -rf /tmp/cmdline-tools*
fi

export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

echo "Accepting SDK licenses..."
yes | sdkmanager --licenses > /dev/null 2>&1 || true

echo "Installing Android platforms and build-tools..."
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" > /dev/null 2>&1

echo "Installing Gradle..."
if [ ! -d "/opt/gradle" ]; then
    wget -q https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -O /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d /opt
    mv /opt/gradle-8.10.2 /opt/gradle
    rm -f /tmp/gradle.zip
fi
export PATH="/opt/gradle/bin:$PATH"

cd /home/tresnaid/project/bookmedia-android
echo "Building APK..."
gradle assembleDebug --no-daemon

echo "=== Build finished! ==="
