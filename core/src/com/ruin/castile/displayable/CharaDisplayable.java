package com.ruin.castile.displayable;

import com.badlogic.gdx.math.Vector3;
import com.ruin.castile.chara.AnimationSet;
import com.ruin.castile.chara.AnimationType;
import com.ruin.castile.chara.Direction;
import net.dermetfan.utils.libgdx.graphics.AnimatedDecal;

/**
 * Created by Prin on 2016/02/17.
 */
public class CharaDisplayable extends DecalDisplayable {

    protected AnimationSet animations;

    public CharaDisplayable(Vector3 pos, String animSetName) {
        super(pos);

        animations = new AnimationSet(animSetName);

        AnimatedDecal decal
                = AnimatedDecal.newAnimatedDecal(DEFAULT_DIMENSIONS.x,
                DEFAULT_DIMENSIONS.y,
                animations.getDefault(),
                true);

        decal.setKeepSize(true);
        decal.setPosition(position);
        decal.rotateX(DEFAULT_ROTATION);

        setMainDecal(decal);
    }
    @Override
    public void animate(AnimationType type, Direction direction, float duration) {
        AnimatedDecal animatedDecal = ((AnimatedDecal) this.mainDecal);

        animatedDecal.stop();
        animatedDecal.setAnimated(animations.get(type, direction, duration));
        animatedDecal.play();
    }

    @Override
    public void orientate(Direction direction) {
        AnimatedDecal animatedDecal = ((AnimatedDecal) this.mainDecal);

        animatedDecal.setAnimated(animations.get(AnimationType.IDLE, direction));
        animatedDecal.play();
    }
}
