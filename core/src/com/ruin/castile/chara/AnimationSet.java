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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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

        FileHandle textureFile
                = Gdx.files.internal(
                DIRECTORY_NAME + "/" + name + "." + TEXTURE_EXTENSION);

        Texture texture = new Texture(textureFile);

        Gson gson = new Gson();

        JsonObject object = new JsonParser().parse(file.reader()).getAsJsonObject();

        JsonArray entries = object.get("anims").getAsJsonArray();

        for (int index = 0; index < entries.size(); index++) {
            JsonObject animObject = entries.get(index).getAsJsonObject();

            AnimationType type = AnimationType.valueOf(animObject.get("name").getAsString());

            JsonObject directions = animObject.get("directions").getAsJsonObject();

            for (Map.Entry<String, JsonElement> entr : directions.entrySet()) {

                Direction dir = Direction.valueOf(entr.getKey());

                JsonObject elements = entr.getValue().getAsJsonObject();

                int[][] startCoords = gson.fromJson(elements.get("startCoords"), int[][].class);
                int[][] sizes = gson.fromJson(elements.get("sizes"), int[][].class);

                TextureRegion[] frames = new TextureRegion[startCoords.length];

                for (int i = 0; i < startCoords.length; i++) {
                    frames[i] = new TextureRegion(texture, startCoords[i][0], startCoords[i][1], sizes[i][0], sizes[i][1]);
                    if (dir.shouldFlipSprite()) {
                        frames[i].flip(true, false);
                    }
                }

                Animation animation = new Animation(1, frames);

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
        animation.setPlayMode(Animation.PlayMode.REVERSED);

        if (animation == null)
            return getDefault();

        //animation.setFrameDuration(duration / animation.getKeyFrames().length);

        return animation;
    }

    public Animation getDefault() {
        return animations.get(AnimationType.IDLE).get(Direction.SOUTHWEST);
    }

}