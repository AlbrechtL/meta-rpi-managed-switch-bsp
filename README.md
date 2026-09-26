# meta-rpi-managed-switch-bsp

> **⚠️ Proof of concept.** This project is a proof of concept, created with
> the help of AI. It has not undergone thorough review or hardening, and
> should not be assumed suitable for production use.

Part of **Ethernet Switch OS**. See [ethernet-switch-os](https://github.com/AlbrechtL/ethernet-switch-os).

Yocto BSP layer for the [4-port managed switch HAT](https://github.com/AlbrechtL/rpi-managed-switch-4-port)
for the Raspberry Pi: an RTL8367S switch whose CPU port is connected to an
ENC28J60 SPI Ethernet controller. Linux drives it with DSA (`rtl8365mb`).
The layer builds on [meta-raspberrypi](https://git.yoctoproject.org/meta-raspberrypi)
(branch `wrynose`). It is the hardware side of Ethernet Switch OS: kernel,
device tree overlay, boot, and the SD card layout with A/B slots. It boots to
a shell on its own; the userspace comes from
[meta-ethernet-switch-os](https://github.com/AlbrechtL/meta-ethernet-switch-os).

## Documentation

**Hardware, SD card layout, A/B boot, flashing and the known limitations are
documented in the user guide, which is the only place for that information:**
[Raspberry Pi switch](https://albrechtl.github.io/ethernet-switch-os/installation/raspberry-pi/).

## Supported machines

| `MACHINE` | Hardware |
|---|---|
| `rpi-managed-switch-rpi0` | The HAT on a Raspberry Pi Zero (BCM2835, ARMv6), experimental |

Other Pi models need a machine `.conf` each, see the table in the user guide.

## Building

The layer is meant to be built through
[ethernet-switch-os](https://github.com/AlbrechtL/ethernet-switch-os), whose
kas files check out this layer, meta-raspberrypi and the rest, see
[Building the firmware](https://albrechtl.github.io/ethernet-switch-os/development/building/).

## Layer contents

The hardware support is ported from the OpenWrt branch
[AlbrechtL/openwrt `rpi_managed_switch`](https://github.com/AlbrechtL/openwrt/tree/rpi_managed_switch):

| OpenWrt | Here |
|---|---|
| `999-0002-add-rtl8365mb-enc28j60-switch-overlay.patch` | `0003-ARM-dts-overlays-add-rtl8365mb-enc28j60-switch.patch`, built as `overlays/rtl8365mb-enc28j60-switch.dtbo` |
| `999-0001-increase-enc28j60-mtu-for-dsa.patch` | `0002-net-enc28j60-allow-an-MTU-of-1508-for-a-DSA-conduit.patch` |
| `config.txt`: `dtparam=spi=on`, `dtoverlay=…` | `ENABLE_SPI_BUS`, `RPI_EXTRA_CONFIG` in `conf/machine/include/rpi-managed-switch.inc` |
| `kmod-dsa-rtl8365mb`, `kmod-enc28j60`, … | `rpi-managed-switch.cfg`, everything built in |
| `02_network`: MAC address from the serial number | `rpi-switch-mac` init script, same addresses |
| OpenWrt's rtl8365mb backports (`generic/backport-6.18/94*`) | `recipes-kernel/linux/files/rtl8365mb-backport/`, for hardware forwarding, VLAN and FDB offload |

Settings shared by every Pi model live in
`conf/machine/include/rpi-managed-switch.inc`; a machine `.conf` requires the
meta-raspberrypi machine of its Pi model and then that file. It puts
`rpi-managed-switch` into `MACHINEOVERRIDES`, so recipes use
`COMPATIBLE_MACHINE = "^rpi-managed-switch$"`.

The layer is hardware-only: kernel, boot, and SD card layout. With plain
`poky-tiny`, `rpi-switch-image` boots to a shell.

The SD card layout, the A/B boot with U-Boot and `/sbin/overlay-init`
(`rpi-switch-overlay-init`) are described in the user guide.

## Links

- [ethernet-switch-os](https://github.com/AlbrechtL/ethernet-switch-os):
  the build entry point (kas) and the [user guide](https://albrechtl.github.io/ethernet-switch-os/)
- [meta-ethernet-switch-os](https://github.com/AlbrechtL/meta-ethernet-switch-os):
  the distro and userspace on top of this layer
- [rpi-managed-switch-4-port](https://github.com/AlbrechtL/rpi-managed-switch-4-port):
  the switch HAT hardware
- [AlbrechtL/openwrt `rpi_managed_switch`](https://github.com/AlbrechtL/openwrt/tree/rpi_managed_switch):
  the OpenWrt port this layer is derived from
- [meta-raspberrypi](https://git.yoctoproject.org/meta-raspberrypi):
  the Raspberry Pi BSP this layer builds on
- [meta-rtl83xx-bsp](https://github.com/AlbrechtL/meta-rtl83xx-bsp):
  the sibling BSP for Realtek RTL83xx switches
