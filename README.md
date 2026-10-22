This mod handles the rendering of textures, models, and animations-both custom assets (located via the path `/runemodels/assets/runemodels/`, with subfolders for `animations/`, `geo/`, and `textures/`) and assets provided by other mods (following the same structure, but placed within the `resources/` folder of the respective mod).

If you are a mod creator, you can change the directory where your mod files are located by using the `rw.modden.api.Paths` class from the `main` branch-specifically, its `setRoot(Path newRoot)` method. I have also included Javadoc comments wherever I felt they were necessary, so feel free to take a look.
Alternatively, you can (as mentioned above) simply create the folders `animations/`, `geo/`, and `textures/` directly inside your mod's `resources/` folder.

## Files
For a name `name`, the mod looks for:
- `textures/entity/name.png` - texture;
- `geo/name.geo.json` - model (GeckoLib / Blockbench Bedrock format);
- `animations/name.animation.json` - animations of that model.

Names from other mods are written as `mymod:name` (in commands - in quotes: `"mymod:name"`).

## Commands
This mod includes the following server commands:
1. /runemodels set 'targets' 'name'                      (texture + model, if it exists, + animations, by one name)
2. /runemodels set 'targets' model 'model' ['texture']   (model; the texture of the same name if not given; vanilla animations)
3. /runemodels set 'targets' texture 'texture'           (texture only, uses the vanilla model)
4. /runemodels animlist set 'targets' 'animation_set'
5. /runemodels animlist remove 'targets'
6. /runemodels clear 'targets'
7. /runemodels play 'targets' 'animation' [loop|once|hold]
8. /runemodels stop 'targets'

And one client command:
- /rmlist - loaded models; /rmlist 'model' - animations of that model.

`set` and `texture` keep the animation set already given to the entity; `clear` removes everything.
The commands only write entity tags (`runemodels.<name>`, `runemodels.model.<x>`, `runemodels.texture.<y>`, `runemodels.set.<s>`), so the same tags can also be given with `/tag`.

## Animations
- With `set 'name'` the model plays the animations of its own file by their names: `idle` (or `idle_1`, `idle_...`) while standing, `walk` (`walk_...`) while walking, `run` (`run_...`) while sprinting.
- An animation set from `runemodels/animation_sets.json` (command `animlist set`) replaces this choice:
  `{ "animation_sets": { "my_set": { "idle": ["idle", "idle_1"], "walk": ["walk"], "run": ["run"] } } }`
- Everything a custom animation does not move is moved by vanilla Minecraft animations (walking, looking around, sneaking, attacking, shield, bow...). Vanilla actions (attack, shield, bow, crossbow) always win on the arms that do them.

## Bones
Vanilla animations, armor and items find bones by name. Case and `_` do not matter (`Right_arm` = `rightarm` = `RightArm`).
- Bones named like the parts of the entity's vanilla model get its vanilla animations: for players and other humanoids - `head`, `body`, `right_arm`, `left_arm`, `right_leg`, `left_leg`; for other mobs - the names of their own vanilla parts (`right_hind_leg`, `tail`, `right_wing`...).
- Armor is put on `head`, `body`, arms and legs, and is stretched to the size of the bone's cubes.
- Items in hands are drawn at `right_item` / `left_item`, or, if there are no such bones, at the end of `right_arm` / `left_arm`.
- In first person, the `right_arm` / `left_arm` bone of the model replaces the vanilla arm.

The model is drawn at the same scale as the vanilla entity (for example, players are drawn at 15/16 of the size in Blockbench).

If you want, you can use it in your game, to render:
- texture;
- models with texture;
- model with texture and with animation;
- animations, once the models and textures have already been rendered;

This mod was created specifically for the minecraft project AxoRune.
During development, the Claude neural network was used: first the Sonnet 5.5 model, later Opus 5.5.
However, the code was verified both before and after its use.