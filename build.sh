#!/usr/bin/env bash

set -eou pipefail

yum install git wget clang -y
yum install java-1.8.0-openjdk-devel -y
yum groupinstall "Development Tools" -y

export JAVA_HOME="/usr/lib/jvm/java-1.8.0-openjdk.x86_64"
export PATH="$JAVA_HOME/bin:$PATH"

# Install latest cmake
wget https://cmake.org/files/v3.12/cmake-3.12.4-Linux-x86_64.sh
chmod 755 cmake-3.12.4-Linux-x86_64.sh
mkdir -p cmake
./cmake-3.12.4-Linux-x86_64.sh --skip-license --prefix=cmake
export PATH="/app/cmake/bin:$PATH"

# Build lightGBM java wrapper
git clone --recursive https://github.com/Microsoft/LightGBM
cd LightGBM
mkdir -p build
cd build
cmake -DUSE_SWIG=ON -DUSE_OPENMP=OFF ..
make -j4