package com.greghaskins.spectrum;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import com.greghaskins.spectrum.Spectrum.Block;

class Context implements Executable {

    private final Description description;
    private final List<Block> setupBlocks;
    private final List<Block> teardownBlocks;
    private final List<RunOnceBlock> fixtureSetupBlocks;
    private final List<Block> fixtureTeardownBlocks;
    private final Deque<Executable> executables;

    public Context(final Description description) {
        this.description = description;
        setupBlocks = new ArrayList<Block>();
        teardownBlocks = new ArrayList<Block>();
        fixtureSetupBlocks = new ArrayList<RunOnceBlock>();
        fixtureTeardownBlocks = new ArrayList<Block>();

        executables = new ArrayDeque<Executable>();
    }

    @Override
    public void execute(final RunNotifier notifier) {
        if (descriptionHasAnyTests(description)) {
            addExecutablesForFixtureLevelSetupAndTeardown();
        } else {
            notifier.fireTestIgnored(description);
        }
        for (final Executable child : executables) {
            child.execute(notifier);
        }
    }

    private boolean descriptionHasAnyTests(final Description currentDescription) {
        for (final Description child : currentDescription.getChildren()) {
            if (isTest(child) || descriptionHasAnyTests(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTest(final Description child) {
        return child.getMethodName() != null;
    }

    private void addExecutablesForFixtureLevelSetupAndTeardown() {
        executables.addFirst(new BlockExecutable(description, new CompositeBlock(fixtureSetupBlocks)));
        executables.addLast(new BlockExecutable(description, new CompositeBlock(fixtureTeardownBlocks)));
    }

    public void addSetup(final Block block) {
        setupBlocks.add(block);
    }

    public void addTeardown(final Block block) {
        teardownBlocks.add(block);
    }

    public void addFixtureSetup(final Block block) {
        fixtureSetupBlocks.add(new RunOnceBlock(block));
    }

    public void addFixtureTeardown(final Block block) {
        fixtureTeardownBlocks.add(block);
    }

    public void addTest(final String behavior, final Block block) {
        final Description testDescription = Description.createTestDescription(description.getClassName(), behavior);
        final CompositeBlock testBlock = putTestBlockInContext(block);
        final Test test = new Test(testDescription, testBlock);
        description.addChild(testDescription);
        executables.add(test);
    }

    private CompositeBlock putTestBlockInContext(final Block testBlock) {
        return new CompositeBlock(new CompositeBlock(fixtureSetupBlocks), new CompositeBlock(setupBlocks), testBlock,
                new CompositeBlock(teardownBlocks));
    }

    public void addChild(final Context childContext) {
        description.addChild(childContext.description);
        childContext.addFixtureSetup(new CompositeBlock(fixtureSetupBlocks));
        childContext.addSetup(new CompositeBlock(setupBlocks));
        childContext.addTeardown(new CompositeBlock(teardownBlocks));
        executables.add(childContext);
    }


}