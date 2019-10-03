#!/usr/bin/env bash

# Checkout a specific LightGBM version/commit and build it with the SWIG wrapper and no OpenMP
# Reference https://lightgbm.readthedocs.io/en/latest/Installation-Guide.html

set -oe pipefail

function die() {
    >&2 echo -e "$@"
    exit 1
}

version=${1:-}

[[ -z "${JAVA_HOME}" ]] && die "JAVA_HOME must be set"
[[ -z "${version}" ]] && die "Missing LightGBM commit, eg: $0 v2.2.2"

git clone --branch "$version" --depth 1 https://github.com/Microsoft/LightGBM
cd LightGBM
mkdir -p build
cd build

if [[ "$OSTYPE" == "darwin"* ]]; then
  DYLIB="-DAPPLE_OUTPUT_DYLIB=ON"
else
  DYLIB=""
fi

cmake --version || die "cmake must be installed"
cmake -DUSE_SWIG=ON -DUSE_OPENMP=OFF "$DYLIB" ..
make -j4

