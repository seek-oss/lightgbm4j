# Build for amazonlinux so the libs work in AWS Lambda
# The built libs should also work in Ubuntu
FROM amazonlinux:2018.03

RUN yum -y update && yum -y install git wget clang java-1.8.0-openjdk-devel
RUN yum -y groupinstall "Development Tools"

#ENV TZ 'Australia/Melbourne'
ENV JAVA_HOME "/usr/lib/jvm/java-1.8.0-openjdk.x86_64"

# newer version of cmake is needed to build with SWIG
RUN wget "https://github.com/Kitware/CMake/releases/download/v3.12.4/cmake-3.12.4-Linux-x86_64.tar.gz" -q -O - \
        | tar -xz --strip-components=1 -C /usr/local

WORKDIR /app

COPY build-lightgbm.sh .

ARG version
ENV version ${version}
RUN ./build-lightgbm.sh $version
