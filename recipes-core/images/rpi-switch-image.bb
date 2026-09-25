# SD card image for the Raspberry Pi switch: two squashfs root filesystem
# slots and a data partition for the overlay (files/wic/rpi-managed-switch-ab.wks.in).
#
# Boots to a shell on its own. The distro layer adds its userspace from a
# .bbappend, as it does for meta-rtl83xx-bsp's images.

SUMMARY = "A/B SD card image for the Raspberry Pi 4-port managed switch"

IMAGE_INSTALL = " \
    packagegroup-core-boot \
    rpi-switch-overlay-init \
    rpi-switch-mac \
    rpi-switch-ab \
    ${CORE_IMAGE_EXTRA_INSTALL} \
"

IMAGE_LINGUAS = " "

LICENSE = "MIT"

inherit core-image

COMPATIBLE_MACHINE = "^rpi-managed-switch$"

# The squashfs is the content of both slots, and what an update writes. The
# .wic.bz2 is flashed once; bmaptool uses the .wic.bmap to skip the empty
# space. No read-only-rootfs feature: overlay-init makes / writable before
# busybox init runs.
IMAGE_FSTYPES = "squashfs-xz wic.bz2 wic.bmap"
IMAGE_TYPEDEP:wic = "squashfs-xz"
WKS_FILE = "rpi-managed-switch-ab.wks.in"

# Same block size as meta-rtl83xx-bsp's squashfs.
EXTRA_IMAGECMD:squashfs-xz = "-b 262144"

rpi_switch_fstab() {
    # The boot partition holds uboot.env, which fw_setenv and SWUpdate write,
    # and the kernels an update replaces.
    install -d ${IMAGE_ROOTFS}/boot
    printf '/dev/mmcblk0p1\t/boot\tvfat\tdefaults\t0\t0\n' >> ${IMAGE_ROOTFS}${sysconfdir}/fstab

    # overlay-init mounts /var/volatile itself and creates log/ and tmp/ in
    # it. Left in fstab, "mount -a" would stack an empty tmpfs over them.
    sed -i '\#[[:space:]]/var/volatile[[:space:]]#d' ${IMAGE_ROOTFS}${sysconfdir}/fstab
}
ROOTFS_POSTPROCESS_COMMAND += "rpi_switch_fstab;"

# The kernel has already mounted devtmpfs on /dev (CONFIG_DEVTMPFS_MOUNT), and
# overlay-init moves it into the overlay root. Mounting it again fails:
#   mount: mounting devtmpfs on /dev failed: Resource busy
rpi_switch_drop_devtmpfs_inittab() {
    sed -i '\#^::sysinit:/bin/mount -t devtmpfs devtmpfs /dev$#d' ${IMAGE_ROOTFS}${sysconfdir}/inittab
}
ROOTFS_POSTPROCESS_COMMAND += "rpi_switch_drop_devtmpfs_inittab;"
