cmake_minimum_required(VERSION 3.4.1)

project(goengine CXX)

add_subdirectory(gnugo-2.6)
add_subdirectory(amigogtp-1.8)
add_subdirectory(Ray)
add_subdirectory(liberty)
add_subdirectory(KataGo/cpp)
add_subdirectory(gnugo-3.8)

#add_library(
#  goengine
#  SHARED
  # add JNI brich here.
#)

# add_library(goengine STATIC IMPORTED)
