package com.greghaskins.spectrum;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import com.greghaskins.spectrum.Spectrum.Block;

class Spec implements Child {

  private final Block block;
  private final Description description;
  private final Parent parent;

  public Spec(final Description description, final Block block, final Parent parent) {
    this.description = description;
    this.block = block;
    this.parent = parent;
  }

  @Override
  public Description getDescription() {
    return this.description;
  }

  @Override
  public void run(final RunNotifier notifier) {
    notifier.fireTestStarted(this.description);
    try {
      this.block.run();
    } catch (final Throwable e) {
      notifier.fireTestFailure(new Failure(this.description, e));
    }
    notifier.fireTestFinished(this.description);
  }

  @Override
  public int testCount() {
    return 1;
  }

  @Override
  public void focus() {
    this.parent.focus(this);
  }

}
