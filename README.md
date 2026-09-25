# meta-rpi-managed-switch-bsp

> **⚠️ Proof of concept.** This project is a proof of concept, created with
> the help of AI. It has not undergone thorough review or hardening, and
> should not be assumed suitable for production use.

Part of **Ethernet Switch OS**. See [ethernet-switch-os](https://github.com/AlbrechtL/ethernet-switch-os).

Yocto BSP layer for the [4-port managed switch HAT](https://github.com/AlbrechtL/rpi-managed-switch-4-port)
for the Raspberry Pi: an RTL8367S switch whose CPU port is connected to an
ENC28J60 SPI Ethernet controller. Linux drives it with DSA (`rtl8365mb`).
The layer builds on [meta-raspberrypi](https://git.yoctoproject.org/meta-raspberrypi)
(branch `wrynose`).

## Supported hardware

| Hardware | `MACHINE` | Status |
|---|---|---|
| 4-port managed switch HAT on a Raspberry Pi Zero (BCM2835, ARMv6) | `rpi-managed-switch-rpi0` | Experimental |
| … on a Raspberry Pi Zero 2, 3 or 4 | — | Not yet: needs a machine `.conf` each. The HAT itself supports them. |
| … on a Raspberry Pi 5 | — | Not planned for now; the HAT does not work with OpenWrt on a Pi 5 either ([openwrt/openwrt#18034](https://github.com/openwrt/openwrt/issues/18034)). |

The switch chip is a Realtek RTL8367S with four Gigabit front ports
(`lan1`..`lan4`). The Pi talks to it through an ENC28J60 on SPI0 (the DSA
conduit) and manages it over Realtek SMI, bit-banged on GPIO17/GPIO27. See
the [hardware repository](https://github.com/AlbrechtL/rpi-managed-switch-4-port)
for schematics, the KiCad design and the known hardware issues.

## Building

The layer is meant to be built through
[ethernet-switch-os](https://github.com/AlbrechtL/ethernet-switch-os), whose
kas files check out this layer, meta-raspberrypi and the rest:

```sh
git clone https://github.com/AlbrechtL/ethernet-switch-os
cd ethernet-switch-os
./kas-container build kas/board/rpi-managed-switch-rpi0.yml
```

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

## SD card layout and A/B boot

| Partition | Content |
|---|---|
| p1 `boot`, vfat, 64 MiB | Raspberry Pi firmware, `config.txt`, DTB and overlays, U-Boot as `kernel.img`, `boot.scr`, `uboot.env`, `kernel-a.img`, `kernel-b.img` |
| p2 slot A, 256 MiB | squashfs root filesystem |
| p3 slot B, 256 MiB | squashfs root filesystem |
| p4 `data`, ext4, 256 MiB | overlay upper layer, shared by both slots |

The Pi Zero's firmware cannot switch between boot slots itself (`autoboot.txt`
and `tryboot` need a Pi 4 or later), so U-Boot does it. `boot.scr` boots the
slot in `rootpart` from `uboot.env` (2 or 3) with its kernel. An update writes
the other slot and its kernel, then sets `rootpart` to it and
`upgrade_available=1`. `boot.scr` then counts every boot, and
`rpi-switch-ab-confirm` (last in `rc5.d`) clears the counter once the system is
up. After `bootlimit` (3) unconfirmed boots, `boot.scr` switches back. The
kernel runs with `panic=5`, so a slot that cannot mount its root filesystem
fails over too.

`/sbin/overlay-init` (`rpi-switch-overlay-init`) stacks the overlay on the
squashfs, as in meta-rtl83xx-bsp. The DTB and the overlays in the boot
partition are shared by both slots and are not part of an update.

## Images

| File | What it is for |
|---|---|
| **`rpi-switch-image-rpi-managed-switch-rpi0.rootfs.wic.bz2`** + `.wic.bmap` | The SD card. Both slots are identical at first. |
| `rpi-switch-image-rpi-managed-switch-rpi0.rootfs.squashfs-xz` | Content of one slot. |
| `uImage-rpi-managed-switch-rpi0.bin` | Kernel, installed as `kernel-a.img` and `kernel-b.img`. |

With meta-ethernet-switch-os, `ethernet-switch-os-swu-upgrade-rpi-managed-switch-rpi0.swu`
comes on top: the squashfs and the kernel for whichever slot is not running.

## Flashing

```sh
bmaptool copy rpi-switch-image-rpi-managed-switch-rpi0.rootfs.wic.bz2 /dev/sdX
```

The serial console is on the GPIO header (GPIO14/15), 115200 8N1.

## Known limitations

- Only the Pi Zero (1). Other models need their own machine `.conf`. On a Pi
  with onboard Ethernet, the ENC28J60 conduit is no longer `eth0`.
- The ENC28J60 link to the switch is 10 Mbit/s half-duplex. Traffic to and
  from the Pi itself (management, the web UI, an update) is correspondingly
  slow. Traffic between the front ports is forwarded by the switch.
- `uboot.env` is a single copy on vfat. A power cut while it is being written
  can damage it, and U-Boot then falls back to its default environment, which
  boots slot A.
- There is no watchdog yet. A slot that hangs without a kernel panic is not
  rolled back without a power cycle.

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
