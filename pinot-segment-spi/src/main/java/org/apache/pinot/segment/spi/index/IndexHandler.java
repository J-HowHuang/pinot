/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.segment.spi.index;

import javax.annotation.Nullable;
import org.apache.pinot.segment.spi.store.SegmentDirectory;


/**
 * Interface for index handlers, which update the corresponding type of indices,
 * like adding, removing or converting the format.
 */
public interface IndexHandler {
  /**
   * Describes what will happen to a single index type on a column during reload.
   */
  enum IndexAction {
    /** Index does not exist and will be created. */
    ADD,
    /** Index exists and will be dropped. */
    REMOVE,
    /** Index exists and will be dropped then recreated (config or format change). */
    REBUILD
  }

  /**
   * Adds new indices and removes obsolete indices.
   */
  void updateIndices(SegmentDirectory.Writer segmentWriter)
      throws Exception;

  /**
   * Check if there is a need to add new indices or removes obsolete indices.
   * @return true if there is a need to update.
   */
  boolean needUpdateIndices(SegmentDirectory.Reader segmentReader)
      throws Exception;

  /**
   * Returns the {@link IndexAction} that would be applied to the given column during
   * {@link #updateIndices}, or {@code null} if no change is needed for that column.
   *
   * <p>Used by downstream projects to preview the effect of a reload without performing it.
   * The default implementation always returns {@code null}. Implementations should override
   * this to provide accurate dry-run information.
   */
  @Nullable
  default IndexAction getIndexChange(String column, SegmentDirectory.Reader segmentReader)
      throws Exception {
    return null;
  }

  /**
   * Performs any cleanup actions required after the indexes have been updated.
   * Should be called only after all IndexHandlers have run.
   */
  void postUpdateIndicesCleanup(SegmentDirectory.Writer segmentWriter)
    throws Exception;

  public static class NoOp implements IndexHandler {
    public static final NoOp INSTANCE = new NoOp();

    private NoOp() {
    }

    @Override
    public void updateIndices(SegmentDirectory.Writer segmentWriter) {
    }

    @Override
    public boolean needUpdateIndices(SegmentDirectory.Reader segmentReader) {
      return false;
    }

    @Override
    public void postUpdateIndicesCleanup(SegmentDirectory.Writer segmentWriter) {
    }
  }
}
