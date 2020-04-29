/* Copyright 2018 The MathWorks, Inc. */
package com.example.IMUControl;

import org.ros.node.NodeMainExecutor;

public interface MWNodeMainExecutorListener {

  /**
   * @param nodeMainExecutor
   *          the newly created {@link NodeMainExecutor}
   */
  void onNewNodeMainExecutor(NodeMainExecutor nodeMainExecutor);
}
