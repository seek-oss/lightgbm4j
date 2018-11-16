FROM amazonlinux:2017.03.1.20170812

ENV TZ 'Australia/Melbourne'
ENV app_home /app

# RUN yum remove cmake -y
# RUN yum remove gcc –y
# RUN yum remove gcc –y
# RUN yum install gcc48 gcc48-cpp –y
# RUN yum install gcc-c++ -y

VOLUME .:/$app_home
WORKDIR $app_home

# RUN yum install git wget clang -y
# RUN yum install java-1.8.0-openjdk-devel -y
# RUN yum groupinstall "Development Tools" -y

# RUN export JAVA_HOME="/usr/lib/jvm/java-1.8.0-openjdk.x86_64"
# RUN export PATH="$JAVA_HOME/bin:$PATH"

# # Install latest cmake
# RUN wget https://cmake.org/files/v3.12/cmake-3.12.4-Linux-x86_64.sh
# RUN chmod 755 cmake-3.12.4-Linux-x86_64.sh
# RUN ./cmake-3.12.4-Linux-x86_64.sh --skip-license

# # Build lightGBM java wrapper
# # RUN git clone --recursive https://github.com/Microsoft/LightGBM
# RUN cd LightGBM
# RUN mkdir build
# RUN cd build
# RUN cmake -DUSE_SWIG=ON -DUSE_OPENMP=OFF ..
# RUN make -j4

# RUN bash
