// Copyright (c) 2026 Andis-08.
// Adapted from org.rocksdb.RocksMemEnv (Facebook, Inc.). This file is
// licensed under both the GPLv2 (found in the COPYING file in the root
// directory) and Apache 2.0 License (found in the LICENSE.Apache file in
// the root directory), matching the rest of rocksdb/java.

package org.rocksdb;

/**
 * Env backed by ZenFS on a raw zoned block device.
 *
 * The ZNS namespace must already be formatted with
 * {@code zenfs mkfs --zbd=<zbdName> --aux_path=<path>} before constructing
 * this Env. The device must be bound to the kernel's nvme driver (not to
 * SPDK's vfio-pci) and its I/O scheduler must be set to "deadline".
 *
 * Requires the ZenFS RocksDB plugin to be compiled into librocksdb
 * (built with {@code ROCKSDB_PLUGINS=zenfs}).
 */
public class ZenfsEnv extends Env {

  /**
   * Creates a new env that stores data on the ZenFS-formatted zoned block
   * device identified by {@code zbdName}.
   *
   * The caller must {@code close()} the result when it is no longer needed.
   *
   * @param zbdName raw zoned block device name without the /dev/ prefix
   *     (e.g. {@code "nvme6n2"})
   * @throws RuntimeException if opening ZenFS on the device fails
   */
  public ZenfsEnv(final String zbdName) {
    super(createZenfsEnv(zbdName));
    if (nativeHandle_ == 0) {
      throw new RuntimeException(
          "ZenfsEnv: failed to open ZenFS on device '" + zbdName
              + "'. Confirm the device is mkfs'd with zenfs and bound to the "
              + "kernel nvme driver.");
    }
  }

  private static native long createZenfsEnv(final String zbdName);

  @Override
  protected final void disposeInternal(final long handle) {
    disposeInternalJni(handle);
  }

  private static native void disposeInternalJni(final long handle);
}
