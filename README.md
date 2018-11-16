## Instructions

1. Build the prepared docker image (in the current directory)

```
docker build . --tag amazonlinux-lightgbm
```

2. Run the docker image, mounting the current directory so we can retrieve the generated native library files (*.so)

```
docker run -it -v $(pwd):/app amazonlinux-lightgbm
```

3. Once in docker, run `./build.sh`

4. The generated native library files should be in:

```
LightGBM/build/com/microsoft/ml/lightgbm/linux/x86_64
```

- lib_lightgbm.so
- lib_lightgbm_swig.so