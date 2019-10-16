MAKEFLAGS += --warn-undefined-variables
SHELL = /bin/bash -o pipefail
.DEFAULT_GOAL := help
.PHONY: help test build-libs-mac build-libs-linux

# -----------------------------------------
# Gradle setup

gradle := ./gradlew

## display this help message
help:
	@awk '/^##.*$$/,/^[~\/\.a-zA-Z_-]+:/' $(MAKEFILE_LIST) | awk '!(NR%2){print $$0p}{p=$$0}' | awk 'BEGIN {FS = ":.*?##"}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' | sort

## test
test:
	$(gradle) check

# extract the currently targeted version of LightGBM
lightGbmVersion := v$(shell grep lightGbmVersion gradle.properties | sed 's/.*lightGbmVersion=//')

## build native libraries for mac
build-libs-mac:
	cd build-libs && rm -rf LightGBM && ./build-lightgbm.sh $(lightGbmVersion)
	cp build-libs/LightGBM/build/com/microsoft/ml/lightgbm/osx/x86_64/* src/main/resources/lib_lightgbm/mac_osx/

## build native libraries for linux
build-libs-linux:
	docker build build-libs/. --tag amazonlinux-lightgbm --build-arg version=$(lightGbmVersion)
	id=$$(docker create amazonlinux-lightgbm) && \
		docker cp $$id:/app/LightGBM/build/com/microsoft/ml/lightgbm/linux/x86_64 src/main/resources/lib_lightgbm/ && \
		docker rm -v $$id
