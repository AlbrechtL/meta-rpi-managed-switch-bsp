# Settings shared by every Raspberry Pi carrying the 4-port managed switch HAT
# (https://github.com/AlbrechtL/rpi-managed-switch-4-port). A machine .conf
# next to this directory requires the meta-raspberrypi machine of its Pi model
# first, then this file.

# Puts "rpi-managed-switch" into MACHINEOVERRIDES, so recipes can use
# COMPATIBLE_MACHINE = "^rpi-managed-switch$" and :rpi-managed-switch
# overrides for every Pi model at once.
MACHINEOVERRIDES =. "rpi-managed-switch:"

# poky-tiny (and with it ethernet-switch-os) assigns linux-yocto-tiny with a
# plain "=", which beats meta-raspberrypi's "?=". An override beats both.
PREFERRED_PROVIDER_virtual/kernel:rpi-managed-switch = "linux-raspberrypi"
# The rtl8365mb backports in recipes-kernel/linux are made for 6.18.
PREFERRED_VERSION_linux-raspberrypi = "6.18.%"

# -----------------------------------------------------------------------------
# The switch
# -----------------------------------------------------------------------------
# dtparam=spi=on, then the overlay (0003-ARM-dts-overlays-add-rtl8365mb-*):
# ENC28J60 on SPI0 CE0 as DSA conduit, RTL8367S on bit-banged SMI.
ENABLE_SPI_BUS = "1"
RPI_KERNEL_DEVICETREE_OVERLAYS:append = " overlays/rtl8365mb-enc28j60-switch.dtbo"
RPI_EXTRA_CONFIG = "dtoverlay=rtl8365mb-enc28j60-switch"

# Headless: no vc4 KMS overlay. The serial console is the one to use.
DISABLE_VC4GRAPHICS = "1"
ENABLE_UART = "1"

# Kernel messages on HDMI as well as on the serial port. U-Boot already
# prints on both, meta-raspberrypi only gives the kernel serial0. The last
# console= is /dev/console, where init and the rc scripts write, so serial
# stays last.
CMDLINE_SERIAL = "console=tty1 console=serial0,115200"

# Everything the switch needs is built into the kernel
# (rpi-managed-switch.cfg). The full module tree would only add tens of MB to
# each root filesystem slot.
MACHINE_EXTRA_RRECOMMENDS:remove = "kernel-modules"

# -----------------------------------------------------------------------------
# A/B boot
# -----------------------------------------------------------------------------
# The firmware's own A/B switch (autoboot.txt, tryboot) exists on the Pi 4 and
# later only, so U-Boot does it: the firmware loads U-Boot as kernel.img, and
# boot.scr (recipes-bsp/rpi-u-boot-scr) picks the slot from "rootpart" in
# uboot.env. See files/wic/rpi-managed-switch-ab.wks.in for the partitions.
RPI_USE_U_BOOT = "1"

# The zImage itself, booted with bootz. meta-raspberrypi's default with
# U-Boot wraps it in a legacy uImage for bootm, which first copies the image
# to its load address, and rpi_config limits that copy to
# CONFIG_SYS_BOOTM_LEN = 8 MiB. This kernel is larger:
#   Image too large: increase CONFIG_SYS_BOOTM_LEN
# bootz starts the zImage where boot.scr loaded it, without that limit.
KERNEL_IMAGETYPE_UBOOT = "zImage"
KERNEL_BOOTCMD = "bootz"

# One kernel per slot in the boot partition, as the images are identical at
# first. U-Boot has no squashfs support in rpi_config, so the kernel cannot live
# in the slot itself. The DTB and overlays stay shared.
RPI_EXTRA_IMAGE_BOOT_FILES = " \
    ${KERNEL_IMAGETYPE};kernel-a.img \
    ${KERNEL_IMAGETYPE};kernel-b.img \
    u-boot.bin;${SDIMG_KERNELIMAGE} \
    boot.scr \
"

# boot.scr adds root= for the active slot; the firmware's cmdline.txt must not
# name one of its own.
CMDLINE_ROOTFS = ""
