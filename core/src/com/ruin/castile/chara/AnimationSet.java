/*
 * Copyright (C) 2014, 2015 Helix Engine Developers
 * (http://github.com/fauu/HelixEngine)
 *
 * This software is licensed under the GNU General Public License
 * (version 3 or later). See the COPYING file in this distribution.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authored by: Piotr Grabowski <fau999@gmail.com>
 */

package com.ruin.castile.chara;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.gson.*;
import com.ruin.castile.chara.AnimationType;
import com.ruin.castile.chara.Direction;

import java.util.EnumMap;
import java.util.Map;

public class AnimationSet {

    private static final String DIRECTORY_NAME;

    private static final String EXTENSION;

    private static final String TEXTURE_EXTENSION;
    EnumMap<AnimationType, EnumMap<Direction, Animation>> animations;

    static {
        DIRECTORY_NAME = "anims";
        EXTENSION = "anim";
        TEXTURE_EXTENSION = "png";
    }

    public AnimationSet(String name) {
        animations = new EnumMap<AnimationType, EnumMap<Direction, Animation>>(
                AnimationType.class);

        for (AnimationType at : AnimationType.values()) {
            animations.put(at, new EnumMap<Direction, Animation>(Direction.class));
        }

        load(name);
    }

    private void load(String name) {
        FileHandle file
                = Gdx.files.internal(DIRECTORY_NAME + "/" + name + "." + EXTENSION);

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("packedimages/" + "game" + ".atlas"));

        Gson gson = new Gson();

        JsonObject object = new JsonParser().parse(file.reader()).getAsJsonObject();

        JsonArray entries = object.get("anims").getAsJsonArray();

        for (int index = 0; index < entries.size(); index++) {
            JsonObject animObject = entries.get(index).getAsJsonObject();

            AnimationType type = AnimationType.valueOf(animObject.get("name").getAsString());

            JsonObject directions = animObject.get("directions").getAsJsonObject();

            for (Direction dir : Direction.values()) {

                Direction theDir = dir;
                if(dir.shouldFlipSprite())
                    theDir = dir.getReverseSpriteDirection();

                JsonElement entr = directions.get(theDir.toString());

                if(entr == null)
                    continue;

                JsonObject elements = entr.getAsJsonObject();

                String[] frameNames = gson.fromJson(elements.get("frames"), String[].class);
                int[][] offsets = gson.fromJson(elements.get("offsets"), int[][].class);
                TextureRegion[] frames = new TextureRegion[frameNames.length];

                for (int i = 0; i < frameNames.length; i++) {
                    frames[i] = atlas.findRegion(frameNames[i]);
                    if (dir.shouldFlipSprite()) {
                        frames[i] = new TextureRegion(frames[i]);
                        frames[i].flip(true, false);
                        frames[i].setRegionX(frames[i].getRegionX() + offsets[i][1]);
                        frames[i].setRegionY(frames[i].getRegionY() + offsets[i][0]);
                        frames[i].setRegionWidth(frames[i].getRegionWidth() + offsets[i][1]);
                        frames[i].setRegionHeight(frames[i].getRegionHeight() + offsets[i][0]);
                    }
                    else {
                        frames[i].setRegionX(frames[i].getRegionX() + offsets[i][0]);
                        frames[i].setRegionY(frames[i].getRegionY() + offsets[i][1]);
                        frames[i].setRegionWidth(frames[i].getRegionWidth() + offsets[i][0]);
                        frames[i].setRegionHeight(frames[i].getRegionHeight() + offsets[i][1]);
                    }
                }

                Animation animation = new Animation(1, frames);

                animation.setFrameDuration(1.0f / animation.getKeyFrames().length);

                add(type, dir, animation);
            }
        }
    }

    private void add(AnimationType type,
                     Direction direction,
                     Animation animation) {
        animations.get(type).put(direction, animation);
    }

    public Animation get(AnimationType type, Direction direction) {
        return get(type, direction, 1);
    }

    public Animation get(AnimationType type, Direction direction, float duration) {
        Animation animation;
        Direction lastDirection;

        if (!animations.get(type).containsKey(direction))
            lastDirection = direction.convertToFourDirections();
        else
            lastDirection = direction;

        animation = animations.get(type).get(lastDirection);

        if (animation == null)
            animation = getDefault();
        animation.setPlayMode(Animation.PlayMode.LOOP);

        //animation.setFrameDuration(duration / animation.getKeyFrames().length);

        return animation;
    }

    public Animation getDefault() {
        return animations.get(AnimationType.IDLE).get(Direction.SOUTHWEST);
    }

}