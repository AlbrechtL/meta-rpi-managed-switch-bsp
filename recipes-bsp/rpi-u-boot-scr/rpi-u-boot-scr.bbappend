# boot.cmd.in with A/B slot selection instead of meta-raspberrypi's, which
# always boots the one kernel and leaves root= to cmdline.txt.
FILESEXTRAPATHS:prepend:rpi-managed-switch := "${THISDIR}/files:"
