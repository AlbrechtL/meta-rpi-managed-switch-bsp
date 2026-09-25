FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# rtl8365mb bridge, VLAN and FDB offload, backported by OpenWrt
# (target/linux/generic/backport-6.18, openwrt/openwrt#23738). Without it the
# switch forwards nothing in hardware: every frame between two front ports
# crosses the ENC28J60, and that SPI link manages about 5 Mbit/s.
#
# 0001 only adjusts context: OpenWrt's 6.18.y already has
# gpiod_set_value_cansleep() in rtl83xx.c, the Raspberry Pi 6.18.33 does not.
SRC_URI:append:rpi-managed-switch = " \
    file://0001-net-dsa-realtek-use-gpiod_set_value_cansleep-for-reset.patch \
    file://rtl8365mb-backport/940-01-v7.1-net-dsa-tag_rtl8_4-update-format-description.patch \
    file://rtl8365mb-backport/940-02-v7.1-net-dsa-tag_rtl8_4-set-KEEP-flag.patch \
    file://rtl8365mb-backport/941-v7.2-net-dsa-realtek-rtl8365mb-add-support-for-rtl8367sb.patch \
    file://rtl8365mb-backport/942-01-v7.2-net-dsa-realtek-rtl8365mb-use-ERR_PTR.patch \
    file://rtl8365mb-backport/942-02-v7.2-net-dsa-realtek-rtl8365mb-reject-unsupported-topolog.patch \
    file://rtl8365mb-backport/942-03-v7.2-net-dsa-realtek-rtl8365mb-use-dsa-helpers-for-port-i.patch \
    file://rtl8365mb-backport/942-04-v7.2-net-dsa-realtek-rtl8365mb-prepare-for-multiple-sourc.patch \
    file://rtl8365mb-backport/942-05-v7.2-net-dsa-realtek-rtl8365mb-add-table-lookup-interface.patch \
    file://rtl8365mb-backport/942-06-v7.2-net-dsa-realtek-rtl8365mb-add-VLAN-support.patch \
    file://rtl8365mb-backport/942-07-v7.2-net-dsa-realtek-rtl8365mb-add-FDB-support.patch \
    file://rtl8365mb-backport/942-08-v7.2-net-dsa-realtek-rtl8365mb-add-port_bridge_-join-leav.patch \
    file://rtl8365mb-backport/942-09-v7.2-net-dsa-realtek-rtl8365mb-add-bridge-port-flags.patch \
    file://rtl8365mb-backport/943-01-v7.3-net-dsa-realtek-rtl8365mb-add-SGMII-support.patch \
    file://rtl8365mb-backport/943-02-v7.3-net-dsa-realtek-rtl8365mb-add-HSGMII-support.patch \
    file://rtl8365mb-backport/944-01-v7.2-net-dsa-realtek-rtl8365mb-use-devm_mutex_init-for-mib_lock.patch \
    file://rtl8365mb-backport/944-02-v7.2-net-dsa-realtek-use-devm_mutex_init-for-regmap-lock.patch \
    file://rtl8365mb-backport/944-03-v7.2-net-dsa-realtek-use-devm_mutex_init-for-vlan_lock.patch \
    file://rtl8365mb-backport/944-04-v7.2-net-dsa-realtek-use-devm_mutex_init-for-l2_lock.patch \
"

# The board itself, from https://github.com/AlbrechtL/openwrt/tree/rpi_managed_switch:
# ENC28J60 MTU for the 8 byte DSA tag, and the device tree overlay.
SRC_URI:append:rpi-managed-switch = " \
    file://0002-net-enc28j60-allow-an-MTU-of-1508-for-a-DSA-conduit.patch \
    file://0003-ARM-dts-overlays-add-rtl8365mb-enc28j60-switch.patch \
    file://rpi-managed-switch.cfg \
"
