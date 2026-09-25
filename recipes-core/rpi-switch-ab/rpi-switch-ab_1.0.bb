SUMMARY = "A/B slot handling for the Raspberry Pi switch"
DESCRIPTION = "Confirms a freshly updated slot to boot.scr once the system is \
up, and brings the U-Boot environment tools the confirmation and SWUpdate \
use. The environment lives in uboot.env in the boot partition."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://rpi-switch-ab-confirm"

S = "${UNPACKDIR}"

inherit allarch update-rc.d

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/rpi-switch-ab-confirm ${D}${sysconfdir}/init.d/rpi-switch-ab-confirm
}

# Last in rc5.d, after clixon-backend (S05) and clixon-restconf (S35).
INITSCRIPT_NAME = "rpi-switch-ab-confirm"
INITSCRIPT_PARAMS = "defaults 99"

# fw_printenv/fw_setenv, and /etc/fw_env.config (/boot/uboot.env), which
# meta-raspberrypi's u-boot bbappend installs into u-boot-env.
RDEPENDS:${PN} = "busybox libubootenv-bin u-boot-env"
