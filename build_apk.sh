#!/usr/bin/env bash
set -e

if ! command -v wget >/dev/null 2>&1 || ! command -v unzip >/dev/null 2>&1; then
    echo "=== Installing wget & unzip ==="
    apt-get update -qq && apt-get install -y -qq wget unzip
fi

export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

export ANDROID_HOME="/opt/android-sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools"

if [ ! -d "$ANDROID_HOME/platforms/android-35" ]; then
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
    yes | sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" "build-tools;34.0.0" > /dev/null 2>&1 || true
else
    export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
fi

echo "Installing Gradle..."
if [ ! -f "/opt/gradle/bin/gradle" ]; then
    wget -q https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -O /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d /tmp/gradle-extract
    mkdir -p /opt/gradle
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

APK_OUT="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_OUT" ]; then
    echo "Copying APK to distribution locations..."
    cp "$APK_OUT" /home/tresnaid/project/bookmedia-gallery.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/src/bookmedia/web/archie.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/src/bookmedia/web/bookmedia-gallery.apk 2>/dev/null || true
    mkdir -p /home/tresnaid/project/bookmedia/data/media/0 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/0/archie.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/0/archie-v0318.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/0/archie-v0319.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/0/archie-v0320.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/0/archie-v0325.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/0/archie-v0326.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/0/bookmedia-gallery.apk 2>/dev/null || true
    mkdir -p /home/tresnaid/project/bookmedia/data/media/apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/apk/archie.apk 2>/dev/null || true
    cp "$APK_OUT" /home/tresnaid/project/bookmedia/data/media/apk/bookmedia-gallery.apk 2>/dev/null || true
fi

echo "=== Build finished! ==="
