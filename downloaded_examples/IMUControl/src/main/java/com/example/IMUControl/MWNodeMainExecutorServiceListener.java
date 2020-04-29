/* Copyright 2018 The MathWorks, Inc. */

package com.example.IMUControl;

public interface MWNodeMainExecutorServiceListener {

  /**
   * @param nodeMainExecutorService the {@link MWNodeMainExecutorService} that was shut down
   */
  void onShutdown(MWNodeMainExecutorService nodeMainExecutorService);
}
