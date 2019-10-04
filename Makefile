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

## build native libraries for mac
build-libs-mac: require-version
	cd build-libs && rm -rf LightGBM && ./build-lightgbm.sh $(version)
	cp build-libs/LightGBM/build/com/microsoft/ml/lightgbm/osx/x86_64/* src/main/resources/lib_lightgbm/mac_osx/

## build native libraries for linux
build-libs-linux: require-version
	docker build build-libs/. --tag amazonlinux-lightgbm --build-arg version=$(version)
	id=$$(docker create amazonlinux-lightgbm) && \
		docker cp $$id:/app/LightGBM/build/com/microsoft/ml/lightgbm/linux/x86_64 src/main/resources/lib_lightgbm/ && \
		docker rm -v $$id

require-version:
	$(call require_var,version,. Provide version eg: version=v2.2.2)

require_var = $(foreach 1,$1,$(__require_var))
__require_var = $(if $(value $1),,\
	$(error Missing $1$(if $(value 2),$(strip $2))))
