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
rm -rf "$ANDROID_HOME/.temp"
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" "build-tools;34.0.0" > /dev/null 2>&1

echo "Installing Gradle..."
if [ ! -f "/opt/gradle/bin/gradle" ]; then
    wget -q https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -O /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d /tmp/gradle-extract
    cp -r /tmp/gradle-extract/gradle-8.10.2/* /opt/gradle/
    rm -rf /tmp/gradle*
fi
export PATH="/opt/gradle/bin:$PATH"

cd /home/tresnaid/project/bookmedia-android
mkdir -p keystore
if [ ! -f "keystore/archie.keystore" ]; then
    echo "Generating permanent signing keystore..."
    keytool -genkey -v -keystore keystore/archie.keystore \
        -alias archie -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass archieapp -keypass archieapp \
        -dname "CN=archie, OU=bookmedia, O=bookmedia, L=City, ST=State, C=US"
fi

echo "Building APK..."
gradle assembleDebug --no-daemon

echo "=== Build finished! ==="
