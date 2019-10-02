MAKEFLAGS += --warn-undefined-variables
SHELL = /bin/bash -o pipefail
.DEFAULT_GOAL := help
.PHONY: help test build-lib-mac build-lib-linux

# -----------------------------------------
# Gradle setup

gradle := ./gradlew

## display this help message
help:
	@awk '/^##.*$$/,/^[~\/\.a-zA-Z_-]+:/' $(MAKEFILE_LIST) | awk '!(NR%2){print $$0p}{p=$$0}' | awk 'BEGIN {FS = ":.*?##"}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' | sort

## test
test:
	$(gradle) check

## build the mac libraries
build-lib-mac:
	cd build-libs && rm -rf LightGBM && ./build-lightgbm.sh v2.2.2
	cp build-libs/LightGBM/build/com/microsoft/ml/lightgbm/osx/x86_64/* src/main/resources/lib_lightgbm/mac_osx/

## build the linux libraries
build-lib-linux:
	docker build build-libs/. --tag amazonlinux-lightgbm
	id=$$(docker create amazonlinux-lightgbm) && \
		docker cp $$id:/app/LightGBM/build/com/microsoft/ml/lightgbm/linux/x86_64 src/main/resources/lib_lightgbm/ && \
		docker rm -v $$id
