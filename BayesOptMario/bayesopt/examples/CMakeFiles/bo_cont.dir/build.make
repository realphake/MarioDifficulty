# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 2.8

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list

# Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# The program to use to edit the cache.
CMAKE_EDIT_COMMAND = /usr/bin/ccmake

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/stathis/Libraries/bayesopt

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/stathis/Libraries/bayesopt

# Include any dependencies generated for this target.
include examples/CMakeFiles/bo_cont.dir/depend.make

# Include the progress variables for this target.
include examples/CMakeFiles/bo_cont.dir/progress.make

# Include the compile flags for this target's objects.
include examples/CMakeFiles/bo_cont.dir/flags.make

examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o: examples/CMakeFiles/bo_cont.dir/flags.make
examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o: examples/bo_cont.cpp
	$(CMAKE_COMMAND) -E cmake_progress_report /home/stathis/Libraries/bayesopt/CMakeFiles $(CMAKE_PROGRESS_1)
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Building CXX object examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o"
	cd /home/stathis/Libraries/bayesopt/examples && /usr/bin/g++   $(CXX_DEFINES) $(CXX_FLAGS) -o CMakeFiles/bo_cont.dir/bo_cont.cpp.o -c /home/stathis/Libraries/bayesopt/examples/bo_cont.cpp

examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/bo_cont.dir/bo_cont.cpp.i"
	cd /home/stathis/Libraries/bayesopt/examples && /usr/bin/g++  $(CXX_DEFINES) $(CXX_FLAGS) -E /home/stathis/Libraries/bayesopt/examples/bo_cont.cpp > CMakeFiles/bo_cont.dir/bo_cont.cpp.i

examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/bo_cont.dir/bo_cont.cpp.s"
	cd /home/stathis/Libraries/bayesopt/examples && /usr/bin/g++  $(CXX_DEFINES) $(CXX_FLAGS) -S /home/stathis/Libraries/bayesopt/examples/bo_cont.cpp -o CMakeFiles/bo_cont.dir/bo_cont.cpp.s

examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o.requires:
.PHONY : examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o.requires

examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o.provides: examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o.requires
	$(MAKE) -f examples/CMakeFiles/bo_cont.dir/build.make examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o.provides.build
.PHONY : examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o.provides

examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o.provides.build: examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o

# Object files for target bo_cont
bo_cont_OBJECTS = \
"CMakeFiles/bo_cont.dir/bo_cont.cpp.o"

# External object files for target bo_cont
bo_cont_EXTERNAL_OBJECTS =

bin/bo_cont: examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o
bin/bo_cont: lib/libbayesopt.a
bin/bo_cont: /usr/local/lib/libnlopt.a
bin/bo_cont: examples/CMakeFiles/bo_cont.dir/build.make
bin/bo_cont: examples/CMakeFiles/bo_cont.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --red --bold "Linking CXX executable ../bin/bo_cont"
	cd /home/stathis/Libraries/bayesopt/examples && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/bo_cont.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
examples/CMakeFiles/bo_cont.dir/build: bin/bo_cont
.PHONY : examples/CMakeFiles/bo_cont.dir/build

examples/CMakeFiles/bo_cont.dir/requires: examples/CMakeFiles/bo_cont.dir/bo_cont.cpp.o.requires
.PHONY : examples/CMakeFiles/bo_cont.dir/requires

examples/CMakeFiles/bo_cont.dir/clean:
	cd /home/stathis/Libraries/bayesopt/examples && $(CMAKE_COMMAND) -P CMakeFiles/bo_cont.dir/cmake_clean.cmake
.PHONY : examples/CMakeFiles/bo_cont.dir/clean

examples/CMakeFiles/bo_cont.dir/depend:
	cd /home/stathis/Libraries/bayesopt && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/stathis/Libraries/bayesopt /home/stathis/Libraries/bayesopt/examples /home/stathis/Libraries/bayesopt /home/stathis/Libraries/bayesopt/examples /home/stathis/Libraries/bayesopt/examples/CMakeFiles/bo_cont.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : examples/CMakeFiles/bo_cont.dir/depend
