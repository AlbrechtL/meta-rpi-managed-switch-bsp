SUMMARY = "Fixed MAC addresses for the Raspberry Pi switch ports"
DESCRIPTION = "Init script that gives the ENC28J60 conduit and the DSA user \
ports addresses derived from the SoC serial number, instead of the random \
one the ENC28J60 driver picks on every boot."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://rpi-switch-mac"

S = "${UNPACKDIR}"

inherit allarch update-rc.d

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/rpi-switch-mac ${D}${sysconfdir}/init.d/rpi-switch-mac
}

# rcS.d runs completely before rc5.d, where clixon-backend (S05) configures
# the ports.
INITSCRIPT_NAME = "rpi-switch-mac"
INITSCRIPT_PARAMS = "start 40 S ."

# sed, readlink and ip, all from busybox here.
RDEPENDS:${PN} = "busybox"
