cmake_minimum_required(VERSION 3.8)
project(mineretro LANGUAGES CXX C)

set(CMAKE_CXX_STANDARD 20)

add_library(Mineretro SHARED src/mineretro.cc)
