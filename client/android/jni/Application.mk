APP_PLATFORM := android-14
NDK_TOOLCHAIN_VERSION := 4.9
APP_STL := gnustl_static
APP_CPPFLAGS += -fexceptions
LOCAL_CFLAGS := -funwind-tables -Wl,--no-merge-exidx-entries