#!/bin/bash

# OrbitX Launcher APK Build Script
# This script builds the debug and/or release APK

set -e

echo "=========================================="
echo "OrbitX Launcher APK Build Script"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed!${NC}"
    echo "Please install Java JDK 17 from: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -i version | sed 's/.*version "\(.*\)".*/\1/' | sed 's/\..*//')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${YELLOW}Warning: Java version $JAVA_VERSION detected. Recommended: Java 17+${NC}"
fi

# Check Android SDK
if [ -z "$ANDROID_HOME" ] || [ ! -d "$ANDROID_HOME" ]; then
    echo -e "${RED}Error: Android SDK is not configured!${NC}"
    echo "Please install Android Studio and set ANDROID_HOME"
    echo "Export ANDROID_HOME=/path/to/android/sdk"
    exit 1
fi

# Check Gradle
if [ ! -f "gradlew" ]; then
    echo -e "${RED}Error: gradlew not found in current directory!${NC}"
    echo "Please run this script from the project root"
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

# Function to build debug APK
build_debug() {
    echo -e "${GREEN}Building Debug APK...${NC}"
    ./gradlew clean assembleDebug --no-daemon --stacktrace
    
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        echo -e "${GREEN}✓ Debug APK built successfully!${NC}"
        echo "Location: app/build/outputs/apk/debug/app-debug.apk"
        
        # Rename with timestamp
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        cp app/build/outputs/apk/debug/app-debug.apk "app/build/outputs/apk/debug/OrbitX-Launcher_${TIMESTAMP}_debug.apk"
        echo "Also saved as: app/build/outputs/apk/debug/OrbitX-Launcher_${TIMESTAMP}_debug.apk"
    else
        echo -e "${RED}✗ Debug APK build failed!${NC}"
        exit 1
    fi
}

# Function to build release APK
build_release() {
    echo -e "${GREEN}Building Release APK...${NC}"
    
    # Check if keystore exists
    if [ ! -f "keystore.jks" ]; then
        echo -e "${YELLOW}Warning: keystore.jks not found!${NC}"
        echo "Creating a new keystore..."
        
        # Generate a new keystore (for development only)
        keytool -genkey -v -keystore keystore.jks \
            -alias orbitx \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -storepass changeit \
            -keypass changeit \
            -dname "CN=OrbitX, OU=Development, O=OrbitX, L=Unknown, ST=Unknown, C=US"
        
        echo "Keystore created. Please update app/build.gradle with your signing config."
    fi
    
    # Build release
    ./gradlew clean assembleRelease --no-daemon --stacktrace
    
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        echo -e "${GREEN}✓ Release APK built successfully!${NC}"
        echo "Location: app/build/outputs/apk/release/app-release.apk"
        
        # Rename with version
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        cp app/build/outputs/apk/release/app-release.apk "app/build/outputs/apk/release/OrbitX-Launcher_${TIMESTAMP}_release.apk"
        echo "Also saved as: app/build/outputs/apk/release/OrbitX-Launcher_${TIMESTAMP}_release.apk"
    else
        echo -e "${RED}✗ Release APK build failed!${NC}"
        exit 1
    fi
}

# Function to build both
build_both() {
    build_debug
    build_release
}

# Function to install dependencies
install_deps() {
    echo -e "${GREEN}Installing dependencies...${NC}"
    ./gradlew --refresh-dependencies
}

# Function to clean
clean_build() {
    echo -e "${GREEN}Cleaning build...${NC}"
    ./gradlew clean
}

# Function to run tests
run_tests() {
    echo -e "${GREEN}Running tests...${NC}"
    ./gradlew testDebugUnitTest --no-daemon --stacktrace
}

# Function to run lint
run_lint() {
    echo -e "${GREEN}Running lint...${NC}"
    ./gradlew lintDebug --no-daemon --stacktrace
}

# Show menu
show_menu() {
    echo ""
    echo "Select build option:"
    echo "  1) Build Debug APK (for testing)"
    echo "  2) Build Release APK (for distribution)"
    echo "  3) Build Both"
    echo "  4) Install dependencies"
    echo "  5) Clean build"
    echo "  6) Run tests"
    echo "  7) Run lint"
    echo "  8) Exit"
    echo ""
}

# Main script
show_menu
read -p "Enter your choice (1-8): " choice

case $choice in
    1)
        build_debug
        ;;
    2)
        build_release
        ;;
    3)
        build_both
        ;;
    4)
        install_deps
        ;;
    5)
        clean_build
        ;;
    6)
        run_tests
        ;;
    7)
        run_lint
        ;;
    8)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice!${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}Build complete!${NC}"
echo ""
echo "APK files are located in: app/build/outputs/apk/"
