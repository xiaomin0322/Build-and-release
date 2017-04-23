/*
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package com.sun.media.sound;

/**
 * A standard director who chooses performers
 * by there keyfrom,keyto,velfrom,velto properties.
 *
 * @author Karl Helgason
 */
public class ModelStandardDirector implements ModelDirector {

    ModelPerformer[] performers;
    ModelDirectedPlayer player;
    boolean noteOnUsed = false;
    boolean noteOffUsed = false;

    public ModelStandardDirector(ModelPerformer[] performers,
            ModelDirectedPlayer player) {
        this.performers = performers;
        this.player = player;
        for (int i = 0; i < performers.length; i++) {
            ModelPerformer p = performers[i];
            if (p.isReleaseTriggered()) {
                noteOffUsed = true;
            } else {
                noteOnUsed = true;
            }
        }
    }

    public void close() {
    }

    public void noteOff(int noteNumber, int velocity) {
        if (!noteOffUsed)
            return;
        for (int i = 0; i < performers.length; i++) {
            ModelPerformer p = performers[i];
            if (p.getKeyFrom() <= noteNumber && p.getKeyTo() >= noteNumber) {
                if (p.getVelFrom() <= velocity && p.getVelTo() >= velocity) {
                    if (p.isReleaseTriggered()) {
                        player.play(i, null);
                    }
                }
            }
        }
    }

    public void noteOn(int noteNumber, int velocity) {
        if (!noteOnUsed)
            return;
        for (int i = 0; i < performers.length; i++) {
            ModelPerformer p = performers[i];
            if (p.getKeyFrom() <= noteNumber && p.getKeyTo() >= noteNumber) {
                if (p.getVelFrom() <= velocity && p.getVelTo() >= velocity) {
                    if (!p.isReleaseTriggered()) {
                        player.play(i, null);
                    }
                }
            }
        }
    }
}
